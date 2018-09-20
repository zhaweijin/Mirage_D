package com.update.download;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/*
 * @author zhaweijin
 * 通知广播，针对软件包的安装监听
 */
public class ReceivePackageManage extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")
					|| intent.getAction().equals(
							"android.intent.action.PACKAGE_REPLACED")) {
				String packagename = intent.getDataString();
				packagename = packagename.substring(
						packagename.indexOf(":") + 1, packagename.length());
				DownloadUtils downloadUtils = new DownloadUtils();
				downloadUtils.print("packagename", packagename);
				
				if(packagename.equals(context.getPackageName()))
				{
					downloadUtils.notificationCancel(context, DownloadUtils.NOTI_ID);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	

}
