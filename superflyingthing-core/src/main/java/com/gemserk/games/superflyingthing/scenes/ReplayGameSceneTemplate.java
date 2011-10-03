package com.gemserk.games.superflyingthing.scenes;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.Handles;
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
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.time.TimeStepProvider;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.gamestates.LevelLoader;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.EventManagerTemplate;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.games.superflyingthing.templates.LabelTemplate;
import com.gemserk.games.superflyingthing.templates.TimerTemplate;
import com.gemserk.resources.ResourceManager;

public class ReplayGameSceneTemplate extends SceneTemplateImpl {

	public static class ReplayScript extends ScriptJavaImpl {

		EntityFactory entityFactory;
		EventManager eventManager;
		Injector injector;

		private World world;

		@Override
		public void init(com.artemis.World world, Entity e) {
			this.world = world;

			Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
			TargetComponent targetComponent = GameComponents.getTargetComponent(mainCamera);

			Entity mainReplayShip = world.getTagManager().getEntity(Groups.MainReplayShip);
			targetComponent.setTarget(mainReplayShip);

			ReplayComponent replayComponent = mainReplayShip.getComponent(ReplayComponent.class);
			Replay replay = replayComponent.replay;

			EntityTemplate timerTemplate = injector.getInstance(TimerTemplate.class);

			// also starts a timer to invoke game over game state
			entityFactory.instantiate(timerTemplate, new ParametersWrapper() //
					.put("time", (float) (replay.duration - 100) * 0.001f) //
					.put("eventId", Events.gameOver));

			eventManager.registerEvent(Events.gameStarted, e);
		}

		@Handles(ids = Events.gameStarted)
		public void resetCameraZoomWhenGameStarted(Event event) {
			Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
			CameraComponent cameraComponent = Components.getCameraComponent(mainCamera);

			Camera camera = cameraComponent.getCamera();
			camera.setZoom(Gdx.graphics.getWidth() * 24f / 800f);
		}

		@Handles(ids = Events.gameOver)
		public void gameOver(Event event) {
			Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
			TargetComponent targetComponent = GameComponents.getTargetComponent(mainCamera);
			targetComponent.setTarget(null);
		}

	}

	TimeStepProvider timeStepProvider;
	ResourceManager<String> resourceManager;
	Injector injector;

	@Override
	public void apply(WorldWrapper worldWrapper) {

		Boolean backgroundEnabled = getParameters().get("backgroundEnabled", true);

		EventManager eventManager = new EventManagerImpl();

		com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(), false);

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
		injector.bind("entityBuilder", entityBuilder);
		injector.bind("entityFactory", entityFactory);
		injector.bind("eventManager", eventManager);
		injector.bind("bodyBuilder", new BodyBuilder(physicsWorld));
		injector.bind("mesh2dBuilder", new Mesh2dBuilder());
		injector.bind("jointBuilder", new JointBuilder(physicsWorld));

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

		EntityTemplates entityTemplates = new EntityTemplates(injector);

		// creates and registers all the controller templates

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, new ParametersWrapper() //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		// loadLevelForChallengeMode();

		Level level = getParameters().get("level");
		ReplayList replayList = getParameters().get("replayList");

		// loadLevel(level, true);

		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, false).loadLevel(level);

		ArrayList<Replay> replays = replayList.getReplays();

		for (int i = 0; i < replays.size(); i++) {

			// could be a flag on the replay itself instead checking the last one.
			Replay replay = replays.get(i);

			Entity replayShip = entityFactory.instantiate(entityTemplates.replayShipTemplate, new ParametersWrapper() //
					.put("replay", replay) //
					);

			entityFactory.instantiate(entityTemplates.replayPlayerTemplate, new ParametersWrapper() //
					.put("replay", replay) //
					.put("target", replayShip) //
					);

		}

		entityBuilder //
				.component(new ScriptComponent(injector.getInstance(ReplayScript.class))) //
				.build();

		entityFactory.instantiate(entityTemplates.secondCameraTemplate, new ParametersWrapper() //
				.put("camera", new CameraImpl()) //
				.put("libgdx2dCamera", secondBackgroundLayerCamera)//
				);

		// creates a new particle emitter spawner template which creates a new explosion when the ship dies.
		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);

		// create gui label..

		// BitmapFont levelFont = resourceManager.getResourceValue("LevelFont");

		EntityTemplate eventManagerTemplate = injector.getInstance(EventManagerTemplate.class);
		EntityTemplate labelTemplate = injector.getInstance(LabelTemplate.class);

		entityFactory.instantiate(eventManagerTemplate);

		entityFactory.instantiate(labelTemplate, new ParametersWrapper() //
				.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.9f)) //
				.put("text", "Playing replay, touch to continue...") //
				.put("fontId", "LevelFont") //
				.put("layer", 250) //
				.put("center", new Vector2(0.5f, 0f))
		// .put color
				);

		// guiContainer.add(GuiControls.label("Playing replay, touch to continue...") //
		// .position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.9f) //
		// .center(0.5f, 0f) //
		// .color(Colors.yellow) //
		// .font(levelFont) //
		// .build());

	}

}
