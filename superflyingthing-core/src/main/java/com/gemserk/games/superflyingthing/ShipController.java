package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.math.Vector2;

public class ShipController {
	
	private boolean shouldReleaseShip;
	private Vector2 position = new Vector2();
	private float movementDirection = 0f;
	
	public void setShouldReleaseShip(boolean shouldReleaseShip) {
		this.shouldReleaseShip = shouldReleaseShip;
	}

	public boolean shouldReleaseShip() {
		return shouldReleaseShip;
	}
	
	public void setMovementDirection(float movementDirection) {
		this.movementDirection = movementDirection;
	}

	/**
	 * A value between -1 and 1 to determine ship angular velocity.
	 */
	public float getMovementDirection() {
		return movementDirection;
	}
	
}