package com.update.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mirage.dlna.R;
import com.mirage.util.Utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class FileDownloader {
	private BufferedRandomAccessFile bufferedRandomAccessFile;
	private BufferedInputStream bis;
	private String webpath;
	private String tempfiledirpath;
	private String filepath;
	private String name;
	private int tempcount = 0;
	private Activity activity;
	private int downloadlength = 0;
	private int count;


	private String TAG = "FileDownloader";
	private boolean downloading = false;
	private DownloadUtils downloadUtils;

	public FileDownloader(Activity activity,String downpath,String name,String filedirpath) {
		
		downloadUtils = new DownloadUtils();
		startDownload(activity,downpath,name,filedirpath);
		
	}

	public void startDownload(Activity activity,String downpath,String name,String filedirpath) {
		try {
			
			this.name = name;
			this.webpath = downpath;
			tempfiledirpath = filedirpath;
			this.activity = activity;
			// 连接网络，设置网络参数
			String downpathString = downloadUtils.toUtf8String(webpath);
			
			downloadUtils.print(TAG, webpath);
			HttpURLConnection http = null;
			URL downUrl = new URL(downpathString);
			http = (HttpURLConnection) downUrl.openConnection();
			
			http.setDoInput(true);
			http.setConnectTimeout(6 * 10 * 1000);
			http.setReadTimeout(40*1000);
			http.connect();
			
			downloadUtils.print(TAG,"code " + http.getResponseCode()+"");
			if(http.getResponseCode()==200)
			{
				InputStream inStream = http.getInputStream();
				downloadUtils.print(TAG,"SSSS  "+ http.getHeaderField("Content-Length")+"");
				long length = Long.parseLong(http.getHeaderField("Content-Length"));
				if(length<1)
				{
					return;
				}
				else {
					downloadUtils.print(TAG,"GET-length"+ "   "+length);
					length = length + downloadlength;
					// 创建文件夹
					File sdcardFile = new File(tempfiledirpath);
					if (!sdcardFile.exists()) {
						sdcardFile.mkdirs();
						downloadUtils.execMethod(tempfiledirpath);
					}
					// 创建文件
                    String tempName = webpath.substring(webpath.lastIndexOf("/")+1,webpath.length());
                    filepath = tempfiledirpath + tempName;
					
				
					File tempFile = new File(filepath);
					if (!tempFile.exists())
						tempFile.createNewFile();
					bufferedRandomAccessFile = new BufferedRandomAccessFile(tempFile,"rwd");
					if (downloadlength > 0)
						bufferedRandomAccessFile.setLength(downloadlength);

					// 修改系统文件权限
					downloadUtils.execMethod(filepath);

					// 从网络流写入文件内容
					bis = new BufferedInputStream(inStream, 1024);
					bufferedRandomAccessFile.seek(downloadlength);
				try {
					if (bis != null) {
						byte[] buf = new byte[8 * 1024];
						int ch = -1;
					    downloading = true;
					    sendNotification();
						while ((ch = bis.read(buf)) != -1) {
							bufferedRandomAccessFile.write(buf, 0, ch);
							count += ch;
							tempcount = (int) ((count / (float) length) * 100);
						}
					}
				} catch (Exception e) {
						e.printStackTrace();
				}
				finally{
					downloadUtils.print(TAG, "download thread finished");
					bis.close();
					downloading = false;
					bufferedRandomAccessFile.close();
				}
			}
		}


	} catch (Exception e) {
		e.printStackTrace();
	}

}



	public void sendNotification() {
		// 初始化通知
		final NotificationManager notificationManager = (NotificationManager) activity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification(
				android.R.drawable.stat_sys_download, "正在下载",
				System.currentTimeMillis());
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(activity.getPackageName(),
				R.layout.notification);
		notification.contentView.setProgressBar(R.id.progress, 100, 0,
				false);
		notification.contentView.setTextViewText(R.id.value, 0 + "%");
		notification.contentView.setTextViewText(R.id.progress_software_name,name);

		// 设置点击通知跳转的方向
		Intent intent = new Intent();
		// 第一次发送通知
		notification.contentIntent = PendingIntent.getActivity(activity.getApplicationContext(), 
				DownloadUtils.NOTI_ID, intent, 0);
		notificationManager.notify(DownloadUtils.NOTI_ID, notification);

		// 启动连续发送通知的线程
		new Thread(new Runnable() {

			public void run() {
				try {
					while (true) {
						if (!downloading || tempcount==100){
							break;
						} else if (tempcount < 100){
							notification.contentView.setProgressBar(
									R.id.progress, 100, tempcount, false);
							notification.contentView.setTextViewText(
									R.id.value, tempcount + "%");
							notificationManager.notify(DownloadUtils.NOTI_ID, notification);
							Thread.sleep(1000);
						}
					}
					if(tempcount==100)
					{
						notificationManager.cancel(DownloadUtils.NOTI_ID);
						sendResultNotification(notificationManager, DownloadUtils.NOTI_ID);						
					}		

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/*
	 * @notificationManager 通知管理
	 * 
	 * @Noti 通知ID号 发送最后一次的通知
	 */
	public void sendResultNotification(NotificationManager notificationManager,
			int Noti) {
		try {
			Notification ni = new Notification(android.R.drawable.stat_sys_download_done, "下载完成",
					System.currentTimeMillis());
			//DownloadUtils.print("filepath", filepath);
			File file = new File(filepath);
			Intent intent = new Intent();
			// 直接跳转到软件安装页面
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			String type = downloadUtils.getMIMEType(file);
			intent.setDataAndType(Uri.fromFile(file), type);
			PendingIntent pIntent = PendingIntent.getActivity(activity, 0,intent, 0);


			ni.setLatestEventInfo(activity, name,name + "下载完成", pIntent);
			notificationManager.notify(Noti, ni);

			if (file.exists())
				downloadUtils.installFile(file, activity);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
