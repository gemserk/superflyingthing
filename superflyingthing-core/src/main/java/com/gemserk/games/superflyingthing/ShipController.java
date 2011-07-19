package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.controllers.Controller;

public interface ShipController extends Controller {

	boolean isDown();

	Vector2 getPosition();

	/**
	 * A value between -1 and 1 to determine ship angular velocity.
	 */
	float getMovementDirection();

}