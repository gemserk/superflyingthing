package com.gemserk.games.superflyingthing.scenes;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.EntityBuilder;
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
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.time.TimeStepProvider;
import com.gemserk.commons.reflection.ObjectConfigurator;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.gamestates.LevelLoader;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.BasicAIControllerTemplate;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.EventManagerTemplate;
import com.gemserk.games.superflyingthing.templates.NormalModeGameLogicTemplate;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class BackgroundSceneTemplate extends SceneTemplateImpl {

	ResourceManager<String> resourceManager;
	TimeStepProvider timeStepProvider;
	ObjectConfigurator objectConfigurator;

	public void apply(WorldWrapper worldWrapper) {

		Boolean backgroundEnabled = getParameters().get("backgroundEnabled", true);
		Integer levelNumber = getParameters().get("levelNumber", 1);

		final EventManager eventManager = new EventManagerImpl();

		final com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(), false);

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		RenderLayers renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), backgroundEnabled);
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), backgroundEnabled);
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));
		
		objectConfigurator.add("renderLayers", renderLayers);

		World world = worldWrapper.getWorld();
		final EntityFactory entityFactory = new EntityFactoryImpl(world);
		parameters = new ParametersWrapper();
		// add render and all stuff...

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

		final EntityBuilder entityBuilder = new EntityBuilder(world);

		objectConfigurator.add("physicsWorld", physicsWorld);
		objectConfigurator.add("resourceManager", resourceManager);
		objectConfigurator.add("entityBuilder", entityBuilder);
		objectConfigurator.add("entityFactory", entityFactory);
		objectConfigurator.add("eventManager", eventManager);
		objectConfigurator.add("bodyBuilder", new BodyBuilder(physicsWorld));
		objectConfigurator.add("mesh2dBuilder", new Mesh2dBuilder());
		objectConfigurator.add("jointBuilder", new JointBuilder(physicsWorld));

		EntityTemplates entityTemplates = new EntityTemplates(objectConfigurator);

		Provider provider = new ProviderImpl(objectConfigurator);

		EntityTemplate basicAiControllerTemplate = provider.get(BasicAIControllerTemplate.class);
		EntityTemplate eventManagerTemplate = provider.get(EventManagerTemplate.class);
		EntityTemplate normalModeGameLogicTemplate = provider.get(NormalModeGameLogicTemplate.class);

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, parameters //
				.put("color", Color.WHITE) //
				.put("layer", -999) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		Resource<Level> levelResource = resourceManager.get(Levels.levelId(levelNumber));
		Level level = levelResource.get();

		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, false).loadLevel(level);

		entityFactory.instantiate(basicAiControllerTemplate);

		entityFactory.instantiate(normalModeGameLogicTemplate, new ParametersWrapper() //
				.put("gameData", new GameData()) //
				);

		entityFactory.instantiate(eventManagerTemplate);

		entityFactory.instantiate(entityTemplates.secondCameraTemplate, new ParametersWrapper() //
				.put("camera", new CameraImpl()) //
				.put("libgdx2dCamera", secondBackgroundLayerCamera)//
				);

		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);
	}

}
