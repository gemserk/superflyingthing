package com.gemserk.games.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Entity {

	Map<Class<?>, Object> components;

	ArrayList<Behavior> behaviors;

	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> clazz) {
		return (T) components.get(clazz);
	}

	public Entity() {
		components = new HashMap<Class<?>, Object>();
		behaviors = new ArrayList<Behavior>();
	}

	public void addComponent(Object component) {
		addComponent(component.getClass(), component);
	}

	public void addComponent(Class<?> clazz, Object component) {
		components.put(clazz, component);
	}

	public void addBehavior(Behavior behavior) {
		behaviors.add(behavior);
	}

}