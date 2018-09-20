package com.update.download;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UTFDataFormatException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.mirage.dlna.R;
import com.mirage.util.Utils;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class DownloadUtils {


	
	public static int NOTI_ID = 10000;
	
	
	public void print(String tag,String value)
	{
//		Log.v(tag, value);
	}
	
	
	public  boolean compareVersions(String newVersion, String oldVersion) {
		if (newVersion.equals(oldVersion))
			return false;

		// Replace all - by . So a CyanogenMod-4.5.4-r2 will be a
		// CyanogenMod.4.5.4.r2
		newVersion = newVersion.replaceAll("-", "\\.");
		oldVersion = oldVersion.replaceAll("-", "\\.");

		String[] sNewVersion = newVersion.split("\\.");
		String[] sOldVersion = oldVersion.split("\\.");

		ArrayList<String> newVersionArray = new ArrayList<String>();
		ArrayList<String> oldVersionArray = new ArrayList<String>();

		newVersionArray.addAll(Arrays.asList(sNewVersion));
		oldVersionArray.addAll(Arrays.asList(sOldVersion));

		// Make the 2 Arrays the Same size filling it with 0. So Version 2
		// compared to 2.1 will be 2.0 to 2.1
		if (newVersionArray.size() > oldVersionArray.size()) {
			int difference = newVersionArray.size() - oldVersionArray.size();
			for (int i = 0; i < difference; i++) {
				oldVersionArray.add("0");
			}
		} else {
			int difference = oldVersionArray.size() - newVersionArray.size();
			for (int i = 0; i < difference; i++) {
				newVersionArray.add("0");
			}
		}

		int i = 0;
		for (String s : newVersionArray) {
			String old = oldVersionArray.get(i);
			// First try an Int Compare, if its a string, make a string compare
			try {
				int newVer = Integer.parseInt(s);
				int oldVer = Integer.parseInt(old);
				if (newVer > oldVer)
					return true;
				else if (newVer < oldVer)
					return false;
				else
					i++;
			} catch (Exception ex) {
				// If we reach here, we have to string compare cause the version
				// contains strings
				int temp = s.compareToIgnoreCase(old);
				if (temp < 0)
					return false;
				else if (temp > 0)
					return true;
				else
					// its the same value so continue
					i++;
			}
		}
		// Its Bigger so return true
		return true;
	}
	
	
	
	public  String toUtf8String(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = Character.toString(c).getBytes("utf-8");
				} catch (Exception ex) {
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}
	
	
	
	/**
	 * @author zhaweijin
	 * @fucntion 获取打开文件文件的类型
	 */
	public  String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("apk")) {
			type = "application/vnd.android.package-archive";
		} else {
			type = "*";
		}
		return type;
	}
	
	
	public  void installFile(File file, Activity activity) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(file);
		print("filepath", file.getAbsolutePath());
		intent.setDataAndType(Uri.fromFile(file), type);
		activity.startActivity(intent);
	}
	
	
	public  String getFileDirPath(Activity activity)
	{
		String packagenameString = activity.getPackageName();
		return "data/data/"+packagenameString + "/download/";
	}
	
	
	public  void execMethod(String path) {

		String args[] = new String[3];
		args[0] = "chmod";
		args[1] = "777";
		args[2] = path;
		try {
			Runtime.getRuntime().exec(args);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
	public  boolean checkNetworkIsActive(Context context) {
		boolean mIsNetworkUp = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			mIsNetworkUp = info.isAvailable();
		}
		return mIsNetworkUp;
	}
	
	/**
	 * @author zhaweijin
	 * @fucntion 解析网络数据
	 */
	public  DataSet getSoftwareWebData(String webpage) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(webpage);
			
			//setparams
			httpost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 8000);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 8000);

			String[] key = {"id"};						
		    String[] value = {"update"};
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (int i = 0; i < key.length; i++) {
				nvps.add(new BasicNameValuePair(key[i], value[i]));
			}

			httpost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
			HttpResponse response = httpclient.execute(httpost);
			InputStream inputStream = response.getEntity().getContent();	
				
			
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			DataHandle gwh = new DataHandle();
			InputStreamReader isr = new InputStreamReader(inputStream, "utf-8");
			InputSource is = new InputSource(isr);
			xr.setContentHandler(gwh);
			xr.parse(is);
			return gwh.getDataSet();

		} catch (Exception e) {
			e.printStackTrace();
			Utils.print("aa", "aa");
			return null;
		}
	}
	
	
	public  boolean checkIsUpdate(final Activity activity,DataSet dataSet)
	{
		boolean result = false;
		if(dataSet!=null){
			print("size", dataSet.getWebDatas().size()+"");
			int size = dataSet.getWebDatas().size();
			
			int i;
			for(i=0;i<size;i++){
				print("name", dataSet.getWebDatas().get(i).getName());
	            if(replaceBlank(dataSet.getWebDatas().get(i).getName(),"").equals("MirageDLNA"))
	            {
	            	String oldversion = getCurrenVersion(activity);
	            	String newversion = replaceBlank(dataSet.getWebDatas().get(i).getVersion(),"");
	            	
	            	if(compareVersions(newversion, getCurrenVersion(activity)))
	    			{
	            		result = true;
	            		final String softwareName = replaceBlank(dataSet.getWebDatas().get(i).getName(),"");
	            		final String softwarePath = replaceBlank(dataSet.getWebDatas().get(i).getWebpath(),"");
	            		final String description = replaceBlank(dataSet.getWebDatas().get(i).getDescription(),"");
	            		new AlertDialog.Builder(activity)
	            		.setTitle(activity.getResources().getString(R.string.has_update))
	            		.setMessage(description)
	            		.setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
			            		new Thread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										try {
											new FileDownloader(activity, softwarePath,softwareName, 
						    						replaceBlank(getFileDirPath(activity),""));
										} catch (Exception e) {
											// TODO: handle exception
											e.printStackTrace();
										}
									}
								}).start();
							}
						})
						.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						})
	            		.show();
	    			}
	            	break;
	            }
			}
			
		}
		return result;
		
	}
	
	public  String replaceBlank(String oldString,String replace) {
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");

		Matcher m = p.matcher(oldString);

		return m.replaceAll(replace);
	}
	
	public  String getCurrenVersion(Activity activity)
	{
		try {
			PackageManager manager = activity.getPackageManager();
			PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
			String oldversion = info.versionName;
			print("oldversion", oldversion);
			return oldversion;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}
	
	
	
	public  void notificationCancel(Context activity,int swid) {
		NotificationManager notificationManager = (NotificationManager) activity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(swid);
	}

}
