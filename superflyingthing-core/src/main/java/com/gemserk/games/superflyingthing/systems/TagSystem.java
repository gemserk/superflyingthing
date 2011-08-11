package com.gemserk.games.superflyingthing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.gemserk.games.superflyingthing.components.TagComponent;

public class TagSystem extends EntityProcessingSystem {

	private ComponentMapper<TagComponent> tagComponentMapper;

	public TagSystem() {
		super(TagComponent.class);
	}

	@Override
	protected void initialize() {
		super.initialize();
		tagComponentMapper = new ComponentMapper<TagComponent>(TagComponent.class, world.getEntityManager());
	}

	@Override
	protected void added(Entity e) {
		super.added(e);
		TagComponent tagComponent = tagComponentMapper.get(e);
		world.getTagManager().register(tagComponent.getTag(), e);
	}

	@Override
	protected void removed(Entity e) {
		super.removed(e);
		TagComponent tagComponent = tagComponentMapper.get(e);
		Entity entityWithTag = world.getTagManager().getEntity(tagComponent.getTag());
		if (entityWithTag == null)
			return;
		if (entityWithTag != e)
			return;
		world.getTagManager().unregister(tagComponent.getTag());
	}

	@Override
	protected void process(Entity e) {

	}

}
