package com.gemserk.games.superflyingthing.components;

import java.util.ArrayList;

public class Replay {

	public static class ReplayEntry {

		/**
		 * Used probably for interpolations.
		 */
		public int time;

		public float x, y;
		public float angle;

		public ReplayEntry(int time, float x, float y, float angle) {
			this.time = time;
			this.x = x;
			this.y = y;
			this.angle = angle;
		}

	}

	public ArrayList<ReplayEntry> replayEntries = new ArrayList<ReplayEntry>();

	public ReplayEntry getEntry(int i) {
		return replayEntries.get(i);
	}
	
	public int getEntriesCount() {
		return replayEntries.size();
	}

}