/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.protocol;

import org.teleal.cling.UpnpService;
import org.teleal.cling.binding.xml.DescriptorBindingException;
import org.teleal.cling.binding.xml.DeviceDescriptorBinder;
import org.teleal.cling.binding.xml.ServiceDescriptorBinder;
import org.teleal.cling.model.ValidationError;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.message.StreamRequestMessage;
import org.teleal.cling.model.message.StreamResponseMessage;
import org.teleal.cling.model.message.UpnpRequest;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.registry.RegistrationException;
import org.teleal.common.util.Exceptions;

import com.mirage.util.Utils;

import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * Retrieves all remote device XML descriptors, parses them, creates an immutable device and service metadata graph.
 * <p>
 * This implementation encapsulates all steps which are necessary to create a fully usable and populated
 * device metadata graph of a particular UPnP device. It starts with an unhydrated and typically just
 * discovered {@link org.teleal.cling.model.meta.RemoteDevice}, the only property that has to be available is
 * its {@link org.teleal.cling.model.meta.RemoteDeviceIdentity}.
 * </p>
 * <p>
 * This protocol implementation will then retrieve the device's XML descriptor, parse it, and retrieve and
 * parse all service descriptors until all device and service metadata has been retrieved. The fully
 * hydrated device is then added to the {@link org.teleal.cling.registry.Registry}.
 * </p>
 * <p>
 * Any descriptor retrieval, parsing, or validation error of the metadata will abort this protocol
 * with a warning message in the log.
 * </p>
 *
 * @author Christian Bauer
 */
public class RetrieveRemoteDescriptors implements Runnable {

    final private static Logger log = Logger.getLogger(RetrieveRemoteDescriptors.class.getName());

    final private static String TAG = "RetrieveRemoteDescriptors";
    
    private final UpnpService upnpService;
    private RemoteDevice rd;
    private boolean store = false;
    private static final Set<URL> activeRetrievals = new CopyOnWriteArraySet();

    public RetrieveRemoteDescriptors(UpnpService upnpService, RemoteDevice rd) {
    	
    	//Log.i(TAG , "[78]upnpService : "+upnpService.toString() );
    	
    	if(!activeRetrievals.isEmpty()){
    		//Log.i(TAG , "not empty : "+ activeRetrievals.toString());
    	}else{
    		//Log.i(TAG ,"[82]activeRetrievals is empty");
    	}
        this.upnpService = upnpService;
        this.rd = rd;
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public void run() {
        URL deviceURL = rd.getIdentity().getDescriptorURL();
    	//Log.i(TAG , "[89]run(),deviceURL = "+deviceURL.toString());
    	
        // Performance optimization, try to avoid concurrent GET requests for device descriptor,
        // if we retrieve it once, we have the hydrated device. There is no different outcome
        // processing this several times concurrently.
        if (activeRetrievals.contains(deviceURL)) {
            //Log.i(TAG ,"[98]Exiting early, active retrieval for URL already in progress: ");
            return;
        }

        // Exit if it has been discovered already, could be have been waiting in the executor queue too long
        if (getUpnpService().getRegistry().getRemoteDevice(rd.getIdentity().getUdn(), true) != null) {
            //Log.i(TAG ,"[104]Exiting early, already discovered: ");
            return;
        }
        
        try {
        	//Log.i(TAG, "[115] add devices URL");
            activeRetrievals.add(deviceURL);
            describe();
        } finally {
        	//Log.i(TAG, "[116] remove devices URL");
            activeRetrievals.remove(deviceURL);
        }
    }

    protected void describe() {
    	//Log.i(TAG , "[117]describe....nn");
        // All of the following is a very expensive and time consuming procedure, thanks to the
        // braindead design of UPnP. Several GET requests, several descriptors, several XML parsing
        // steps - all of this could be done with one and it wouldn't make a difference. So every
        // call of this method has to be really necessary and rare.

        StreamRequestMessage deviceDescRetrievalMsg =
                new StreamRequestMessage(UpnpRequest.Method.GET, rd.getIdentity().getDescriptorURL());
        
        //Log.i(TAG ,"[126]Sending device descriptor retrieval message: " + deviceDescRetrievalMsg);
        StreamResponseMessage deviceDescMsg =null;
      
     // by andy
        if(getUpnpService()!=null){
        	if(getUpnpService().getRouter()!=null){
        		if(deviceDescRetrievalMsg!=null){
        			deviceDescMsg = getUpnpService().getRouter().send(deviceDescRetrievalMsg);		
        		}
        	}
        }
     //
 //       StreamResponseMessage deviceDescMsg = getUpnpService().getRouter().send(deviceDescRetrievalMsg);

        if (deviceDescMsg == null) {
            //Log.i(TAG ,"[130]Device descriptor retrieval failed, no response: " + rd.getIdentity().getDescriptorURL());
            return;
        }

        if (deviceDescMsg.getOperation().isFailed()) {
            Utils.print(TAG ,
                    "Device descriptor retrieval failed: "
                    + rd.getIdentity().getDescriptorURL() +
                    ", "
                    + deviceDescMsg.getOperation().getResponseDetails()
            );
            return;
        }

        if (!deviceDescMsg.isContentTypeTextUDA()) {
            //Log.i(TAG ,"[145]Received device descriptor without or with invalid Content-Type: " + rd.getIdentity().getDescriptorURL());
            // We continue despite the invalid UPnP message because we can still hope to convert the content
        }
        //Log.i(TAG ,"[149]Received root device descriptor: " + deviceDescMsg);
        describe(deviceDescMsg.getBodyString());
    }

    protected void describe(String descriptorXML) {
    	//Log.i(TAG , "[154]describe : descriptorXML");
        boolean notifiedStart = false;
        RemoteDevice describedDevice = null;
        try {

            DeviceDescriptorBinder deviceDescriptorBinder =
                    getUpnpService().getConfiguration().getDeviceDescriptorBinderUDA10();

            describedDevice = deviceDescriptorBinder.describe(
                    rd,
                    descriptorXML
            );

            //Log.i(TAG ,"[168]Remote device described (without services) notifying listeners: " + describedDevice);
            notifiedStart = getUpnpService().getRegistry().notifyDiscoveryStart(describedDevice);

            //Log.i(TAG ,"[171]Hydrating described device's services: " + describedDevice);
            RemoteDevice hydratedDevice = describeServices(describedDevice);
            if (hydratedDevice == null) {
                //Log.i(TAG ,"[174]Device service description failed: " + rd);
                if (notifiedStart)
                    getUpnpService().getRegistry().notifyDiscoveryFailure(
                            describedDevice,
                            new DescriptorBindingException("[178]Device service description failed: " + rd)
                    );
                return;
            }

            //Log.i(TAG ,"[183]Adding fully hydrated remote device to registry: " + hydratedDevice);
            // The registry will do the right thing: A new root device is going to be added, if it's
            // already present or we just received the descriptor again (because we got an embedded
            // devices' notification), it will simply update the expiration timestamp of the root
            // device.
            getUpnpService().getRegistry().addDevice(hydratedDevice);

        } catch (ValidationException ex) {
            //Log.i(TAG ,"[191]Could not validate device model: " + rd);
            for (ValidationError validationError : ex.getErrors()) {
                //Log.i(TAG ,"[193]"+validationError.toString());
            }
            if (describedDevice != null && notifiedStart)
                getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, ex);

        } catch (DescriptorBindingException ex) {
            //Log.i(TAG ,"[199]Could not hydrate device or its services from descriptor: " + rd);
            //Log.i(TAG ,"[200]Cause was: " + Exceptions.unwrap(ex));
            if (describedDevice != null && notifiedStart)
                getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, ex);

        } catch (RegistrationException ex) {
            //Log.i(TAG ,"[205]Adding hydrated device to registry failed: " + rd);
            //Log.i(TAG ,"[206]Cause was: " + ex.toString());
            if (describedDevice != null && notifiedStart)
                getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, ex);
        }
    }

    protected RemoteDevice describeServices(RemoteDevice currentDevice)
            throws DescriptorBindingException, ValidationException {
    	//Log.i(TAG , "[214]...describeServices...currentDevice ="+currentDevice.toString());
    	
        List<RemoteService> describedServices = new ArrayList();
        List<RemoteService> tempServices = new ArrayList<RemoteService>();
        if (currentDevice.hasServices()) {
            List<RemoteService> filteredServices = filterExclusiveServices(currentDevice.getServices());
            for (RemoteService service : filteredServices) {
                RemoteService svc = describeService(service);
                //Log.i(TAG , "[227]");
                if (svc == null) { 
                	//Log.i(TAG , "Something went wrong...return null");
                    return null;
                }
                describedServices.add(svc);
                tempServices.add(service);
            }
        }


        //Log.i(TAG , "[229]...describeServices");
        List<RemoteDevice> describedEmbeddedDevices = new ArrayList();
        if (currentDevice.hasEmbeddedDevices()) {
            for (RemoteDevice embeddedDevice : currentDevice.getEmbeddedDevices()) {
                if (embeddedDevice == null) continue;
                RemoteDevice describedEmbeddedDevice = describeServices(embeddedDevice);
                if (describedEmbeddedDevice == null) { // Something was wrong, recursively
                	//Log.i(TAG , "[236]Something was wrong, recursively");
                    return null;
                }
                describedEmbeddedDevices.add(describedEmbeddedDevice);
            }
        }


        
        //Log.i(TAG , "[243]...describeServices");
        Icon[] iconDupes = new Icon[currentDevice.getIcons().length];
        for (int i = 0; i < currentDevice.getIcons().length; i++) {
            Icon icon = currentDevice.getIcons()[i];
            iconDupes[i] = icon.deepCopy();
            
        }

//        try {
//            //if iconDupes length is 1
//            if(tempServices.size()>0 && tempServices.get(0)!=null){
//            	for(int i=0;i<tempServices.size();i++){
//            		Utils.print("bbbb", "bbbb");
//            		if(tempServices.get(i).getDevice()!=null){
//            			Utils.print("aaa", "aaaa");
//                		URL descriptorURL = tempServices.get(i).getDevice().normalizeURI(
//                      		  describedServices.get(i).getDescriptorURI());
//                        StreamRequestMessage serviceDescRetrievalMsg = new StreamRequestMessage(UpnpRequest.Method.GET,
//                      		  descriptorURL);
//                        StreamResponseMessage serviceDescMsg = getUpnpService().getRouter().send(serviceDescRetrievalMsg);
//                        
//                        if(serviceDescMsg.getBodyBytes()!=null){
//                        	iconDupes[0].setData(serviceDescMsg.getBodyBytes());
//                        	Utils.print("ssssss", "sssssssss");
//                        
//                        }
//                           
//                	}
//            	}
//            	
//              
//            }
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}

        
        
        //Log.i(TAG , "[250]...describeServices...........OK");
        // Yes, we create a completely new immutable graph here
        return currentDevice.newInstance(
                currentDevice.getIdentity().getUdn(),
                currentDevice.getVersion(),
                currentDevice.getType(),
                currentDevice.getDetails(),
                iconDupes,
                currentDevice.toServiceArray(describedServices),
                describedEmbeddedDevices
        );
    }

   
    
    protected RemoteService describeService(RemoteService service)
            throws DescriptorBindingException, ValidationException {

        URL descriptorURL = service.getDevice().normalizeURI(service.getDescriptorURI());
//        Utils.print("888", service.getDescriptorURI()+"");
        StreamRequestMessage serviceDescRetrievalMsg = new StreamRequestMessage(UpnpRequest.Method.GET, descriptorURL);

        //Log.i(TAG ,"[269]Sending service descriptor retrieval message: " + serviceDescRetrievalMsg);
        StreamResponseMessage serviceDescMsg = getUpnpService().getRouter().send(serviceDescRetrievalMsg);

        if (serviceDescMsg == null) {
            //Log.i(TAG ,"[273]Could not retrieve service descriptor: " + service);
            return null;
        }
        
        if (serviceDescMsg.getOperation().isFailed()) {
            //Log.i(TAG ,"[278]Service descriptor retrieval failed: "
            //                    + descriptorURL+ ", " + serviceDescMsg.getOperation().getResponseDetails());
            return null;
        }
//        if(!store){
//        	store = true;
//        	Utils.saveToFile(serviceDescMsg.getBodyBytes());
//        }
        
        if (!serviceDescMsg.isContentTypeTextUDA()) {
            //Log.i(TAG ,"[286]Received service descriptor without or with invalid Content-Type: " + descriptorURL);
            // We continue despite the invalid UPnP message because we can still hope to convert the content
        }

        String descriptorContent = serviceDescMsg.getBodyString();
        if (descriptorContent == null || descriptorContent.length() == 0) {
            //Log.i(TAG ,"[292]Received empty descriptor:" + descriptorURL);
            return null;
        }
//        Utils.print("operation", "operation"+serviceDescMsg.getBodyType());
        //Log.i(TAG ,"[296]Received service descriptor, hydrating service model: " + serviceDescMsg);
        ServiceDescriptorBinder serviceDescriptorBinder =
                getUpnpService().getConfiguration().getServiceDescriptorBinderUDA10();

        return serviceDescriptorBinder.describe(service, serviceDescMsg.getBodyString());
    }

    protected List<RemoteService> filterExclusiveServices(RemoteService[] services) {
        ServiceType[] exclusiveTypes = getUpnpService().getConfiguration().getExclusiveServiceTypes();

        if (exclusiveTypes == null || exclusiveTypes.length == 0)
            return Arrays.asList(services);

        List<RemoteService> exclusiveServices = new ArrayList();
        for (RemoteService discoveredService : services) {
            for (ServiceType exclusiveType : exclusiveTypes) {
                if (discoveredService.getServiceType().implementsVersion(exclusiveType)) {
                    //Log.i(TAG ,"[313]Including exlusive service: " + discoveredService);
                    exclusiveServices.add(discoveredService);
                } else {
                    //Log.i(TAG ,"[316]Excluding unwanted service: " + exclusiveType);
                }
            }
        }
        return exclusiveServices;
    }

}
