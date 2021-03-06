package com.gemserk.games.superflyingthing.scenes;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.systems.CameraUpdateSystem;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.PreviousStateSpatialSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.time.TimeStepProvider;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.CameraTemplate;
import com.gemserk.games.superflyingthing.templates.StaticSpriteTemplate;
import com.gemserk.resources.ResourceManager;

public class TutorialSceneTemplate extends SceneTemplateImpl {

	ResourceManager<String> resourceManager;
	TimeStepProvider timeStepProvider;
	Injector injector;

	public void apply(WorldWrapper worldWrapper) {
		int screenWidth = Gdx.graphics.getWidth();
		int screenHeight = Gdx.graphics.getHeight();

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl(screenWidth * 0.5f, screenHeight * 0.5f);
		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl(screenWidth * 0.5f, screenHeight * 0.5f);

		RenderLayers renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), true);
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), true);
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		World world = worldWrapper.getWorld();
		EntityFactory entityFactory = new EntityFactoryImpl(world);

		final com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(), false);

		final EventManager eventManager = new EventManagerImpl();

		worldWrapper.addUpdateSystem(new PreviousStateSpatialSystem());
		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));
		worldWrapper.addRenderSystem(new CameraUpdateSystem(timeStepProvider));
		worldWrapper.addRenderSystem(new SpriteUpdateSystem(timeStepProvider));
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();
		
		injector.bind("bodyBuilder", new BodyBuilder(physicsWorld));
		injector.bind("eventManager", eventManager);

		float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;
		Camera camera = new CameraImpl(0f, 0f, cameraZoom, 0f);

		// EntityTemplate starTemplate = templateProvider.get(StarTemplate.class);
		EntityTemplate cameraTemplate = injector.getInstance(CameraTemplate.class);
		EntityTemplate staticSpriteTemplate = injector.getInstance(StaticSpriteTemplate.class);

		entityFactory.instantiate(staticSpriteTemplate, new ParametersWrapper() //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		entityFactory.instantiate(cameraTemplate, new ParametersWrapper() //
				.put("camera", camera) //
				.put("libgdxCamera", worldCamera) //
				.put("spatial", new SpatialImpl(0f, 0f, 1f, 1f, 0f))//
				);

	}

}
