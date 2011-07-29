package com.gemserk.games.superflyingthing;

import java.util.HashMap;
import java.util.Map;

public class PlayerProfile {
	
	// private String privateProfileId (from datastore server)
	
	private int latestLevelPlayed;
	
	private Map<Integer, Integer> levelsInformation;
	
	public PlayerProfile() {
		this.latestLevelPlayed = 0;
		this.levelsInformation = new HashMap<Integer, Integer>();
	}
	
	public boolean hasPlayedLevel(int level) {
		return levelsInformation.containsKey(level);
	}
	
	public int getLatestLevelPlayed() {
		return latestLevelPlayed;
	}
	
	public int getTimeForLevel(int level) {
		return levelsInformation.get(level);
	}
	
	public void setTimeForLevel(int level, int time) {
		if (level > latestLevelPlayed)
			latestLevelPlayed = level;
		
		if (hasPlayedLevel(level)) {
			int previousTime = getTimeForLevel(level);
			if (time < previousTime)
				levelsInformation.put(level, time);
		} else
			levelsInformation.put(level, time);
	}

}
