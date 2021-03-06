package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.Replay.ReplayEntry;

/**
 * Applies all the replay information to an entity, applied to the ship to reproduce a ship replay.
 * 
 * @author acoppes
 * 
 */
public class ReplayPlayerScript extends ScriptJavaImpl {

	private final EventManager eventManager;
	private final Entity target;

	private final Replay replay;
	private float time;

	private ReplayEntry previousReplayEntry;
	private ReplayEntry currentReplayEntry;

	private int currentFrame;

	private boolean finished;

	public ReplayPlayerScript(Replay replay, EventManager eventManager, Entity target) {
		this.replay = replay;
		this.eventManager = eventManager;
		this.target = target;
	}

	@Override
	public void init(World world, Entity e) {
		time = 0;
		finished = false;
		currentFrame = 0;
		nextReplayFrame();
	}

	public void update(com.artemis.World world, Entity e) {

		SpatialComponent spatialComponent = GameComponents.getSpatialComponent(target);
		Spatial spatial = spatialComponent.getSpatial();

		if (finished) {
			// removes entity if replay is finished.

			if (!replay.main)
				eventManager.registerEvent(Events.explosion, spatial);

			target.delete();
			e.delete();

			return;
		}

		if (Math.abs(currentReplayEntry.x - previousReplayEntry.x) > 1f || Math.abs(currentReplayEntry.y - previousReplayEntry.y) > 1f) {
			// in this case, do not interpolate and move to the next frame...
			time = 0;
			nextReplayFrame();
			return;
		}

		float t = (float) time / (float) getTimeBetweenFrames();

		float x = FloatInterpolator.interpolate(previousReplayEntry.x, currentReplayEntry.x, t);
		float y = FloatInterpolator.interpolate(previousReplayEntry.y, currentReplayEntry.y, t);

		float angleDiff = Math.abs(currentReplayEntry.angle - previousReplayEntry.angle);

		float previousAngle = previousReplayEntry.angle;
		float nextAngle = currentReplayEntry.angle;

		if (angleDiff > 180) {
			if (previousAngle > nextAngle)
				previousAngle -= 360f;
			else
				nextAngle -= 360f;
		}

		float angle = FloatInterpolator.interpolate(previousAngle, nextAngle, t);

		spatial.setPosition(x, y);
		spatial.setAngle(angle);

		time += GlobalTime.getDelta();

		while (time > getTimeBetweenFrames()) {
			time -= getTimeBetweenFrames();
			nextReplayFrame();
		}
	}

	private float getTimeBetweenFrames() {
		return (float) (currentReplayEntry.time - previousReplayEntry.time) * 0.001f;
	}

	private void nextReplayFrame() {
		if (currentFrame + 1 >= replay.getEntriesCount()) {
			finished = true;
			Gdx.app.log("SuperFlyingThing", "Replay finished");
			return;
		}
		currentFrame++;
		previousReplayEntry = replay.getEntry(currentFrame - 1);
		currentReplayEntry = replay.getEntry(currentFrame);
	}

}