package com.gemserk.games.superflyingthing;

import com.artemis.Component;
import com.artemis.Entity;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.CameraComponent;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;
import com.gemserk.games.superflyingthing.Components.TriggerComponent;

public class ComponentWrapper {
	
	public static Physics getPhysics(Entity e) {
		PhysicsComponent physicsComponent = getComponent(e, PhysicsComponent.class);
		return physicsComponent.getPhysics();
	}

	public static Spatial getSpatial(Entity e) {
		SpatialComponent spatialComponent = getComponent(e, SpatialComponent.class);
		return spatialComponent.getSpatial();
	}

	public static SpriteComponent getSprite(Entity e) {
		return getComponent(e, SpriteComponent.class);
	}
	
	public static TriggerComponent getTriggers(Entity e) {
		return getComponent(e, TriggerComponent.class);
	}

	public static Camera getCamera(Entity e) {
		CameraComponent component = getComponent(e, CameraComponent.class);
		if (component == null)
			return null;
		return component.getCamera();
	}

	public static AttachmentComponent getEntityAttachment(Entity e) {
		return getComponent(e, AttachmentComponent.class);
	}
	
	public static MovementComponent getMovementComponent(Entity e) {
		return getComponent(e, MovementComponent.class);
	}
	
	public static void addMovementComponent(Entity e, MovementComponent movementComponent) {
		e.addComponent(movementComponent);
	}


	static <T> T getComponent(Entity e, Class<? extends Component> clazz) {
		if (e == null)
			return null;
		return (T) e.getComponent(clazz);
	}

	public static GameDataComponent getGameData(Entity e) {
		return e.getComponent(GameDataComponent.class);
	}

}