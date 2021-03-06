package com.gemserk.games.superflyingthing.scenes;

import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;

public abstract class SceneTemplateImpl implements SceneTemplate {

	protected Parameters parameters = new ParametersWrapper();

	public Parameters getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public abstract void apply(WorldWrapper worldWrapper);

}
