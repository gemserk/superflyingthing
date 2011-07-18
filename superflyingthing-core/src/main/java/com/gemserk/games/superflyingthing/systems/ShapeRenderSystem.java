package com.gemserk.games.superflyingthing.systems;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.ShapeUtils;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.ShapeComponent;

public class ShapeRenderSystem extends EntityProcessingSystem {

	public ShapeRenderSystem(Class<? extends Component> requiredType) {
		super(requiredType);
	}

	@Override
	protected void added(Entity e) {
		ShapeComponent shapeComponent = e.getComponent(ShapeComponent.class);
		if (shapeComponent.triangulator == null)
			shapeComponent.triangulator = ShapeUtils.triangulate(shapeComponent.getVertices());
	}

	@Override
	protected void process(Entity e) {
		ShapeComponent shapeComponent = e.getComponent(ShapeComponent.class);
		if (shapeComponent == null)
			return;
		Spatial spatial = ComponentWrapper.getSpatial(e);
		if (spatial == null)
			return;
		ImmediateModeRendererUtils.draw(shapeComponent.triangulator, spatial.getX(), spatial.getY(), spatial.getAngle(), shapeComponent.color);
	}
}