package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.entities.Entity;

public class Components {

	// the real component ->

	public static class EntityAttachment {

		Entity entity;

		Joint joint;

		public Entity getEntity() {
			return entity;
		}

		public Joint getJoint() {
			return joint;
		}

		public void setEntity(Entity entity) {
			this.entity = entity;
		}

		public void setJoint(Joint joint) {
			this.joint = joint;
		}

	}

	// the classes to use to abstract access to the real components :s, similar to Spatial stuff.

	public static class SpatialComponent {

		Spatial spatial;

		public SpatialComponent(Spatial spatial) {
			this.spatial = spatial;
		}

	}

	public static class PhysicsComponent {

		Physics physics;

		public Physics getPhysics() {
			return physics;
		}

		public PhysicsComponent(Body body) {
			physics = new PhysicsImpl(body);
		}

	}

	public static class CameraComponent {

		private Camera camera;

		public void setCamera(Camera camera) {
			this.camera = camera;
		}

		public Camera getCamera() {
			return camera;
		}

		public CameraComponent(Camera camera) {
			setCamera(camera);
		}

	}

	public static class SpriteComponent {

		private Sprite sprite;

		public void setSprite(Sprite sprite) {
			this.sprite = sprite;
		}

		public Sprite getSprite() {
			return sprite;
		}

		public SpriteComponent(Sprite sprite) {
			setSprite(sprite);
		}

	}

	public static class MovementComponent {

		final Vector2 direction = new Vector2();

		float angularVelocity = 0f;

		public Vector2 getDirection() {
			return direction;
		}

		public MovementComponent(float dx, float dy) {
			direction.set(dx, dy);
		}

	}

	public static class AliveComponent {

		boolean dead;

		public boolean isDead() {
			return dead;
		}

		public AliveComponent(boolean dead) {
			this.dead = dead;
		}

	}

	public static class TargetComponent {

		Entity target;

		public void setTarget(Entity target) {
			this.target = target;
		}

		public TargetComponent(Entity target) {
			this.target = target;
		}

	}

	public static class AttachmentComponent {

		EntityAttachment entityAttachment;

		public EntityAttachment getEntityAttachment() {
			return entityAttachment;
		}

		public AttachmentComponent() {
			entityAttachment = new EntityAttachment();
		}

	}

	public static class AttachableComponent {

		Entity owner;

		public Entity getOwner() {
			return owner;
		}

	}

	public static class ReleaseEntityComponent {

		int releaseTime;

	}

	public static class GrabbableComponent {

		boolean grabbed;

	}

	public static class InputDirectionComponent {

		float direction;

	}

}