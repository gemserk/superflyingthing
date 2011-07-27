package com.gemserk.games.superflyingthing;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.controllers.Controller;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.timers.CountDownTimer;
import com.gemserk.componentsengine.utils.timers.Timer;
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.superflyingthing.Behaviors.FixCameraTargetBehavior;
import com.gemserk.games.superflyingthing.Components.AliveComponent;
import com.gemserk.games.superflyingthing.Components.AnimationComponent;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.CameraComponent;
import com.gemserk.games.superflyingthing.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.Components.GameData;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;

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
			removeWhenGrabbedBehavior = new Behaviors.RemoveWhenGrabbedBehavior(world);
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

			// TriggerComponent triggerComponent = ComponentWrapper.getTriggers(e);
			// Trigger trigger = triggerComponent.getTrigger(Triggers.destinationReachedTrigger);
			// trigger.trigger(e);
		}
	}

	public static class MovingObstacleScript extends ScriptJavaImpl {
		private final Vector2[] points;
		int currentTarget;

		public MovingObstacleScript(Vector2[] points) {
			this.points = points;
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			currentTarget = 0;
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

	public static class UpdateControllerScript extends ScriptJavaImpl {

		private final Controller controller;

		public UpdateControllerScript(Controller controller) {
			this.controller = controller;
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			controller.update(world.getDelta());
		}

	}

	public static class GameScript extends ScriptJavaImpl {

		private final EventManager eventManager;

		private ShipController controller;
		private EntityTemplates entityTemplates;
		private GameData gameData;
		private boolean invulnerable;

		Behavior fixCameraTargetBehavior = new FixCameraTargetBehavior();

		public GameScript(EventManager eventManager, ShipController controller, EntityTemplates entityTemplates, GameData gameData, boolean invulnerable) {
			this.eventManager = eventManager;
			this.controller = controller;
			this.entityTemplates = entityTemplates;
			this.gameData = gameData;
			this.invulnerable = invulnerable;
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
			Entity attachedShip = entityTemplates.attachedShip(spatial.getX(), spatial.getY() + 2f, new Vector2(1f, 0f));

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

		private final EntityTemplates entityTemplates;

		Timer fireTimer;

		public LaserGunScript(EntityTemplates entityTemplates) {
			this.entityTemplates = entityTemplates;
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			fireTimer = new CountDownTimer(2000, true);
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			if (fireTimer.update(world.getDelta())) {
				Spatial spatial = ComponentWrapper.getSpatial(e);
				entityTemplates.laser(spatial.getX(), spatial.getY(), 10f, spatial.getAngle(), new Scripts.LaserScript());
				fireTimer.reset();
			}
		}

	}

	public static class LaserScript extends ScriptJavaImpl {
		
		Timer aliveTimer;
		
		@Override
		public void init(com.artemis.World world, Entity e) {
			aliveTimer = new CountDownTimer(500, true);
		}
		
		@Override
		public void update(com.artemis.World world, Entity e) {
			if (aliveTimer.update(world.getDelta())) {
				world.deleteEntity(e);
			}
		}

	}

}
