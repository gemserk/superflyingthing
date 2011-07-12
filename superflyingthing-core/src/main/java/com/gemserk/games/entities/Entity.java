package com.gemserk.games.entities;

import java.util.HashMap;
import java.util.Map;

public class Entity {

	Map<String, Object> components;

	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> clazz) {
		return (T) components.get(clazz.getName());
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getComponent(String name) {
		return (T) components.get(name);
	}
	
	public Entity() {
		components = new HashMap<String, Object>();
	}

	public void addComponent(Object component) {
		addComponent(component.getClass(), component);
	}

	public void addComponent(Class<?> clazz, Object component) {
		addComponent(clazz.getName(), component);
	}

	public void addComponent(String name, Object component) {
		components.put(name, component);
	}

}