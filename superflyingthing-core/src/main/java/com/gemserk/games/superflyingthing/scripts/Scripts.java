package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.animation4j.timeline.Builders;
import com.gemserk.animation4j.timeline.TimelineAnimation;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.box2d.Contact;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.AliveComponent;
import com.gemserk.games.superflyingthing.components.Components.AnimationComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.PortalComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.Components.TimerComponent;
import com.gemserk.games.superflyingthing.components.Components.WeaponComponent;
import com.gemserk.games.superflyingthing.scripts.Behaviors.FixCameraTargetBehavior;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;

public class Scripts {

	public static class CameraScript extends ScriptJavaImpl {

		private final EventManager eventManager;

		float startX;
		float startY;

		TimeTransition timeTransition = new TimeTransition();

		boolean movingToTarget = false;

		public CameraScript(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			Spatial spatial = ComponentWrapper.getSpatial(e);
			startX = spatial.getX();
			startY = spatial.getY();
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			updatePosition(world, e);

			Spatial spatial = ComponentWrapper.getSpatial(e);
			CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(e);
			Camera camera = cameraComponent.getCamera();
			camera.setPosition(spatial.getX(), spatial.getY());

			Libgdx2dCamera libgdxCamera = cameraComponent.getLibgdx2dCamera();

			libgdxCamera.move(camera.getX(), camera.getY());
			libgdxCamera.zoom(camera.getZoom());
			libgdxCamera.rotate(camera.getAngle());
		}

		private void updatePosition(com.artemis.World world, Entity e) {
			TargetComponent targetComponent = e.getComponent(TargetComponent.class);
			Entity target = targetComponent.target;

			if (target == null)
				return;

			Spatial targetSpatial = ComponentWrapper.getSpatial(target);
			if (targetSpatial == null)
				return;
			Spatial spatial = ComponentWrapper.getSpatial(e);

			timeTransition.update(world.getDelta());

			if (!timeTransition.isFinished()) {
				float x = FloatInterpolator.interpolate(startX, targetSpatial.getX(), timeTransition.get());
				float y = FloatInterpolator.interpolate(startY, targetSpatial.getY(), timeTransition.get());
				spatial.setPosition(x, y);
			} else {

				if (movingToTarget) {
					eventManager.registerEvent(Events.cameraReachedTarget, e);
					movingToTarget = false;
				}

				Event event = eventManager.getEvent(Events.moveCameraToPlanet);
				if (event != null) {
					startX = spatial.getX();
					startY = spatial.getY();
					timeTransition.start(800);
					movingToTarget = true;
					eventManager.handled(event);
				} else {
					spatial.set(targetSpatial);
				}

			}

		}
	}

	public static class ShipScript extends ScriptJavaImpl {

		Behavior fixMovementBehavior = new Behaviors.FixMovementBehavior();
		Behavior fixDirectionFromControllerBehavior = new Behaviors.FixDirectionFromControllerBehavior();
		Behavior calculateInputDirectionBehavior = new Behaviors.CalculateInputDirectionBehavior();
		Behavior collisionHandlerBehavior = new Behaviors.CollisionHandlerBehavior();
		Behavior updateSpriteFromAnimation = new Behaviors.UpdateSpriteFromAnimation();

		@Override
		public void update(com.artemis.World world, Entity e) {
			fixMovementBehavior.update(world, e);
			fixDirectionFromControllerBehavior.update(world, e);
			calculateInputDirectionBehavior.update(world, e);
			collisionHandlerBehavior.update(world, e);
			updateSpriteFromAnimation.update(world, e);
		}

	}

	public static class AttachedShipScript extends ScriptJavaImpl {

		Behavior fixMovementBehavior = new Behaviors.FixMovementBehavior();
		Behavior updateSpriteFromAnimation = new Behaviors.UpdateSpriteFromAnimation();

		@Override
		public void update(com.artemis.World world, Entity e) {
			fixMovementBehavior.update(world, e);
			updateSpriteFromAnimation.update(world, e);
		}

	}

	public static class StarScript extends ScriptJavaImpl {

		private final EventManager eventManager;

		Behavior removeWhenGrabbedBehavior;

		float rotationSpeed = 0.3f;
		float angle = 0f;

		public StarScript(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			removeWhenGrabbedBehavior = new Behaviors.RemoveWhenGrabbedBehavior();
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			removeWhenGrabbedBehavior.update(world, e);
			updateGrabbable(e);
			updateAnimation(world, e);
		}

		public void updateAnimation(com.artemis.World world, Entity e) {
			AnimationComponent animationComponent = ComponentWrapper.getAnimation(e);
			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);

			angle += rotationSpeed * (float) world.getDelta();

			Animation animation = animationComponent.getCurrentAnimation();
			int frameIndex = getAnimationForAngle(angle - 5f);

			Sprite frame = animation.getFrame(frameIndex);
			spriteComponent.setSprite(frame);
		}

		private int getAnimationForAngle(float angle) {
			if (angle < 360f)
				angle += 360f;
			angle %= 360f;
			double floor = Math.floor(angle * 0.1f);
			return (int) (floor);
		}

		private void updateGrabbable(Entity e) {
			GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
			if (!grabbableComponent.grabbed)
				return;

			Gdx.app.log("SuperFlyingThing", "Registering event for item taken");
			eventManager.registerEvent(Events.itemTaken, e);
		}
	}

	public static class StartPlanetScript extends ScriptJavaImpl {

		private final EventManager eventManager;
		private final World physicsWorld;

		Behavior attachEntityBehavior;
		Behavior calculateInputDirectionBehavior;

		boolean enabled = true;

		public StartPlanetScript(World physicsWorld, JointBuilder jointBuilder, EventManager eventManager) {
			this.physicsWorld = physicsWorld;
			this.eventManager = eventManager;
			attachEntityBehavior = new Behaviors.AttachEntityBehavior(jointBuilder);
			calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionBehavior();
		}

		@Override
		public void update(com.artemis.World world, Entity e) {

			Event event = eventManager.getEvent(Events.disablePlanetReleaseShip);
			if (event != null) {
				enabled = false;
				eventManager.handled(event);
				Gdx.app.log("SuperFlyingShip", "Release ship from planet disabled");
			}

			event = eventManager.getEvent(Events.enablePlanetReleaseShip);
			if (event != null) {
				enabled = true;
				eventManager.handled(event);
				Gdx.app.log("SuperFlyingShip", "Release ship from planet enabled");
			}

			updateReleaseAttachment(world, e);
			attachEntityBehavior.update(world, e);
			calculateInputDirectionBehavior.update(world, e);
		}

		private void updateReleaseAttachment(com.artemis.World world, Entity e) {
			if (!enabled)
				return;

			AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);
			Entity attachedEntity = entityAttachment.entity;

			if (attachedEntity == null)
				return;

			if (!shouldReleaseShip(world, e))
				return;

			if (entityAttachment.joint != null) {
				Gdx.app.log("SuperFlyingThing", "Destroying planet joint");
				physicsWorld.destroyJoint(entityAttachment.joint);
			}

			AttachableComponent attachableComponent = attachedEntity.getComponent(AttachableComponent.class);
			attachableComponent.owner = null;

			entityAttachment.joint = null;
			entityAttachment.entity = null;
		}

		private boolean shouldReleaseShip(com.artemis.World world, Entity e) {
			ControllerComponent controllerComponent = ComponentWrapper.getControllerComponent(e);
			ShipController shipController = controllerComponent.getController();
			return shipController.shouldReleaseShip();
		}

	}

	public static class DestinationPlanetScript extends ScriptJavaImpl {
		private final EventManager eventManager;

		Behavior attachEntityBehavior;
		Behavior calculateInputDirectionBehavior;

		boolean destinationReached;

		public DestinationPlanetScript(EventManager eventManager, JointBuilder jointBuilder) {
			this.eventManager = eventManager;
			this.attachEntityBehavior = new Behaviors.AttachEntityBehavior(jointBuilder);
			calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionBehavior();
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			destinationReached = false;
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			attachEntityBehavior.update(world, e);
			calculateInputDirectionBehavior.update(world, e);

			AttachmentComponent attachmentComponent = ComponentWrapper.getEntityAttachment(e);
			if (attachmentComponent.entity == null)
				return;

			if (destinationReached)
				return;

			Gdx.app.log("SuperFlyingThing", "Registering event for destination reached");

			eventManager.registerEvent(Events.destinationPlanetReached, e);
			destinationReached = true;

			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
			Color currentColor = spriteComponent.getColor();

			Synchronizers.transition(currentColor, Transitions.transitionBuilder(currentColor) //
					.end(Colors.yellow) //
					.time(1000) //
					);

		}
	}

	public static class MovingObstacleScript extends ScriptJavaImpl {
		private final Vector2[] points;
		int currentTarget;

		public MovingObstacleScript(Vector2[] points, int currentTarget) {
			this.points = points;
			if (currentTarget >= points.length)
				currentTarget = points.length - 1;
			this.currentTarget = currentTarget;
		}

		private Vector2 getCurrentTargetPosition() {
			return points[currentTarget];
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			Physics physics = ComponentWrapper.getPhysics(e);
			Spatial spatial = ComponentWrapper.getSpatial(e);

			Body body = physics.getBody();

			Vector2 force = getCurrentTargetPosition().tmp().sub(spatial.getPosition());
			force.nor().mul(50000f);
			body.applyForce(force, spatial.getPosition());
			body.applyTorque(10f);

			if (spatial.getPosition().dst(getCurrentTargetPosition()) < 1f) {
				currentTarget++;
				if (currentTarget >= points.length)
					currentTarget = 0;
			}

			Vector2 linearVelocity = body.getLinearVelocity();
			float speed = linearVelocity.len();

			float maxSpeed = 5f;

			if (speed > maxSpeed) {
				linearVelocity.mul(maxSpeed / speed);
				body.setLinearVelocity(linearVelocity);
			}

			float angularVelocity = body.getAngularVelocity();
			float maxAngularVelocity = 1f;

			if (angularVelocity > maxAngularVelocity) {
				angularVelocity = maxAngularVelocity / angularVelocity;
				body.setAngularVelocity(angularVelocity);
			}
		}
	}

	public static class GameScript extends ScriptJavaImpl {

		private final EventManager eventManager;
		private final EntityFactory entityFactory;

		private ShipController controller;
		private EntityTemplates entityTemplates;
		private GameData gameData;
		private boolean invulnerable;

		Behavior fixCameraTargetBehavior = new FixCameraTargetBehavior();

		private Parameters parameters = new ParametersWrapper();
		
		public GameScript(EventManager eventManager, EntityTemplates entityTemplates, EntityFactory entityFactory, GameData gameData, ShipController controller, //
				boolean invulnerable) {
			this.eventManager = eventManager;
			this.controller = controller;
			this.entityTemplates = entityTemplates;
			this.gameData = gameData;
			this.invulnerable = invulnerable;
			this.entityFactory = entityFactory;
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			removeShipIfDead(world, e);
			regenerateShipIfNoShip(world, e);
			generateShipIfAttachedShipReleased(world, e);
			fixCameraTargetBehavior.update(world, e);

			Event event = eventManager.getEvent(Events.cameraReachedTarget);
			if (event != null) {
				eventManager.handled(event);
				Gdx.app.log("SuperFlyingShip", "Camera reached target.");
				eventManager.registerEvent(Events.enablePlanetReleaseShip, e);
			}

		}

		private void removeShipIfDead(com.artemis.World world, Entity e) {
			if (invulnerable)
				return;

			GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);

			Entity ship = gameDataComponent.ship;
			if (ship == null)
				return;
			AliveComponent aliveComponent = ship.getComponent(AliveComponent.class);

			if (aliveComponent == null)
				return;
			if (!aliveComponent.isDead())
				return;

			Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.ship);
			entityTemplates.explosionEffect(spatial.getX(), spatial.getY());

			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(gameDataComponent.ship);
			entityTemplates.deadShip(spatial, spriteComponent.getSprite());

			world.deleteEntity(gameDataComponent.ship);
			gameDataComponent.ship = null;
			gameData.deaths++;

			eventManager.registerEvent(Events.shipDeath, e);
			eventManager.registerEvent(Events.disablePlanetReleaseShip, e);
			eventManager.registerEvent(Events.moveCameraToPlanet, e);
		}

		private void regenerateShipIfNoShip(com.artemis.World world, Entity e) {
			GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);

			if (gameDataComponent.attachedShip != null)
				return;

			if (gameDataComponent.ship != null)
				return;

			Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.startPlanet);

			parameters.put("position", spatial.getPosition().tmp().add(0f, 2f));

			// Entity attachedShip = entityTemplates.attachedShip(spatial.getX(), spatial.getY() + 2f, new Vector2(1f, 0f));
			
			Entity attachedShip = entityFactory.instantiate(entityTemplates.getAttachedShipTemplate(), parameters);

			AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
			attachmentComponent.setEntity(attachedShip);
			attachmentComponent.setJoint(null);

			AttachableComponent attachableComponent = attachedShip.getComponent(AttachableComponent.class);
			attachableComponent.setOwner(gameDataComponent.startPlanet);

			gameDataComponent.attachedShip = attachedShip;
		}

		private void generateShipIfAttachedShipReleased(com.artemis.World world, Entity e) {
			GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);

			if (gameDataComponent.attachedShip == null)
				return;

			if (gameDataComponent.ship != null)
				return;

			AttachableComponent attachableComponent = gameDataComponent.attachedShip.getComponent(AttachableComponent.class);
			if (attachableComponent.getOwner() != null)
				return;

			Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.attachedShip);
			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(gameDataComponent.attachedShip);
			gameDataComponent.ship = entityTemplates.ship(spatial.getX(), spatial.getY(), movementComponent.getDirection(), controller);

			world.deleteEntity(gameDataComponent.attachedShip);
			gameDataComponent.attachedShip = null;
		}
	}

	public static class LaserGunScript extends ScriptJavaImpl {

		private final EntityFactory entityFactory;
		private final World physicsWolrd;

		private final Parameters bulletParameters;

		private static final Vector2 direction = new Vector2();
		private static final Vector2 target = new Vector2();

		public LaserGunScript(EntityFactory entityFactory, World physicsWolrd) {
			this.physicsWolrd = physicsWolrd;
			this.entityFactory = entityFactory;
			this.bulletParameters = new ParametersWrapper();
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			WeaponComponent weaponComponent = e.getComponent(WeaponComponent.class);
			int reloadTime = weaponComponent.getReloadTime();
			reloadTime -= world.getDelta();

			if (reloadTime <= 0) {
				Spatial spatial = ComponentWrapper.getSpatial(e);

				direction.set(1f, 0f).rotate(spatial.getAngle());
				target.set(spatial.getPosition()).add(direction.tmp().mul(100f));

				physicsWolrd.rayCast(new RayCastCallback() {
					@Override
					public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
						target.set(point);
						return fraction;
					}
				}, new Vector2(spatial.getX(), spatial.getY()), target);

				bulletParameters.put("position", spatial.getPosition());
				bulletParameters.put("length", target.dst(spatial.getPosition()));
				bulletParameters.put("angle", spatial.getAngle());
				bulletParameters.put("duration", weaponComponent.getBulletDuration());

				EntityTemplate bulletTemplate = weaponComponent.getBulletTemplate();
				entityFactory.instantiate(bulletTemplate, bulletParameters);

				reloadTime += weaponComponent.getFireRate();
			}
			weaponComponent.setReloadTime(reloadTime);

			AnimationComponent animationComponent = ComponentWrapper.getAnimation(e);
			Animation currentAnimation = animationComponent.getCurrentAnimation();
			currentAnimation.update(world.getDelta());

			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
			spriteComponent.setSprite(currentAnimation.getCurrentFrame());
		}

	}

	public static class LaserScript extends ScriptJavaImpl {

		private TimelineAnimation laserTimelineAnimation;

		@Override
		public void init(com.artemis.World world, Entity e) {
			TimerComponent timerComponent = e.getComponent(TimerComponent.class);
			int totalTime = timerComponent.getTotalTime();

			Spatial spatial = ComponentWrapper.getSpatial(e);

			laserTimelineAnimation = Builders.animation(Builders.timeline() //
					.value(Builders.timelineValue("alpha") //
							.keyFrame(0, 0f) //
							.keyFrame(0.2f, 1f) //
							.keyFrame(0.8f, 1f) //
							.keyFrame(1f, 0f)) //
					.value(Builders.timelineValue("width") //
							.keyFrame(0, 0f) //
							.keyFrame(0.2f, spatial.getHeight()) //
							.keyFrame(0.8f, spatial.getHeight()) //
							.keyFrame(1f, 0f)) //
					) //
					.speed(1f / totalTime) //
					.build();
			laserTimelineAnimation.start(1);
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			TimerComponent timerComponent = e.getComponent(TimerComponent.class);

			laserTimelineAnimation.update((float) world.getDelta());

			timerComponent.setCurrentTime(timerComponent.getCurrentTime() - world.getDelta());
			if (timerComponent.isFinished()) {
				world.deleteEntity(e);
				return;
			}

			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
			spriteComponent.getColor().a = (Float) laserTimelineAnimation.getValue("alpha");

			Spatial spatial = ComponentWrapper.getSpatial(e);
			float width = spatial.getWidth();
			float height = (Float) laserTimelineAnimation.getValue("width");
			spatial.setSize(width, height);

			Physics physics = ComponentWrapper.getPhysics(e);
			Contact contact = physics.getContact();

			for (int i = 0; i < contact.getContactCount(); i++) {
				if (!contact.isInContact(i))
					continue;
				Entity e2 = (Entity) contact.getUserData(i);
				if (e2 == null)
					continue;

				// send an event/message something

				AliveComponent aliveComponent = e2.getComponent(AliveComponent.class);
				if (aliveComponent != null)
					aliveComponent.setDead(true);

			}
		}

	}

	public static class PortalScript extends ScriptJavaImpl {

		// convert it maybe to system.

		private static final Vector2 direction = new Vector2();

		@Override
		public void update(com.artemis.World world, Entity e) {
			PortalComponent portalComponent = e.getComponent(PortalComponent.class);

			Physics physics = ComponentWrapper.getPhysics(e);
			Contact contact = physics.getContact();

			Spatial portalSpatial = ComponentWrapper.getSpatial(e);
			portalSpatial.setAngle(portalSpatial.getAngle() + 0.25f * world.getDelta());

			Entity portal = world.getTagManager().getEntity(portalComponent.getTargetPortalId());
			if (portal == null)
				return;

			for (int i = 0; i < contact.getContactCount(); i++) {
				if (!contact.isInContact(i))
					continue;
				Entity e2 = (Entity) contact.getUserData(i);
				if (e2 == null)
					continue;

				Gdx.app.log("SuperFlyingThing", "Teleporting entity " + e2.getUniqueId() + " to " + portalComponent.getTargetPortalId());

				// start transition of e2 to target portal,
				Spatial targetPortalSpatial = ComponentWrapper.getSpatial(portal);
				Spatial entitySpatial = ComponentWrapper.getSpatial(e2);

				// direction.set(portalSpatial.getPosition()).sub(entitySpatial.getPosition()).nor();

				MovementComponent movementComponent = ComponentWrapper.getMovementComponent(e2);
				if (movementComponent == null)
					continue;
				direction.set(movementComponent.getDirection());

				entitySpatial.setPosition(targetPortalSpatial.getX() + direction.x, targetPortalSpatial.getY() + direction.y);

				return;
			}
		}

	}

}
