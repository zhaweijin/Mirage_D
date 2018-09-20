/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
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

package org.teleal.cling.mediarenderer.gstreamer;

import org.teleal.cling.binding.LocalServiceBinder;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.mediarenderer.MediaRenderer;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.ServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteDeviceIdentity;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.lastchange.LastChangeAwareServiceManager;
import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import com.mirage.util.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.IOException;
import java.net.URI;

/**
 * @author Christian Bauer
 */
public class GstMediaRenderer {

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;

    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    final protected LocalService connectionManagerService;
    final protected LocalService<GstAVTransportService> avTransportService;
    final protected LocalService<GstAudioRenderingControl> renderingControlService;
    
    final protected GstMediaPlayer mediaPlayer;


    
    final protected ServiceManager<GstConnectionManagerService> connectionManager;
    final protected LastChangeAwareServiceManager<GstAVTransportService> avTransport;
    final protected LastChangeAwareServiceManager<GstAudioRenderingControl> renderingControl;

    protected LocalDevice device;
    public static boolean mainThreadFlag = true;

    private String deviceType = "MediaRenderer";
    private int version = 1;
    
    private SharedPreferences preferences;
    final protected Context context;
    private final static int playerFlag = 0x111;
    
				
				
    public GstMediaRenderer(final Context context,int numberOfPlayers) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        // This is the backend which manages the actual player instances
        mediaPlayer = new GstMediaPlayer(
        		context,
                numberOfPlayers,
                avTransportLastChange,
                renderingControlLastChange
                );

        
//        Utils.print("render services start", "render services start");
        // The connection manager doesn't have to do much, HTTP is stateless
        connectionManagerService = binder.read(GstConnectionManagerService.class);
        
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new GstConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

		// The AVTransport just passes the calls on to the backend players
		avTransportService = binder.read(GstAVTransportService.class);
		avTransport = new LastChangeAwareServiceManager<GstAVTransportService>(
				avTransportService, new AVTransportLastChangeParser()) {
			@Override
			protected GstAVTransportService createServiceInstance() throws Exception {
				return new GstAVTransportService(avTransportLastChange,mediaPlayer);
			}
		};
        avTransportService.setManager(avTransport);
        

        // The Rendering Control just passes the calls on to the backend players
        renderingControlService = binder.read(GstAudioRenderingControl.class);
        renderingControl =
                new LastChangeAwareServiceManager<GstAudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected GstAudioRenderingControl createServiceInstance() throws Exception {
                        return new GstAudioRenderingControl(renderingControlLastChange, mediaPlayer,context);
                    }
                };
        renderingControlService.setManager(renderingControl);

  
    }

    
    
//    private void initVideoDisplay()
//    {
////        GPlayer player = new GPlayer();
//		
//        Intent intent = new Intent(context,GPlayer.class);
//        intent.putExtra("playURI", "");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//    }
    
    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    while (mainThreadFlag) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange();
                        renderingControl.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                        
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    
    public boolean getMainState()
    {
    	return mainThreadFlag;
    }

    public void closeDevices()
    {
    	device = null;
    }
    
    
    
    
    public void setMainState(Boolean state)
    {
    	this.mainThreadFlag = state;
    }
    
	public LocalDevice getDevice() {
		return device;
	}

//    synchronized public DisplayHandler getDisplayHandler() {
//        return displayHandler;
//    }
//
//    synchronized public void setDisplayHandler(DisplayHandler displayHandler) {
//        this.displayHandler = displayHandler;
//    }

    synchronized public GstMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    synchronized public void stopAllMediaPlayers() {
 
    }

    public ServiceManager<GstConnectionManagerService> getConnectionManager() {
        return connectionManager;
    }

    public ServiceManager<GstAVTransportService> getAvTransport() {
        return avTransport;
    }

    public ServiceManager<GstAudioRenderingControl> getRenderingControl() {
        return renderingControl;
    }

 
    

}
