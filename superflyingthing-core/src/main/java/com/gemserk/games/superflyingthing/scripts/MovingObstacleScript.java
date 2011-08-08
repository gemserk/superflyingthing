package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.scripts.Behaviors.PerformDamageToCollidingEntityBehavior;

public class MovingObstacleScript extends ScriptJavaImpl {

	private final Vector2[] points;
	int currentTarget;
	
	private Behavior performDamageToCollidingEntitiesBehavior = new PerformDamageToCollidingEntityBehavior();

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

		performDamageToCollidingEntitiesBehavior.update(world, e);

	}
	
}