package com.gemserk.games.superflyingthing.scenes;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.components.TextComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
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
import com.gemserk.commons.artemis.systems.TextLocationUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
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
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.PropertiesComponent;
import com.gemserk.games.superflyingthing.gamestates.GameInformation;
import com.gemserk.games.superflyingthing.gamestates.LevelLoader;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.EventManagerTemplate;
import com.gemserk.games.superflyingthing.templates.NormalModeGameLogicTemplate;
import com.gemserk.resources.ResourceManager;

public class NormalModeSceneTemplate extends SceneTemplateImpl {

	public static class ItemsTakenLabelEntityTemplate extends EntityTemplateImpl {

		private class UpdateItemsTakenScript extends ScriptJavaImpl {
			private int currentItems;

			@Override
			public void init(com.artemis.World world, Entity e) {
				currentItems = -1;
			}

			@Override
			public void update(com.artemis.World world, Entity e) {

				PropertiesComponent propertiesComponent = ComponentWrapper.getPropertiesComponent(e);
				GameData gameData = (GameData) propertiesComponent.properties.get("gameData");

				if (currentItems == gameData.currentItems)
					return;

				TextComponent textComponent = Components.getTextComponent(e);
				textComponent.text = MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems);
			}
		}

		ResourceManager<String> resourceManager;

		@Override
		public void apply(Entity entity) {
			GameData gameData = parameters.get("gameData");

			BitmapFont font = resourceManager.getResourceValue("GameFont");

			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("gameData", gameData);

			entity.addComponent(new TagComponent("ItemsTakenLabel"));
			entity.addComponent(new SpatialComponent(new SpatialImpl(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.95f)));
			entity.addComponent(new RenderableComponent(250));
			entity.addComponent(new TextComponent("", font, 0f, 0f, 0.5f, 0.5f));
			entity.addComponent(new PropertiesComponent(properties));
			entity.addComponent(new ScriptComponent(new UpdateItemsTakenScript()));
		}

	}

	public static class TimerLabelEntityTemplate extends EntityTemplateImpl {

		private class UpdateLabelScript extends ScriptJavaImpl {

			private StringBuilder timerLabelBuilder = new StringBuilder();
			private final String format = "Time: %1$.2f";

			@Override
			public void update(com.artemis.World world, Entity e) {
				PropertiesComponent propertiesComponent = ComponentWrapper.getPropertiesComponent(e);
				GameData gameData = (GameData) propertiesComponent.properties.get("gameData");

				timerLabelBuilder.delete(0, timerLabelBuilder.length());
				timerLabelBuilder.append(String.format(Locale.US, format, gameData.travelTime));

				TextComponent textComponent = Components.getTextComponent(e);
				textComponent.text = timerLabelBuilder;
			}

		}

		ResourceManager<String> resourceManager;

		@Override
		public void apply(Entity entity) {
			GameData gameData = parameters.get("gameData");

			BitmapFont font = resourceManager.getResourceValue("GameFont");

			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("gameData", gameData);

			entity.addComponent(new TagComponent("TimerLabel"));
			entity.addComponent(new SpatialComponent(new SpatialImpl(Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getHeight() * 0.95f)));
			entity.addComponent(new RenderableComponent(250));
			entity.addComponent(new TextComponent("", font, 0f, 0f, 0f, 0.5f));
			entity.addComponent(new PropertiesComponent(properties));
			entity.addComponent(new ScriptComponent(new UpdateLabelScript()));
		}

	}
	
	ResourceManager<String> resourceManager;
	TimeStepProvider timeStepProvider;
	Injector injector;

	public void apply(WorldWrapper worldWrapper) {

		Boolean backgroundEnabled = getParameters().get("backgroundEnabled", false);
		Boolean shouldRemoveItems = getParameters().get("shouldRemoveItems", true);

		// Boolean randomLevel = getParameters().get("randomLevel", false);
		// Integer levelNumber = getParameters().get("levelNumber");

		Level level = getParameters().get("level");
		GameData gameData = getParameters().get("gameData");

		EventManager eventManager = new EventManagerImpl();

		World physicsWorld = new World(new Vector2(), false);

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

		com.artemis.World world = worldWrapper.getWorld();
		EntityFactory entityFactory = new EntityFactoryImpl(world);
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
		worldWrapper.addRenderSystem(new TextLocationUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		EntityBuilder entityBuilder = new EntityBuilder(world);

		injector.bind("physicsWorld", physicsWorld);
		injector.bind("entityBuilder", entityBuilder);
		injector.bind("entityFactory", new EntityFactoryImpl(world));
		injector.bind("eventManager", eventManager);
		injector.bind("bodyBuilder", new BodyBuilder(physicsWorld));
		injector.bind("mesh2dBuilder", new Mesh2dBuilder());
		injector.bind("jointBuilder", new JointBuilder(physicsWorld));
		injector.bind("renderLayers", renderLayers);

		EntityTemplates entityTemplates = new EntityTemplates(injector);

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, parameters //
				.put("color", Color.WHITE) //
				.put("layer", -999) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		EntityTemplate eventManagerTemplate = injector.getInstance(EventManagerTemplate.class);
		EntityTemplate normalModeGameLogicTemplate = injector.getInstance(NormalModeGameLogicTemplate.class);

		entityFactory.instantiate(eventManagerTemplate);

		entityFactory.instantiate(normalModeGameLogicTemplate, new ParametersWrapper() //
				.put("invulnerable", GameInformation.gameMode == GameInformation.PracticeGameMode) //
				);

		entityFactory.instantiate(entityTemplates.replayRecorderTemplate);

		entityFactory.instantiate(entityTemplates.secondCameraTemplate, new ParametersWrapper() //
				.put("camera", new CameraImpl()) //
				.put("libgdx2dCamera", secondBackgroundLayerCamera)//
				);

		// creates a new particle emitter spawner template which creates a new explosion when the ship dies.
		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);

		// if (randomLevel) {
		// Resource<Document> resource = resourceManager.get("RandomLevelTilesDocument");
		// RandomLevelGenerator randomLevelGenerator = new RandomLevelGenerator(resource.get());
		// level = randomLevelGenerator.generateRandomLevel();
		// } else {
		// if (Levels.hasLevel(levelNumber)) {
		// Resource<Level> levelResource = resourceManager.get(Levels.levelId(levelNumber));
		// level = levelResource.get();
		// }
		// }

		EntityTemplate itemTakenLabelTemplate = injector.getInstance(ItemsTakenLabelEntityTemplate.class);
		EntityTemplate timerLabelTemplate = injector.getInstance(TimerLabelEntityTemplate.class);

		// EntityTemplate gameModeTemplate = injector.getInstance(GameModeEntityTemplate.class);

		// HashMap<String, Object> gameModeProperties = new HashMap<String, Object>();
		// gameModeProperties.put("totalItems", level.items.size());
		// gameModeProperties.put("currentItems", 0);
		// entityFactory.instantiate(gameModeTemplate, new ParametersWrapper().put("properties", gameModeProperties));

		
		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, shouldRemoveItems).loadLevel(level);
		
		gameData.totalItems = level.items.size();
		
		if (gameData.totalItems > 0)
			entityFactory.instantiate(itemTakenLabelTemplate, new ParametersWrapper().put("gameData", gameData));

		entityFactory.instantiate(timerLabelTemplate, new ParametersWrapper().put("gameData", gameData));
		
	}

}
