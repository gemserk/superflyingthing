package com.gemserk.games.superflyingthing.systems;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.Array;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.SpriteComponentComparator;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.ShapeComponent;

public class RenderLayerShapeImpl implements RenderLayer {

	private static final SpriteComponentComparator spriteComponentComparator = new SpriteComponentComparator();

	private final int minLayer, maxLayer;

	Array<Entity> orderedByLayerEntities = new Array<Entity>();

	Libgdx2dCamera camera;

	public RenderLayerShapeImpl(int minLayer, int maxLayer, Libgdx2dCamera camera) {
		this.minLayer = minLayer;
		this.maxLayer = maxLayer;
		this.camera = camera;
	}
	
	@Override
	public void init() {
		
	}
	
	@Override
	public void dispose() {

	}

	@Override
	public boolean belongs(Entity e) {
		RenderableComponent renderableComponent = e.getComponent(RenderableComponent.class);
		if (renderableComponent == null)
			return false;

		ShapeComponent shapeComponent = e.getComponent(ShapeComponent.class);
		if (shapeComponent == null)
			return false;
		
		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
		if (spatialComponent == null)
			return false;
		
		return renderableComponent.getLayer() >= minLayer && renderableComponent.getLayer() < maxLayer;
	}

	@Override
	public void add(Entity entity) {
		orderedByLayerEntities.add(entity);
		orderedByLayerEntities.sort(spriteComponentComparator);
	}

	@Override
	public void remove(Entity entity) {
		orderedByLayerEntities.removeValue(entity, true);
	}

	@Override
	public void render() {
		camera.apply();
		for (int i = 0; i < orderedByLayerEntities.size; i++) {
			Entity e = orderedByLayerEntities.get(i);
			ShapeComponent shapeComponent = e.getComponent(ShapeComponent.class);
			Spatial spatial = ComponentWrapper.getSpatial(e);
			if (shapeComponent.texture != null)
				shapeComponent.texture.bind();
			ImmediateModeRendererUtils.draw(GL10.GL_TRIANGLES, shapeComponent.mesh2d, spatial.getX(), spatial.getY(), spatial.getAngle());
		}
	}

}
