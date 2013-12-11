package com.xuyan.flashlight;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends Activity implements OnClickListener {

	private Context mContext;
	private Button mFlashlightButton;
	// 定义系统所用的照相机
	private Camera mCamera;
	private Parameters parameters;
	// 定义开关状态，默认、关闭状态为false，打开状态为true
	private static boolean isOn = false;
	private long exitTime = 0;

	private SharedPreferences sharedPreferences;
	private ImageView img_click;
	private TextView tv_click;

	// 增加timer，用于控制闪光灯开启时间
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	private Handler mHandler = null;
	private int mTime = 120000;// 默认开启时间为2m

	private static final int CLOSR_FLASHLIGHT = 0;

	public static final String PREFERENCES = "FlashLight";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 保持屏幕唤醒
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		mContext = this;

		// 友盟回复自动提示
		FeedbackAgent agent = new FeedbackAgent(this);
		agent.sync();
		youmengUpdata();

		sharedPreferences = this
				.getSharedPreferences(PREFERENCES, MODE_PRIVATE);

		MobclickAgent.setDebugMode(true);

		findViews();
		bindViews();
		init();

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CLOSR_FLASHLIGHT:
					stopFlashLight();
					break;
				default:
					break;
				}
			}
		};
	}

	private void youmengUpdata() {
		// 友盟自动升级
		UmengUpdateAgent.update(this);
		UmengUpdateAgent.setDownloadListener(new UmengDownloadListener() {

			@Override
			public void OnDownloadStart() {
				Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void OnDownloadUpdate(int progress) {

			}

			@Override
			public void OnDownloadEnd(int result, String file) {
				// Toast.makeText(mContext, "download result : " + result ,
				// Toast.LENGTH_SHORT).show();
				Toast.makeText(mContext, "下载完成 : " + file, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	private void init() {
		boolean isFirstStart = sharedPreferences.getBoolean("isFirst", true);
		if (isFirstStart) {
			img_click.setVisibility(View.VISIBLE);
			tv_click.setVisibility(View.VISIBLE);
		}
		sharedPreferences.edit().putBoolean("isFirst", false).commit();
	}

	private void findViews() {
		mFlashlightButton = (Button) findViewById(R.id.btn_flashLight);
		img_click = (ImageView) findViewById(R.id.img_click);
		tv_click = (TextView) findViewById(R.id.tv_click);
	}

	private void bindViews() {
		mFlashlightButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_flashLight:
			if (isOn) {
				stopFlashLight();
				isOn = false;
			} else {
				mTime = sharedPreferences.getInt("protectTime", mTime);
				startFlashLight(mTime);
				isOn = true;
				img_click.setVisibility(View.INVISIBLE);
				tv_click.setVisibility(View.INVISIBLE);
			}
			break;
		default:
			break;
		}
	}

	// 打开手电筒
	private void startFlashLight(int time) {
		mFlashlightButton.setBackgroundResource(R.drawable.flashlight_on);
		// 获得Camera对象
		mCamera = Camera.open();
		parameters = mCamera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(parameters);
		startTimer(time);
	}

	// 关闭手电筒
	private void stopFlashLight() {
		mFlashlightButton.setBackgroundResource(R.drawable.flashlight_off);
		if (mCamera != null) {
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
			// 释放手机摄像头
			mCamera.release();
		}
		mCamera = null;
		stopTimer();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setting:
			Intent intent = new Intent();
			intent.setClass(this, SettingActivity.class);
			startActivityForResult(intent, 0);
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);
			break;
		case R.id.feedback:
			FeedbackAgent agent = new FeedbackAgent(this);
			agent.startFeedbackActivity();
			break;
		case R.id.updata:
			UmengUpdateAgent.forceUpdate(this);
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	// 暂时关闭这个方法，因为没有用到它
	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// switch (requestCode) {
	// case 0:
	// if (resultCode == RESULT_OK) {
	// mTime = sharedPreferences.getInt("protectTime", 120);
	// }
	// break;
	//
	// default:
	// break;
	// }
	// }

	private void exit() {
		if (System.currentTimeMillis() - exitTime > 2000) {
			Toast.makeText(getApplicationContext(), "再次点击退出",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			stopFlashLight();
			finish();
			overridePendingTransition(R.anim.slide_in_top,
					R.anim.slide_out_bottom);
			System.exit(0);
		}
	}

	private void startTimer(int time) {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					if (mHandler != null) {
						Message message = Message.obtain(mHandler,
								CLOSR_FLASHLIGHT);
						mHandler.sendMessage(message);
					}
				}
			};
		}
		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, time * 1000);
	}

	private void stopTimer() {

		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

}
