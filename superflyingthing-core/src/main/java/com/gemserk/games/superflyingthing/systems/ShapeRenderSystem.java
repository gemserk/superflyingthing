package com.gemserk.games.superflyingthing.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.graphics.GL10;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.ShapeComponent;

public class ShapeRenderSystem extends EntityProcessingSystem {

	private final Libgdx2dCamera camera;

	public ShapeRenderSystem(Libgdx2dCamera camera) {
		super(ShapeComponent.class);
		this.camera = camera;
	}
	
	@Override
	protected void begin() {
		camera.apply();
	}
	
	@Override
	protected void process(Entity e) {
		ShapeComponent shapeComponent = e.getComponent(ShapeComponent.class);
		if (shapeComponent == null)
			return;
		Spatial spatial = ComponentWrapper.getSpatial(e);
		if (spatial == null)
			return;
		drawMesh(shapeComponent, spatial);
	}

	private void drawMesh(ShapeComponent shapeComponent, Spatial spatial) {
		if (shapeComponent.texture != null)
			shapeComponent.texture.bind();
		ImmediateModeRendererUtils.draw(GL10.GL_TRIANGLES, shapeComponent.mesh2d, spatial.getX(), spatial.getY(), spatial.getAngle());
	}

}