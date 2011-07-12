package com.gemserk.games.superflyingthing;

import com.artemis.Entity;

public class Trigger {

	private boolean triggered = false;

	/**
	 * Call it if you want the trigger to be called only once.
	 */
	protected void triggered() {
		this.triggered = true;
	}

	public void trigger(Entity e) {
		if (triggered)
			return;
		onTrigger(e);
	}

	protected void onTrigger(Entity e) {};

}