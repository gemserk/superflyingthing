package com.gemserk.games.superflyingthing;

public class GamePreferences {
	
	// TODO: save using libgdx preeferences...

	private boolean tutorialEnabled;

	public boolean isTutorialEnabled() {
		return tutorialEnabled;
	}

	public void setTutorialEnabled(boolean tutorialEnabled) {
		this.tutorialEnabled = tutorialEnabled;
	}

	public GamePreferences() {
		this.tutorialEnabled = true;
	}
}
