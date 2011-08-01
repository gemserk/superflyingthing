package com.gemserk.games.superflyingthing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;

public class ControllerSystem extends EntityProcessingSystem {
	
	private ComponentMapper<ControllerComponent> controllerComponentMapper;

	public ControllerSystem() {
		super(ControllerComponent.class);
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		controllerComponentMapper = new ComponentMapper<ControllerComponent>(ControllerComponent.class, world.getEntityManager());
	}

	@Override
	protected void process(Entity e) {
		ControllerComponent controllerComponent = controllerComponentMapper.get(e);
		ShipController controller = controllerComponent.getController();
		controller.update(world, e);
	}

}
