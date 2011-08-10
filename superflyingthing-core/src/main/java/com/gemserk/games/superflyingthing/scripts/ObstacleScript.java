package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.scripts.Behaviors.PerformDamageToCollidingEntityBehavior;

public class ObstacleScript extends ScriptJavaImpl {
	
	private Behavior performDamageToCollidingEntitiesBehavior = new PerformDamageToCollidingEntityBehavior();

	@Override
	public void update(com.artemis.World world, Entity e) {
		performDamageToCollidingEntitiesBehavior.update(world, e);
	}
	
}
