package com.xuyan.flashlight;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
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

public class MainActivity extends Activity implements OnClickListener {
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
	private int mTime = 120000;// 默认开启时间为12s

	private static final int CLOSR_FLASHLIGHT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 保持屏幕唤醒
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		sharedPreferences = this.getSharedPreferences("FlashLight",
				MODE_PRIVATE);

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

	private void init() {
		boolean isFirstStart = sharedPreferences.getBoolean("isFirst", true);
		if (isFirstStart) {
			img_click.setVisibility(View.VISIBLE);
			tv_click.setVisibility(View.VISIBLE);
		}
		sharedPreferences.edit().putBoolean("isFirst", false).commit();
		mTime = sharedPreferences.getInt("Time", mTime);
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
				startFlashLight();
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
	private void startFlashLight() {
		mFlashlightButton.setBackgroundResource(R.drawable.flashlight_on);
		// 获得Camera对象
		mCamera = Camera.open();
		parameters = mCamera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(parameters);
		startTimer();
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
		menu.add("设置");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Toast.makeText(this, "!!!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void exit() {
		if (System.currentTimeMillis() - exitTime > 2000) {
			Toast.makeText(getApplicationContext(), "再次点击退出",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			stopFlashLight();
			finish();
			System.exit(0);
		}
	}

	private void startTimer() {
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
			mTimer.schedule(mTimerTask, mTime);
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
}
