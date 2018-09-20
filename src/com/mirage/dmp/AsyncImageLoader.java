package com.mirage.dmp;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDeviceIdentity;
import com.mirage.util.Utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
	private static final String TAG="AsyncImageLoader";
	private BlockingQueue queue ;  
	private HashMap<String, SoftReference<Bitmap>> imageCache;
	private ThreadPoolExecutor executor ;
	  

	
	     public AsyncImageLoader() {
	    	 imageCache = new HashMap<String, SoftReference<Bitmap>>();
	    	 queue = new LinkedBlockingQueue();
	    	 executor = new ThreadPoolExecutor(1, 50, 180, TimeUnit.SECONDS, queue);
	     }
	  
	     public Bitmap loadBitmap(final DeviceItem deviceItem, final ImageCallback imageCallback) {
	         if (imageCache.containsKey(deviceItem.getName())) {
	             SoftReference<Bitmap> softReference = imageCache.get(deviceItem.getName());
	             Bitmap bitmap = softReference.get();
	             if (bitmap != null) {
	                 return bitmap;
	             }
	         }
	         final Handler handler = new Handler() {
	             public void handleMessage(Message message) {
	                 imageCallback.imageLoaded((Bitmap) message.obj, deviceItem);
	             }
	         };
	         
	        
	         executor.execute(new Runnable() {
	             public void run() {
	            	 Bitmap bitmap = loadImageFromUrl(deviceItem);
	                 imageCache.put(deviceItem.getName(), new SoftReference<Bitmap>(bitmap));
	                 Message message = handler.obtainMessage(0, bitmap);
	                 handler.sendMessage(message);
	             }
	         });
	         
	         return null;
	     }
	  
	  
		public Bitmap loadImageFromUrl(DeviceItem deviceItem) {
 
			Bitmap icon = null ;
			
			try {
				Device tempdevice = deviceItem.getDevice();
				if(tempdevice==null)
				{
					return null;
				}
				RemoteDeviceIdentity remDev = (RemoteDeviceIdentity) tempdevice.getIdentity();					
				

				
				URL descriptorURL = remDev.getDescriptorURL();
//				Utils.print(TAG, "descriptorURL======= "+descriptorURL.toString() );
				
				Icon[] devIcons = tempdevice.getIcons();
				if(null==devIcons ||   0 == devIcons.length ){
//					Utils.print(TAG, "NO ICON ======"+tempdevice.getDetails().getFriendlyName());					
					return null;
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
				
				String icon_url = descriptorURL.getProtocol() + "://"
						+ descriptorURL.getAuthority()
						+ devIcons[j].getUri().toString();
//				if(devIcons[j].getUri().toString().contains("/"))
//					icon_url = icon_url + devIcons[j].getUri().toString();
//				else {
//					icon_url = icon_url + "/" + devIcons[j].getUri().toString(); 
//				}

//                icon = getIconDrawable(devIcons[j].getData());
//				if(devIcons[j].getData()!=null){
//					icon = getIconDrawable(devIcons[j].getData());
//				}else {
					icon = fetchBitmap(icon_url);
//				}
//				Utils.print("3333", devIcons[j].getUri().toString());
				

			} catch (Exception e) {
				// TODO: handle exception
//				e.printStackTrace();
			}
			
	        return icon ;
		}
	  
	     public interface ImageCallback {
	         public void imageLoaded(Bitmap imageBitmap, DeviceItem deviceItem);
	     }
	     
	public Bitmap getIconBitmap(byte[] data)
	{
		if (data != null) {
//			Utils.print("111", "111");
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			//Drawable drawable = new BitmapDrawable(bitmap);

			return bitmap;
		}
//		Utils.print("222", "222");
		return null;
	}
	     
	     
	public Bitmap fetchBitmap(String urlString) {
//		Utils.print(TAG, "image url:" + urlString);

		try {
			
			URL m;
	        InputStream i = null;
	        BufferedInputStream bis = null;
	        ByteArrayOutputStream out =null;
	        try {
	            m = new URL(urlString);
	            i = (InputStream) m.getContent();
	            return getBitmapFromStream(i, 48, 48);
	        } catch (IOException e) {
//	            e.printStackTrace();
	        }  
	        return null;
		} catch (Exception e) {
//			Utils.printe(TAG, "fetchDrawable ICON failed", e);
			return null;
		}
	}
	
	
	public Bitmap getBitmapFromStream(InputStream ins, int width, int height) {
	    if (null != ins) {
	        BitmapFactory.Options opts = null;
	        if (width > 0 && height > 0) {
	            opts = new BitmapFactory.Options();
	            opts.inJustDecodeBounds = true;
	            //BitmapFactory.decodeStream(ins,null,opts);
	            // 计算图片缩放比例
	            final int minSideLength = Math.min(width, height);
	            opts.inSampleSize = Utils.computeSampleSize(opts, minSideLength,
	                    width * height);
	            opts.inJustDecodeBounds = false;
	            opts.inInputShareable = true;
	            opts.inPurgeable = true;
	        }
	        try {
	            return BitmapFactory.decodeStream(ins,null,opts);
	        } catch (OutOfMemoryError e) {
	            e.printStackTrace();
	        }
	    }
	    return null;
	}

}
