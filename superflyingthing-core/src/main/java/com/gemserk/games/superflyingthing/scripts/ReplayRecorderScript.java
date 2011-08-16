package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventListenerManager;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.Replay.ReplayEntry;

public class ReplayRecorderScript extends ScriptJavaImpl {

	private final EventListenerManager eventListenerManager;

	private boolean recording;
	private int replayTime;
	private Entity recordingShip;
	
	private Replay currentReplay;

	public ReplayRecorderScript(EventListenerManager eventListenerManager) {
		this.eventListenerManager = eventListenerManager;
	}

	@Override
	public void init(final com.artemis.World world, final Entity e) {
		eventListenerManager.register(Events.shipDeath, new EventListener() {
			@Override
			public void onEvent(Event event) {
				shipDeath(world, e, event);
			}
		});
		eventListenerManager.register(Events.shipReleased, new EventListener() {
			@Override
			public void onEvent(Event event) {
				shipReleased(world, e, event);
			}
		});
	}

	private void shipDeath(com.artemis.World world, Entity e, Event event) {
		// stops the ship recording
		recording = false;
		recordingShip = null;
		
		ReplayComponent replayComponent = ComponentWrapper.getReplayComponent(e);
		replayComponent.getReplayList().add(currentReplay);

	}

	private void shipReleased(com.artemis.World world, Entity e, Event event) {
		// starts a new ship recording
		recording = true;
		recordingShip = (Entity) event.getSource();

		currentReplay = new Replay();
		replayTime = 0;
	}

	public void update(com.artemis.World world, Entity e) {
		if (!recording)
			return;

		SpatialComponent spatialComponent = ComponentWrapper.getSpatialComponent(recordingShip);
		Spatial spatial = spatialComponent.getSpatial();

		// Gdx.app.log("SuperFlyingThing", "position: " + spatial.getPosition() + ", angle: " + spatial.getAngle());

		currentReplay.replayEntries.add(new ReplayEntry(replayTime, spatial.getX(), spatial.getY(), spatial.getAngle()));

		replayTime += world.getDelta();
	}

}
