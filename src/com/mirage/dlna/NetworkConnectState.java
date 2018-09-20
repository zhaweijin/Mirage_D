package com.mirage.dlna;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import com.mirage.util.Utils;
import android.content.Context;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


public class NetworkConnectState {
	private final static String TAG = "NetworkConnectState";

	public final static int NETWORK_INVALID = 0;
	public final static int NETWORK_USE_WIFI = 1;
	public final static int NETWORK_USE_ETH = 2;
	public final static int NETWORK_USE_SOFTAP = 3;
	public final static int NETWORK_USE_ETH_SOFTAP = 4;
	public final static int NETWORK_USE_WIFI_DIRECT = 5;
	public final static int NETWORK_USE_TI_WIFI = 6;

	private State softap_state = State.UNKNOWN;
	private State wifi_state = State.UNKNOWN;
	private State eth_state = State.UNKNOWN;
	private State wifi_direct_state = State.UNKNOWN;
	private State ti_wifi_state = State.UNKNOWN;
 
	private static ConnectInfo dlnaNetwork;

	private static String softap_info = null;
	private static String softap_ifname = null;

	private static String wifi_info = null;
	private static String wifi_ifname = null;

	private static String ethernet_info = null;
	private static String ethernet_ifname = null;
	
	
	private static String wifi_direct_info = null;
	private static String wifi_direct_name = null;
	
	
	private static String ti_wifi_info = null;
	private static String ti_wifi_ifname = null;
	
	
	private static int choose_connect = NETWORK_INVALID;
	private Context context;



	// do network update
	public NetworkConnectState(Context context) {
		this.context = context;
		
		if (dlnaNetwork == null) {
			dlnaNetwork = new ConnectInfo();
			dlnaNetwork.type = NETWORK_INVALID;
			choose_connect = NETWORK_INVALID;
			UpdateConnectState();
		}
	}

	public void ValidInstance() {

	}

	public static ConnectInfo GetConnectInfo() {
		
		return dlnaNetwork;
	}

	public static void ClearConnectInfo() {
		
		dlnaNetwork = null;
	}

	public static int GetConnectType() {
		if (null != dlnaNetwork) {
			return dlnaNetwork.type;
		} else {
			Utils.print(TAG, "GetConnectType.....null");
			return NETWORK_INVALID;
		}
	}

	private void CheckNetworkConnect() {
		// check soft ap connect state
		List<NetworkInterface> interfaces;
		try {
			interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			Utils.print("is used network", ""+interfaces.size());
			softap_state = State.UNKNOWN;
			eth_state = State.UNKNOWN;
			wifi_state = State.UNKNOWN;
			wifi_direct_state = State.UNKNOWN;
			
			for (NetworkInterface iface : interfaces) {
				Utils.print("info--->", iface.getDisplayName());
				if (iface.getDisplayName().equals("tiap0") || iface.getDisplayName().equals("wl0.1")) {    //wifi 热点
					Utils.print(TAG, "softap_info");
					List<InetAddress> addresses = Collections.list(iface
							.getInetAddresses());
					softap_state = State.CONNECTED;
					softap_info =  addresses.get(0).toString().substring(1);
					softap_ifname = iface.getDisplayName();
					
				} else if (iface.getDisplayName().equals("eth0")) {  //ethernet
					Utils.print(TAG, "ethernet_info");
					List<InetAddress> addresses = Collections.list(iface
							.getInetAddresses());
					if(addresses.size()>0)
					{
						Utils.print("size>", addresses.size()+"");
						eth_state = State.CONNECTED;
						ethernet_info = addresses.get(0).toString().substring(1);
						ethernet_ifname = iface.getDisplayName();
					}
					//tiwlan0
				} else if (iface.getDisplayName().equals("wlan0")) {  //common wlan
					Utils.print(TAG, "wifi_info");
					List<InetAddress> addresses = Collections.list(iface
							.getInetAddresses());
					if(addresses.size()>0)
					{
						Utils.print("size>", addresses.size()+"");
						wifi_state = State.CONNECTED;
						wifi_info =  addresses.get(0).toString().substring(1);
//						Utils.print("wifi_ip", wifi_info);
						wifi_ifname = iface.getDisplayName();
					}		
				}
				else if (iface.getDisplayName().equals("tiwlan0")) {  //tiwan wlan
					Utils.print(TAG, "ti_wifi_info");
					List<InetAddress> addresses = Collections.list(iface
							.getInetAddresses());
					if(addresses.size()>0)
					{
						Utils.print("size>", addresses.size()+"");
						ti_wifi_state = State.CONNECTED;
						ti_wifi_info =  addresses.get(0).toString().substring(1);
						ti_wifi_ifname = iface.getDisplayName();
					}		
				}
				else if(iface.getDisplayName().equals("p2p-wlan0-0"))   //wifi direct
				{
					Utils.print(TAG, "wifi_direct");
					List<InetAddress> addresses = Collections.list(iface
							.getInetAddresses());
					if(addresses.size()>0)
					{
						Utils.print("size>", addresses.size()+"");
						wifi_direct_state = State.CONNECTED;
						wifi_direct_info = addresses.get(0).toString().substring(1);
						wifi_direct_name = iface.getDisplayName();
					}		
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void UpdateConnectState() {
		
		Utils.print(TAG, "UpdateConnectState");
		// cur_network_state = NETWORK_INVALID;
		// config_array_1 = dlna.DlnaGetConfig();

		CheckNetworkConnect();
		
		if (dlnaNetwork == null) {
			Utils.print(TAG, "NetworkConnectState  New One");
			dlnaNetwork = new ConnectInfo();
			dlnaNetwork.type = NETWORK_INVALID;
		}

		if (softap_state == State.CONNECTED && eth_state == State.CONNECTED) {
			// show a dialog, choose softap or ethernet? if softAP, do not
			// check
			// wifi and ethernet.
			if(choose_connect == NETWORK_INVALID){
				dlnaNetwork.type = NETWORK_USE_ETH_SOFTAP;
				dlnaNetwork.ifname = null;
				dlnaNetwork.info = null;
			}
		} else if (eth_state == State.CONNECTED) {
			dlnaNetwork.type = NETWORK_USE_ETH;
			dlnaNetwork.info = ethernet_info;
			dlnaNetwork.ifname = ethernet_ifname;
		} else if (softap_state == State.CONNECTED) {
			dlnaNetwork.type = NETWORK_USE_SOFTAP;
			dlnaNetwork.ifname = softap_ifname;
			dlnaNetwork.info = softap_info;
		}else if(wifi_direct_state == State.CONNECTED)
		{
			dlnaNetwork.type = NETWORK_USE_WIFI_DIRECT;
			dlnaNetwork.ifname = wifi_direct_name;
			dlnaNetwork.info = wifi_direct_info;
		
		} else if (wifi_state == State.CONNECTED) {
			dlnaNetwork.type = NETWORK_USE_WIFI;
			dlnaNetwork.ifname = wifi_ifname;

			// get wifi ssid which we connected to .
//			WifiManager wifi_serviceManager = (WifiManager) context
//					.getSystemService(Context.WIFI_SERVICE);
//			WifiInfo wifiInfo = wifi_serviceManager.getConnectionInfo();
//			String ssidString = wifiInfo.getSSID();
//			int ipaddr = wifiInfo.getIpAddress();
//			wifi_info = ssidString + "  " + intToIp(ipaddr);

			dlnaNetwork.info = wifi_info;

		}else if (ti_wifi_state == State.CONNECTED) {
			dlnaNetwork.type = NETWORK_USE_TI_WIFI;
			dlnaNetwork.ifname = ti_wifi_ifname;

			// get wifi ssid which we connected to .
//			WifiManager wifi_serviceManager = (WifiManager) context
//					.getSystemService(Context.WIFI_SERVICE);
//			WifiInfo wifiInfo = wifi_serviceManager.getConnectionInfo();
//			String ssidString = wifiInfo.getSSID();
//			int ipaddr = wifiInfo.getIpAddress();
//			ti_wifi_info = ssidString + "  " + intToIp(ipaddr);
			

			dlnaNetwork.info = ti_wifi_info;

		}
		else if (wifi_state != State.CONNECTED
				&& eth_state != State.CONNECTED
				&& softap_state != State.CONNECTED) {
			// tell the user should do network configuration firstly
			dlnaNetwork.type = NETWORK_INVALID;
			dlnaNetwork.ifname = null;
			dlnaNetwork.info = null;
			Utils.print(TAG, " No valid connected");
		}
		
		else {
			Utils.print(TAG, "get network state error");
		}
		
		Utils.print(TAG, "UpdateConnectState , network info" + dlnaNetwork.info + dlnaNetwork.ifname);
	}

	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}
	
	public static void SetConnectTye(int type) {
		if(dlnaNetwork.type != type){
			dlnaNetwork.type = type;
			choose_connect = type;
			if (dlnaNetwork.type == NETWORK_USE_SOFTAP) {
				dlnaNetwork.info = softap_info;
				dlnaNetwork.ifname = "tiap0";
			} else if (dlnaNetwork.type == NETWORK_USE_ETH) {
				dlnaNetwork.info = ethernet_info;
				dlnaNetwork.ifname = "eth0";
			}
		}
	}

}
