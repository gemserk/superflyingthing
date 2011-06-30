package com.gemserk.games.entities;

public class EntityBuilder {
	
	private Entity e;

	public EntityBuilder() {
		reset();
	}
	
	private void reset() {
		e = new Entity();
	}
	
	public EntityBuilder component(Object component) {
		e.addComponent(component);
		return this;
	}

	public EntityBuilder component(String name, Object component) {
		e.addComponent(name, component);
		return this;
	}
	
	public EntityBuilder behavior(Behavior behavior) {
		e.addBehavior(behavior);
		return this;
	}

	public Entity build() {
		Entity newEntity = e;
		reset();
		return newEntity;
	}

}
