package com.gemserk.games.superflyingthing;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gemserk.games.superflyingthing.Game;

public class AndroidApplication extends com.badlogic.gdx.backends.android.AndroidApplication {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = false;
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useWakelock = true;
		initialize(new Game(), config);
	}
	
}