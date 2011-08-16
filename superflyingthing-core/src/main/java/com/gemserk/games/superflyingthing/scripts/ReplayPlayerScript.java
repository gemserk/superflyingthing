package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
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

	private final EntityFactory entityFactory;
	private final EntityTemplate particleEmitterTemplate;

	private final Replay replay;
	private int time;

	private ReplayEntry previousReplayEntry;
	private ReplayEntry currentReplayEntry;

	private int currentFrame;

	private boolean finished;
	private Parameters parameters = new ParametersWrapper();

	public ReplayPlayerScript(Replay replay, EntityFactory entityFactory, EntityTemplate particleEmitterTemplate) {
		this.replay = replay;
		this.entityFactory = entityFactory;
		this.particleEmitterTemplate = particleEmitterTemplate;
	}

	@Override
	public void init(World world, Entity e) {
		time = 0;
		finished = false;
		currentFrame = 0;
		nextReplayFrame();
	}

	public void update(com.artemis.World world, Entity e) {
		
		SpatialComponent spatialComponent = ComponentWrapper.getSpatialComponent(e);
		Spatial spatial = spatialComponent.getSpatial();
		
		if (finished) {
			// removes entity if replay is finished.
			
			parameters.put("position", spatial.getPosition());
			parameters.put("emitter", "ExplosionEmitter");
			
			entityFactory.instantiate(particleEmitterTemplate, parameters);
			
			e.delete();
			
			return;
		}

		float t = (float) time / (float) (currentReplayEntry.time - previousReplayEntry.time);

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