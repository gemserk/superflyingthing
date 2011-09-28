package com.gemserk.games.superflyingthing.scenes;

import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.componentsengine.utils.Parameters;

public interface SceneTemplate {
	
	void apply(WorldWrapper worldWrapper);
	
	void setParameters(Parameters parameters);
	
	Parameters getParameters();
	
}
