package com.gemserk.games.superflyingthing.preferences;

import java.util.HashMap;
import java.util.Map;

import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;

public class PlayerProfile {

	// private String privateProfileId (from datastore server)

	public static class LevelInformation {

		public int time;

		public int stars;
		
		public LevelInformation(int time, int stars) {
			this.time = time;
			this.stars = stars;
		}

	}

	private int latestLevelPlayed;

	private Map<Integer, LevelInformation> levelsInformation;
	
	private ControllerType controllerType;
	
	public void setControllerType(ControllerType controllerType) {
		this.controllerType = controllerType;
	}
	
	public ControllerType getControllerType() {
		return controllerType;
	}

	// information to save:
	// - level time
	// - small stars
	// - big stars got (depends on level time)
	// - total deaths
	// - best deaths?

	public PlayerProfile() {
		this.latestLevelPlayed = 0;
		this.levelsInformation = new HashMap<Integer, LevelInformation>();
	}

	public boolean hasPlayedLevel(int level) {
		if (level <= 0)
			return true;
		return levelsInformation.containsKey(level);
	}

	public int getLatestLevelPlayed() {
		return latestLevelPlayed;
	}

	public LevelInformation getLevelInformation(int level) {
		return levelsInformation.get(level);
	}

	public void setLevelInformationForLevel(int level, LevelInformation levelInformation) {
		if (level > latestLevelPlayed)
			latestLevelPlayed = level;

		if (hasPlayedLevel(level)) {
			LevelInformation currentLevelInformation = getLevelInformation(level);
			int previousTime = currentLevelInformation.time;
			if (levelInformation.time < previousTime) 
				currentLevelInformation.time = levelInformation.time;
		} else {
			levelsInformation.put(level, levelInformation);
		}
	}

}
