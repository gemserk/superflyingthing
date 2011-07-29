package com.gemserk.games.superflyingthing.preferences;

import java.util.HashMap;
import java.util.Map;

public class PlayerProfile {

	// private String privateProfileId (from datastore server)

	static class LevelInformation {

		public int time;

		public int stars;

	}

	private int latestLevelPlayed;

	private Map<Integer, LevelInformation> levelsInformation;

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

	public int getTimeForLevel(int level) {
		return levelsInformation.get(level).time;
	}

	public void setTimeForLevel(int level, int time) {
		if (level > latestLevelPlayed)
			latestLevelPlayed = level;

		if (hasPlayedLevel(level)) {
			int previousTime = getTimeForLevel(level);
			if (time < previousTime) {
				LevelInformation levelInformation = levelsInformation.get(level);
				levelInformation.time = time;
				// levelsInformation.put(level, time);
			}
		} else {
			LevelInformation levelInformation = new LevelInformation();
			levelInformation.time = time;
			levelsInformation.put(level, levelInformation);
		}
	}

}
