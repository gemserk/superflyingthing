package com.gemserk.games.superflyingthing.systems;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.GL10;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.OrderedByLayerEntities;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.games.superflyingthing.components.Components.ShapeComponent;

public class RenderLayerShapeImpl implements RenderLayer {

	private static final Class<RenderableComponent> renderableComponentClass = RenderableComponent.class;
	private static final Class<ShapeComponent> shapeComponentClass = ShapeComponent.class;
	private static final Class<SpatialComponent> spatialComponentClass = SpatialComponent.class;

	private final Libgdx2dCamera camera;
	private final OrderedByLayerEntities orderedByLayerEntities;
	private boolean enabled;

	public RenderLayerShapeImpl(int minLayer, int maxLayer, Libgdx2dCamera camera) {
		this.camera = camera;
		this.orderedByLayerEntities = new OrderedByLayerEntities(minLayer, maxLayer);
		this.enabled = true;
	}
	
	@Override
	public void init() {
		
	}
	
	@Override
	public void dispose() {

	}

	@Override
	public boolean belongs(Entity e) {
		RenderableComponent renderableComponent = e.getComponent(renderableComponentClass);
		if (renderableComponent == null)
			return false;

		ShapeComponent shapeComponent = e.getComponent(shapeComponentClass);
		if (shapeComponent == null)
			return false;
		
		SpatialComponent spatialComponent = e.getComponent(spatialComponentClass);
		if (spatialComponent == null)
			return false;
		
		return orderedByLayerEntities.belongs(renderableComponent.getLayer());
	}

	@Override
	public void add(Entity entity) {
		orderedByLayerEntities.add(entity);
	}

	@Override
	public void remove(Entity entity) {
		orderedByLayerEntities.remove(entity);
	}

	@Override
	public void render() {
		camera.apply();
		for (int i = 0; i < orderedByLayerEntities.size(); i++) {
			Entity e = orderedByLayerEntities.get(i);
			ShapeComponent shapeComponent = e.getComponent(shapeComponentClass);
			Spatial spatial = e.getComponent(spatialComponentClass).getSpatial();
			if (shapeComponent.texture != null)
				shapeComponent.texture.bind();
			ImmediateModeRendererUtils.draw(GL10.GL_TRIANGLES, shapeComponent.mesh2d, spatial.getX(), spatial.getY(), spatial.getAngle());
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
