package com.gemserk.games.superflyingthing.scenes;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.resources.ResourceManager;

public class EmptySceneTemplate {

	private ResourceManager<String> resourceManager;
	
	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}
	
	public void apply(WorldWrapper worldWrapper) {

		RenderLayers renderLayers = new RenderLayers();

		final Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera));
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, backgroundLayerCamera));

		World world = worldWrapper.getWorld();
		
		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.init();

		EntityFactory entityFactory = new EntityFactoryImpl(world);
		Parameters parameters = new ParametersWrapper();

		EntityTemplates entityTemplates = new EntityTemplates(null, world, resourceManager, new EntityBuilder(world), new EntityFactoryImpl(world), null);

		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0f)) //
				.put("center", new Vector2(0f, 0f)) //
				.put("spriteId", "BackgroundSprite") //
				);

		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.GREEN) //
				.put("layer", (-400)) //
				.put("spatial", new SpatialImpl((Gdx.graphics.getWidth() * 0.57f), (Gdx.graphics.getHeight() * 0.23f), 160f, 160f, 86f)) //
				.put("center", new Vector2(0.5f, 0.5f)) //
				.put("spriteId", "FogSprite") //
				);
		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.RED) //
				.put("layer", (-400)) //
				.put("spatial", new SpatialImpl((Gdx.graphics.getWidth() * 0.24f), (Gdx.graphics.getHeight() * 0.68f), 120f, 120f, 189f)) //
				.put("center", new Vector2(0.5f, 0.5f)) //
				.put("spriteId", "FogSprite") //
				);
		
	}

}
