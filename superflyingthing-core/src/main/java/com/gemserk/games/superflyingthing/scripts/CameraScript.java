package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventListenerManager;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;

public class CameraScript extends ScriptJavaImpl {

	private final EventManager eventManager;
	private final EventListenerManager eventListenerManager;
	private Entity owner;

	float startX;
	float startY;

	TimeTransition timeTransition = new TimeTransition();

	boolean movingToTarget = false;

	public CameraScript(EventManager eventManager, EventListenerManager eventListenerManager) {
		this.eventManager = eventManager;
		this.eventListenerManager = eventListenerManager;
	}

	@Override
	public void init(com.artemis.World world, Entity e) {
		this.owner = e;
		Spatial spatial = ComponentWrapper.getSpatial(e);
		startX = spatial.getX();
		startY = spatial.getY();
		eventListenerManager.register(Events.moveCameraToPlanet, new EventListener() {
			@Override
			public void onEvent(Event event) {
				moveCameraToPlanet(event);
			}
		});
	}

	private void moveCameraToPlanet(Event event) {
		Spatial spatial = ComponentWrapper.getSpatial(owner);
		startX = spatial.getX();
		startY = spatial.getY();
		timeTransition.start(800);
		movingToTarget = true;
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
			} else {
				spatial.set(targetSpatial);
			}
		}

	}
}