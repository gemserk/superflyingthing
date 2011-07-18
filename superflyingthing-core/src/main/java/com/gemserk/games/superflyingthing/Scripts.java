package com.gemserk.games.superflyingthing;

import com.artemis.Entity;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.CameraComponent;
import com.gemserk.games.superflyingthing.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;
import com.gemserk.games.superflyingthing.Components.TriggerComponent;

public class Scripts {

	public static class CameraScript extends ScriptJavaImpl {

		float startX;
		float startY;

		TimeTransition timeTransition = new TimeTransition();

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
				if (spatial.getPosition().dst(targetSpatial.getPosition()) < 3f) {
					spatial.set(targetSpatial);
				} else {
					startX = spatial.getX();
					startY = spatial.getY();
					timeTransition.start(1000);
				}

			}

		}
	}

	public static class ShipScript extends ScriptJavaImpl {

		Behavior fixMovementBehavior = new Behaviors.FixMovementBehavior();
		Behavior fixDirectionFromControllerBehavior = new Behaviors.FixDirectionFromControllerBehavior();
		Behavior calculateInputDirectionBehavior = new Behaviors.CalculateInputDirectionBehavior();
		Behavior collisionHandlerBehavior = new Behaviors.CollisionHandlerBehavior();

		@Override
		public void update(com.artemis.World world, Entity e) {
			fixMovementBehavior.update(world.getDelta(), e);
			fixDirectionFromControllerBehavior.update(world.getDelta(), e);
			calculateInputDirectionBehavior.update(world.getDelta(), e);
			collisionHandlerBehavior.update(world.getDelta(), e);
		}
	}

	public static class GrabbableItemScript extends ScriptJavaImpl {

		Behavior removeWhenGrabbedBehavior;

		@Override
		public void init(com.artemis.World world, Entity e) {
			removeWhenGrabbedBehavior = new Behaviors.RemoveWhenGrabbedBehavior(world);
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			removeWhenGrabbedBehavior.update(world.getDelta(), e);

			GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
			if (!grabbableComponent.grabbed)
				return;

			TriggerComponent triggerComponent = ComponentWrapper.getTriggers(e);
			Trigger trigger = triggerComponent.getTrigger(Triggers.itemGrabbedTrigger);

			trigger.trigger(e);
			trigger.triggered();
		}
	}

	public static class StartPlanetScript extends ScriptJavaImpl {

		Behavior releaseAttachmentBehavior;
		Behavior attachEntityBehavior;
		Behavior calculateInputDirectionBehavior;

		private final World physicsWorld;

		public StartPlanetScript(World physicsWorld, JointBuilder jointBuilder) {
			this.physicsWorld = physicsWorld;
			releaseAttachmentBehavior = new Behaviors.ReleaseAttachmentBehavior(physicsWorld);
			attachEntityBehavior = new Behaviors.AttachEntityBehavior(jointBuilder);
			calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionBehavior();
		}

		@Override
		public void update(com.artemis.World world, Entity e) {

			// releaseAttachmentBehavior.update(world.getDelta(), e);

			updateReleaseAttachment(world, e);

			attachEntityBehavior.update(world.getDelta(), e);
			calculateInputDirectionBehavior.update(world.getDelta(), e);
		}

		private void updateReleaseAttachment(com.artemis.World world, Entity e) {
			AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);
			Entity attachedEntity = entityAttachment.entity;

			if (attachedEntity == null)
				return;

			if (!shouldReleaseShip(world, e))
				return;

			if (entityAttachment.joint != null)
				physicsWorld.destroyJoint(entityAttachment.joint);

			AttachableComponent attachableComponent = attachedEntity.getComponent(AttachableComponent.class);
			attachableComponent.owner = null;

			entityAttachment.joint = null;
			entityAttachment.entity = null;
		}

		private boolean shouldReleaseShip(com.artemis.World world, Entity e) {
			ControllerComponent controllerComponent = ComponentWrapper.getControllerComponent(e);
			Controller controller = controllerComponent.getController();
			if (Gdx.app.getType() == ApplicationType.Android) {
				Spatial spatial = ComponentWrapper.getSpatial(e);
				return (spatial.getPosition().dst(controller.getPosition()) < 5f);
			} else {
				Spatial spatial = ComponentWrapper.getSpatial(e);
				return (spatial.getPosition().dst(controller.getPosition()) < 5f);
				// return controller.releaseButtonPressed();
			}
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

}
