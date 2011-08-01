package com.gemserk.games.superflyingthing;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;

public interface ShipController {

	boolean shouldReleaseShip();

	Vector2 getPosition();

	/**
	 * A value between -1 and 1 to determine ship angular velocity.
	 */
	float getMovementDirection();
	
	void setEnabled(boolean enabled);
	
	void update(World world, Entity e);

}