package com.gemserk.games.entities;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;

public class EntityBuilder {
	
	private Entity e;
	private final World world;

	public EntityBuilder(World world) {
		this.world = world;
		reset();
	}
	
	private void reset() {
		e = world.createEntity();
	}
	
	public EntityBuilder component(Component component) {
		e.addComponent(component);
		return this;
	}

	public Entity build() {
		Entity newEntity = e;
		newEntity.refresh();
		reset();
		return newEntity;
	}

}
