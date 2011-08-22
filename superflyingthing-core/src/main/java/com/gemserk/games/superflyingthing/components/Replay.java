package com.gemserk.games.superflyingthing.components;

import java.util.ArrayList;

public class Replay {

	public static class ReplayEntry {

		/**
		 * Used probably for interpolations.
		 */
		public int time;

		public float x, y;
		public int angle;

		public ReplayEntry(int time, float x, float y, int angle) {
			this.time = time;
			this.x = x;
			this.y = y;
			this.angle = angle;
		}

	}

	private ArrayList<ReplayEntry> replayEntries = new ArrayList<ReplayEntry>();
	public int duration;
	public boolean main;

	public ReplayEntry getEntry(int i) {
		return replayEntries.get(i);
	}
	
	public int getEntriesCount() {
		return replayEntries.size();
	}
	
	public void add(ReplayEntry replayEntry) {
		duration = Math.max(replayEntry.time, duration);
		replayEntries.add(replayEntry);
	}
	

}