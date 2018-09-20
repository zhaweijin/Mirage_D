package com.mirage.dlna;


import com.update.download.DataSet;
import com.update.download.DownloadUtils;
import com.mirage.util.Utils;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class About extends Activity {

	
	private DownloadUtils downloadUtils;
	
	private TextView version_value;
	private RelativeLayout layout_check_update;
	private CustomProgressDialog progressDialog;
	private final static int PROGRESS_CANCEL = 0x111;
	private final static int UPDATE_TIPS = 0x112;
	private final static int START_CHECK = 0x113;
	private final static int NETWORK_CONNECTION_FAILED = 0x114;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case START_CHECK:
				if (!downloadUtils.checkIsUpdate(About.this,(DataSet)msg.obj)) {
					handler.sendEmptyMessage(UPDATE_TIPS);
				}
				handler.sendEmptyMessage(PROGRESS_CANCEL);
				break;
			case PROGRESS_CANCEL:
				stopProgressDialog();
				break;
			case UPDATE_TIPS:
				Toast.makeText(
						About.this,
						About.this.getResources().getString(
								R.string.hased_update), 1500).show();
				break;
			case NETWORK_CONNECTION_FAILED:
				stopProgressDialog();
				Toast.makeText(
						About.this,
						About.this.getResources().getString(
								R.string.network_connection_failed), 1500).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);


		
		downloadUtils = new DownloadUtils();
		version_value = (TextView) findViewById(R.id.version_value);
		layout_check_update = (RelativeLayout) findViewById(R.id.layout_check_update);

		PackageManager pm = this.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(this.getPackageName(), 0);
			version_value.setText(info.versionName); // 版本名
		} catch (Exception e) {
			// TODO: handle exception
		}

		layout_check_update.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					startProgressDialog(About.this.getResources().getString(R.string.check_updateing), true);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(1000);
								if(!Utils.checkNetworkIsActive(About.this)){
									handler.sendEmptyMessage(NETWORK_CONNECTION_FAILED);
								}else {
									DataSet dataSet = downloadUtils.getSoftwareWebData("http://miragerdp.poptronixtech.com/update.php");
								    Message message = new Message();
								    message.obj = dataSet;
								    message.what = START_CHECK;
								    handler.sendMessage(message);
								}
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
						}
					}).start();
				}
		});
	}

	private void startProgressDialog(String mesage, boolean Cancelable) {
		if (progressDialog == null) {
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setCancelable(Cancelable);
			progressDialog.setMessage(mesage);
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
