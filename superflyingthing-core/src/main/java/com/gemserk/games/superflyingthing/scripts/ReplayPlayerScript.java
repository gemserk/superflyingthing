package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.Replay.ReplayEntry;

/**
 * Plays a replay by interpolating values of the recorded replay.
 * 
 * @author acoppes
 * 
 */
public class ReplayPlayerScript extends ScriptJavaImpl {

	private final Replay replay;
	private int time;

	private ReplayEntry previousReplayEntry;
	private ReplayEntry currentReplayEntry;

	private int currentFrame;

	private boolean finished;

	public ReplayPlayerScript(Replay replay) {
		this.replay = replay;
	}

	@Override
	public void init(World world, Entity e) {
		time = 0;
		finished = false;
		currentFrame = 0;
		nextReplayFrame();
	}

	public void update(com.artemis.World world, Entity e) {
		if (finished)
			return;

		SpatialComponent spatialComponent = ComponentWrapper.getSpatialComponent(e);
		Spatial spatial = spatialComponent.getSpatial();

		// interpolate time between previousReplayEntry and currentOne....

		float t = (float) time / (float) (currentReplayEntry.time - previousReplayEntry.time);

		float x = FloatInterpolator.interpolate(previousReplayEntry.x, currentReplayEntry.x, t);
		float y = FloatInterpolator.interpolate(previousReplayEntry.y, currentReplayEntry.y, t);

		float angleDiff = Math.abs(currentReplayEntry.angle - previousReplayEntry.angle);
		if (angleDiff > 180)
			Gdx.app.log("SuperFlyingThing", "angle diff greater than 180 : " + angleDiff);
		float angle = FloatInterpolator.interpolate(previousReplayEntry.angle, currentReplayEntry.angle, t);

		// problems between current angle and next angle when angle variates between 360 and 0?

		spatial.setPosition(x, y);
		spatial.setAngle(angle);

		time += world.getDelta();

		while (time > (currentReplayEntry.time - previousReplayEntry.time)) {
			time -= (currentReplayEntry.time - previousReplayEntry.time);
			nextReplayFrame();
		}
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
