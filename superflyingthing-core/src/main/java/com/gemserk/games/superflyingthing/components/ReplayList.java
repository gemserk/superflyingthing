package com.gemserk.games.superflyingthing.components;

import java.util.ArrayList;

public class ReplayList {
	
	private ArrayList<Replay> replays;
	
	public ArrayList<Replay> getReplays() {
		return replays;
	}
	
	public ReplayList() {
		replays = new ArrayList<Replay>();
	}
	
	public void add(Replay replay) {
		replays.add(replay);
	}
	
}
