package com.mirage.dlna.application;

import java.util.ArrayList;
import java.util.HashMap;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.support.model.DIDLContent;
import android.app.Application;
import com.mirage.dmp.ContentItem;
import com.mirage.dmp.DeviceItem;
 



public class BaseApplication extends Application {

	public DeviceItem deviceItem;

 

	public AndroidUpnpService upnpService;
	public ArrayList<ContentItem> listcontent;

	
	public ArrayList<ContentItem>  listPhoto;
	public ArrayList<ContentItem>  listMusic;
	public ArrayList<ContentItem>  listVideo;

	public HashMap<String, ArrayList<ContentItem>> map;

	public DIDLContent didl;
	public int position;
	
	//add by carter
	public DeviceItem dmrDeviceItem;
	
	//for music play list
	public ArrayList<ContentItem>  listPlayMusic = new ArrayList<ContentItem>();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
}
