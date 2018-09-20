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

package org.teleal.cling.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
//import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.Router;

import com.mirage.util.Utils;


/**
 * Provides a UPnP stack with Android configuration (WiFi network only) as an application service component.
 * <p>
 * Sends a search for all UPnP devices on instantiation. See the {@link org.teleal.cling.android.AndroidUpnpService}
 * interface for a usage example.
 * </p>
 * <p/>
 * Override the {@link #createRouter(org.teleal.cling.UpnpServiceConfiguration, org.teleal.cling.protocol.ProtocolFactory, android.net.wifi.WifiManager, android.net.ConnectivityManager)}
 * and {@link #createConfiguration(android.net.wifi.WifiManager)} methods to customize the service.
 *
 * @author Christian Bauer
 */
public class AndroidUpnpServiceImpl extends Service {
	private final static String TAG="AndroidUpnpServiceImpl";
	
    protected UpnpService upnpService;
    protected Binder binder = new Binder();

    @Override
    public void onCreate() {
		super.onCreate();
		
		final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		upnpService = new UpnpServiceImpl(createConfiguration()) {
			@Override
			protected Router createRouter(ProtocolFactory protocolFactory,Registry registry) {

				AndroidWifiSwitchableRouter router = AndroidUpnpServiceImpl.this.createRouter(
						        AndroidUpnpServiceImpl.this,
								getConfiguration(),	
								protocolFactory,
								connectivityManager );

				Log.i( TAG , "...........goto registerReceiver");
				// Only register for network connectivity changes if we are not running on emulator
				registerReceiver(router.getBroadcastReceiver(),	new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
				
				return router;
			}
		};

	}

	protected AndroidUpnpServiceConfiguration createConfiguration() {

		return new AndroidUpnpServiceConfiguration(0);
	}

	protected AndroidWifiSwitchableRouter createRouter(
			Context context,
			UpnpServiceConfiguration configuration,
			ProtocolFactory protocolFactory,
			ConnectivityManager connectivityManager) {
		return new AndroidWifiSwitchableRouter(context, configuration, protocolFactory, connectivityManager);
    }

    @Override
    public void onDestroy() {
        if (!ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges())
            unregisterReceiver(((AndroidWifiSwitchableRouter) upnpService.getRouter()).getBroadcastReceiver());
        Utils.print("--->shutdown", "--->shutdown");
        upnpService.shutdown();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    

    protected boolean isListeningForConnectivityChanges() {
        return true;
    }

    protected class Binder extends android.os.Binder implements AndroidUpnpService {

        public UpnpService get() {
            return upnpService;
        }

        public UpnpServiceConfiguration getConfiguration() {
            return upnpService.getConfiguration();
        }

        public Registry getRegistry() {
            return upnpService.getRegistry();
        }

        public ControlPoint getControlPoint() {
            return upnpService.getControlPoint();
        }
    }

}