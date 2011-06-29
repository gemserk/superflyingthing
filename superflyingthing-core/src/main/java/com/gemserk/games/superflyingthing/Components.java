package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.games.entities.Entity;

public class Components {

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
		
		private Color color;
		
		public void setSprite(Sprite sprite) {
			this.sprite = sprite;
		}

		public Sprite getSprite() {
			return sprite;
		}
		
		public Color getColor() {
			return color;
		}

		public SpriteComponent(Sprite sprite) {
			setSprite(sprite);
			this.color = new Color(1f, 1f, 1f, 1f);
		}
		
		public SpriteComponent(Sprite sprite, Color color) { 
			setSprite(sprite);
			this.color = new Color(color);
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