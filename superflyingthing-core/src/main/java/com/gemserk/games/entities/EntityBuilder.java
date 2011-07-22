package com.gemserk.games.entities;

import java.util.ArrayList;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;

public class EntityBuilder {

	private final World world;

	private ArrayList<Component> components;

	public EntityBuilder(World world) {
		this.world = world;
		components = new ArrayList<Component>();
		reset();
	}

	private void reset() {
		components.clear();
	}

	public EntityBuilder component(Component component) {
		components.add(component);
		return this;
	}

	public Entity build() {
		Entity e = world.createEntity();
		for (int i = 0; i < components.size(); i++)
			e.addComponent(components.get(i));
		e.refresh();
		reset();
		return e;
	}

}
