package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;

public class BoxObstacleTemplate extends EntityTemplateImpl {
	
	EntityTemplates entityTemplates;

	@Override
	public void apply(Entity entity) {
		
		Float w = parameters.get("w");
		Float h = parameters.get("h");
		
		Vector2[] vertices = new Vector2[] { //
				new Vector2(w * 0.5f, h * 0.5f),//
						new Vector2(w * 0.5f, -h * 0.5f), //
						new Vector2(-w * 0.5f, -h * 0.5f),//
						new Vector2(-w * 0.5f, h * 0.5f), };
		
		parameters.put("vertices", vertices);
		
		EntityTemplate staticObstacleTemplate = entityTemplates.staticObstacleTemplate;
		
		staticObstacleTemplate.setParameters(parameters);
		staticObstacleTemplate.apply(entity);

	}

}