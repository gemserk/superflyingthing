package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GamePreferences {
	
	private final Preferences preferences;

	public boolean isTutorialEnabled() {
		return preferences.getBoolean("tutorialEnabled", true);
	}

	public void setTutorialEnabled(boolean tutorialEnabled) {
		preferences.putBoolean("tutorialEnabled", tutorialEnabled);
		preferences.flush();
		Gdx.app.log("SuperFlyingThing", "Saving preference tutorialEnabled: " + tutorialEnabled);
	}

	public GamePreferences(Preferences preferences) {
		this.preferences = preferences;
	}
}
