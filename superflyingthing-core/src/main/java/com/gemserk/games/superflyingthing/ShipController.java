package com.gemserk.games.superflyingthing;


public class ShipController {
	
	private boolean shouldReleaseShip;
	private float movementDirection = 0f;
	
	public void setShouldReleaseShip(boolean shouldReleaseShip) {
		this.shouldReleaseShip = shouldReleaseShip;
	}

	public boolean shouldReleaseShip() {
		return shouldReleaseShip;
	}
	
	public void setMovementDirection(float movementDirection) {
		this.movementDirection = movementDirection;
		if (movementDirection > 1f)
			movementDirection = 1f;
		else if (movementDirection < -1f)
			movementDirection = -1f;
	}

	/**
	 * A value between -1 and 1 to determine ship angular velocity.
	 */
	public float getMovementDirection() {
		return movementDirection;
	}
	
}