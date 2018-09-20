package com.mirage.dlna.music;

import java.util.ArrayList;
import java.util.List;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.mediarenderer.gstreamer.PlayListener;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.TransportState;
import org.teleal.common.util.MimeType;

import com.mirage.dlna.CustomProgressDialog;
import com.mirage.dlna.R;
import com.mirage.dlna.application.BaseApplication;
import com.mirage.dmp.ContentItem;
import com.mirage.dmp.DeviceItem;

import com.mirage.util.Utils;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MusicPlayer extends Activity implements View.OnTouchListener
		{
	
	private final static String TAG = "MusicPlayer";
	private SharedPreferences preferences;
	public  TextView mCurrentTime;
	private TextView mTotalTime;
	public  TextView nameTitle;
	public  ArrayList<ContentItem> list = new ArrayList<ContentItem>();
	public String path;
	private String metaData;
	public String name;
	
	private CustomProgressDialog progressDialog;
	
	private AudioManager audioManager = null;
	private int position;
	private int music_volume = -1;

	public  boolean isplay = true;

	/* 定义进度条 */
	public  SeekBar mSeekBar = null;
	
	private boolean initGetMute = false;
	private boolean isMute = false;
	
	//播放控制
    public boolean isplaying = false;

    //play button
    private Button playButton;
    private Button nextButton;
    private Button upButton;
    private Button volume_DownButton;
    private Button volume_UpButton;
    private Button olderButton;
    private Button shuffleButton;
    private Button repreatButton;
    private Button muteButton;
    
    private LinearLayout remoteLayout;
    private LinearLayout layout_left_control;
    
    
    private boolean isRemote = false;
    private boolean isRender = false;
	private boolean isUpdatePlaySeek = true; 
	private DeviceItem dmrDeviceItem = null;
	private AndroidUpnpService upnpService = null;
	private String currentContentFormatMimeType="";
    

	public static final int REFRESH_STATE_TIME = 0x11;
	public static final int UN_REFRESH_STATE_TIME = 0x22;;
	public static final int UNKNOW_ERROR = 0x33;
	public static final int RESET_TIME = 0x44;
	
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				switch (msg.what) {
				case REFRESH_STATE_TIME:
					if(PlayerService.mMediaPlayer!=null){
						long pp = Long.parseLong(msg.obj.toString());
						mTotalTime.setText(MusicUtils.makeTimeString(
							     MusicPlayer.this, PlayerService.mMediaPlayer.getDuration() / 1000));
						mSeekBar.setMax(PlayerService.mMediaPlayer.getDuration());
						mSeekBar.setProgress((int)pp);
						mCurrentTime.setText(MusicUtils.makeTimeString(
								MusicPlayer.this, pp / 1000));
						
						if(PlayerService.mMediaPlayer.isPlaying()){
								playButton.setBackgroundResource(R.drawable.audio_pause);
								isplay = true;
						}
						else{
								playButton.setBackgroundResource(R.drawable.audio_play);
								isplay = false;
						}
					}
					break;
				case UN_REFRESH_STATE_TIME:
					mCurrentTime.setText("--:--");
					mSeekBar.setProgress(1000);
					break;
				case UNKNOW_ERROR:
					Toast.makeText(MusicPlayer.this, R.string.unknow_error, 1000).show();
					break;
				case RESET_TIME:
					mCurrentTime.setText("--:--");
					mTotalTime.setText("--:--");
					mSeekBar.setProgress(0);
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Utils.print("music play", "music play");
		
		PlayListener.audioShow = true;
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.music_play);

		Intent intent = getIntent();
		path = intent.getStringExtra("playURI");
		position = intent.getIntExtra("position", 0);
		isRemote = intent.getBooleanExtra("isRemote", false);
        
		Utils.print(TAG, "position-->"+position);
//		if(!isRemote)
//		{
//			creator = intent.getStringExtra("creator");
//			albumArtURI = intent.getStringExtra("albumArtURI");
//		}
		   
		isRender = intent.getBooleanExtra("isRender", false);
		currentContentFormatMimeType = intent.getStringExtra("currentContentFormatMimeType");
		
		
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		BaseApplication myApplication = (BaseApplication) getApplication();
		if(!isRender)
		{
//			Utils.print("sie", myApplication.listPlayMusic.size()+"");
			if(myApplication.listPlayMusic.size()>=0)
			{
				list.addAll(myApplication.listPlayMusic);
			}
			else {
				if(myApplication.listMusic!=null){
					list.addAll(myApplication.listMusic);
				}
			}
		}

		

//		Utils.print("list position", position+"");

		if(isRemote)
		{
			dmrDeviceItem = myApplication.dmrDeviceItem;
			upnpService = myApplication.upnpService;
			metaData = intent.getStringExtra("metaData");	
		}
		
		myApplication = null;
		
		//if imagedisplay to close
		sendBroadcast(new Intent("com.xuzhi.dmr.video.playfinished"));
		sendBroadcast(new Intent("com.xuzhi.dmr.image.playfinished"));
		
		if(!isRender){
			if(list.size()>0)//local or control
		          initView();
		}else {
			initView();                 //render 
		}

        
		
		
        IntentFilter intentfilter = new IntentFilter();
		if(!isRemote)
		{
			intentfilter.addAction("com.android.music.error");
			intentfilter.addAction("com.mirage.music.complete");
			if(isRender){
				intentfilter.addAction("com.xuzhi.dmr.audio.playfinished");
			}			
			intentfilter.addAction("com.mirage.playing");
			
			registerReceiver(errorBroadCast, intentfilter);
		}
		else {
			
			intentfilter.addAction("com.update.play");
			intentfilter.addAction("com.audio.play.error");
			intentfilter.addAction("com.connection.failed");
			intentfilter.addAction("com.connection.sucessed");
			intentfilter.addAction("com.xuzhi.music.dmr"); // 针对远程播放，切换上一首， 下一首
			registerReceiver(updatePlayTime, intentfilter);
		}
		
		if(!isRender){
			if(list.size()>0){
				playMusic();
			}
			else {
				cancelMusicNotification();
				finish();
			}
		}else {
			playMusic();
		}
		
		
		
	}



	
	public void initView()
	{
		RelativeLayout layout_top_control = (RelativeLayout)findViewById(R.id.layout_top_control);
		remoteLayout = (LinearLayout)findViewById(R.id.layout_right);

		//older、shuffle、repreat
		layout_left_control = (LinearLayout)findViewById(R.id.layout_left);
		olderButton = (Button)findViewById(R.id.older);
		shuffleButton = (Button)findViewById(R.id.shuffle);
		repreatButton = (Button)findViewById(R.id.repreat);
		olderButton.setOnClickListener(onClickListener);
		shuffleButton.setOnClickListener(onClickListener);
		repreatButton.setOnClickListener(onClickListener);
		
		//remote sound control
		volume_DownButton = (Button)findViewById(R.id.volume_down);
		volume_UpButton = (Button)findViewById(R.id.volume_up);
		muteButton = (Button)findViewById(R.id.mute);
		muteButton.setOnClickListener(onClickListener);
		volume_DownButton.setOnClickListener(onClickListener);
		volume_UpButton.setOnClickListener(onClickListener);
		
		//play control
		playButton = (Button)findViewById(R.id.play);
		nextButton = (Button)findViewById(R.id.next);
		upButton = (Button)findViewById(R.id.up);
		playButton.setOnClickListener(onClickListener);
		nextButton.setOnClickListener(onClickListener);
		upButton.setOnClickListener(onClickListener);
		
		//common
		nameTitle = (TextView)findViewById(R.id.music_name);
		mSeekBar = (SeekBar)findViewById(R.id.music_seekbar);
		mSeekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
		mCurrentTime = (TextView)findViewById(R.id.current_play_time);
		mTotalTime = (TextView)findViewById(R.id.total_play_time);
		if(!isRemote)
		{
			remoteLayout.setVisibility(View.INVISIBLE);
			if(isRender)
			{
				layout_top_control.setVisibility(View.INVISIBLE);
				upButton.setEnabled(false);
				nextButton.setEnabled(false);
			}
		}
		else {
			layout_left_control.setVisibility(View.INVISIBLE);
		}
		
		
		//init playmode
		int mode = preferences.getInt("PlayMode", 1);
		if(mode==1){
			repreatButton.setBackgroundResource(R.drawable.repeat_selected);
		}
		else if(mode==2){
			shuffleButton.setBackgroundResource(R.drawable.shuffle_selected);
		}
		else if(mode==3){
			olderButton.setBackgroundResource(R.drawable.older_selected);
		}
		
		if(!isRender)
		  nameTitle.setText(list.get(position).getItem().getTitle());
		else {
			nameTitle.setText(getIntent().getStringExtra("name"));
		}
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}


	public void setPlayMode(int mode)
	{
		preferences.edit().putInt("PlayMode", mode).commit();
//		Utils.print("mode", ""+mode);
	}
	
	
	
	
	
	
	public void setAudioRemoteMuteState(boolean state)
	{
		isMute = state;
		if(state)
			muteButton.setBackgroundResource(R.drawable.music_mute);
		else {
			muteButton.setBackgroundResource(R.drawable.music_nomute);
		}
	}
	
	
	public void nextMusic()
	{
		if(!isRemote)
		{
			if (position <= list.size() - 2) {
				position = position + 1;
				Utils.print(TAG ,"position-->next" + position);
				if (position <= (list.size() - 1)) {

					try {
						nameTitle.setText(list.get(position).toString());
                        mHandler.sendEmptyMessage(RESET_TIME);
						path = list.get(position).getItem().getFirstResource().getValue();
						
						Intent intent = new Intent("com.xuzhi.music.play");
						intent.putExtra("path", path);
						intent.putExtra("position", position);
						sendBroadcast(intent);

						playButton.setBackgroundResource(R.drawable.audio_pause);
						isplay = true;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else {
				Toast.makeText(MusicPlayer.this, getResources().getString(R.string.music_already_last),Toast.LENGTH_SHORT).show();
			}
		}
		else {

		}
	}
	
	

	
	public void upMusic()
	{
		if(!isRemote)
		{
			if (position >= 1) {
				position = position - 1;

				Utils.print(TAG ,"position-->pre" + position);
				if (position >= 0) {
					try {
						nameTitle.setText(list.get(position).toString());
						Utils.print(TAG ,"music  list==" + list.size());
						mHandler.sendEmptyMessage(RESET_TIME);
						
                        path = list.get(position).getItem().getFirstResource().getValue();
						
						Intent intent = new Intent("com.xuzhi.music.play");
						intent.putExtra("path", path);
						intent.putExtra("position", position);
						sendBroadcast(intent);

						playButton.setBackgroundResource(R.drawable.audio_pause);
						isplay = true;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
			} else {
				Toast.makeText(MusicPlayer.this, getResources().getString(R.string.music_already_first),Toast.LENGTH_SHORT).show();
			}
		}
		else {

		}

	}
	
	public void setAudioFormat(ContentItem item)
	{
			List<Res> res = item.getItem().getResources();
			String filetype = null;
			MimeType filemt = res.get(0).getProtocolInfo().getContentFormatMimeType();
			
			if (filemt != null) {
				currentContentFormatMimeType = filemt.toString();
				Utils.print("play mine type a  ", currentContentFormatMimeType);
			}
		
	}
	
	 
	
	 
	
	public void playPauseMusic()
	{
		if(!isRemote)
		{
			Utils.print("isplay", Boolean.toString(isplay));
			if (isplay == true) {
				playButton.setBackgroundResource(R.drawable.audio_play);
				isplay = false;
				if(PlayerService.mMediaPlayer!=null)
				    PlayerService.mMediaPlayer.pause();

			} else {
				playButton.setBackgroundResource(R.drawable.audio_pause);
				isplay = true;
                //重新播放
				if(PlayerService.mMediaPlayer!=null){
					 PlayerService.mMediaPlayer.start();
					 isRunning = false;
					 refresh();
				}
				  
			}
		}
		else {
			
			if (isplay == true) {
				playButton.setBackgroundResource(R.drawable.audio_play);
				isplay = false;
 

			} else {
				playButton.setBackgroundResource(R.drawable.audio_pause);
				isplay = true;
 
//				dmcControl.setCurrentPlayPath(list.get(
//						position).getItem().getFirstResource().getValue()); 
//				dmcControl.getProtocolInfos(currentContentFormatMimeType);
			}
			
			
		}

	}
	
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.up:
				upMusic();
				break;
			case R.id.next:
				nextMusic();
				break;
			case R.id.play:
				playPauseMusic();
				break;
 
			case R.id.older:
				setPlayMode(3);
				break;
			case R.id.shuffle:
				setPlayMode(2);
				break;
			case R.id.repreat:
				setPlayMode(1);
				break;
			}
		}
	};

	private View.OnClickListener mUpListener = new View.OnClickListener() {
		public void onClick(View v) {
			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			music_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (music_volume > 0) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			}
		}
	};

	private View.OnClickListener mDownListener = new View.OnClickListener() {
		public void onClick(View v) {
			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			music_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (music_volume == 0) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
		}

	};

	
	public void cancelMusicNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(PlayMode.Music_Notification_ID);
	}
	
	/**
	 * 播放音乐
	 */

	private void playMusic() { 
		
		try {
			Utils.print("status", (PlayerService.mMediaPlayer==null?true:false)+"");
			if(PlayerService.mMediaPlayer == null || isRender)
			{
				if(!isRemote)
				{
					Utils.print("first start server", "first start server");
					Intent intent = new Intent();
					intent.setClass(MusicPlayer.this, PlayerService.class);
					
					intent.putExtra("path", path);
			        intent.putExtra("position", position);
			        intent.putExtra("isRender", isRender);
					
					stopService(intent);
					startService(intent);
				}
			}
			else {
				Utils.print("send----->", "send----->");
				if(PlayerService.mMediaPlayer == null){
					Utils.print("null", "null");
					Intent intent = new Intent();
					intent.setClass(MusicPlayer.this, PlayerService.class);
					
					intent.putExtra("path", path);
			        intent.putExtra("position", position);
			        intent.putExtra("isRender", isRender);
					startService(intent);
				}else {
					Intent intent = new Intent("com.xuzhi.music.play");
					intent.putExtra("path", path);
					intent.putExtra("position", position);
					sendBroadcast(intent);
				}
			}
			Utils.print("isRender", Boolean.toString(isRender));

			if(isRemote)
			{
				cancelMusicNotification();
				startProgressDialog();
 
				
				Intent intent = new Intent();
				intent.setClass(MusicPlayer.this, PlayerService.class);
				stopService(intent);
			}
			
			
			//Utils.print("isplayer-->", (PlayerService.mMediaPlayer.isPlaying()?true:false)+"");
			
			if(PlayerService.mMediaPlayer!=null)
			{
				if(PlayerService.mMediaPlayer.isPlaying())
				{
					playButton.setBackgroundResource(R.drawable.audio_pause);
					isplay = true;
				}
				else
				{
					playButton.setBackgroundResource(R.drawable.audio_play);
					isplay = false;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
	}
	
	


	/*
	 * SeekBar进度改变事件
	 */
	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

		// @Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			if(!isRemote)              //本地
			{
				if (!fromUser) {

				} else {
					Utils.print(TAG ,"progress===" + progress);
					if(PlayerService.mMediaPlayer != null){
						PlayerService.mMediaPlayer.seekTo(progress);
					}
				}
			}

		}

		// @Override
		public void onStartTrackingTouch(SeekBar seekBar) {
              if(isRemote)
            	  isUpdatePlaySeek = false;
		}

		// @Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Utils.print(TAG ,"StopTrackingTouch");
			isUpdatePlaySeek = true;
			if(!isRemote)
			{                       //本地拖动
				if(PlayerService.mMediaPlayer != null){
					PlayerService.mMediaPlayer.seekTo(seekBar.getProgress());
				}
			}
			else {                   //远程拖动
				//执行seekbar远程命令
				String seekto = Utils.format(seekBar.getProgress());
			}
		
			
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utils.print(TAG, "onDestroy");
		if(isRemote){
			unregisterReceiver(updatePlayTime);
			cancelMusicNotification();
		}
		else {
			unregisterReceiver(errorBroadCast);
		}
		
		PlayListener.audioShow = false;
 
		list.clear();
		
		if(isRender)
		{
			if(PlayListener.getGstMediaPlayer()!=null)
			{
				PlayListener.getGstMediaPlayer().transportStateChanged(TransportState.NO_MEDIA_PRESENT);
			}
		}
		

	}

	 
	 



	private BroadcastReceiver errorBroadCast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				if (intent.getAction().equals("com.android.music.error")
						|| intent.getAction().equals("com.mirage.music.complete")) {
					Utils.print(TAG, "com.android.music.error");
					finish();
				}
				else if (intent.getAction().equals("com.mirage.playing")) {
					Utils.print(TAG ,"set total time");
					if(null !=PlayerService.mMediaPlayer )
					{
						if(PlayerService.mMediaPlayer.isPlaying() && list.size()>0)
						{
							int temPosition = intent.getIntExtra("position", 0);
							//Utils.print("aaaa" ,"--->"+temPosition);
							mTotalTime.setText(MusicUtils.makeTimeString(
								     MusicPlayer.this, PlayerService.mMediaPlayer.getDuration() / 1000));
							nameTitle.setText(list.get(temPosition).toString());
						    
						}
						else if(PlayerService.mMediaPlayer.isPlaying())
						{
							mTotalTime.setText(MusicUtils.makeTimeString(
								     MusicPlayer.this, PlayerService.mMediaPlayer.getDuration() / 1000));
						}
						
						refresh();
					}
					else
					{
						mTotalTime.setText("--:--");
						nameTitle.setText("");
					}
						
				}
				else if(intent.getAction().equals("com.xuzhi.dmr.audio.playfinished"))
				{
					Utils.print(TAG, "com.xuzhi.dmr.audio.playfinished");
					if(isRender)
					  finish();
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}

	};

	
	
	//update playseek bar broadcast--isRender display
	private BroadcastReceiver updatePlayTime = new BroadcastReceiver() {
		
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
//			Utils.print("update action", intent.getAction());
			if(intent.getAction().equals("com.update.play"))
			{
				if(isUpdatePlaySeek)
				{
					Bundle bundle = intent.getExtras();
					String trackTime = bundle.getString("TrackDuration");
					String relTime = bundle.getString("RelTime");
					
					int maxTime = Utils.getRealTime(trackTime);
					int currentTime = Utils.getRealTime(relTime);
					
					mSeekBar.setMax(maxTime);
					mSeekBar.setProgress(currentTime);
					
					mTotalTime.setText(trackTime);
					mCurrentTime.setText(relTime);
					
					stopProgressDialog();
					
					if(!initGetMute){
						initGetMute = true;
 
					}
				}	
			}
//			else if(intent.getAction().equals("com.continue.display"))
//			{
//				isUpdatePlaySeek = true;
//			}
			else if(intent.getAction().equals("com.audio.play.error"))
			{
//				playButton.setBackgroundResource(R.drawable.audio_play);
				Utils.print(TAG, "playerror");
//				Toast.makeText(MusicPlayer.this, intent.getStringExtra("message"), 1000).show();
//				isplay = false;
				stopProgressDialog();
				
//				finish();
			}
			else if(intent.getAction().equals("com.connection.failed"))
			{
				Utils.print(TAG, "connectionfailed");
				stopProgressDialog();
				Toast.makeText(MusicPlayer.this, getResources().getString(R.string.connection_serices_failed), 1500).show();
				finish();
			}
			else if(intent.getAction().equals("com.connection.sucessed")){
		        stopProgressDialog();
			}
//			else if(intent.getAction().equals("com.xuzhi.music.dmr")){
//				String playpath = intent.getStringExtra("path");
//				String playname = intent.getStringExtra("name");
//				nameTitle.setText(playname);
//				
//				Intent intent2 = new Intent("com.xuzhi.music.play");
//				intent2.putExtra("path", playpath);
//				intent2.putExtra("position", 0);
//				sendBroadcast(intent2);
//			}
		}
	};
	
	

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
 
			
			if(isRender)
			{
				//PlayerService.mMediaPlayer.release();
				stopService(new Intent(MusicPlayer.this, PlayerService.class));

				if (PlayListener.getGstMediaPlayer() != null) {
					PlayListener.getGstMediaPlayer().transportStateChanged(TransportState.NO_MEDIA_PRESENT);
				}
				
				
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	

    private boolean isRunning = false;
	private void refresh() {
	if(!isRunning)
	{
		    isRunning = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						int CurrentPosition = 0;// 设置默认进度条当前位
						while (PlayerService.mMediaPlayer != null
								 && !MusicPlayer.this.isFinishing()) {

							CurrentPosition = PlayerService.mMediaPlayer.getCurrentPosition();
							Utils.print("refresh", "refresh");
							Message msg = mHandler.obtainMessage();
							msg.obj = CurrentPosition;
							msg.what = REFRESH_STATE_TIME;
							msg.sendToTarget();
							
							
							Thread.sleep(1000);
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					isRunning = false;
				}
			}).start();
	}
    
	}
	
	private void startProgressDialog() {
		if (progressDialog == null) {
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setCancelable(true);
			progressDialog.setMessage(getResources().getString(R.string.connecting_serices));
		}

		progressDialog.show();
	}

	private void stopProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
}

