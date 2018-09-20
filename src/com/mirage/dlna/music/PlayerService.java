package com.mirage.dlna.music;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import org.teleal.cling.mediarenderer.gstreamer.PlayListener;
import org.teleal.cling.support.model.TransportState;
import com.mirage.dlna.R;
import com.mirage.dlna.application.BaseApplication;
import com.mirage.dmp.ContentItem;

import com.mirage.util.Utils;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;


public class PlayerService extends Service {
	private final static String TAG = "PlayerService";
	

	/* 定于一个多媒体对象 */
	public static MediaPlayer mMediaPlayer = null;
	public  ArrayList<ContentItem> listplay = new ArrayList<ContentItem>(); 

	private SharedPreferences preferences;
	private String path;
	private int playPosition = 0 ;
	
	private boolean isRender = false;
	private playRecevieBrocast playRecevieBrocast = new playRecevieBrocast();
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
        Utils.print(TAG, "oncreate");
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
		}
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(intent!=null){
			if (intent.getStringExtra("path") != null) {
				path = intent.getStringExtra("path");
				isRender = intent.getBooleanExtra("isRender", false);
				playPosition = intent.getIntExtra("position",0);

			}
		}
//		Utils.print("music", path);
//		Utils.print("music", name);
		Utils.print("music play servies", "music play services");
		
		registerBrocast();
		
		try {
			if(isRender)
			{
				PlayListener.setMediaPlayer(mMediaPlayer);
			}
			else {
				BaseApplication myApplication = (BaseApplication) getApplication();
				if(myApplication.listMusic!=null){
					listplay.addAll(myApplication.listMusic);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		if(path!=null)
		  playMusic();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utils.print(TAG, "playServicerDestory");
		cancelMusicNotification();
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		mMediaPlayer = null;
		Utils.print(TAG, "null");
		listplay.clear();

		if(isRender)
		{
			PlayListener.setMediaPlayer(null);
		}
		unregisterBrocast();
		
	}

	public void cancelMusicNotification()
	{
		Utils.print("cancel", "cancel");
		NotificationManager notificationManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(PlayMode.Music_Notification_ID);
	}
	

	
	

	public void playMusic() {

		try {
            Utils.print("play", "play"+path);
//            for(int i=0;i<listplay.size();i++)
//            {
//            	Utils.print("a-->", listplay.get(i).getItem().getFirstResource().getValue())
//            }
            
			if(isRender)
			{
				//set state
				if(PlayListener.getGstMediaPlayer()!=null)
				{
					PlayListener.getGstMediaPlayer().transportStateChanged(TransportState.PLAYING);
				}
			}
			else {
				if(listplay.size()>0 && playPosition>=0)
				{
					sendMusicBroadcast(listplay.get(playPosition).getItem().getTitle(),
							listplay.get(playPosition).getItem().getCreator(),
							listplay.get(playPosition).getItem().getFirstResource().getValue());	
				}
			}
//				Utils.print("99", listplay.size()+"");
               
				
			
			
			
			
			/* 重置多媒体 */
			mMediaPlayer.reset();
			/* 读取mp3文件 */
			// mMediaPlayer = MediaPlayer.create(this, Uri.parse(path));

			try {
				mMediaPlayer.setDataSource(path);
				mMediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Utils.print("music error1", "music error");
				Intent intent = new Intent("com.android.music.error");
				sendBroadcast(intent);
				stopSelf();
				mMediaPlayer = null;
				Toast.makeText(PlayerService.this, R.string.music_error, 1000).show();
				e.printStackTrace();
				return;
			}

			/* 开始播放 */

			mMediaPlayer.start();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {

				public boolean onError(MediaPlayer mp, int what, int extra) {
					// TODO Auto-generated method stub
					Utils.print("music error2", "music error");
					Toast.makeText(PlayerService.this,getResources().getString(R.string.music_error),1000).show();
					Intent intent = new Intent("com.android.music.error");
					sendBroadcast(intent);
					stopSelf();
					mMediaPlayer = null;
					return false;
				}
			});


			
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					try {
					  int mode = preferences.getInt("PlayMode",1);

		
						Utils.print("music complete", "music complete");
						sendBroadcast(new Intent("com.mirage.music.complete"));
						stopSelf();
					
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
			}
			});

			//发送更新进度条的消息----------------------------
			refreshBroadCast();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


	
	private void resetPlay(String path) {
		Utils.print(TAG, path);
 
		try {
			
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			
			
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if(listplay.size()>0 && playPosition>=0)
		{
			sendMusicBroadcast(listplay.get(playPosition).getItem().getTitle(),
					listplay.get(playPosition).getItem().getCreator(),
					listplay.get(playPosition).getItem().getFirstResource().getValue());
		}
		
		refreshBroadCast();
		//发送更新进度条的消息----------------------------
	}

	
	//把当前播放列表的ID号发过去就可以了,以偏更新:进度条、名称、当前时间、总的时间
	public void refreshBroadCast() {
		Intent intent = new Intent();
		intent.setAction("com.mirage.playing");
		intent.putExtra("position", playPosition);
		sendBroadcast(intent);
	}


	/**
	 * 产生随机播放的单曲
	 * 
	 * @param begin
	 * @return
	 */
	public int getRandom(int begin) {

		Random rand = new Random();
		int i = rand.nextInt(); // int范围类的随机数
		i = rand.nextInt(begin); // 生成0-100以内的随机数

		i = (int) (Math.random() * begin); // 0-100以内的随机数，用Matn.random()方式

		return i;
	}



	
	class playRecevieBrocast extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Utils.print("action--->", action);
			if(action.equals("com.xuzhi.dmr.play"))
			{
				String helpAction = intent.getStringExtra("helpAction");
				Utils.print("getbroast", helpAction);
				if(helpAction.endsWith("play"))
				{
					playstart();  
				}
				else if(helpAction.endsWith("stop"))
				{
					playStop();
				}
				else if(helpAction.endsWith("pause"))
				{
					playPause();
				}
			}
			else if(action.equals("com.xuzhi.dmr.audio.playfinished"))
			{
				playStop();
			}
			else if(action.equals("com.xuzhi.music.play"))
			{
				String temppath = intent.getStringExtra("path");
				if(!temppath.equals(path)){
					//reset play
					Utils.print("re play", "re play");
					path = temppath;
					playPosition = intent.getIntExtra("position", 0);
					resetPlayList();
					resetPlay(path);
				}
				else {
					refreshBroadCast();
				}
			}
		}
	}
	
	public void resetPlayList()
	{
		Utils.print("resetPlayList", "resetPlayList");
		listplay.clear();
		BaseApplication myApplication = (BaseApplication) getApplication();

		listplay.addAll(myApplication.listPlayMusic);
		
        myApplication = null;
	}
	
	
	public void playstart() {
		// TODO Auto-generated method stub	
		try {
			Utils.print("PlayService", Boolean.toString(mMediaPlayer.isPlaying()));
			if(mMediaPlayer!=null && !mMediaPlayer.isPlaying())
			{
				mMediaPlayer.start();
			}
            
		} catch (Exception e) {
			// TODO: handle exception
		}

		
		if(PlayListener.getGstMediaPlayer()!=null)
		{
			PlayListener.getGstMediaPlayer().transportStateChanged(TransportState.PLAYING);
		}
	}
	
	public void playPause()
	{
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			
			if(PlayListener.getGstMediaPlayer()!=null)
			{
				PlayListener.getGstMediaPlayer().transportStateChanged(TransportState.PAUSED_PLAYBACK);
			}
		}
	}
	
	public void playStop()
	{
		Utils.print("music playservice play stop", "play stop");
		Intent intent = new Intent("com.mirage.music.complete");
		sendBroadcast(intent);
		stopSelf();
		
		if(PlayListener.getGstMediaPlayer()!=null)
		{
			PlayListener.getGstMediaPlayer().transportStateChanged(TransportState.STOPPED);
		}
		
		try {
			mMediaPlayer.stop();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
	
	public void registerBrocast()
	{
		IntentFilter intentFilter = new IntentFilter();
		if(isRender){
		  intentFilter.addAction("com.xuzhi.dmr.play");
		  intentFilter.addAction("com.xuzhi.dmr.playfinished");
		}
		intentFilter.addAction("com.xuzhi.music.play");
		registerReceiver(playRecevieBrocast,intentFilter);
	}
	
	public void unregisterBrocast()
	{
		unregisterReceiver(playRecevieBrocast);
	}
	
	
	public  void sendMusicBroadcast(final String title, final String creator,
			final String path) {

		try {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			Notification notification = new Notification(R.drawable.music_notification_icon,title, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(getPackageName(),R.layout.music_notification);
			notification.contentView.setTextViewText(R.id.title, title);
			notification.contentView.setTextViewText(R.id.creator, creator);

			Intent intent = new Intent(PlayerService.this, MusicPlayer.class);
			intent.putExtra("position", playPosition);
			intent.putExtra("playURI", path);
			intent.putExtra("isRemote", false);
			
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent mContentIntent = PendingIntent.getActivity(
					getApplicationContext(), PlayMode.Music_Notification_ID, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			notification.contentIntent = mContentIntent;

			notificationManager.notify(PlayMode.Music_Notification_ID, notification);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public Bitmap getIconBitmap(String urlString) {
		Utils.print("22", "image url:" + urlString);

		try {
			
			URL m;
	        InputStream i = null;
	        BufferedInputStream bis = null;
	        ByteArrayOutputStream out =null;
	        try {
	            m = new URL(urlString);
	            i = (InputStream) m.getContent();
	            bis = new BufferedInputStream(i,1024 * 8);
	            out = new ByteArrayOutputStream();
	            int len=0;
	            byte[] buffer = new byte[1024];
	            while((len = bis.read(buffer)) != -1){
	                out.write(buffer, 0, len);
	            }
	            out.close();
	            bis.close();
	        } catch (MalformedURLException e1) {
	            e1.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        if(out!=null)
	        {
		        byte[] data = out.toByteArray();
		        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				
		        return bitmap;
	        }
	        return null;
		} catch (Exception e) {
            e.printStackTrace();
			return null;
		}
	}
	
}
