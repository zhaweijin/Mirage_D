package com.mirage.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.teleal.cling.transport.spi.InitializationException;
import com.mirage.dlna.ConnectInfo;
import com.mirage.dlna.NetworkConnectState;
import com.mirage.dlna.music.MusicPlayer;
import com.mirage.dmp.ContentItem;
import com.mirage.dmp.ImageDisplay;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class Utils {

	
	public static final String TAG = "Utils";
	public static final int OPEN_TEXT = 0;

	public static final int OPEN_MUSIC = 1;

	public static final int OPEN_VIDEO = 2;

	public static final int OPEN_IMAGE = 3;
	
	//dmr 
    public final static String DMR_DEFAULT_NAME = "Mirage MediaPlayer";
    public final static String MEDIA_DETAIL = "Xuzhitech Media Render";
	//dms
    public final static String DMS_DEFAULT_NAME = "Mirage MediaServer";
    public final static String DMS_DETAIL = "GNaP MediaServer for Android";
    //local render name
    public final static String LOCAL_RENDER_NAME = "Local Render";
	
	private static NetworkInfo currentNetworkInfo = null;
	
	 public static InputStream getFromAssets(Context context,String fileName){ 
         try { 
        	 
        	 AssetManager assetManager = context.getAssets();
             // 需要解压的对象
             InputStream dataSource = assetManager.open(fileName);
                
             return dataSource;
           
         } catch (Exception e) { 
             e.printStackTrace(); 
             return null;
         }
     } 
	 
	 
	 public static void setNetInfo(Context context)
	 {
		 if(currentNetworkInfo==null)
		 {
			 ConnectivityManager cwjManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);   
			 currentNetworkInfo = cwjManager.getActiveNetworkInfo();
		 }
	 }
	 
	 public static NetworkInfo getNetInfo()
	 {
		 return currentNetworkInfo;
	 }
	 
	 
	 public static void print(String tag,String value)
	 {
//		 Log.v(tag, value);
	 }
	 
	 
	public static String getLocalIpAddress(Context context) {
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface
//					.getNetworkInterfaces(); en.hasMoreElements();) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf
//						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//					if (!inetAddress.isLoopbackAddress()) {
//						if (inetAddress.getHostAddress().toString()
//								.substring(0, 3).compareTo("172") != 0) {
//							return inetAddress.getHostAddress().toString();
//						}
//					}
//				}
//			}
//		} catch (SocketException e) {
//			return "localhost";
//		}
		
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		
		
		return intToIp(ipAddress);
	}
		

    /**
     * 把int->ip地址
     * @param ipInt
     * @return String
     */
    public static String intToIp(int i) {
//        return new StringBuilder().append(((ipInt >> 24) & 0xff)).append('.')
//                .append((ipInt >> 16) & 0xff).append('.').append(
//                        (ipInt >> 8) & 0xff).append('.').append((ipInt & 0xff))
//                .toString();
        return (i & 0xFF ) + "." +  ((i >> 8 ) & 0xFF) + "." +   ((i >> 16 ) & 0xFF) + "." +  ( i >> 24 & 0xFF) ;
    }	
		
	public static int getRealTime(String time) {
		int result = 0;
		if (time.indexOf(":") > 0) {
			String[] temptime = time.split(":");
			result = Integer.parseInt(temptime[2])
					+ Integer.parseInt(temptime[1]) * 60
					+ Integer.parseInt(temptime[0]) * 3600;
		}
		return result;
	}

	public static String format(long ms) {

		int mi = 60;
		int hh = mi * 60;

		long hour = ms / hh;
		long minute = (ms - hour * hh) / mi;
		long second = ms - hour * hh - minute * mi;

		String strHour = hour < 10 ? "0" + hour : "" + hour;
		String strMinute = minute < 10 ? "0" + minute : "" + minute;
		String strSecond = second < 10 ? "0" + second : "" + second;

		return strHour + ":" + strMinute + ":" + strSecond;
	}
		
	public static void setStringContentToFile(String filePath, String content) {
		try {
			String parentpathString = filePath.substring(0,
					filePath.lastIndexOf("/"));
			File parentFile = new File(parentpathString);
			if (!parentFile.exists())
				parentFile.mkdirs();

			File file = new File(filePath);
			if (!file.exists())
				file.createNewFile();

			PrintWriter pw = new PrintWriter(file);
			pw.write(content);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static String replaceBlank(String oldString, String replace) {
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");

		Matcher m = p.matcher(oldString);

		return m.replaceAll(replace);
	}
		
	public static int getResult(int pid) {
		String result = "";
		// String[] cmd_str = { "ps", Integer.toString(pid) };
		String[] cmd_str = { "ps", "minidlna" };
		try {
			result = execute(cmd_str, "/");
		} catch (IOException e) {
			// Utils.print(TAG, "IOException");
			e.printStackTrace();
		}

		String space_str = " ";
		String null_str = "";
		String[] ss = result.trim().split(space_str);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].equals(null_str))
				continue;
		}

		// if process name == minidlna, mean the process is alive
		// Utils.print(TAG, result.trim());
		// Utils.print(TAG, "Process Name : " + ss[ss.length - 1]);
		if (ss[ss.length - 1].equals("minidlna")) {
			return 1;
		}
		return 0;
	}

	public static void killminidlna() {
		String result = "";
		String[] cmd_str = { "ps", "minidlna" };

		Utils.print(TAG, "================================= getResult2");

		try {
			result = execute(cmd_str, "/");
		} catch (IOException e) {
			// Utils.print(TAG, "IOException");
			e.printStackTrace();
		}

		String space_str = " ";
		String null_str = "";
		String[] ss = result.trim().split(space_str);

		// if process name == minidlna, mean the process is alive
		Utils.print(TAG, "====>>>>> " + result.trim());
		Utils.print(TAG, "");

		if (ss[ss.length - 1].equals("minidlna")) {
			Utils.print(TAG, "Mini-dlna is still alive, Now Kill it!!!");

			int jj = 0;
			String pidstr = null;

			// get pidstr
			for (int i = 0; i < ss.length; i++) {
				if (ss[i].equals(null_str))
					continue;
				// Utils.print(TAG, ">>>>> "+jj+ " ======= " + ss[i]);
				if (jj == 8) {
					pidstr = ss[i];
					break;
				}
				jj++;
			}

			dokill(pidstr);
		}
	}

	public static void dokill(String pidstr) {

		Utils.print(TAG, "pid ==== " + pidstr);

		// String[] cmd_str = { "ps", Integer.toString(pid) };
		String[] cmd_str = { "kill", "-9", pidstr };
		try {
			execute(cmd_str, "/");
		} catch (IOException e) {
			// Utils.print(TAG, "IOException");
			e.printStackTrace();
		}

		// Delete db file when stop dlna server
		File dbfile = new File("/data/data/com.wireme/app_etc/files.db");
		if (dbfile.exists()) {
			dbfile.delete();
		}
	}

	public static String execute(String[] cmmand, String directory)
			throws IOException {
		String result = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(cmmand);

			if (directory != null)
				builder.directory(new File(directory));
			builder.redirectErrorStream(true);
			Process process = builder.start();

			// 得到命令执行后的结果
			InputStream is = process.getInputStream();
			byte[] buffer = new byte[200];

			while (is.read(buffer) != -1) {
				result = result + new String(buffer);
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isNetworkConnected(Context context) {
		boolean mIsNetworkUp = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			mIsNetworkUp = info.isAvailable();
		}
		return mIsNetworkUp;
	}
	
	public static String getNetworkType(Context context) {
		String net = "W";
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return net;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			net = "G";
		}
		return net;
	}
	
	
	
	

	public static Intent getMIMEType(Activity activity, int index,
			ContentItem item) {
		Intent intent = new Intent();
		String type = "";

		if (index == OPEN_MUSIC) {

			intent.setClass(activity, MusicPlayer.class);
			intent.putExtra("name", item.toString());
			intent.putExtra("playURL", item.getItem().getFirstResource()
					.getValue());

		} else if (index == OPEN_IMAGE) {
			intent.setClass(activity, ImageDisplay.class);
			intent.putExtra("path", item.getItem().getFirstResource()
					.getValue());
		} else if (index == OPEN_TEXT) {
			type = "text/*";

		} else if (index == OPEN_VIDEO) {
			type = "video/*";
		}

		Log.i(TAG, "item type ====" + type);
		if (!type.equals("")) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);

			// fix mantis bug 933, modified by martin
			String value = item.getItem().getFirstResource().getValue();
			Uri uri = Uri.parse(value);
			intent.setDataAndType(uri, type);

			// intent.setDataAndType(Uri.fromFile(new File(item.getItem()
			// .getFirstResource().getValue())), type);
		}
		return intent;
	}
	
	
	
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		String pathString = "";
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			pathString = cursor.getString(column_index);
			if (cursor != null)
				cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			if (cursor != null)
				cursor.close();
			return "";
		}

		if (!cursor.isClosed())
			cursor.close();

		return pathString;
	}
	
	
	/**
	  * @author zhaweijin
	  * @fucntion  获取打开文件文件的类型
	  */
	 public  static String getMIMEType(String uri) 
	    { 
	      String type="";
//	      String fName=uri.substring(uri.lastIndexOf("."+1),uri.length());
	      String end=uri.substring(uri.lastIndexOf(".")+1,uri.length()).toLowerCase(); 
	     
	      if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||end.equals("xmf")||
	    		  end.equals("ogg")||end.equals("wav") ||end.equals("aac") ||end.equals("amr")  
	    		  ||end.equals("wma") ||end.equals("3gpp"))
	      {
	        type = "audio"; 
	      }
	      else if(end.equals("3gp")||end.equals("mp4") ||end.equals("m4v") ||end.equals("wmv")
	    		  ||end.equals("flv") ||end.equals("asf") ||end.equals("mkv") ||end.equals("ogg")
	    		  ||end.equals("avi") ||end.equals("rmvb") ||end.equals("mpg") ||end.equals("h264")
	    		  ||end.equals("mov") ||end.equals("ts"))
	      {
	        type = "video";
	      }
	      else if(end.equals("jpg")||end.equals("gif")||end.equals("png")
	    		  ||end.equals("jpeg")||end.equals("bmp") ||end.equals("tiff"))
	      {
	        type = "image";
	      }
	      else if(end.equals("apk")) 
	      { 
	        type = "application/vnd.android.package-archive";
	      } 
	      else
	      {
	        type="*";
	      }
	      return type;  
	    }

	 
		public static String getAPNType(Context context) {
			String net = "";
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo == null) {
				return net;
			}
			int nType = networkInfo.getType();
			if (nType == ConnectivityManager.TYPE_MOBILE) {
				net = "G";
			} else if (nType == ConnectivityManager.TYPE_WIFI) {
				net = "W";
			}
			return net;
		}

		
		
		
	/**
	 * 判断字符串的编码
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "";
	}
	
	
	/**
	 * @author zhaweijin
	 * @fucntion 删除文件，根据路径名
	 */
	public static void delFile(String strFileName) {
		File myFile = new File(strFileName);
		if (myFile.exists()) {
			myFile.delete();
		}
	}

	/**
	 * @author zhaweijin
	 * @fucntion 删除目录文件
	 */
	public static boolean deleteFileDir(File f) {
		boolean result = false;
		try {
			if (f.exists()) {
				File[] files = f.listFiles();
				for (File file : files) {

					if (file.isDirectory()) {
						if (deleteFile(file))
							result = false;
					} else {
						deleteFile(file);
					}
				}
				f.delete();
				result = true;
			}
		} catch (Exception e) {
			return result;
		}
		return result;
	}

	/**
	 * @author zhaweijin
	 * @fucntion 删除文件，根据文件对象
	 */
	public static boolean deleteFile(File f) {
		boolean result = false;
		try {
			if (f.exists()) {
				Utils.print("file delete", "file delete");
				f.delete();
				result = true;
			}
		} catch (Exception e) {
			return result;
		}
		return result;
	}
	
	
	
	public static void saveToFile(byte[] data) {
		try {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + "carter.png";
			OutputStream out = new FileOutputStream(path);
			out.write(data, 0, data.length);
			out.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	public static int computeSampleSize(BitmapFactory.Options options,
	        int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength,
	            maxNumOfPixels);

	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }

	    return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
	        int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;

	    int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
	            .sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
	            .floor(w / minSideLength), Math.floor(h / minSideLength));

	    if (upperBound < lowerBound) {
	        // return the larger one when there is no overlapping zone.
	        return lowerBound;
	    }

	    if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}
	
	public static NetworkInterface getActualNetworkInterface() {
//		Utils.print(TAG, " ..................getActualNetworkInterface.");
		{
			try {				
				
				List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
				ConnectInfo mConnectInfo = NetworkConnectState.GetConnectInfo();
				for (NetworkInterface iface : interfaces) {
//					Utils.print("1iface>name", iface.getDisplayName());					
					
					if (iface.getDisplayName().equals("eth0")) {
						if (mConnectInfo.type == NetworkConnectState.NETWORK_USE_ETH){
//							Log.v(TAG , ">>>>>>>>>>>> NETWORK_USE_ETH");
							return iface;
						}
					}else if (iface.getDisplayName().equals("wlan0")) {
						if (mConnectInfo.type == NetworkConnectState.NETWORK_USE_WIFI){
//							Log.i(TAG , ">>>>>>>>>>>> NETWORK_USE_WIFI");
							return iface;
						}
					}else if (iface.getDisplayName().equals("tiwlan0")) {
						if (mConnectInfo.type == NetworkConnectState.NETWORK_USE_TI_WIFI){
//							Log.i(TAG , ">>>>>>>>>>>> ti wifi");
							return iface;
						}
					}else if (iface.getDisplayName().equals("tiap0") || iface.getDisplayName().equals("wl0.1")) {
						if (mConnectInfo.type == NetworkConnectState.NETWORK_USE_SOFTAP){
//							Log.i(TAG , ">>>>>>>>>>>> NETWORK_USE_SOFTAP");
							return iface;
						}
					}
					else if(iface.getDisplayName().equals("p2p-wlan0-0"))
					{
						if(mConnectInfo.type == NetworkConnectState.NETWORK_USE_WIFI_DIRECT)
						{
//							Utils.print(TAG, "use wifi direct");
							return iface;
						}
					}
					
					else if(iface.getDisplayName().equals("ppp0"))
					{
						return iface;
					}
				}

			} catch (Exception ex) {
				throw new InitializationException("Could not find emulator's network interface: " + ex,ex);
			}
		}

		return null;
	}
	
    private static List<InetAddress> getInetAddresses(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses());
    }
	
    public static boolean isUsableAddress(InetAddress address) {
		if (!(address instanceof Inet4Address)) {
			Utils.print(TAG , "Skipping unsupported non-IPv4 address: " + address);
			return false;
        }
        return true;
    }
	
	public static String getCurrentAddress() {
		String currentInetAddress = "";
		NetworkInterface networkIface = Utils.getActualNetworkInterface();
		if(networkIface!=null){
			for (InetAddress inetAddress : getInetAddresses(networkIface)) {

				if (inetAddress == null) {
					continue;
				}
				if (isUsableAddress(inetAddress)) {
//					Utils.print("current ip", inetAddress.getHostAddress());
					currentInetAddress = inetAddress.getHostAddress();
				}
			}
		}

		return currentInetAddress;
	}
	
	
	public static String getLocaleLanguage() {
		Locale l = Locale.getDefault();
		return String.format("%s-%s", l.getLanguage(), l.getCountry());
		
	}
	
	
	public static String getImageStoreFilepath(Activity activity)
	{
		if(avaiableMedia()){
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}else {
			return "/data/data/"+ activity.getPackageName();
		}
	}
	
	public static boolean avaiableMedia() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED))
			return true;
		else {
			return false;
		}
	}
	
	
	public static boolean checkNetworkIsActive(Context context) {
		boolean mIsNetworkUp = false;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			mIsNetworkUp = info.isAvailable();
		}
		return mIsNetworkUp;
	}
}
