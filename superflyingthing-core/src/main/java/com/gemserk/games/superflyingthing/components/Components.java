package com.gemserk.games.superflyingthing.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.graphics.Mesh2d;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.games.superflyingthing.ShipController;

public class Components {

	public static class LabelComponent extends Component {

		public final String label;

		public LabelComponent(String label) {
			this.label = label;
		}

	}

	public static class ControllerComponent extends Component {

		private final ShipController shipControllerImpl;

		public ShipController getController() {
			return shipControllerImpl;
		}

		public ControllerComponent(ShipController controller) {
			this.shipControllerImpl = controller;
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

		public final Vector2 direction = new Vector2();
		public float angularVelocity = 0f;
		private float maxLinearSpeed;
		private float maxAngularVelocity;

		public Vector2 getDirection() {
			return direction;
		}

		public MovementComponent(float dx, float dy, float maxLinearSpeed, float maxAngularVelocity) {
			this.maxLinearSpeed = maxLinearSpeed;
			this.maxAngularVelocity = maxAngularVelocity;
			direction.set(dx, dy);
		}

		public void setMaxAngularVelocity(float maxAngularVelocity) {
			this.maxAngularVelocity = maxAngularVelocity;
		}

		public float getMaxLinearSpeed() {
			return maxLinearSpeed;
		}

		public void setMaxLinearSpeed(float maxLinearSpeed) {
			this.maxLinearSpeed = maxLinearSpeed;
		}

		public float getMaxAngularVelocity() {
			return maxAngularVelocity;
		}

	}

	public static class HealthComponent extends Component {

		private Container health;

		public Container getHealth() {
			return health;
		}

		public HealthComponent(Container health) {
			this.health = health;
		}

	}

	public static class DamageComponent extends Component {

		private float damage;

		public float getDamage() {
			return damage;
		}

		public DamageComponent(float damage) {
			this.damage = damage;
		}

	}

	public static class TargetComponent extends Component {

		public Entity target;

		public void setTarget(Entity target) {
			this.target = target;
		}

		public TargetComponent(Entity target) {
			this.target = target;
		}

	}

	public static class AttachmentComponent extends Component {

		public Entity entity;

		public Joint joint;

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

		public Entity owner;

		public void setOwner(Entity owner) {
			this.owner = owner;
		}

		public Entity getOwner() {
			return owner;
		}

	}

	public static class GrabbableComponent extends Component {

		public boolean grabbed;

	}

	public static class GameData {

		public int deaths;
		public int totalItems;
		public int currentItems;
		public float time;
		public int averageFPS;
		
		/**
		 * Time to travel from source planet to destination planet
		 */
		public float travelTime;

		public GameData() {
			this.deaths = 0;
			this.currentItems = 0;
			this.totalItems = 0;
			this.time = 0;
			this.averageFPS = 0;
			this.travelTime = 0f;
		}

	}
	
	public static class GameDataComponent extends Component {

		public Entity ship;
		public Entity attachedShip;

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

	public static class WeaponComponent extends Component {

		private float fireRate;
		private float reloadTime;
		private float bulletDuration;
		private EntityTemplate bulletTemplate;

		public void setFireRate(int fireRate) {
			this.fireRate = fireRate;
		}

		public float getReloadTime() {
			return reloadTime;
		}

		public float getFireRate() {
			return fireRate;
		}

		public float getBulletDuration() {
			return bulletDuration;
		}

		public void setReloadTime(float reloadTime) {
			this.reloadTime = reloadTime;
		}

		public EntityTemplate getBulletTemplate() {
			return bulletTemplate;
		}

		public WeaponComponent(int fireRate, int bulletDuration, int currentReloadTime, EntityTemplate bulletTemplate) {
			this((float) fireRate * 0.001f, (float) bulletDuration * 0.001f, (float) currentReloadTime * 0.001f, bulletTemplate);
		}

		public WeaponComponent(float fireRate, float bulletDuration, float currentReloadTime, EntityTemplate bulletTemplate) {
			this.fireRate = fireRate;
			this.bulletDuration = bulletDuration;
			this.reloadTime = currentReloadTime;
			this.bulletTemplate = bulletTemplate;
		}

	}

	public static class PortalComponent extends Component {

		private String targetPortalId;
		private float outAngle;

		public String getTargetPortalId() {
			return targetPortalId;
		}

		public float getOutAngle() {
			return outAngle;
		}

		public PortalComponent(String targetPortalId, float outAngle) {
			this.targetPortalId = targetPortalId;
			this.outAngle = outAngle;
		}

	}

	public static class ReplayListComponent extends Component {

		private final ReplayList replayList;

		public ReplayList getReplayList() {
			return replayList;
		}

		public ReplayListComponent(ReplayList replayList) {
			this.replayList = replayList;
		}

	}

	public static class ReplayComponent extends Component {

		public final Replay replay;

		public ReplayComponent(Replay replay) {
			this.replay = replay;
		}

	}

}