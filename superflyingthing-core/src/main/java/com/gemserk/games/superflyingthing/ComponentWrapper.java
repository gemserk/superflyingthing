package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.CameraComponent;
import com.gemserk.games.superflyingthing.Components.EntityAttachment;
import com.gemserk.games.superflyingthing.Components.PhysicsComponent;
import com.gemserk.games.superflyingthing.Components.SpatialComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;

public class ComponentWrapper {
	
	public static Physics getPhysics(Entity e) {
		PhysicsComponent component = getComponent(e, PhysicsComponent.class);
		if (component == null)
			return null;
		return component.getPhysics();
	}

	public static Spatial getSpatial(Entity e) {
		SpatialComponent component = getComponent(e, SpatialComponent.class);
		if (component == null)
			return null;
		return component.spatial;
	}

	public static Sprite getSprite(Entity e) {
		SpriteComponent component = getComponent(e, SpriteComponent.class);
		if (component == null)
			return null;
		return component.sprite;
	}

	public static Camera getCamera(Entity e) {
		CameraComponent component = getComponent(e, CameraComponent.class);
		if (component == null)
			return null;
		return component.camera;
	}

	public static EntityAttachment getEntityAttachment(Entity e) {
		AttachmentComponent component = getComponent(e, AttachmentComponent.class);
		if (component == null)
			return null;
		return component.entityAttachment;
	}

	@SuppressWarnings("unchecked")
	static <T> T getComponent(Entity e, Class<?> clazz) {
		if (e == null)
			return null;
		return (T) e.getComponent(clazz);
	}

}