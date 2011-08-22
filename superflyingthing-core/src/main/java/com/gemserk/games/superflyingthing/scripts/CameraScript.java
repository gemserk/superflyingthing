package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.animation4j.interpolator.function.InterpolationFunctions;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;

public class CameraScript extends ScriptJavaImpl {

	private final EventManager eventManager;

	private final Vector2 transitionOrigin = new Vector2();
	private final Vector2 transitionTarget = new Vector2();

	TimeTransition timeTransition = new TimeTransition();

	boolean movingToTarget = false;
	private Entity owner;

	public CameraScript(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	@Override
	public void init(com.artemis.World world, Entity e) {
		this.owner = e;
	}

	@Handles(ids = Events.moveCameraToEntity)
	public void moveCameraToPlanet(Event event) {
		Spatial spatial = ComponentWrapper.getSpatial(owner);
		transitionOrigin.set(spatial.getPosition());

		Entity entity = (Entity) event.getSource();
		if (entity == null)
			return;

		Spatial targetSpatial = ComponentWrapper.getSpatial(entity);
		transitionTarget.set(targetSpatial.getPosition());

		timeTransition.start(0.8f);
		movingToTarget = true;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updatePosition(world, e);
	}

	private void updatePosition(com.artemis.World world, Entity e) {
		TargetComponent targetComponent = ComponentWrapper.getTargetComponent(e);
		Entity target = targetComponent.target;

		Spatial spatial = ComponentWrapper.getSpatial(e);

		if (target == null) {
			timeTransition.update(GlobalTime.getDelta());

			if (!timeTransition.isFinished()) {
				float x = FloatInterpolator.interpolate(transitionOrigin.x, transitionTarget.x, timeTransition.get(), InterpolationFunctions.easeInOut());
				float y = FloatInterpolator.interpolate(transitionOrigin.y, transitionTarget.y, timeTransition.get(), InterpolationFunctions.easeInOut());
				spatial.setPosition(x, y);
			} else {
				if (movingToTarget) {
					eventManager.registerEvent(Events.cameraReachedTarget, e);
					// to avoid sending same event several times.
					movingToTarget = false;
				}
			}

			return;
		} else {
			
			// entity was removed from the world...
			if (world.getEntity(target.getId()) != target) {
				targetComponent.setTarget(null);
				return;
			}
			
			Spatial targetSpatial = ComponentWrapper.getSpatial(target);
			spatial.set(targetSpatial);
		}

	}
}