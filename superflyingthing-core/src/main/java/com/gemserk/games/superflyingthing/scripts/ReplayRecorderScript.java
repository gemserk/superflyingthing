package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.components.Components.ReplayListComponent;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.Replay.ReplayEntry;
import com.gemserk.games.superflyingthing.components.ReplayList;

public class ReplayRecorderScript extends ScriptJavaImpl {

	private boolean recording;
	private int replayTime;
	private Entity recordingShip;

	private Replay currentReplay;

	private float frameTime = 0f;

	private Entity owner;

	@Override
	public void init(final com.artemis.World world, final Entity e) {
		this.owner = e;
	}

	@Handles(ids = { Events.gameOver, Events.shipDeath })
	public void stopReplayRecording(Event event) {
		ReplayListComponent replayListComponent = GameComponents.getReplayComponent(owner);
		ReplayList replayList = replayListComponent.getReplayList();
		replayList.add(currentReplay);
		
		currentReplay.main = event.getId().equals(Events.gameOver);

		// stops the ship recording
		recording = false;
		recordingShip = null;
	}

	@Handles(ids = Events.shipReleased)
	public void changeRecordedEntity(Event event) {
		recordingShip = (Entity) event.getSource();
	}

	@Handles(ids = Events.shipSpawned)
	public void startReplayRecording(Event event) {
		// starts a new ship recording
		recording = true;
		recordingShip = (Entity) event.getSource();

		currentReplay = new Replay();
		replayTime = 0;
	}

	public void update(com.artemis.World world, Entity e) {
		if (!recording)
			return;

		frameTime += GlobalTime.getDelta();

		if (frameTime < 0.1f)
			return;

		frameTime -= 0.1f;

		replayTime += 100;

		SpatialComponent spatialComponent = GameComponents.getSpatialComponent(recordingShip);

		// recordingShip could be deleted already :(
		if (spatialComponent == null)
			return;

		Spatial recordingShipSpatial = spatialComponent.getSpatial();

		currentReplay.add(new ReplayEntry(replayTime, recordingShipSpatial.getX(), recordingShipSpatial.getY(), (int) recordingShipSpatial.getAngle()));
	}

}
