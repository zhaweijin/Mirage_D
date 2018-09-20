package com.mirage.dmp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.mediarenderer.gstreamer.PlayListener;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.TransportState;
import org.teleal.common.util.MimeType;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mirage.dlna.CustomProgressDialog;
import com.mirage.dlna.R;
import com.mirage.dlna.application.BaseApplication;
import com.mirage.dlna.music.MusicPlayer;
 

import com.mirage.util.Utils;

public class ImageDisplay extends Activity implements OnTouchListener{
	private final static String TAG = "ImageDisplay";


	private LinearLayout layoutZoom;
	// 播放幻灯片默认是4000MS
	private long slidetimer = 5000;
	private Button preButton, nextButton, pauseButton;
	private Button smallButton,bigButton;
	private String path;
	private String metaData;
	private int position;

//	private WebView webView;
	private ImageView imageView;
	private ProgressBar progressBar;
	private float tScale;
	
    private Matrix matrix ;
    private Matrix savedMatrix = new Matrix();
    DisplayMetrics dm;
    Bitmap bitmap;
    
    private boolean imageDownloaded = true;

    private SharedPreferences preferences;
    private boolean controlIsShow = true;
    private CustomProgressDialog progressDialog;
    float minScaleR;// 最小缩放比例
    static final float MAX_SCALE = 6f;// 最大缩放比例

    static final int NONE = 0;// 初始状态
    static final int DRAG = 1;// 拖动
    static final int ZOOM = 2;// 缩放
    int mode = NONE;

    PointF prev = new PointF();
    PointF mid = new PointF();
    float dist = 1f;

	private int imageDisplayCount = 0;
    
	private RelativeLayout mRelativeLayout;
	private ArrayList<ContentItem> list;
	private Boolean ishow = false;
    private boolean showFlag = true;
    private boolean nextLoaded = false;
//    private BitmapFactory.Options options = new BitmapFactory.Options();
    
	private boolean isRemote = false;
	private boolean isRender = false;
	private DeviceItem dmrDeviceItem = null;
	private AndroidUpnpService upnpService = null;
	private String currentContentFormatMimeType="";
	
	private PlayRecevieBrocast playRecevieBrocast = new PlayRecevieBrocast();
	private UpdatePlayTime updatePlayTime = new UpdatePlayTime();

    private final static int UPDATE_IMAGE = 0x111;
    private static final int DisplayView = 0x112;
    private final static int CONTROL_DISPLAY = 0x113;
    private final static int GET_NETWORK_IMAGE_FAILED = 0x114;
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case UPDATE_IMAGE:
					if(position<list.size())
					{
						//fetchBitmapPath(list.get(position).getItem().getFirstResource().getValue());
						setImageFormat(list.get(position));
						path = list.get(position).getItem().getFirstResource().getValue();
						image_zoom(path);
					}
					break;
				case DisplayView:
					Utils.print("image loaded finished", "image loaded finished");
					
					progressBar.setVisibility(View.INVISIBLE);
					fixImageview(""+msg.obj);
					
						
					mHandler.sendMessageDelayed(mHandler.obtainMessage(CONTROL_DISPLAY), 2000);
					break;
				case CONTROL_DISPLAY:
					mHandler.removeMessages(CONTROL_DISPLAY);
					if(mRelativeLayout.isShown()){
						mRelativeLayout.setVisibility(View.INVISIBLE);
					}

					break;
				case GET_NETWORK_IMAGE_FAILED:
					Toast.makeText(ImageDisplay.this, getResources().getString(R.string.get_current_network_image_failed), 1500).show();
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.image_scale);

		
		dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取分辨率
		
		
		Intent intent = getIntent();
		path = intent.getStringExtra("playURI");
		isRemote = intent.getBooleanExtra("isRemote", false);
		isRender = intent.getBooleanExtra("isRender", false);
		
		if(isRender)
			PlayListener.imageShow = true;
		
		currentContentFormatMimeType = intent.getStringExtra("currentContentFormatMimeType");
		
		slidetimer = PreferenceManager.getDefaultSharedPreferences(this).getInt("sliding_div", 5000);
		
		BaseApplication myApplication = (BaseApplication) getApplication();
		list = myApplication.listPhoto;
		position = myApplication.position;

		Utils.print("position", position+"");
		Utils.print("isRemote", Boolean.toString(isRemote));
		Utils.print(TAG, "path" + path);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
        
		
		registerBrocast();
		if(isRemote)
		{
			dmrDeviceItem = myApplication.dmrDeviceItem;
			upnpService = myApplication.upnpService;
			metaData = intent.getStringExtra("metaData");
			startProgressDialog();
		}
		
		//if imagedisplay to close
		sendBroadcast(new Intent("com.xuzhi.dmr.video.playfinished"));
		sendBroadcast(new Intent("com.xuzhi.dmr.audio.playfinished"));
		
		initView();


        image_zoom(path);
	}

	
	public void setImageFormat(ContentItem item)
	{
		if(isRemote)
		{
			List<Res> res = item.getItem().getResources();
			String filetype = null;
			MimeType filemt = res.get(0).getProtocolInfo().getContentFormatMimeType();
			
			if (filemt != null) {
				currentContentFormatMimeType = filemt.toString();
				Utils.print("play mine type a  ", currentContentFormatMimeType);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterBrocast();
		
		ishow = false;
		

		PlayListener.imageShow = false;

		deleteFile();
		
		if(bitmap!=null){
			bitmap.recycle();
			System.gc();
		}
	}
	
	private void deleteFile()
	{
		File dirFile = new File(Utils.getImageStoreFilepath(ImageDisplay.this)+"/MirageDLNA/Image/");
		if(dirFile.exists())
			Utils.deleteFileDir(dirFile);
	}

	
	public void initView()
	{
		mRelativeLayout = (RelativeLayout)findViewById(R.id.mRelativeLayout);
		mRelativeLayout.setVisibility(View.VISIBLE);

		layoutZoom = (LinearLayout)findViewById(R.id.layout_zoom);
		
		if(preferences.getBoolean("zoom_control", false))
			layoutZoom.setVisibility(View.VISIBLE);
		else {
			layoutZoom.setVisibility(View.INVISIBLE);
		}
		
		
		if(isRender)
			mRelativeLayout.setVisibility(View.INVISIBLE);
		

		preButton = (Button) findViewById(R.id.preButton);
		pauseButton = (Button) findViewById(R.id.pauseButton);
		nextButton = (Button)findViewById(R.id.nextButton);
		
		
        imageView = (ImageView)findViewById(R.id.imag);
        progressBar = (ProgressBar)findViewById(R.id.progress);
		
		preButton.setOnClickListener(onClickListener);
		pauseButton.setOnClickListener(onClickListener);
		nextButton.setOnClickListener(onClickListener);
		
//		if(isRemote)
//			pauseButton.setVisibility(View.INVISIBLE);
		
		smallButton = (Button)findViewById(R.id.small);
		bigButton = (Button)findViewById(R.id.big);
		smallButton.setOnClickListener(onClickListener);
		bigButton.setOnClickListener(onClickListener);
		
		
		imageView.setOnTouchListener(this);// 设置触屏监听
        

	}

	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.preButton:
				mHandler.removeMessages(CONTROL_DISPLAY);
				prePic();
				 
				Utils.print("pre", "pre");
				break;
			case R.id.nextButton:
				mHandler.removeMessages(CONTROL_DISPLAY);
				nextPic();
				 
				Utils.print("next", "next");
				break;
			case R.id.pauseButton:
				playPause();
				break;
			case R.id.small:
				tScale = (float)0.8;
//                Log.v("newDist", tScale+"");
				if(matrix!=null){
					matrix.postScale(tScale, tScale, dm.widthPixels/2, dm.heightPixels/2);
	                imageView.setImageMatrix(matrix);
	                CheckView();
				}
				break;
			case R.id.big:
                tScale = (float)1.25;				
//                Log.v("newDist", tScale+"");
                if(matrix!=null){
                	matrix.postScale(tScale, tScale, dm.widthPixels/2, dm.heightPixels/2);                
                    imageView.setImageMatrix(matrix);
                    CheckView();
                }
				break;
			}
		}
	};

	

	private void playPause()
	{
		if(ishow)
		{
			ishow = false;      //停止播放
			pauseButton.setBackgroundResource(R.drawable.image_play);
			
			preButton.setVisibility(View.VISIBLE);
    		nextButton.setVisibility(View.VISIBLE);
		}
		else {
			ishow = true;        //开始播放
			pauseButton.setBackgroundResource(R.drawable.image_pause);			
            mRelativeLayout.setVisibility(View.INVISIBLE);
			
			displaySlide();
		}		 
	}

    private void image_zoom(String picpath){

        try {
        	if(isRemote){
    		}
        	
        	imageDisplayCount++;

        	Utils.print("image_zoom", "image_zoom");
        	
            fetchBitmapPath(picpath);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}


    }
	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mRelativeLayout.setVisibility(View.VISIBLE);
		super.onStop();
	}


	
	public void nextPic(){
//		Utils.print("position", position+""+list.size());
		if (position <= list.size() - 2) {
		    position ++;
			Utils.print(TAG, "position" + position);
			if (position <= (list.size() - 1)) {
				setImageFormat(list.get(position));
				path = list.get(position).getItem().getFirstResource().getValue();
				image_zoom(path);
			}
		} else {
			Toast.makeText(ImageDisplay.this, getResources().getString(R.string.photo_already_last),Toast.LENGTH_SHORT).show();
		}
	}
	
	public void prePic(){
//		Utils.print("position", position+"");
		if (position >= 1) {
			position --;
			Utils.print(TAG, "position" + position);
			if (position >= 0) {
				setImageFormat(list.get(position));
				path = list.get(position).getItem().getFirstResource().getValue();
				image_zoom(path);
			}
		} else {
			Toast.makeText(ImageDisplay.this, getResources().getString(R.string.photo_already_first),Toast.LENGTH_SHORT).show();
		}
	}

	public void displaySlide()
	{

		if(showFlag)
		{
			showFlag = false;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						while(ishow)
						{
							position++;
							if(position>=list.size()-1)
								position = 0;
							path = list.get(position).getItem().getFirstResource().getValue();
							if(imageDownloaded){
								mHandler.sendEmptyMessage(UPDATE_IMAGE);
								
								Thread.sleep(slidetimer);
								Utils.print("update pic", "update pic"+position);
							}else {
								Thread.sleep(slidetimer);
								Utils.print("update sleep", "update sleep");
							}
							
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
					showFlag = true;
				}
			}).start();
		}

	}
	
	
	public void registerBrocast()
	{
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xuzhi.dmr.play");
		intentFilter.addAction("com.mirage.image.dmr");
		intentFilter.addAction("com.xuzhi.dmr.image.playfinished");
		registerReceiver(playRecevieBrocast,intentFilter);
		
		
		intentFilter.addAction("com.connection.failed");
		intentFilter.addAction("com.image.play.error");
		intentFilter.addAction("com.connection.sucessed");
		registerReceiver(updatePlayTime, intentFilter);
	}
	
	public void unregisterBrocast()
	{
		unregisterReceiver(playRecevieBrocast);
		unregisterReceiver(updatePlayTime);
	}

	//update playseek bar broadcast
	class UpdatePlayTime extends BroadcastReceiver {
		
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
//			Utils.print("update action>", intent.getAction());
            if(intent.getAction().equals("com.connection.failed")){
				Toast.makeText(ImageDisplay.this, getResources().getString(R.string.connection_serices_failed), 1500).show();
				stopProgressDialog();
				finish();
			}
            else if(intent.getAction().equals("com.image.play.error")){
            	Toast.makeText(ImageDisplay.this, intent.getStringExtra("message"), 1500).show();
            	stopProgressDialog();
            	finish();
			}
	        else if(intent.getAction().equals("com.connection.sucessed")){
	        	stopProgressDialog();
			}
		}
	}
	
	class PlayRecevieBrocast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			String helpAction = intent.getStringExtra("helpAction");
			if (action.equals("com.xuzhi.dmr.play")) {
				Utils.print("getbroast", helpAction);
				if (helpAction.endsWith("stop")) {
					Utils.print("---------->stop", "---------->stop");
					finish();
				}
			}
			else if(action.equals("com.mirage.image.dmr")){
				String playpath = intent.getStringExtra("playpath");
				Utils.print("playpath", playpath);
				image_zoom(playpath);
			}
		}
	}
	
	
	public void fetchBitmapPath(final String urlString) {
		Utils.print(TAG, "image url:" + urlString);
		progressBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				imageDownloaded = false;
				String filepath = "";
				try {
					
					if(imageDisplayCount>=30){
						imageDisplayCount = 0;
						deleteFile();
					}
					
//					Utils.print("re", Utils.getImageStoreFilepath(ImageDisplay.this));
					File dirFile = new File(Utils.getImageStoreFilepath(ImageDisplay.this)+"/MirageDLNA/Image/");

					if(!dirFile.exists())
						dirFile.mkdirs();
					
					String filename = urlString.substring(urlString.lastIndexOf("/")+1, urlString.length());
//					Utils.print(TAG, "filename:" + filename);
					filepath = dirFile.getAbsolutePath() + "/"+ filename;
					Utils.print(TAG, "filepath:" + filepath);
					File tempFile = new File(filepath);
					
					if(!tempFile.exists())
					{
						tempFile.createNewFile();
						InputStream inputStream = fetch(urlString);
						if(inputStream!=null)
						{
							FileOutputStream fos = new FileOutputStream(tempFile);

							byte[] b = new byte[1024];
							int length = -1;
							while ((length = inputStream.read(b)) != -1) {
								fos.write(b, 0, length);
							}
							fos.close();
							inputStream.close();
						}else {
							mHandler.sendEmptyMessage(GET_NETWORK_IMAGE_FAILED);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				imageDownloaded = true;

				Message message= mHandler.obtainMessage(DisplayView);
				message.obj = filepath;
				mHandler.sendMessage(message);
			}
		}).start();
		
		
	}
	 	
	private InputStream fetch(String urlString) throws MalformedURLException,
			IOException {
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(urlString);
			HttpResponse response = httpClient.execute(request);
			return response.getEntity().getContent();
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}
		
		
	public void fixImageview(String filepath) {
		try {		
			

			final int minSideLength = Math.min(dm.widthPixels,dm.heightPixels);
			
			BitmapFactory.Options opts = new BitmapFactory.Options(); 
			opts.inJustDecodeBounds = true;
			if(new File(filepath).exists()){
				Utils.print("file exist", "file exist");
			}else{
				Utils.print("file not exist", "file not exist");
			}
			BitmapFactory.decodeFile(filepath, opts);
			opts.inSampleSize = Utils.computeSampleSize(opts, minSideLength, 
					dm.widthPixels*dm.heightPixels);
			Utils.print("computer size1", ""+opts.inSampleSize);
			opts.inSampleSize = opts.inSampleSize;
			//Utils.print("computer size2", ""+opts.inSampleSize);
			
			
			opts.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(filepath, opts);
			if(bitmap!=null)
			{
				imageView.setImageBitmap(bitmap);
			}
			

		} catch (OutOfMemoryError e) {
			
//			options.inSampleSize = options.inSampleSize/2;// 图片宽高都为原来的二分之一，即图片为原来的四分之一
			Utils.print(TAG, "OutOfMemoryError---");
//			bitmap = BitmapFactory.decodeFile(filepath, options);
//			imageView.setImageBitmap(bitmap);
		}
		 
		if(bitmap!=null){
			 minZoom();
		     center();
		     imageView.setImageMatrix(matrix);
		}

	}

	
    /**
     * 触屏监听
     */
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        // 主点按下
        case MotionEvent.ACTION_DOWN:
        	
        	if(!isRender){
        		if(mRelativeLayout.isShown()){
        			mRelativeLayout.setVisibility(View.INVISIBLE);
        		}
        		else {
        			mRelativeLayout.setVisibility(View.VISIBLE);
        		}
        	}
        	

            savedMatrix.set(matrix);
            prev.set(event.getX(), event.getY());
            mode = DRAG;
            break;
        // 副点按下
        case MotionEvent.ACTION_POINTER_DOWN:
            dist = spacing(event);
            // 如果连续两点距离大于10，则判定为多点模式
            if (spacing(event) > 10f) {
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
            }
            break;
        case MotionEvent.ACTION_UP:
        	mHandler.sendMessageDelayed(mHandler.obtainMessage(CONTROL_DISPLAY), 2000);
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
        case MotionEvent.ACTION_MOVE:
        	
            if (mode == DRAG) {
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - prev.x, event.getY()
                        - prev.y);
            } else if (mode == ZOOM) {
                float newDist = spacing(event);
                if (newDist > 10f) {
                    matrix.set(savedMatrix);
                    float tScale = newDist / dist;
//                    Log.v("newDist", newDist+"");
//                    Log.v("dist", dist+"");
                    matrix.postScale(tScale, tScale, mid.x, mid.y);
                }
            }
            break;
        }
        imageView.setImageMatrix(matrix);
        CheckView();
        return true;
    }

    /**
     * 限制最大最小缩放比例，自动居中
     */
    private void CheckView() {
        float p[] = new float[9];
        if(matrix!=null){
            matrix.getValues(p);
            if (mode == ZOOM) {
                if (p[0] < minScaleR) {
                    matrix.setScale(minScaleR, minScaleR);
                }
                if (p[0] > MAX_SCALE) {
                    matrix.set(savedMatrix);
                }
            }
            center();
        }
    }

    /**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
    	try {
        	matrix = new Matrix();
            minScaleR = Math.min(
                    (float) dm.widthPixels / (float) bitmap.getWidth(),
                    (float) dm.heightPixels / (float) bitmap.getHeight());
            Utils.print("minscale", ""+minScaleR);
            if (minScaleR < 1.0) {
                matrix.postScale(minScaleR, minScaleR);
            }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

    }

    private void center() {
    	
        center(true, true);
    }

    /**
     * 横向、纵向居中
     */
    protected void center(boolean horizontal, boolean vertical) {

    	try {
            Matrix m = new Matrix();
            m.set(matrix);
            RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            m.mapRect(rect);

            float height = rect.height();
            float width = rect.width();

            float deltaX = 0, deltaY = 0;

            if (vertical) {
                // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下放留空则往下移
                int screenHeight = dm.heightPixels;
                if (height < screenHeight) {
                    deltaY = (screenHeight - height) / 2 - rect.top;
                } else if (rect.top > 0) {
                    deltaY = -rect.top;
                } else if (rect.bottom < screenHeight) {
                    deltaY = imageView.getHeight() - rect.bottom;
                }
            }

            if (horizontal) {
                int screenWidth = dm.widthPixels;
                if (width < screenWidth) {
                    deltaX = (screenWidth - width) / 2 - rect.left;
                } else if (rect.left > 0) {
                    deltaX = -rect.left;
                } else if (rect.right < screenWidth) {
                    deltaX = screenWidth - rect.right;
                }
            }
            matrix.postTranslate(deltaX, deltaY);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		initView();
		image_zoom(path);
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
