package com.gemserk.games.superflyingthing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidApplication extends com.badlogic.gdx.backends.android.AndroidApplication implements AdWhirlInterface {

	private PausableAdWhirlLayout adView;

	private final int SHOW_ADS = 1;

	private final int HIDE_ADS = 0;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_ADS: {
				adView.onResume();
				break;
			}
			case HIDE_ADS: {
				adView.onPause();
				break;
			}
			}
		}
	};

	private class CustomAdViewHandler implements AdWhirlViewHandler {
		@Override
		public void show() {
			handler.sendEmptyMessage(SHOW_ADS);
		}

		@Override
		public void hide() {
			handler.sendEmptyMessage(HIDE_ADS);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// initialize(new HelloWorld(new CustomAdViewHandler()), false);

		RelativeLayout layout = new RelativeLayout(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		config.useGL20 = false;
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useWakelock = true;

		View gameView = initializeForView(new Game(new CustomAdViewHandler()), config);

		AdWhirlManager.setConfigExpireTimeout(1000 * 15);
		AdWhirlTargeting.setAge(23);
		AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
		AdWhirlTargeting.setKeywords("online games gaming");
		AdWhirlTargeting.setPostalCode("94123");
		AdWhirlTargeting.setTestMode(false);

		adView = new PausableAdWhirlLayout(this, "5d99c9fc499b41e5be30b22e3b52d799");

		int diWidth = 400;
		int diHeight = 32;
		
		float density = getResources().getDisplayMetrics().density;

		adView.setAdWhirlInterface(this);
		adView.setMaxWidth((int) (diWidth * density));
		adView.setMaxHeight((int) (diHeight * density));

		RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

		layout.addView(gameView);
		layout.addView(adView, adParams);

		setContentView(layout);
	}

	@Override
	public void adWhirlGeneric() {

	}

}