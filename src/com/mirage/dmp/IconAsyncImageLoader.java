package com.mirage.dmp;


import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDeviceIdentity;
import org.teleal.cling.support.model.item.Item;

import com.mirage.dlna.application.ContentConfigData;
import com.mirage.dlna.application.MessageControl;

import com.mirage.util.Utils;

import android.R.integer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class IconAsyncImageLoader {
	private static final String TAG="IconAsyncImageLoader";
	private BlockingQueue queue ;  
	private HashMap<String, SoftReference<Bitmap>> imageCache;
	private ThreadPoolExecutor executor ;	  

	
	     public IconAsyncImageLoader() {
	    	 imageCache = new HashMap<String, SoftReference<Bitmap>>();
	    	 queue = new LinkedBlockingQueue();
	    	 executor = new ThreadPoolExecutor(2, 50, 180, TimeUnit.SECONDS, queue);
	     }
	  
	     public Bitmap loadBitmap(final String path, final ImageCallback imageCallback) {
	         if (imageCache.containsKey(path)) {
	             SoftReference<Bitmap> softReference = imageCache.get(path);
	             Bitmap bitmap = softReference.get();
	             if (bitmap != null) {
	                 return bitmap;
	             }
	         }
	         final Handler handler = new Handler() {
	             public void handleMessage(Message message) {
	                 imageCallback.imageLoaded((Bitmap) message.obj, path);
	             }
	         };
	         
	        
	         executor.execute(new Runnable() {
	             public void run() {
	            	 Bitmap bitmap = fetchBitmap(path);
	                 imageCache.put(path, new SoftReference<Bitmap>(bitmap));
	                 
	                 Message message = handler.obtainMessage(0, bitmap);
	                 handler.sendMessage(message);
	             }
	         });
	         
	         return null;
	     }
	  
	  
	     public interface ImageCallback {
	         public void imageLoaded(Bitmap imageBitmap, String path);
	     }
	     
	     
	public Bitmap fetchBitmap(String urlString) {
//		Utils.print(TAG, "icon image url:" + urlString);
		try {
			InputStream is = fetch(urlString);

//			BitmapFactory.Options opts = new BitmapFactory.Options();
//		    //opts.inSampleSize = 8;
//		    Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
		    
			

			return getBitmapFromStream(is,64,64);
		} catch (MalformedURLException e) {
			Utils.print(TAG, "fetchDrawable ICON failed");
			return null;
		} catch (IOException e) {
			Utils.print(TAG, "fetchDrawable ICON failed");
			return null;
		}
	}
	 	
		private InputStream fetch(String urlString) throws MalformedURLException,IOException {
			try {
//				HttpGet httpRequest = new HttpGet(urlString);
//				HttpClient httpclient = new DefaultHttpClient();
//				HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
//				HttpEntity entity = response.getEntity();
//				BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
//				return bufferedHttpEntity.getContent();
				
//				DefaultHttpClient httpClient = new DefaultHttpClient();
//				HttpGet request = new HttpGet(urlString);
//				HttpResponse response = httpClient.execute(request);
//				return response.getEntity().getContent();
				URL url=new URL(urlString);
				HttpURLConnection connection=(HttpURLConnection) url.openConnection();
				return connection.getInputStream();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
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
//		            opts.inSampleSize = Utils.computeSampleSize(opts, minSideLength,
//		                    width * height);
		            opts.inSampleSize = 5;
		            Utils.print("opts.inSampleSize", opts.inSampleSize+"");
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
