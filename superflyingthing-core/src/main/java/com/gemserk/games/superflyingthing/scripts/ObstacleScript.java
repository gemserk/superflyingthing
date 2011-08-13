package com.gemserk.games.superflyingthing.scripts;

import com.gemserk.commons.artemis.scripts.ScriptCompositeImpl;
import com.gemserk.games.superflyingthing.scripts.Behaviors.PerformDamageToCollidingEntityScript;

public class ObstacleScript extends ScriptCompositeImpl {
	
	public ObstacleScript() {
		super(new PerformDamageToCollidingEntityScript());
	}
	
}
