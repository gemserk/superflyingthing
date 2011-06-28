package com.gemserk.games.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Entity {

	Map<Class<? extends Component>, Component> components;

	ArrayList<Behavior> behaviors;
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent(Class<T> clazz) {
		return (T) components.get(clazz);
	}

	public Entity() {
		components = new HashMap<Class<? extends Component>, Component>();
		behaviors = new ArrayList<Behavior>();
	}

	public void addComponent(Component component) {
		addComponent(component.getClass(), component);
	}

	public void addComponent(Class<? extends Component> clazz, Component component) {
		components.put(clazz, component);
	}

	public void addBehavior(Behavior behavior) {
		behaviors.add(behavior);
	}
	
}