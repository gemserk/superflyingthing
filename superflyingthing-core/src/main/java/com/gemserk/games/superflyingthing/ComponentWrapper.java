package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.CameraComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;

public class ComponentWrapper {
	
	public static Physics getPhysics(Entity e) {
		return getComponent(e, Physics.class);
	}

	public static Spatial getSpatial(Entity e) {
		return getComponent(e, Spatial.class);
	}

	public static Sprite getSprite(Entity e) {
		SpriteComponent component = getComponent(e, SpriteComponent.class);
		if (component == null)
			return null;
		return component.getSprite();
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

	@SuppressWarnings("unchecked")
	static <T> T getComponent(Entity e, Class<?> clazz) {
		if (e == null)
			return null;
		return (T) e.getComponent(clazz);
	}

}