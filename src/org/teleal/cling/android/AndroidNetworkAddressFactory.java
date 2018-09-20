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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.teleal.cling.model.Constants;
import org.teleal.cling.transport.spi.InitializationException;
import org.teleal.cling.transport.spi.NetworkAddressFactory;

import com.mirage.dlna.ConnectInfo;
import com.mirage.dlna.NetworkConnectState;
import com.mirage.util.Utils;



import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation appropriate for Android environment, avoids unavailable methods.
 * <p>
 * Detects only (one) WiFi network interface on an Android device and its addresses,
 * ignores all other interfaces. Requires the Android <code>WifiManager</code> to
 * ensure that the discovered interface is really the WiFi interface.
 * </p>
 *
 * @author Christian Bauer
 */
public class AndroidNetworkAddressFactory implements NetworkAddressFactory {
	private final static  String TAG="AndroidNetworkAddressFactory >>> ";

    final private static Logger log = Logger.getLogger(NetworkAddressFactory.class.getName());


    protected NetworkInterface networkIface;
    protected List<InetAddress> bindAddresses = new ArrayList();

    /**
     * Defaults to an ephemeral port.
     */
	public AndroidNetworkAddressFactory()throws InitializationException {
		Log.i(TAG , "AndroidNetworkAddressFactory");

		networkIface = getNetworkInterface();

        
        if (networkIface == null)
            throw new InitializationException("Could not discover a network interface");
        log.info("Discovered WiFi network interface: " + networkIface.getDisplayName());

        discoverBindAddresses();
    }

    protected void discoverBindAddresses() throws InitializationException {
        try {

			Log.i(TAG , "Discovering addresses of interface: "+ networkIface.getDisplayName());
            for (InetAddress inetAddress : getInetAddresses(networkIface)) {
				Log.i(TAG , "inetAddress===" + inetAddress);
                if (inetAddress == null) {
					Log.i(TAG , "Network has a null address: "+ networkIface.getDisplayName());
					continue;
				}
				if (isUsableAddress(inetAddress)) {
					Log.i(TAG , "Discovered usable network interface address: "+ inetAddress.getHostAddress());
					bindAddresses.add(inetAddress);
				} else {
					Log.i(TAG , "Ignoring non-usable network interface address: "+ inetAddress.getHostAddress());
				}
            }

        } catch (Exception ex) {
            throw new InitializationException("Could not not analyze local network interfaces: " + ex, ex);
        }
    }

    protected boolean isUsableAddress(InetAddress address) {
		if (!(address instanceof Inet4Address)) {
			Log.i(TAG , "Skipping unsupported non-IPv4 address: " + address);
			return false;
        }
        return true;
    }

    protected List<InetAddress> getInetAddresses(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses());
    }

    public InetAddress getMulticastGroup() {
        try {
            return InetAddress.getByName(Constants.IPV4_UPNP_MULTICAST_GROUP);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getMulticastPort() {
        return Constants.UPNP_MULTICAST_PORT;
    }

    public int getStreamListenPort() {
        return 0; // Ephemeral
    }

    public NetworkInterface[] getNetworkInterfaces() {
        return new NetworkInterface[] { networkIface };
    }

    public InetAddress[] getBindAddresses() {
        return bindAddresses.toArray(new InetAddress[bindAddresses.size()]);
    }

    public byte[] getHardwareAddress(InetAddress inetAddress) {
        return null; // TODO: Get this from WifiInfo from WifiManager
    }

    public InetAddress getBroadcastAddress(InetAddress inetAddress) {
        return null; // TODO: No low-level network interface methods available on Android API
    }

    public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress) {
        // TODO: This is totally random because we can't access low level InterfaceAddress on Android!
        for (InetAddress localAddress : getInetAddresses(networkInterface)) {
            if (isIPv6 && localAddress instanceof Inet6Address)
                return localAddress;
            if (!isIPv6 && localAddress instanceof Inet4Address)
                return localAddress;
        }
        throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
    }

    // Code from: http://www.gubatron.com/blog/2010/09/19/android-programming-how-to-obtain-the-wifis-corresponding-networkinterface/

    public static NetworkInterface getNetworkInterface() {

		return Utils.getActualNetworkInterface();
	}

	// public static NetworkInterface
	// getEmulatorWifiNetworkInterface(WifiManager manager) {
	public static NetworkInterface getEmulatorWifiNetworkInterface(Object manager) {
		// Return the first network interface that is not loopback
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface iface : interfaces) {
				List<InetAddress> addresses = Collections.list(iface
						.getInetAddresses());
				for (InetAddress address : addresses) {
					if (!address.isLoopbackAddress())
						return iface;
				}
			}
		} catch (Exception ex) {
			throw new InitializationException("Could not find emulator's network interface: " + ex, ex);
		}
		return null;
	}

	

    static int byteArrayToInt(byte[] arr, int offset) {
        if (arr == null || arr.length - offset < 4)
            return -1;

        int r0 = (arr[offset] & 0xFF) << 24;
        int r1 = (arr[offset + 1] & 0xFF) << 16;
        int r2 = (arr[offset + 2] & 0xFF) << 8;
        int r3 = arr[offset + 3] & 0xFF;
        return r0 + r1 + r2 + r3;
    }
    

    
}
