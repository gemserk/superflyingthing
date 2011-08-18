package com.gemserk.games.superflyingthing.scripts;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.utils.ImmutableBag;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventListenerManager;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.GlobalTime;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.Replay.ReplayEntry;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.templates.Groups;

public class ReplayRecorderScript extends ScriptJavaImpl {

	private final EventListenerManager eventListenerManager;

	private boolean recording;
	private int replayTime;
	private Entity recordingShip;

	private Replay currentReplay;

	// private int replayUpdateInterval = 10;
	// private int currentUpdateInterval = replayUpdateInterval;

	private float frameTime = 0f;

	// TODO: remove them, should not be here.
	private final EntityFactory entityFactory;
	private final EntityTemplate shipReplayTemplate;

	public ReplayRecorderScript(EventListenerManager eventListenerManager, EntityFactory entityFactory, EntityTemplate shipReplayTemplate) {
		this.eventListenerManager = eventListenerManager;
		this.entityFactory = entityFactory;
		this.shipReplayTemplate = shipReplayTemplate;
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
		ReplayComponent replayComponent = ComponentWrapper.getReplayComponent(e);
		ReplayList replayList = replayComponent.getReplayList();
		replayList.add(currentReplay);

		// stops the ship recording
		recording = false;
		recordingShip = null;
	}

	private void shipReleased(com.artemis.World world, Entity e, Event event) {
		// starts a new ship recording
		recording = true;
		recordingShip = (Entity) event.getSource();

		currentReplay = new Replay();
		replayTime = 0;

		ReplayComponent replayComponent = ComponentWrapper.getReplayComponent(e);
		ReplayList replayList = replayComponent.getReplayList();

		// remove previous replays being reproduced
		ImmutableBag<Entity> previousReplays = world.getGroupManager().getEntities(Groups.ReplayShipGroup);
		for (int i = 0; i < previousReplays.size(); i++)
			previousReplays.get(i).delete();

		// reproduce each replay to test....
		ArrayList<Replay> replays = replayList.getReplays();
		for (int i = 0; i < replays.size(); i++) {
			entityFactory.instantiate(shipReplayTemplate, new ParametersWrapper().put("replay", replays.get(i)));
		}
	}

	public void update(com.artemis.World world, Entity e) {
		if (!recording)
			return;

		frameTime += GlobalTime.getDelta();

		if (frameTime < 0.1f)
			return;

		frameTime -= 0.1f;

		replayTime += 100;

		// currentUpdateInterval--;
		//
		// if (currentUpdateInterval > 0) {
		// replayTime += world.getDelta();
		// return;
		// }
		//
		// currentUpdateInterval = replayUpdateInterval;

		SpatialComponent spatialComponent = ComponentWrapper.getSpatialComponent(recordingShip);

		// recordingShip could be deleted already :(
		if (spatialComponent == null)
			return;

		Spatial recordingShipSpatial = spatialComponent.getSpatial();

		currentReplay.replayEntries.add(new ReplayEntry(replayTime, recordingShipSpatial.getX(), recordingShipSpatial.getY(), (int) recordingShipSpatial.getAngle()));

		// replayTime += world.getDelta();
	}

}
