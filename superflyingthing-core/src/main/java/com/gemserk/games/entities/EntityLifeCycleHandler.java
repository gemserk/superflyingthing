package com.gemserk.games.entities;

import com.artemis.Entity;

public interface EntityLifeCycleHandler {

	/**
	 * Called after the entity was added to the world.
	 * 
	 * @param e
	 *            The entity added to the world.
	 */
	void init(Entity e);

	/**
	 * Called after the entity was removed from the world.
	 * 
	 * @param e
	 *            The entity removed from the world.
	 */
	void dispose(Entity e);

}