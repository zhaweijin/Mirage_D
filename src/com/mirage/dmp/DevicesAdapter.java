package com.mirage.dmp;


import java.util.ArrayList;

import com.mirage.dlna.HomeActivity;
import com.mirage.dlna.R;
import com.mirage.dlna.R.id;
import com.mirage.dlna.application.BaseApplication;
import com.mirage.dmp.AsyncImageLoader.ImageCallback;
import com.mirage.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DevicesAdapter extends BaseAdapter {

	private final static String TAG = "DevicesAdapter";
	private ArrayList<DeviceItem> deviceItems;
	private LayoutInflater mInflater;
	private AsyncImageLoader asyncImageLoader;
	private GridView gridView;
    private ListView listView;
    private int TYPE = 1;  //1 gridview devices,2 listview mediaplay,3 listview devices
    public final static  int GRID_DEVICES_ADAPTER = 1;
    public final static int LIST_MEDIA_ADAPTER = 2;
    public final static int LIST_DEVICES_ADAPTER = 3;
    private Context context;
    public int dmrPosition = 0;
    
    private DeviceItem selectDeviceItem = null;
	public DevicesAdapter(Context context, ArrayList<DeviceItem> list,GridView gridView) {
		super();
        TYPE = GRID_DEVICES_ADAPTER;
		this.deviceItems = list;
		this.context = context;
		this.gridView = gridView;
		asyncImageLoader = new AsyncImageLoader();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public DevicesAdapter(Context context, ArrayList<DeviceItem> list,ListView listView,int type) {
		super();

		TYPE = type;
		this.deviceItems = list;
		this.context = context;
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}
	
	@Override
	public int getCount() {
		return deviceItems.size();
	}

	@Override
	public Object getItem(int position) {
		return deviceItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder; 
		if (convertView == null) {
			if(TYPE==GRID_DEVICES_ADAPTER)
			  convertView = mInflater.inflate(R.layout.gridview_item, null);
			else if(TYPE==LIST_MEDIA_ADAPTER) 
			  convertView = mInflater.inflate(R.layout.dmr_play_listview_item, null);
			else if(TYPE==LIST_DEVICES_ADAPTER)
			  convertView = mInflater.inflate(R.layout.list_item, null);
			holder = new Holder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.devices_icon);
			holder.filename = (TextView) convertView.findViewById(R.id.devices_name);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.filename.setSelected(true);
		
		try {
			DeviceItem deviceItem = deviceItems.get(position);
			// Utils.print(TAG ,
			// "getView  device==="+deviceItem.getDevice().getDetails().getFriendlyName());

			
			if (deviceItem != null && 
					deviceItem.getLabel()!=null && 
					!deviceItem.getLabel()[0].equals(Utils.LOCAL_RENDER_NAME)) {
				
				if (deviceItem.icon == null) {
					if(TYPE==GRID_DEVICES_ADAPTER || TYPE==LIST_DEVICES_ADAPTER)
					{
						holder.imageView.setImageResource(R.drawable.devices);
					}
					else {
						holder.imageView.setImageResource(R.drawable.device_play_icon);
					}
					
				} else {
					BitmapDrawable bd = (BitmapDrawable) deviceItem.icon;
					Bitmap bm = bd.getBitmap();
					holder.imageView.setImageBitmap(bm);
				}


				if(deviceItem.getDevice()!=null)
				{
					String name = deviceItem.getDevice().getDetails().getFriendlyName();
					if(Utils.getEncoding(name).equals("ISO-8859-1")){
						name = new String(name.getBytes(Utils.getEncoding(name)), "utf-8"); 
					}
					holder.filename.setText(name);
				}
				
				
				


				holder.imageView.setTag(deviceItem.getName());
				final Bitmap bitmap = asyncImageLoader.loadBitmap(deviceItem, new ImageCallback() {
					

					public void imageLoaded(Bitmap imageBitmap, DeviceItem deviceItem) {
						// TODO Auto-generated method stub
						ImageView imageView=null;
						if(TYPE == GRID_DEVICES_ADAPTER)
						{
							imageView = (ImageView)gridView.findViewWithTag(deviceItem.getName());
						}
						else if(TYPE == LIST_MEDIA_ADAPTER || TYPE ==LIST_DEVICES_ADAPTER)
						{
							imageView = (ImageView)listView.findViewWithTag(deviceItem.getName());
						}
					
						if(imageView!=null && imageBitmap!=null)
						{
							imageView.setImageBitmap(imageBitmap);
						}
						  
					}
				});
				if(bitmap!=null)
				{
//					Utils.print("bitmap not null", "bitmap not null");
					holder.imageView.setImageBitmap(bitmap);
				}
			}
			else {
//				Utils.print("other", "other"+position);
				   holder.imageView.setTag(Utils.LOCAL_RENDER_NAME);
				   
				   if(TYPE == GRID_DEVICES_ADAPTER || TYPE == LIST_DEVICES_ADAPTER)
					{
						holder.imageView.setImageResource(R.drawable.devices);
					}
					else {
						holder.imageView.setImageResource(R.drawable.device_play_icon);
					}
					
//					Utils.print("local", "local"+position);
					holder.filename.setText(Utils.LOCAL_RENDER_NAME);
			}
			
			
			final int id = position;
			if(TYPE==LIST_MEDIA_ADAPTER)
			{
				convertView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dmrPosition = id;
						if(id!=0){
							Utils.print("select id-->", dmrPosition+"");
							BaseApplication application = (BaseApplication)((HomeActivity)context).getApplication();
							application.dmrDeviceItem = deviceItems.get(id);
							selectDeviceItem = deviceItems.get(id);
						}
						else {
							selectDeviceItem = null;
						}
						((HomeActivity)context).mPopupWindow.dismiss();
						notifyDataSetChanged();
					}
				});
				
				
				//设置选中设备的背景颜色
				if(selectDeviceItem!=null){
					if(selectDeviceItem.equals(deviceItems.get(id))){
						convertView.setBackgroundResource(R.drawable.dmr_list_background);
					}
					else {
						convertView.setBackgroundResource(R.drawable.dmr_listview_background);
					}
				}
				else {
					if(id==0){
						convertView.setBackgroundResource(R.drawable.dmr_list_background);
					}
					else{
						convertView.setBackgroundResource(R.drawable.dmr_listview_background);
					}
				}
				
				
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		

		
		return convertView;
	}

	class Holder {
		ImageView imageView;
		TextView filename;
	}
	

	
	public void setSelectDevices()
	{
		Utils.print("set", "set"+deviceItems.size());
		
		BaseApplication application = (BaseApplication)((HomeActivity)context).getApplication();
		application.dmrDeviceItem = null;
		selectDeviceItem = null;
		dmrPosition = 0;
		
		
		
	}
}
