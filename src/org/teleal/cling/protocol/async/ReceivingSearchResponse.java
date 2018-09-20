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

package org.teleal.cling.protocol.async;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.ValidationError;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.message.IncomingDatagramMessage;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.discovery.IncomingSearchResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteDeviceIdentity;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.protocol.ReceivingAsync;
import org.teleal.cling.protocol.RetrieveRemoteDescriptors;

import com.mirage.util.Utils;

import android.util.Log;

import java.util.logging.Logger;

/**
 * Handles reception of search response messages.
 * <p>
 * This protocol implementation is basically the same as
 * the {@link org.teleal.cling.protocol.async.ReceivingNotification} protocol for
 * an <em>ALIVE</em> message.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingSearchResponse extends ReceivingAsync<IncomingSearchResponse> {
	
	final private static String TAG = "ReceivingSearchResponse";
	
    final private static Logger log = Logger.getLogger(ReceivingSearchResponse.class.getName());

    public ReceivingSearchResponse(UpnpService upnpService, IncomingDatagramMessage<UpnpResponse> inputMessage) {
        super(upnpService, new IncomingSearchResponse(inputMessage));
    }

    protected void execute() {
//    	Utils.print(TAG , "execute");
    	
        if (!getInputMessage().isSearchResponseMessage()) {
            //Log.i(TAG , "Ignoring invalid search response message: " + getInputMessage());
            return;
        }

        UDN udn = getInputMessage().getRootDeviceUDN();
        if (udn == null) {
            //Log.i(TAG , "Ignoring search response message without UDN: " + getInputMessage());
            return;
        }

        RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity(getInputMessage());
        //Log.i(TAG , "Received device search response: " + rdIdentity);

        if (getUpnpService().getRegistry().update(rdIdentity)) {
            //Log.i(TAG , "Remote device was already known: " + udn);
            return;
        }

        RemoteDevice rd;
        try {
            rd = new RemoteDevice(rdIdentity);
        } catch (ValidationException ex) {
            //Log.i(TAG , "Validation errors of device during discovery: " + rdIdentity);
            for (ValidationError validationError : ex.getErrors()) {
                //Log.i(TAG , validationError.toString());
            }
            return;
        }

        if (rdIdentity.getDescriptorURL() == null) {
            //Log.i(TAG , "Ignoring message without location URL header: " + getInputMessage());
            return;
        }

        if (rdIdentity.getMaxAgeSeconds() == null) {
            //Log.i(TAG , "Ignoring message without max-age header: " + getInputMessage());
            return;
        }

//        Utils.print(TAG, "go descriptors");
        // Unfortunately, we always have to retrieve the descriptor because at this point we
        // have no idea if it's a root or embedded device
        getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(
                new RetrieveRemoteDescriptors(getUpnpService(), rd)
        );

    }

}
