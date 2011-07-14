package com.gemserk.games.superflyingthing;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.graphics.Triangulator;

public class Components {
	
	public static class TriggerComponent extends Component {
		
		private Map<String, Trigger> triggers;

		public Trigger getTrigger(String name) {
			return triggers.get(name);
		}
		
		public TriggerComponent() {
			this(new HashMap<String, Trigger>());
		}
		
		public TriggerComponent(Map<String, Trigger> triggers) {
			this.triggers = triggers;
		}
		
		
	}

	public static class CameraComponent extends Component {

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

	public static class ShapeComponent extends Component {

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

	public static class MovementComponent extends Component {

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

	public static class AliveComponent extends Component {

		boolean dead;

		public boolean isDead() {
			return dead;
		}

		public AliveComponent(boolean dead) {
			this.dead = dead;
		}

	}

	public static class TargetComponent extends Component {

		Entity target;

		public void setTarget(Entity target) {
			this.target = target;
		}

		public TargetComponent(Entity target) {
			this.target = target;
		}

	}

	public static class AttachmentComponent extends Component {

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

	public static class AttachableComponent extends Component {

		Entity owner;

		public Entity getOwner() {
			return owner;
		}

	}

	public static class ReleaseEntityComponent extends Component {

		int releaseTime;

	}

	public static class GrabbableComponent extends Component {

		boolean grabbed;

	}

	public static class ShipControllerComponent extends Component {

		float direction;

	}
	
	public static class GameData {
		
		public int deaths;
		
		public int totalItems;
		
		public int currentItems;
		
		public GameData() {
			this.deaths = 0;
			this.currentItems = 0;
			this.totalItems = 0;
		}
		
	}

	public static class GameDataComponent extends Component {

		public Entity ship;
		public Entity startPlanet;
		public Entity camera;

		public GameDataComponent(Entity ship, Entity startPlanet, Entity camera) {
			this.ship = ship;
			this.startPlanet = startPlanet;
			this.camera = camera;
		}

	}
	
	public static class ParticleEmitterComponent extends Component {
		
		private final ParticleEmitter particleEmitter;
		private final float scale;
		
		public ParticleEmitter getParticleEmitter() {
			return particleEmitter;
		}
		
		public float getScale() {
			return scale;
		}
		
		public ParticleEmitterComponent(ParticleEmitter particleEmitter, float scale) {
			this.particleEmitter = particleEmitter;
			this.scale = scale;
		}
		
	}

}