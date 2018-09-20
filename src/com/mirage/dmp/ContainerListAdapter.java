package com.mirage.dmp;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;



import org.teleal.cling.support.model.Res;

import com.mirage.dlna.R;
import com.mirage.dmp.IconAsyncImageLoader.ImageCallback;

import com.mirage.util.Utils;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class ContainerListAdapter extends BaseAdapter {
	
	private final static String TAG="ContentListAdapter";
	
	private ArrayList<ContentItem> list;
	private LayoutInflater mInflater;
	private GridView gridView;
	private ListView listView;
	private IconAsyncImageLoader iconAsyncImageLoader;
    private int TYPE = 1;  //1 gridview,2 listview
	private Context context;

//	public ContainerListAdapter(Context context, ArrayList<ContentItem> list,Handler mHandler) {
//		super();
//		this.list = list;
//		this.context=context;
//		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	}

	public ContainerListAdapter(Context context, ArrayList<ContentItem> list,GridView gridView,int type) {
		super();
		this.list = list;
		TYPE = type;
		this.context=context;
		this.gridView = gridView;
		iconAsyncImageLoader = new IconAsyncImageLoader();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ContainerListAdapter(Context context, ArrayList<ContentItem> list,ListView listView,int type) {
		super();
		this.list = list;
		TYPE = type;
		this.context=context;
		this.listView = listView;
		iconAsyncImageLoader = new IconAsyncImageLoader();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	// @Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	// @Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	// @Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// @Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// TODO Auto-generated method stub
//		Utils.print(TAG , "list"+TYPE);
		
		Holder holder;
		if (convertView == null) {
			if(TYPE==1){
				convertView = mInflater.inflate(R.layout.gridview_item, null);
			}else if(TYPE==2){
				convertView = mInflater.inflate(R.layout.list_item, null);
			}
			
			holder = new Holder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.devices_icon);
			holder.filename = (TextView) convertView.findViewById(R.id.devices_name);
			holder.filename.setTextColor(Color.WHITE);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		try {
			ContentItem content = list.get(position);
			// Utils.print(TAG , "content===="+content);

			if (content != null) {
				
				//is a container
				if (content.isContainer()) {
						holder.filename.setText(content.toString()+
								"("+content.getContainer().getChildCount()+")");
					holder.imageView.setImageResource(R.drawable.icon_media_folder);
				}
				else
				{
				//is a file			
					if (content.getItem().getFirstResource() != null) {
						if (content.getItem().getFirstResource().getValue() != null) {
							//Utils.print(TAG, "getFirstResource"+content.getItem().getFirstResource().getValue());
						}
					}

					if (content.getItem().getTitle().toString() != null) {					
						holder.filename.setText(content.getItem().getTitle().toString());					
					} else {
						holder.filename.setText(context.getText(R.string.nofilename));
					}
					
					
					
					

					List <Res> res = content.getItem().getResources();				
					if (res.get(0).getProtocolInfo().getContentFormat().substring(0, 
							res.get(0).getProtocolInfo().getContentFormat().indexOf("/")).equals("image")) {
						
						holder.imageView.setImageResource(R.drawable.icon_image_file);
						
					}
					else if (res.get(0).getProtocolInfo().getContentFormat().substring(0, 
							res.get(0).getProtocolInfo().getContentFormat().indexOf("/")).equals("audio")) {
						holder.imageView.setImageResource(R.drawable.icon_audio_file);					
					}
					else if (res.get(0).getProtocolInfo().getContentFormat().substring(0, 
							res.get(0).getProtocolInfo().getContentFormat().indexOf("/")).equals("video")) {
						
						if (content.getItem().getFirstResource() == null) {
							holder.imageView.setImageResource(R.drawable.unknowvideo);
						} else {
							holder.imageView.setImageResource(R.drawable.icon_video_file);
						}
					}
					else{
						holder.imageView.setImageResource(R.drawable.unkonwfile);
					}
					

					
					String albumArtURI="";

//					Utils.print("properies size", content.getItem().getProperties().size()+"");
					for(int i=0;i<content.getItem().getProperties().size();i++)
					{
//						Utils.print("albumArtURI-->uri", content.getItem().getProperties().get(i).getDescriptorName());
						if(content.getItem().getProperties().get(i).getDescriptorName().equals("albumArtURI"))
						{
							albumArtURI = content.getItem().getProperties().get(i).getValue().toString();
							if(!albumArtURI.equals(""))
								break;
						}
					}
					
					if(albumArtURI.equals(""))
					{
						if(res.get(0).getProtocolInfo().getContentFormat().substring(0, 
								res.get(0).getProtocolInfo().getContentFormat().indexOf("/")).equals("image"))
						{
							albumArtURI = getRealImagePath(content);
						}
					}
//					Utils.print("albumArtURI", albumArtURI+"###");
					if(!albumArtURI.equals(""))
					{
						holder.imageView.setTag(albumArtURI);
						final Bitmap bitmap = iconAsyncImageLoader.loadBitmap(albumArtURI, 
								new ImageCallback() {
							
							public void imageLoaded(Bitmap imageBitmap, String path) {
								// TODO Auto-generated method stub
								ImageView imageView = null;
								if(TYPE==1){
									imageView = (ImageView)gridView.findViewWithTag(path);
								}else if(TYPE==2){
									imageView = (ImageView)listView.findViewWithTag(path);
								}
								 
								if(imageView!=null && imageBitmap!=null)
								{
//									Utils.print("set", "set");
									imageView.setImageBitmap(imageBitmap);
								}
							}
						});
						
						if(bitmap!=null)
						{
							Utils.print("bitmap not null", "bitmap not null");
							holder.imageView.setImageBitmap(bitmap);
						}
					}
				}
			}
			    // Utils.print(TAG , "contentItem.getItem()"+content.getItem());
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
	
	
	public String getRealImagePath(ContentItem content)
	{
		//分辨率是null的话，图标一定是最小的。。所以那那个地址就可以了
		//如果没是null的话，则获取分辨率最小的做图标缩列图
		
		int size = content.getItem().getResources().size();
//		Utils.print("res size", size+"");
		int[] resolutionValues = new int[size];
		String resultpath = "";
		for(int i=0;i<content.getItem().getResources().size();i++)
		{
			if(content.getItem().getResources().get(i).getValue()!=null)
			{
				String resolution = content.getItem().getResources().get(i).getResolution();
				if(resolution==null)
				{
					resultpath = content.getItem().getResources().get(i).getValue();
					break;
				}
				
				String[] tempVaules = resolution.split("x");
				resolutionValues[i] = Integer.parseInt(tempVaules[0])*Integer.parseInt(tempVaules[1]);
				
				Utils.print("resolutionValues", resolutionValues[i]+"");
//				Utils.print("image path", content.getItem().getResources().get(i).getValue());
			} 
		}
		
		if(!resultpath.equals(""))
			return resultpath;
		else {
			return content.getItem().getResources().get(getMaxID(resolutionValues)).getValue();
		}
		
	}

	
	public int getMaxID(int[] values)
	{
		int max = values[0];
		int min = values[0];
		int maxPosition = 0;
		int minPosition = 0;
		for (int i = 1; i<values.length;i++ )
		{
		  if (values[i]>max) { max = values[i]; maxPosition = i;}
		  if (values[i]<min) { min = values[i]; minPosition = i;}

		}
        Utils.print("max", ""+maxPosition);
        Utils.print("min", ""+minPosition);
        
        return minPosition;
	}
}
