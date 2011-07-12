package com.gemserk.games.entities;

import com.artemis.Entity;

/**
 * Manages all entities of the world.
 * 
 * @author acoppes
 * 
 */
public interface EntityManager {

	/**
	 * Queues an Entity addition for the next update method call.
	 * 
	 * @param e
	 *            the entity to be added.
	 */
	void add(Entity e);

	/**
	 * Queues an Entity removal for the next update() method call.
	 * 
	 * @param e
	 *            the entity to be removed.
	 */
	void remove(Entity e);

	/**
	 * Updates all entities behaviors.
	 * 
	 * @param delta
	 *            the time in ms from the last update call.
	 */
	void update(int delta);

	/**
	 * Returns the entities count.
	 */
	int entitiesCount();

	/**
	 * Returns an entity given an index.
	 */
	Entity get(int index);
	
	/**
	 * Returns time from the last update.
	 */
	int getDelta();
	
}