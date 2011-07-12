package com.gemserk.games.entities;

import java.util.ArrayList;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;

public class EntityManagerImpl implements EntityManager {
	
	static class EntityLifeCycleHandlerNullImpl implements EntityLifeCycleHandler {
		@Override public void init(Entity e) {}
		@Override public void dispose(Entity e) {}
	}

	ArrayList<Entity> entities, entitiesToAdd, entitiesToRemove;

	EntityLifeCycleHandler entityLifeCycleHandler;

	private int delta;
	
	@Override
	public int getDelta() {
		return delta;
	}

	public EntityManagerImpl() {
		this(new EntityLifeCycleHandlerNullImpl());
	}
	
	public EntityManagerImpl(EntityLifeCycleHandler entityLifeCycleHandler) {
		entities = new ArrayList<Entity>();
		entitiesToAdd = new ArrayList<Entity>();
		entitiesToRemove = new ArrayList<Entity>();
		this.entityLifeCycleHandler = entityLifeCycleHandler;
	}

	@Override
	public void add(Entity e) {
		entitiesToAdd.add(e);
	}

	@Override
	public void remove(Entity e) {
		entitiesToRemove.add(e);
	}

	@Override
	public void update(int delta) {
		this.delta = delta;
		updateAdd(delta);
		updateEntities(delta);
		updateRemove(delta);
	}

	private void updateAdd(int delta) {
		for (int i = 0; i < entitiesToAdd.size(); i++) {
			Entity e = entitiesToAdd.get(i);
			entities.add(e);
			// init entity
			entityLifeCycleHandler.init(e);
		}
		entitiesToAdd.clear();
	}

	private void updateEntities(int delta) {
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			updateEntity(delta, e);
		}
	}

	private void updateEntity(int delta, Entity e) {
		ScriptComponent scriptComponent = e.getComponent(ScriptComponent.class);
		if (scriptComponent == null)
			return;
		scriptComponent.getScript().update(null, e);
	}

	private void updateRemove(int delta) {
		for (int i = 0; i < entitiesToRemove.size(); i++) {
			Entity e = entitiesToRemove.get(i);
			entities.remove(e);
			// dispose entity
			entityLifeCycleHandler.dispose(e);
		}
		entitiesToRemove.clear();
	}

	@Override
	public int entitiesCount() {
		return entities.size();
	}

	@Override
	public Entity get(int index) {
		return entities.get(index);
	}

}