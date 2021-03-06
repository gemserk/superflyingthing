package com.gemserk.games.superflyingthing.scenes;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
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
import com.gemserk.commons.artemis.systems.SoundSpawnerSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.systems.TextLocationUpdateSystem;
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
import com.gemserk.commons.reflection.Injector;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.gamestates.LevelLoader;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.BasicAIControllerTemplate;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.EventManagerTemplate;
import com.gemserk.games.superflyingthing.templates.LabelTemplate;
import com.gemserk.games.superflyingthing.templates.NormalModeGameLogicTemplate;
import com.gemserk.games.superflyingthing.templates.SoundSpawnerTemplate;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class BackgroundSceneTemplate extends SceneTemplateImpl {

	ResourceManager<String> resourceManager;
	TimeStepProvider timeStepProvider;
	Injector injector;

	public void apply(WorldWrapper worldWrapper) {

		Boolean backgroundEnabled = getParameters().get("backgroundEnabled", true);
		Integer levelNumber = getParameters().get("levelNumber", 1);
		Rectangle adsArea = getParameters().get("adsArea");

		final EventManager eventManager = new EventManagerImpl();

		final com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(), false);

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		Libgdx2dCamera hudCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		RenderLayers renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), backgroundEnabled);
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), backgroundEnabled);
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		renderLayers.add(Layers.Hud, new RenderLayerSpriteBatchImpl(200, 10000, hudCamera));

		World world = worldWrapper.getWorld();

		EntityFactory entityFactory = new EntityFactoryImpl(world);
		EntityBuilder entityBuilder = new EntityBuilder(world);

		injector.bind("renderLayers", renderLayers);
		injector.bind("physicsWorld", physicsWorld);
		injector.bind("resourceManager", resourceManager);
		injector.bind("entityBuilder", entityBuilder);
		injector.bind("entityFactory", entityFactory);
		injector.bind("eventManager", eventManager);
		injector.bind("bodyBuilder", new BodyBuilder(physicsWorld));
		injector.bind("mesh2dBuilder", new Mesh2dBuilder());
		injector.bind("jointBuilder", new JointBuilder(physicsWorld));

		EntityTemplates entityTemplates = new EntityTemplates(injector);

		// add render and all stuff...

		worldWrapper.addUpdateSystem(new PreviousStateSpatialSystem());
		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));
		worldWrapper.addUpdateSystem(injector.getInstance(SoundSpawnerSystem.class));

		worldWrapper.addRenderSystem(new CameraUpdateSystem(timeStepProvider));
		worldWrapper.addRenderSystem(new SpriteUpdateSystem(timeStepProvider));
		worldWrapper.addRenderSystem(new TextLocationUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		EntityTemplate basicAiControllerTemplate = injector.getInstance(BasicAIControllerTemplate.class);
		EntityTemplate eventManagerTemplate = injector.getInstance(EventManagerTemplate.class);
		EntityTemplate normalModeGameLogicTemplate = injector.getInstance(NormalModeGameLogicTemplate.class);
		EntityTemplate labelTemplate = injector.getInstance(LabelTemplate.class);
		EntityTemplate soundSpawnerTemplate = injector.getInstance(SoundSpawnerTemplate.class);

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, new ParametersWrapper() //
				.put("color", Color.WHITE) //
				.put("layer", -999) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, new ParametersWrapper() //
				.put("color", new Color(0.2f, 0.2f, 0.2f, 0.3f)) //
				.put("layer", 201) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "WhiteRectangle") //
				);

		Resource<Level> levelResource = resourceManager.get(Levels.levelId(levelNumber));
		Level level = levelResource.get();

		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, false).loadLevel(level);

		entityFactory.instantiate(basicAiControllerTemplate);

		entityFactory.instantiate(normalModeGameLogicTemplate, new ParametersWrapper());

		entityFactory.instantiate(eventManagerTemplate);

		entityFactory.instantiate(entityTemplates.secondCameraTemplate, new ParametersWrapper() //
				.put("camera", new CameraImpl()) //
				.put("libgdx2dCamera", secondBackgroundLayerCamera)//
				);

		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);

		entityFactory.instantiate(labelTemplate, new ParametersWrapper() //
				.put("position", new Vector2(Gdx.graphics.getWidth() * 0.40f, Gdx.graphics.getHeight() * 0.02f + adsArea.getHeight())) //
				.put("fontId", "VersionFont") //
				.put("text", "Preview level " + levelNumber + "...") //
				.put("layer", 250) //
				.put("center", new Vector2(0f, 1f))//
				.put("color", Colors.yellow) //
				);

		entityFactory.instantiate(soundSpawnerTemplate, new ParametersWrapper() //
				.put("soundId", "ExplosionSound") //
				.put("eventId", Events.explosion)//
				);
	}

}
