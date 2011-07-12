package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.graphics.Triangulator;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityManager;

public class Components {
	
	public static interface Script {
		
		void init(EntityManager world, Entity e);
		
		void update(EntityManager world, Entity e);
		
		void dispose(EntityManager world, Entity e);
		
	}
	
	public static class ScriptJavaImpl implements Script {
		
		public void init(EntityManager world, Entity e) {
			
		}
		
		public void update(EntityManager world, Entity e) {
			
		}
		
		public void dispose(EntityManager world, Entity e) {
			
		}
		
	}
	
	public static class ScriptComponent {
		
		private final Script script;
		
		public Script getScript() {
			return script;
		}

		public ScriptComponent(Script script) {
			this.script = script;
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
	
	public static class ShapeComponent {

		private final Vector2[] vertices;
		
		public Triangulator triangulator;
		
		public Color color;
		
		public Vector2[] getVertices() {
			return vertices;
		}
		
		public ShapeComponent(Vector2[] vertices, Color color) {
			this.vertices = vertices;
			this.color = color;
		}

		public ShapeComponent(Vector2[] vertices, Color color, Triangulator triangulator) {
			this.vertices = vertices;
			this.color = color;
			this.triangulator = triangulator;
		}

	}

	public static class MovementComponent {

		final Vector2 direction = new Vector2();
		float angularVelocity = 0f;
		float maxLinearSpeed = 5f;
		private float maxAngularVelocity = 300f;
		private float minAngularVelocity = 200f;
		private float angularAcceleration = 0.7f;

		public Vector2 getDirection() {
			return direction;
		}

		public MovementComponent(float dx, float dy) {
			direction.set(dx, dy);
		}

		public void setAngularAcceleration(float angularAcceleration) {
			this.angularAcceleration = angularAcceleration;
		}

		public float getAngularAcceleration() {
			return angularAcceleration;
		}

		public void setMinAngularVelocity(float minAngularVelocity) {
			this.minAngularVelocity = minAngularVelocity;
		}

		public float getMinAngularVelocity() {
			return minAngularVelocity;
		}

		public void setMaxAngularVelocity(float maxAngularVelocity) {
			this.maxAngularVelocity = maxAngularVelocity;
		}

		public float getMaxAngularVelocity() {
			return maxAngularVelocity;
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

	public static class ShipControllerComponent {

		float direction;

	}

	public static class GameDataComponent {

		public Entity ship;
		public Entity startPlanet;
		public Entity camera;

		public GameDataComponent(Entity ship, Entity startPlanet, Entity camera) {
			this.ship = ship;
			this.startPlanet = startPlanet;
			this.camera = camera;
		}

	}

}