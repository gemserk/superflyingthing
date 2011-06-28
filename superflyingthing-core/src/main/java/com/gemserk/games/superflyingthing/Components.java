package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.entities.Component;
import com.gemserk.games.entities.Entity;

public class Components {
	
	// the real component ->
	
	public static class EntityAttachment {

		Entity entity;

		Joint joint;

	}
	
	// the classes to use to abstract access to the real components :s, similar to Spatial stuff.
	
	public static class SpatialComponent implements Component {

		Spatial spatial;

		public SpatialComponent(Spatial spatial) {
			this.spatial = spatial;
		}

	}

	public static class PhysicsComponent implements Component {

		Body body;

		public PhysicsComponent(Body body) {
			this.body = body;
		}

	}

	public static class CameraComponent implements Component {

		Camera camera;

		public CameraComponent(Camera camera) {
			this.camera = camera;
		}

	}

	public static class SpriteComponent implements Component {

		Sprite sprite;

		public SpriteComponent(Sprite sprite) {
			this.sprite = sprite;
		}

	}

	public static class MovementComponent implements Component {

		final Vector2 direction = new Vector2();

		public MovementComponent(float dx, float dy) {
			direction.set(dx, dy);
		}

	}

	public static class AliveComponent implements Component {

		boolean dead;

		public AliveComponent(boolean dead) {
			this.dead = dead;
		}

	}

	public static class TargetComponent implements Component {

		Entity target;

		public TargetComponent(Entity target) {
			this.target = target;
		}

	}

	public static class AttachmentComponent implements Component {

		EntityAttachment entityAttachment;

		public AttachmentComponent() {
			entityAttachment = new EntityAttachment();
		}

	}

	public static class AttachableComponent implements Component {

		Entity owner;

	}

	public static class ReleaseEntityComponent implements Component {

		int releaseTime;

	}

	public static class GrabbableComponent implements Component {

		boolean grabbed;

	}

}