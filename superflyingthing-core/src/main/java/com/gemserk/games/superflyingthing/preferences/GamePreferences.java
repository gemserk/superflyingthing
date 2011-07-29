package com.gemserk.games.superflyingthing.preferences;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GamePreferences {

	private final Preferences preferences;

	private PlayerProfile playerProfile;

	private PlayerProfileJsonSerializer playerProfileJsonSerializer;

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
		this.playerProfile = null;
		this.playerProfileJsonSerializer = new PlayerProfileJsonSerializer();
	}

	public PlayerProfile getCurrentPlayerProfile() {
		if (playerProfile == null) {
			String serializedProfile = preferences.getString("currentPlayerProfile");
			if (!"".equals(serializedProfile))
				playerProfile = playerProfileJsonSerializer.parse(serializedProfile);
			else
				playerProfile = new PlayerProfile();
		}
		return playerProfile;
	}

	public void updatePlayerProfile(PlayerProfile playerProfile) {
		String serializedProfile = playerProfileJsonSerializer.serialize(playerProfile);
		preferences.putString("currentPlayerProfile", serializedProfile);
		preferences.flush();
		Gdx.app.log("SuperFlyingThing", "Saving current player profile information");
	}

}