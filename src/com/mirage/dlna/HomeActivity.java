package com.mirage.dlna;



import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.contentdirectory.ui.ContentBrowseActionCallback;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.container.Container;
import org.teleal.common.util.MimeType;
import com.update.download.DataSet;
import com.update.download.DownloadUtils;
import com.mirage.dlna.application.ContentConfigData;
import com.mirage.dlna.application.MessageControl;
import com.mirage.dlna.application.BaseApplication;
import com.mirage.dlna.music.MusicPlayer;
import com.mirage.dlna.music.PlayMode;
import com.mirage.dlna.music.PlayerService;
import com.mirage.dmp.ContainerListAdapter;
import com.mirage.dmp.ContentItem;
import com.mirage.dmp.DeviceItem;
import com.mirage.dmp.DevicesAdapter;
import com.mirage.dmp.ImageDisplay;
import com.mirage.dmp.WireUpnpService;
import com.mirage.util.MimetypeMap;
import com.mirage.util.Utils;
import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends Activity{

	private final static String TAG = "HomeActivity";
	
	private LinearLayout layoutView;
	private LinearLayout.LayoutParams LP_FF = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
			                                         LinearLayout.LayoutParams.FILL_PARENT);
	private GridView gridView = null;
	private ListView dmsListView = null;
    private RelativeLayout flashLayout;
    private RelativeLayout playLayout;
    private RelativeLayout setLayout;
    private RelativeLayout exitLayout;
    
    private RelativeLayout homeLayout;
    
    //for load failed 
    private int loadTimes = 0;
    private DeviceItem currentDeviceItem;
    private ContentItem currentContentItem;
    
    
    private ImageView flashIcon;
    private TextView flashName;
    //preference
	private SharedPreferences preferences;
    //type
    private int TYPE = 1;       // devices 1,file 2,0 exit
    private int CURRENT_DEVICES = 1;
    private int CURRENT_FILE = 2;
    private int CURRENT_EXIT = 0;
    
    private boolean networkIsSelect = false;
    
    //download util
    private DownloadUtils downloadUtils;
    //network change
    private NetworkConnectState mNetworkConnectState;
    
    private int Display_Type = 1;  //1 gridview,2 listview Display
    private int GRIDVIEW_DISPLAY = 1;
    private int LISTVIEW_DISPLAY = 2;
    
    private boolean searchStart = false;
 
    //container
    public HashMap<String, ArrayList<ContentItem>> map = new HashMap<String, ArrayList<ContentItem>>();
	public int folder = -1;  //代表文件夾的层次数。如果是-1就代表非文件夾状态
	
	public static AndroidUpnpService upnpService = null;
	private static DeviceListRegistryListener deviceListRegistryListener;
	//devices data
	private ArrayList<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();
	private DevicesAdapter devicesAdapter=null;
	private int curconnecttype = NetworkConnectState.NETWORK_INVALID;
	
	private CustomProgressDialog progressDialog;
	//dmr 
	private ArrayList<DeviceItem> dmrItemList = new ArrayList<DeviceItem>();
	public  PopupWindow mPopupWindow;
	private ListView dmrListView;
	private boolean dmrAdapterNotificaton = false;
	private boolean isDmrAdding = false;
	private View displayView = null; 
	
	//container data
	private ArrayList<ContentItem> contentItems = new ArrayList<ContentItem>();
	private ArrayList<ContentItem> listPhoto = new ArrayList<ContentItem>();
	private  ArrayList<ContentItem> listMusic = new ArrayList<ContentItem>();
	private  ArrayList<ContentItem> listVideo = new ArrayList<ContentItem>();
	

	
	//用于返回
	private ArrayList<ContentItem> list = new ArrayList<ContentItem>();
	
	private ContainerListAdapter containerListAdapter = null;
	
	private LinearLayout layout_network_tips;
	
	private String currentContentFormatMimeType = "";
	
	private DeviceItem localDeviceItem = null;
    private boolean isCheckUpdate = false;
	

	
	private final int DMR_REFLASH = 0x222;
	private final int DMR_POP = 0x333;
	private final int HOME = 0x444;
	private final int DMS_RFELASH = 0x555;
	private final int SEARCH_CANCEL = 0x666;
	private final int SOFTWARE_UPDATE = 0x777;
	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MessageControl.DEVICES_REFLESH:
				TYPE = CURRENT_DEVICES;
				folder = -1;
				map.clear();
				
				flashIcon.setBackgroundResource(R.drawable.ic_flash);
				flashName.setText(getResources().getString(R.string.flash));
				homeLayout.setBackgroundResource(R.drawable.home_background);
				
                Utils.print("bind1", "bind1");
				getApplication().bindService(
						new Intent(HomeActivity.this, WireUpnpService.class),
						serviceConnection, Context.BIND_AUTO_CREATE);
				break;
			case MessageControl.CONTAINER_REFLESH:
				
				stopProgressDialog();
				
				flashIcon.setBackgroundResource(R.drawable.ic_home);
				flashName.setText(getResources().getString(R.string.home));
				homeLayout.setBackgroundResource(R.drawable.grobal_background);
				
				TYPE = CURRENT_FILE;
				ContentConfigData myContentConfig = (ContentConfigData) msg.obj;
				contentItems = myContentConfig.listcontent;
				listMusic=myContentConfig.listmusic;
				listPhoto=myContentConfig.listphoto;
				listVideo=myContentConfig.listvideo;
				
				BaseApplication myApplication = (BaseApplication) getApplication();
				myApplication.listcontent = contentItems;
				myApplication.listMusic=myContentConfig.listmusic;
				myApplication.listPhoto=myContentConfig.listphoto;
				myApplication.listVideo=myContentConfig.listvideo;
				// 返回建
				folder++;
				Utils.print(TAG, "folder" + folder);
				map.put(folder + "", myContentConfig.listcontent);

				int len = map.size();
//				Utils.print(TAG, "map.size===" + map.size());
//
//				for (int i = 0; i < len; i++) {
//					Utils.print(TAG, "ArrayList<ContentItem>=="+ map.get(i + "").size());
//				}
				
				if(Display_Type==GRIDVIEW_DISPLAY){
					ContainerListAdapter myContentListAdapter = new ContainerListAdapter(
							HomeActivity.this, contentItems, gridView,1);
					gridView.setAdapter(myContentListAdapter);
				}else if(Display_Type==LISTVIEW_DISPLAY){
					ContainerListAdapter myContentListAdapter = new ContainerListAdapter(
							HomeActivity.this, contentItems, dmsListView,2);
					dmsListView.setAdapter(myContentListAdapter);
				}
				
//				containerListAdapter = new ContainerListAdapter(HomeActivity.this, contentItems, gridView);
//				gridView.setAdapter(containerListAdapter);

				break;
			case DMR_REFLASH:
				Utils.print("dmr adapter", "dmr adapter");
				break;
			case MessageControl.LOADING_FAILED:
				loadFailed();
				break;
			case DMR_POP:
				popDMR(displayView);
				break;
			case HOME:
				TYPE = CURRENT_DEVICES;
				folder = -1;
				map.clear();
				
				flashIcon.setBackgroundResource(R.drawable.ic_flash);
				flashName.setText(getResources().getString(R.string.flash));
				homeLayout.setBackgroundResource(R.drawable.home_background);

				if(Display_Type==GRIDVIEW_DISPLAY)
				{
					devicesAdapter = new DevicesAdapter(HomeActivity.this, deviceItemList,gridView);
					gridView.setAdapter(devicesAdapter);
				}
				else if(Display_Type==LISTVIEW_DISPLAY)
				{
					devicesAdapter = new DevicesAdapter(HomeActivity.this, deviceItemList,
							dmsListView,DevicesAdapter.LIST_DEVICES_ADAPTER);
					dmsListView.setAdapter(devicesAdapter);
				}
				
				break;
			case DMS_RFELASH:
				Utils.print("dms reflash", "flash");
				
				RefreshDevcieList();
				break;
			case SEARCH_CANCEL:
				if(deviceItemList.size()<=0)
					stopProgressDialog();
				break;
			case SOFTWARE_UPDATE:
				downloadUtils.checkIsUpdate(HomeActivity.this,(DataSet)msg.obj);
				break;
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home_layout);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		initLayout();
		registerBroadcast();
		
		if(!Utils.isNetworkConnected(this) || Utils.getAPNType(this).equals("G"))
		{
			displayNetworkDialog();
			networkIsSelect = true;
		}else {
			
			//init notwork
			new NetworkConnectState(HomeActivity.this);   //初始化网络状态
			curconnecttype = NetworkConnectState.GetConnectType();
			ConnectInfo mConnectinfo = NetworkConnectState.GetConnectInfo();
			
			
			if (mConnectinfo.type == NetworkConnectState.NETWORK_USE_ETH_SOFTAP) {
				// show a dialog, choose softap or ethernet? if softAP, do not check
				// wifi and ethernet.
				Utils.print(TAG, "===============Please choose SoftAP / Ethernet");
				networkSelectDialog();
			}
			else {
				startLoadData();
			}
		}
	}
	
	
	private void startLoadData()
	{
		networkIsSelect = true;
		DeviceItem item = new DeviceItem();
		item.setLabel(new String[]{Utils.LOCAL_RENDER_NAME});
		dmrItemList.add(item);
		
		if(progressDialog!=null && progressDialog.isShowing()){
			
		}else {
			startProgressDialog(getResources().getString(R.string.search_devices),true);
		}
		
//		layout_network_tips = (LinearLayout)findViewById(R.id.layout_network_tips);
//		Utils.print("-----------", "---------"+Boolean.toString(Utils.isNetworkConnected(this)));
			
		startScanDevices();

			
		if(!isCheckUpdate){
			isCheckUpdate = true;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						if(downloadUtils==null)
							downloadUtils = new DownloadUtils();
						DataSet dataSet = downloadUtils.getSoftwareWebData("http://miragerdp.poptronixtech.com/update.php");
					    Message message = new Message();
					    message.obj = dataSet;
					    message.what = SOFTWARE_UPDATE;
					    handler.sendMessage(message);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}).start();
			
			
		}
		
	    setSearchTimeout();
	}
	
	
	public void initLayout()
	{
		flashLayout = (RelativeLayout)findViewById(R.id.layout_flash);
		playLayout = (RelativeLayout)findViewById(R.id.layout_device_play);
		setLayout = (RelativeLayout)findViewById(R.id.layout_set);
		exitLayout = (RelativeLayout)findViewById(R.id.layout_exit);
		
		flashIcon = (ImageView)findViewById(R.id.flash_icon);
		flashName = (TextView)findViewById(R.id.flash_name);
		
		homeLayout = (RelativeLayout)findViewById(R.id.home_layout);
		
		flashLayout.setOnClickListener(onClickListener);
		playLayout.setOnClickListener(onClickListener);
		setLayout.setOnClickListener(onClickListener);
		exitLayout.setOnClickListener(onClickListener);
		
		layoutView = (LinearLayout)findViewById(R.id.layout_view);
		
		
		Display_Type = preferences.getInt("display_type", LISTVIEW_DISPLAY);
		if(Display_Type==GRIDVIEW_DISPLAY)
		{
			if(gridView==null)
			{
				gridView = new GridView(this);
				gridView.setLayoutParams(LP_FF);
				gridView.setVerticalSpacing(50);
				gridView.setCacheColorHint(Color.TRANSPARENT);
				gridView.setOnItemClickListener(new ItemOnClickListener());
			}
	        layoutView.addView(gridView);
	        
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			gridView.setNumColumns(3);
			gridView.setColumnWidth((int)(metrics.widthPixels/(float)3));

			
		}
		else if(Display_Type==LISTVIEW_DISPLAY)
		{
			if(dmsListView==null)
			{
				dmsListView = new ListView(this);
				dmsListView.setLayoutParams(LP_FF);
				dmsListView.setDivider(getResources().getDrawable(R.drawable.set_horizontal_dot));
				dmsListView.setCacheColorHint(Color.TRANSPARENT);
				dmsListView.setOnItemClickListener(new ItemOnClickListener());
			}
	        layoutView.addView(dmsListView);
		}
		
		mNetworkConnectState = new NetworkConnectState(HomeActivity.this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	public void startScanDevices()
	{
		
		TYPE = CURRENT_DEVICES;
		folder = -1;
		map.clear();
		
		flashIcon.setBackgroundResource(R.drawable.ic_flash);
		flashName.setText(getResources().getString(R.string.flash));
		homeLayout.setBackgroundResource(R.drawable.home_background);
		
		deviceListRegistryListener = new DeviceListRegistryListener();// 检查设备
		
		if(Display_Type==GRIDVIEW_DISPLAY)
		{
			devicesAdapter = new DevicesAdapter(this, deviceItemList,gridView);
			gridView.setAdapter(devicesAdapter);
		}
		else if(Display_Type==LISTVIEW_DISPLAY)
		{
			devicesAdapter = new DevicesAdapter(this, deviceItemList, 
					dmsListView,DevicesAdapter.LIST_DEVICES_ADAPTER);
			dmsListView.setAdapter(devicesAdapter);
		}

		Utils.print(TAG, "............WireUpnpService");
		Utils.print("bind2", "bind2");
		
		searchStart = true;
		
		this.getApplicationContext().bindService(
				new Intent(this, WireUpnpService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	
	class ItemOnClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			try {
				Utils.print(TAG, ">>>>>item on click ");
				if (TYPE == CURRENT_DEVICES) {
					startProgressDialog(getResources().getString(R.string.loading),true);
					DeviceItem deviceItem = (DeviceItem) arg0.getItemAtPosition(arg2);
					Utils.print(TAG,"Now click on device===" + deviceItem.toString());
					BaseApplication application = (BaseApplication) HomeActivity.this.getApplication();
					
					application.deviceItem = deviceItem;
					application.upnpService = upnpService;
					application.map = new HashMap<String, ArrayList<ContentItem>>();

					currentDeviceItem = deviceItem;
					loadTimes = 0;
					startLoadContainer(deviceItem);
					
					
				} else if (TYPE == CURRENT_FILE) {
					final ContentItem content = (ContentItem) arg0.getItemAtPosition(arg2);
					Utils.print(TAG, "arg2====" + arg2);				
 
					if (content.isContainer()) {
						Utils.print(TAG, "isContainer()");
						loadTimes = 0;
						currentContentItem = content;
						startProgressDialog(getResources().getString(R.string.loading),true);
						upnpService.getControlPoint().execute(
								new ContentBrowseActionCallback(HomeActivity.this,
										content.getService(), content.getContainer(),handler));

					} else {

						List<Res> res = content.getItem().getResources();
						String filetype = null;
						MimeType filemt = res.get(0).getProtocolInfo().getContentFormatMimeType();
						
						if (filemt != null) {
							currentContentFormatMimeType = filemt.toString();
							Utils.print("play mine type", currentContentFormatMimeType);
							filetype = filemt.getType();
						}

						if (filetype != null) {
							if (filetype.equals("image")) {
								
								Intent intent = new Intent();
								intent.setClass(HomeActivity.this, ImageDisplay.class);
								intent.putExtra("playURI", content.getItem().getFirstResource().getValue());
								startActivity(intent);
							} else if (filetype.equals("audio")) {
									Intent intent = new Intent();
									intent.setClass(HomeActivity.this,MusicPlayer.class);
									intent.putExtra("playURI", content.getItem().getFirstResource().getValue());
									startActivity(intent);
							}

							else if (filetype.equals("video")) {
								if (content.getItem().getFirstResource() == null) {
									Toast.makeText(HomeActivity.this,R.string.cannotplay, Toast.LENGTH_SHORT).show();
								} else {
                                    Utils.print("22", "22");
                                    Intent it = null;
										it = new Intent(Intent.ACTION_VIEW);
										Uri uri = Uri.parse(content.getItem().getFirstResource().getValue());
										it.setDataAndType(uri, "video/*");
										startActivity(it);
									}
								}
							} else {
								new AlertDialog.Builder(HomeActivity.this)
										.setTitle(android.R.string.dialog_alert_title)
										.setItems(getResources().getTextArray(R.array.choose_types),
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(DialogInterface dialog,int which) {
														// TODO Auto-generated
														// method stub
														startActivity(Utils.getMIMEType(
																		HomeActivity.this,
																		which,
																		content));
													}
												}).show();

							}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	}
	
	public void startLoadContainer(DeviceItem deviceItem)
	{
		try {
			Utils.print("start load container", "start load container");
			if (deviceItem != null) {
				Device device = deviceItem.getDevice();
				Service service = device.findService(new UDAServiceType("ContentDirectory"));

			    Utils.print(TAG, "device=====" + device);
			    Utils.print(TAG, "service====" + service);
				upnpService.getControlPoint().execute(
						new ContentBrowseActionCallback(HomeActivity.this, service,
								createRootContainer(service), handler));

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			handler.sendEmptyMessage(MessageControl.LOADING_FAILED);
		}

	}
	
	
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.layout_flash:
				flashLayout.setSelected(true);
				if (TYPE == CURRENT_DEVICES)
					handler.sendEmptyMessage(DMS_RFELASH);
				else if (TYPE == CURRENT_FILE)
					handler.sendEmptyMessage(HOME);
				break;
			case R.id.layout_device_play:
				playLayout.setSelected(true);
				displayView = v;
//				handler.sendEmptyMessage(DMR_POP);
				break;
			case R.id.layout_set:
				break;
			case R.id.layout_exit:
				exitLayout.setSelected(true);
				if(devicesAdapter!=null)
					deviceItemList.clear();
				
				try {
					
					if(serviceConnection!=null){
						getApplicationContext().unbindService(serviceConnection);
					}

					if (upnpService != null) {
						upnpService.getRegistry()
								.removeListener(deviceListRegistryListener);
					}
					
					if(mPopupWindow!=null && mPopupWindow.isShowing())
						mPopupWindow.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				exit();
				
				break;
			}
		}
	};
	
	
	public void cancelMusicNotification()
	{
		Utils.print("cancel", "cancel");
		NotificationManager notificationManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(PlayMode.Music_Notification_ID);
	}
	
	public void exit()
	{
		startProgressDialog(getResources().getString(R.string.exiting), false);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1500);

					stopService(new Intent(HomeActivity.this, PlayerService.class));  //关闭音乐服务，监听网络变化状态
					
					cancelMusicNotification();
				
					
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(-1);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}).start();
	}
	

	
	protected Container createRootContainer(Service service) {
//		Log.i(TAG, "createRootContainer");
        try {
    		Container rootContainer = new Container();
    		rootContainer.setId("0");
    		rootContainer.setTitle("Content Directory on "
    				+ service.getDevice().getDisplayString());

    		return rootContainer;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
      
	}
	
	
	public class DeviceListRegistryListener extends DefaultRegistryListener {
		/* Discovery performance optimization for very slow Android devices! */
		
		
		// handle the get device icon thread

		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry,
				RemoteDevice device) {
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry,
				final RemoteDevice device, final Exception ex) {
		}

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
//			Utils.print(TAG, "remoteDeviceAdded.....xml = "
//					+ device.getIdentity().getDescriptorURL().toString());
			Utils.print(TAG, "remoteDeviceAdded.....deviceName="
					+ device.getDetails().getFriendlyName());


			if (device.getType().getNamespace().equals("schemas-upnp-org")
					&& device.getType().getType().equals("MediaServer")) {
				Utils.print("device-type-dms", device.getDetails().getFriendlyName());
				final DeviceItem display = new DeviceItem(device, device
						.getDetails().getFriendlyName(), device
						.getDisplayString(), "(REMOTE) "
						+ device.getType().getDisplayString());
				deviceAdded(display);
			}
			else if(device.getType().getNamespace().equals("schemas-upnp-org")
					&& device.getType().getType().equals("MediaRenderer"))
			{
				isDmrAdding = true;
				Utils.print("device-type-dmr", device.getDetails().getFriendlyName());
				final DeviceItem display = new DeviceItem(device, device
						.getDetails().getFriendlyName(), device
						.getDisplayString(), "(REMOTE) "
						+ device.getType().getDisplayString());
			}
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			DeviceItem display = new DeviceItem(device, device
					.getDisplayString());
//			Utils.print("name",device.getDisplayString());

			if (device.getType().getNamespace().equals("schemas-upnp-org")
					&& device.getType().getType().equals("MediaServer")) {
				deviceRemoved(display);
			}
		}

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			Utils.print(TAG, ">>>>>localDeviceAdded");
			final DeviceItem display = new DeviceItem(device, device
					.getDetails().getFriendlyName(), device.getDisplayString(),
					"(REMOTE) " + device.getType().getDisplayString());
			Utils.print("playername-->", display.getName());
			if (device.getType().getNamespace().equals("schemas-upnp-org")
					&& device.getType().getType().equals("MediaServer")) {
				deviceAdded(display);
//				Utils.print("111", device.g)
				localDeviceItem = display;
			}
			
		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			Utils.print(TAG, ">>>>>localDeviceRemoved");
			final DeviceItem display = new DeviceItem(device, device.getDisplayString());
			deviceRemoved(display);
			localDeviceItem = null;
		}

		public void deviceAdded(final DeviceItem di) {
			Utils.print("divices add", "devices add");
			runOnUiThread(new Runnable() {
				public void run() {
					int new_dev = 1;

					Utils.print(TAG, "[Same device?????] ==== "	+ di.getName() + "UDN= "+ di.getUdn().toString());
					
					if (deviceItemList.size() > 0) {
						for (int i = 0; i < deviceItemList.size(); i++) {
							// unique device name)
							if (deviceItemList.get(i).getDevice().getIdentity()
									.getUdn().equals(di.getDevice().getIdentity().getUdn())) {

								Utils.print(TAG, "[Same device?????] ==== "	+ di.getName() + "UDN= "+ di.getUdn().toString());

								// deviceItemList.remove(i);
								// device has been added to the list, do not need add it again
								new_dev = 0;
								break;
							}
						}
					}

					if (new_dev == 1) {
						Utils.print(TAG, "deviceAdded = " + di.toString());

//						deviceItemList.add(di);
						int location = getInsertLocation(di,deviceItemList);
						if(location==0){
							deviceItemList.add(di);
						}else {
							deviceItemList.add(location+1, di);
						}
						
						devicesAdapter.notifyDataSetChanged();
						stopProgressDialog();
					}
				}
			});
		}

		public void deviceRemoved(final DeviceItem di) {
			runOnUiThread(new Runnable() {
				public void run() {
					// deviceListAdapter.remove(di);
					deviceItemList.remove(di);
					devicesAdapter.notifyDataSetChanged();
				}
			});
			
		}

		
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utils.print("home destroy", "home destroy");
		searchStart = false;
		if(mPopupWindow!=null && mPopupWindow.isShowing())
			mPopupWindow.dismiss();
	    unregisterBroadcast();	
	}
	

	
	public  ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Utils.print(TAG, "onServiceConnected");

			upnpService = (AndroidUpnpService) service;
			
			
//			Utils.print(TAG, "upnpService====" + upnpService);
			Utils.print(TAG, "device====="+ upnpService.getRegistry().getDevices().size());
			for (Device device : upnpService.getRegistry().getDevices()) {
				if(!device.getDetails().getFriendlyName().equals(preferences.getString("dms_name", Utils.DMS_DEFAULT_NAME)))
				    deviceListRegistryListener.deviceAdded(new DeviceItem(device));
			}
			
			// Getting ready for future device advertisements
			upnpService.getRegistry().addListener(deviceListRegistryListener);

			// Refresh device list
			upnpService.getControlPoint().search();
			Utils.print("serarch", "search");

			
			BaseApplication application = (BaseApplication) HomeActivity.this.getApplication();
		    application.upnpService = upnpService;
 
 
			
			Utils.print("currentip------>", Utils.getCurrentAddress());
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Utils.print(TAG, "onServiceDisconnected");
			upnpService = null;
		}
	};
	
	
	
	private void RefreshDevcieList() {

	   deviceItemList.clear();

	   if(upnpService==null)
		   return;
	   
	   startProgressDialog(getResources().getString(R.string.search_devices),true);
	   
	   upnpService.getRegistry().removeAllRemoteDevices();
	   upnpService.getControlPoint().search();
       if(localDeviceItem!=null)
    	   deviceItemList.add(localDeviceItem);
       
       if(Display_Type == GRIDVIEW_DISPLAY)
       {
           devicesAdapter = new DevicesAdapter(this, deviceItemList,gridView);
    	   gridView.setAdapter(devicesAdapter);
       }
       else if(Display_Type == LISTVIEW_DISPLAY)
       {
           devicesAdapter = new DevicesAdapter(this, deviceItemList,
        		   dmsListView,DevicesAdapter.LIST_DEVICES_ADAPTER);
    	   dmsListView.setAdapter(devicesAdapter);
       }
		
		
	   new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(200);
				handler.sendEmptyMessage(MessageControl.DEVICES_REFLESH);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		 }
	    }).start();		
	}
	
	
	public void popDMR(View v)
	{
//		for(int i=0;i<dmrItemList.size();i++)
//		{
//			Utils.print("dmr name", dmrItemList.get(i).getName());
//		}
		
		if(mPopupWindow!=null && mPopupWindow.isShowing())
		{
			mPopupWindow.dismiss();
		}
		else {
			getPopupWindowInstance();
			mPopupWindow.showAsDropDown(v,0,0);
		}

	}
	
	/*
	 * 获取PopupWindow实例
	 */
	private void getPopupWindowInstance() {
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
			return;
		} else {
			initPopuptWindow();
		}
	}

 
	/*
	 * 创建PopupWindow
	 */
	private void initPopuptWindow() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View popupWindow = layoutInflater.inflate(R.layout.dmr_layout, null);

		dmrListView = (ListView)popupWindow.findViewById(R.id.listview);
		dmrListView.setDivider(getResources().getDrawable(R.drawable.set_horizontal_dot));
		dmrListView.setDividerHeight(2);

        
        
		mPopupWindow = new PopupWindow(popupWindow, getDispayWidth(), getDisplayHeight());
//		mPopupWindow.setOutsideTouchable(true);

	    popupWindow.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					if (mPopupWindow != null && mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					}
					return true;
				}
			});
        Utils.print("init popwindow", "init popwindow");
	}
	
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Utils.print("home back", "home back"+TYPE);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(mPopupWindow!=null && mPopupWindow.isShowing())
			{
				mPopupWindow.dismiss();
			}
			else {
				if (TYPE == CURRENT_FILE) {
					 
					Utils.print(TAG, "KEYCODE_BACK......folder==" + folder);
					// if( null != progressDialog&&progressDialog.isShowing()){
					// progressDialog.dismiss();
					// }

					// if(progressDialog!=null&&progressDialog.isShowing()){
//					 closeDialo=true;
					// progressDialog.isShowing();
					// }else{
					if (folder == 0 || folder == -1) {
//						goHome();
						handler.sendEmptyMessage(HOME);
//						if(Display_Type==1)
//						{
//							devicesAdapter = new DevicesAdapter(HomeActivity.this, deviceItemList,gridView);
//							gridView.setAdapter(devicesAdapter);
//						}
//						else if(Display_Type==2)
//						{
//							devicesAdapter = new DevicesAdapter(HomeActivity.this, deviceItemList, dmsListView,3);
//							dmsListView.setAdapter(devicesAdapter);
//						}
					} 
					else {
						folder--;
						
						// add by andy(Be careful)
						listPhoto.removeAll(listPhoto);
						listMusic.removeAll(listMusic);
						list = map.get(folder + "");
						if (list != null && list.size() != 0) {
							for (int i = 0; i < list.size(); i++) {
								if (!list.get(i).isContainer()) {

									if (list.get(i).getItem().getTitle().toString() != null) {

										if (list.get(i).getItem().getResources() != null) {
											List<Res> res = list.get(i).getItem()
													.getResources();
											if (res.size() != 0) {
												if (res.get(0).getProtocolInfo() != null) {
													if (res.get(0).getProtocolInfo()
															.getContentFormat() != null) {

														if (res.get(0).getProtocolInfo()
																.getContentFormat()
																.substring(0,res.get(0)
																				.getProtocolInfo()
																				.getContentFormat()
																				.indexOf(
																						"/"))
																.equals("image")) {

															listPhoto.add(list.get(i));

														} else if (res
																.get(0)
																.getProtocolInfo()
																.getContentFormat()
																.substring(0,res.get(0)
																				.getProtocolInfo()
																				.getContentFormat()
																				.indexOf(
																						"/"))
																.equals("audio")) {

															listMusic.add(list.get(i));
														} else {

														}

													}
												}
											}
										}

									}

								}

							}
						}
						BaseApplication myApplication = (BaseApplication) getApplication();
						myApplication.listMusic = listMusic;
						myApplication.listPhoto = listPhoto;
						//
						if(Display_Type==GRIDVIEW_DISPLAY){
							ContainerListAdapter myContentListAdapter = new ContainerListAdapter(
									HomeActivity.this, map.get(folder + ""), gridView,1);
							gridView.setAdapter(myContentListAdapter);
						}else if(Display_Type==LISTVIEW_DISPLAY){
							ContainerListAdapter myContentListAdapter = new ContainerListAdapter(
									HomeActivity.this, map.get(folder + ""), dmsListView,2);
							dmsListView.setAdapter(myContentListAdapter);
						}
					}
				    }else {
					   goHome();
				    }
				}

			return true;
		}
		
	return super.onKeyDown(keyCode, event);
   }


	private void goHome() {
		if(!Utils.isNetworkConnected(this) || Utils.getAPNType(this).equals("G"))
		{
			finish();
		}
		else {
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addCategory(Intent.CATEGORY_HOME);
			startActivity(i);
		}
	}
 
	
	 public int getDisplayHeight()
	 {
		 LinearLayout topLayout = (LinearLayout)findViewById(R.id.title_bar);
	     int topheight = topLayout.getLayoutParams().height;
	     
	     DisplayMetrics metrics = new DisplayMetrics();
		 getWindowManager().getDefaultDisplay().getMetrics(metrics);
		 int screenHeight = metrics.heightPixels;
		 
		 Utils.print(TAG, (screenHeight-topheight-10)+"");
		 return screenHeight-topheight-10;
	 }
	 
	 public int getDispayWidth(){
		 DisplayMetrics metrics = new DisplayMetrics();
		 getWindowManager().getDefaultDisplay().getMetrics(metrics);
		 return metrics.widthPixels;
	 }
	 
	private void startProgressDialog(String mesage, boolean Cancelable) {
		
		progressDialog = CustomProgressDialog.createDialog(this);
		progressDialog.setCancelable(Cancelable);
		progressDialog.setMessage(mesage);
		

		progressDialog.show();
	}

	private void stopProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	
	/**
	 * Check if it's connected to the network return true if connected, false
	 * otherwise.
	 **/
	private void networkSelectDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_network_tit);
		builder.setMessage(R.string.select_network_msg);

		builder.setNegativeButton(R.string.ethnet_string,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Utils.print(TAG, "choose  Ethernet");
						NetworkConnectState.SetConnectTye(NetworkConnectState.NETWORK_USE_ETH);
						startLoadData();
					}
				});

		builder.setPositiveButton(R.string.softap_string,
				new DialogInterface.OnClickListener() {
					// @Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.print(TAG, "choose sotfAP");
						NetworkConnectState.SetConnectTye(NetworkConnectState.NETWORK_USE_SOFTAP);
						startLoadData();
					}
				});

		builder.create().show();
	}
	
	
	public void registerBroadcast()
	{
		//network state
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkChange,intentFilter);
	}
	
	public void unregisterBroadcast()
	{
		unregisterReceiver(networkChange);
	}
	
	private BroadcastReceiver networkChange = new BroadcastReceiver() {
		
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Utils.print("network action", intent.getAction());
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
			{
            	Utils.print("network change", "network change");
				if(Utils.isNetworkConnected(context) && !Utils.getAPNType(context).equals("G"))//网络开启切换
				{
					if(networkIsSelect){
						Utils.print("network con", "network con");
						mNetworkConnectState.UpdateConnectState();  //更新网络状态

					    deviceItemList.clear();
					    dmrItemList.clear();
					    startLoadData();
					}
				}
				else if(!Utils.isNetworkConnected(context))//网络断开
				{
	            	Utils.print("network dis", "network dis");
	            	stopProgressDialog();
	            	try {

					    if(searchStart){
					    	if(serviceConnection!=null){
								getApplicationContext().unbindService(serviceConnection);
							}

							if (upnpService != null) {
								upnpService.getRegistry()
										.removeListener(deviceListRegistryListener);
							}
					    }
						
						if(mPopupWindow!=null && mPopupWindow.isShowing())
							mPopupWindow.dismiss();
						
					  
						//exit();
						cancelMusicNotification();
						stopService(new Intent(HomeActivity.this, PlayerService.class));
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}
		}
	};
	
	
	
	private void setSearchTimeout(){

		handler.sendMessageDelayed(handler.obtainMessage(SEARCH_CANCEL), 30000);
				
	}
	

	private void displayNetworkDialog()
	{
		new AlertDialog.Builder(this)
		    .setTitle(getResources().getString(R.string.network_load_failed))
		    .setMessage(getResources().getString(R.string.please_set_network))
		    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
		    .show();
		
	}
	
	
	private  int getInsertLocation(DeviceItem deviceItem,ArrayList<DeviceItem> deviceItems)
	{
		int location = 0;
		try {
//			Utils.print("devices-->size", deviceItems.size()+"");
			for(int i=0;i<deviceItems.size();i++)
			{
				String oldString = getIconString(deviceItems.get(i));
				String newString = getIconString(deviceItem);
//				Utils.print("oldstring---newstring", ">>>>>>"+oldString+"---"+newString);
				///upnphost/udhisapi.dll? 是针对vim7而做的处理
				if(oldString.equals(newString)){
					location = i;
//					Utils.print("aaaaa", deviceItem.getDevice().getDetails().getFriendlyName()+"---"+i+"---"+newString);
					break;
				}
				else if(oldString.startsWith("/upnphost/udhisapi.dll?") 
						&& newString.startsWith("/upnphost/udhisapi.dll?")){
					location = i;
//					Utils.print("bbbbbb", "bbbbbb");
					break;
					
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return location;
	}
	
	
	private String getIconString(DeviceItem deviceItem)
	{
		try {
			
			if(deviceItem.getLabel()!=null && deviceItem.getLabel()[0].equals(Utils.LOCAL_RENDER_NAME))  //针对local Render 自定义项
			{
//				Utils.print("---->", "---->");
				return "mirage_icon.png";
			}
			
			
//			if(deviceItem.getDevice().getDetails().getFriendlyName().equals(preferences.getString("dms_name", Utils.DMS_DEFAULT_NAME)))
//			{
//				Utils.print("+++++>", "+++++>");
//			}
			
			Device device = deviceItem.getDevice();
			if(null==device)
				return "";
			
			Icon[] devIcons = device.getIcons();
			if(null==devIcons ||   0 == devIcons.length ){				
				return "";
			}
			
			int len = devIcons.length;
			// get max size icon
			int oldwidth = 0, width = 0, j = 0;
			for (int a = 0; a < len; a++) {
				width = devIcons[a].getWidth();
				if (width > oldwidth) {
					j = a;
					oldwidth = width;
				}
			}
			
			return devIcons[j].getUri().toString();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "";
		}

	}
	
	
	private void loadFailed(){
		if(TYPE == CURRENT_DEVICES){
			if(loadTimes<3 && currentDeviceItem!=null)
			{
				loadTimes++;				
				startLoadContainer(currentDeviceItem);
			}
			if(loadTimes>=3){
				if(progressDialog!=null){
					if(progressDialog.isShowing()){
						progressDialog.dismiss();
						Toast.makeText(HomeActivity.this, HomeActivity.this.getText(R.string.loadfileFailure), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		else if(TYPE == CURRENT_FILE){
			if(loadTimes<3 && currentContentItem!=null)
			{
				loadTimes++;				
				upnpService.getControlPoint().execute(
						new ContentBrowseActionCallback(HomeActivity.this,
								currentContentItem.getService(), currentContentItem.getContainer(),handler));
			}
			if(loadTimes>=3){
				if(progressDialog!=null){
					if(progressDialog.isShowing()){
						progressDialog.dismiss();
						Toast.makeText(HomeActivity.this, HomeActivity.this.getText(R.string.loadfileFailure), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}

	}
	
}
