package com.mirage.dmp;


import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;



import android.util.Log;

public class WireUpnpService extends AndroidUpnpServiceImpl {
	@Override
	protected AndroidUpnpServiceConfiguration createConfiguration() {
		

		return new AndroidUpnpServiceConfiguration(0) {

			/*
			 * The only purpose of this class is to show you how you'd configure
			 * the AndroidUpnpServiceImpl in your application:
			 * 
			 * @Override public int getRegistryMaintenanceIntervalMillis() {
			 * return 7000; }
			 * 
			 * @Override public ServiceType[] getExclusiveServiceTypes() {
			 * return new ServiceType[] { new UDAServiceType("SwitchPower") }; }
			 */

		};
	}

}