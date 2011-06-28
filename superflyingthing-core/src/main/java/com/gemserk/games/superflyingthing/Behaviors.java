package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.EntityAttachment;
import com.gemserk.games.superflyingthing.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.Components.InputDirectionComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.ReleaseEntityComponent;
import com.gemserk.games.superflyingthing.Components.SpatialComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;

public class Behaviors {

	public static class CameraFollowBehavior extends Behavior {

		@Override
		public void update(int delta, Entity entity) {
			TargetComponent targetComponent = entity.getComponent(TargetComponent.class);
			Entity target = targetComponent.target;
			if (target == null)
				return;
			SpatialComponent spatialComponent = target.getComponent(SpatialComponent.class);
			if (spatialComponent == null)
				return;
			Camera camera = ComponentWrapper.getCamera(entity);
			camera.setPosition(spatialComponent.spatial.getX(), spatialComponent.spatial.getY());
		}

	}

	public static class FixMovementBehavior extends Behavior {

		@Override
		public void update(int delta, Entity e) {
			MovementComponent movementComponent = e.getComponent(MovementComponent.class);
			Vector2 direction = movementComponent.direction;

			direction.nor();

			Body body = ComponentWrapper.getBody(e);

			Vector2 position = body.getTransform().getPosition();
			float desiredAngle = direction.angle();

			body.setTransform(position, desiredAngle * MathUtils.degreesToRadians);
			body.applyForce(direction.tmp().mul(5000f), position);

			Vector2 linearVelocity = body.getLinearVelocity();

			float speed = linearVelocity.len();

			linearVelocity.set(direction.tmp().mul(speed));

			float maxSpeed = 6f;
			if (speed > maxSpeed) {
				linearVelocity.mul(maxSpeed / speed);
				body.setLinearVelocity(linearVelocity);
			}
		}

	}

	public static class AttachedEntityDirectionBehavior extends Behavior {

		@Override
		public void update(int delta, Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);

			if (entityAttachment.entity == null)
				return;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			Vector2 position = spatial.getPosition();

			Entity attachedEntity = entityAttachment.entity;
			Spatial attachedEntitySpatial = ComponentWrapper.getSpatial(attachedEntity);
			MovementComponent movementComponent = ComponentWrapper.getComponent(attachedEntity, MovementComponent.class);

			Vector2 superSheepPosition = attachedEntitySpatial.getPosition();

			Vector2 diff = superSheepPosition.sub(position).nor();
			diff.rotate(-90f);

			movementComponent.direction.set(diff);
		}

	}

	public static class AttachEntityBehavior extends Behavior {

		private final JointBuilder jointBuilder;

		public AttachEntityBehavior(JointBuilder jointBuilder) {
			this.jointBuilder = jointBuilder;
		}

		@Override
		public void update(int delta, Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);
			if (entityAttachment.entity == null)
				return;
			if (entityAttachment.joint != null)
				return;

			AttachableComponent attachableComponent = entityAttachment.entity.getComponent(AttachableComponent.class);
			attachableComponent.owner = e;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			entityAttachment.joint = jointBuilder.distanceJoint() //
					.bodyA(ComponentWrapper.getBody(entityAttachment.entity)) //
					.bodyB(ComponentWrapper.getBody(e)) //
					.collideConnected(false) //
					.length(spatial.getWidth() * 0.5f * 1.5f) //
					.build();
		}

	}

	public static class ReleaseAttachmentBehavior extends Behavior {

		private final World world;

		public ReleaseAttachmentBehavior(World world) {
			this.world = world;
		}

		@Override
		public void update(int delta, Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);
			Entity attachedEntity = entityAttachment.entity;

			if (attachedEntity == null)
				return;

			ReleaseEntityComponent releaseEntityComponent = e.getComponent(ReleaseEntityComponent.class);

			if (releaseEntityComponent.releaseTime > 0)
				releaseEntityComponent.releaseTime -= delta;

			if (Gdx.app.getType() == ApplicationType.Android) {
				if (!Gdx.input.isTouched()) {
					return;
				}
			} else if (!Gdx.input.isKeyPressed(Keys.SPACE))
				return;

			if (releaseEntityComponent.releaseTime > 0)
				return;

			if (entityAttachment.joint != null)
				world.destroyJoint(entityAttachment.joint);

			AttachableComponent attachableComponent = attachedEntity.getComponent(AttachableComponent.class);
			attachableComponent.owner = null;

			entityAttachment.joint = null;
			entityAttachment.entity = null;

			releaseEntityComponent.releaseTime = 300;
		}

	}

	public static class RemoveWhenGrabbedBehavior extends Behavior {

		private final EntityManager entityManager;

		public RemoveWhenGrabbedBehavior(EntityManager entityManager) {
			this.entityManager = entityManager;
		}

		@Override
		public void update(int delta, Entity e) {
			GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
			if (grabbableComponent.grabbed)
				entityManager.remove(e);
		}
	}

	public static class FixDirectionFromInputBehavior extends Behavior {

		@Override
		public void update(int delta, Entity e) {
			MovementComponent movementComponent = e.getComponent(MovementComponent.class);
			InputDirectionComponent inputDirectionComponent = e.getComponent(InputDirectionComponent.class);

			if (movementComponent == null)
				return;
			if (inputDirectionComponent == null)
				return;

			float movementDirection = inputDirectionComponent.direction;
			Vector2 direction = movementComponent.direction;

			float rotationAngle = 0f;
			float maxAngularVelocity = 600f;
			float acceleration = 1f;
			float angularVelocity = movementComponent.angularVelocity;

			float minimumAngularVelocity = 100f;

			if (movementDirection > 0) {
				if (angularVelocity < 0)
					angularVelocity = minimumAngularVelocity;
				angularVelocity += acceleration * delta;
				if (angularVelocity > maxAngularVelocity)
					angularVelocity = maxAngularVelocity;
				rotationAngle = angularVelocity * delta * 0.001f;
			} else if (movementDirection < 0) {
				if (angularVelocity > 0)
					angularVelocity = -minimumAngularVelocity;
				angularVelocity -= acceleration * delta;
				if (angularVelocity < -maxAngularVelocity)
					angularVelocity = -maxAngularVelocity;
				rotationAngle = angularVelocity * delta * 0.001f;
			} else {
				if (angularVelocity > 0)
					angularVelocity = minimumAngularVelocity;
				if (angularVelocity < 0)
					angularVelocity = -minimumAngularVelocity;
			}

			movementComponent.angularVelocity = angularVelocity;
			direction.rotate(rotationAngle);
		}

	}

	public static class CalculateInputDirectionBehavior extends Behavior {

		@Override
		public void update(int delta, Entity e) {
			InputDirectionComponent inputDirectionComponent = e.getComponent(InputDirectionComponent.class);
			if (inputDirectionComponent == null)
				return;
			inputDirectionComponent.direction = getMovementDirection();
		}

		private float getMovementDirection() {
			if (Gdx.app.getType() == ApplicationType.Android)
				return getMovementDirectionAndroid();
			else
				return getMovementDirectionPC();
		}

		private float getMovementDirectionPC() {
			float movementDirection = 0f;

			if (Gdx.input.isKeyPressed(Keys.LEFT))
				movementDirection += 1f;

			if (Gdx.input.isKeyPressed(Keys.RIGHT))
				movementDirection -= 1f;

			return movementDirection;
		}

		private float getMovementDirectionAndroid() {
			float movementDirection = 0f;

			for (int i = 0; i < 5; i++) {
				if (!Gdx.input.isTouched(i))
					continue;
				float x = Gdx.input.getX(i);
				if (x < Gdx.graphics.getWidth() / 2)
					movementDirection += 1f;
				else
					movementDirection -= 1f;
			}

			return movementDirection;
		}

	}

}