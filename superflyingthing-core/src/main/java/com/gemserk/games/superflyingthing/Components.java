package com.gemserk.games.superflyingthing;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.graphics.Mesh2d;

public class Components {
	
	public static class AnimationComponent extends Component {

		private final Animation[] animations;
		
		private int currentAnimation;
		
		public Animation getCurrentAnimation() {
			return animations[currentAnimation];
		}
		
		public void setCurrentAnimation(int currentAnimation) {
			this.currentAnimation = currentAnimation;
		}

		public AnimationComponent(Animation[] spriteSheets) {
			this.animations = spriteSheets;
		}
		
	}
	
	public static class ControllerComponent extends Component {

		private final ShipController shipControllerImpl;

		public ShipController getController() {
			return shipControllerImpl;
		}

		public ControllerComponent(ShipController shipControllerImpl) {
			this.shipControllerImpl = shipControllerImpl;
		}

	}

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

		private final Libgdx2dCamera libgdx2dCamera;
		private Camera camera;

		public void setCamera(Camera camera) {
			this.camera = camera;
		}

		public Camera getCamera() {
			return camera;
		}

		public Libgdx2dCamera getLibgdx2dCamera() {
			return libgdx2dCamera;
		}

		public CameraComponent(Camera camera, Libgdx2dCamera libgdx2dCamera) {
			this.libgdx2dCamera = libgdx2dCamera;
			setCamera(camera);
		}

	}

	public static class ShapeComponent extends Component {

		public Mesh2d mesh2d;

		public Texture texture;

		public ShapeComponent(Mesh2d mesh2d, Texture texture) {
			this.mesh2d = mesh2d;
			this.texture = texture;
		}

		public ShapeComponent(Mesh2d mesh2d) {
			this(mesh2d, null);
		}

	}

	public static class MovementComponent extends Component {

		final Vector2 direction = new Vector2();
		float angularVelocity = 0f;
		float maxLinearSpeed = 5f;
		private float maxAngularVelocity = 300f;

		public Vector2 getDirection() {
			return direction;
		}

		public MovementComponent(float dx, float dy) {
			direction.set(dx, dy);
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
		
		public void setOwner(Entity owner) {
			this.owner = owner;
		}

		public Entity getOwner() {
			return owner;
		}

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
		public Entity attachedShip;
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

		public ParticleEmitter getParticleEmitter() {
			return particleEmitter;
		}

		public ParticleEmitterComponent(ParticleEmitter particleEmitter) {
			this.particleEmitter = particleEmitter;
		}

	}

}