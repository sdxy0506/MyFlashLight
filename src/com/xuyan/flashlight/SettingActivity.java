package com.xuyan.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends Activity {

	private Context mContext;
	private SharedPreferences sharedPreferences;
	private EditText tv_second;
	private Button btn_ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		mContext = this;
		sharedPreferences = getSharedPreferences(MainActivity.PREFERENCES,
				MODE_PRIVATE);
		findViews();
		init();
	}

	private void findViews() {
		tv_second = (EditText) findViewById(R.id.tv_second);
		btn_ok = (Button) findViewById(R.id.btn_ok);
	}

	private void init() {
		btn_ok.setOnClickListener(btnListener);
		tv_second.setHint("" + sharedPreferences.getInt("protectTime", 120));
	}

	private OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_ok:
				setProtectTime();
				setResult(RESULT_OK);
				finish();
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_right);
				break;

			default:
				break;
			}
		}
	};

	private void setProtectTime() {
		String text = tv_second.getText().toString();
		if (!text.isEmpty()) {
			int seconds = Integer.valueOf(text);
			sharedPreferences.edit().putInt("protectTime", seconds).commit();
		}
	}
}
