package com.gemserk.games.discovertheway;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidApplication extends com.badlogic.gdx.backends.android.AndroidApplication {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = false;
		config.useAccelerometer = true;
		config.useCompass = true;
		config.useWakelock = true;
		initialize(new Game(), config);
	}
	
}