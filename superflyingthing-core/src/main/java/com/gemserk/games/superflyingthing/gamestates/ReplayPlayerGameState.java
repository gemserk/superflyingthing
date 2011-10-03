package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
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
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.resources.ResourceManager;

public class ReplayPlayerGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;
	
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	boolean resetPressed;
	Container guiContainer;
	Libgdx2dCamera guiCamera;
	// private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	EntityTemplates entityTemplates;

	EntityBuilder entityBuilder;
	com.artemis.World world;
	WorldWrapper worldWrapper;
	EntityFactory entityFactory;
	Parameters parameters;

	GameData gameData;
	EventManager eventManager;
	RenderLayers renderLayers;

	private InputAdapter inputProcessor = new InputAdapter() {

		public boolean keyUp(int keycode) {
			nextScreen();
			return super.keyUp(keycode);
		};

		public boolean touchUp(int x, int y, int pointer, int button) {
			nextScreen();
			return false;
		};

	};

	@Handles
	public void toggleFirstBackground(Event e) {
		if (renderLayers != null)
			renderLayers.toggle(Layers.FirstBackground);
	}

	@Handles
	public void toggleSecondBackground(Event e) {
		if (renderLayers != null)
			renderLayers.toggle(Layers.SecondBackground);
	}

	@Override
	public void init() {
		resetPressed = false;
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		guiCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		final Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		secondBackgroundLayerCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), game.getGamePreferences().isSecondBackgroundEnabled());
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		world = new com.artemis.World();
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
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

		worldWrapper.addRenderSystem(new CameraUpdateSystem(new TimeStepProviderGameStateImpl(this)));
		worldWrapper.addRenderSystem(new SpriteUpdateSystem(new TimeStepProviderGameStateImpl(this)));

		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		guiContainer = new Container();

		InjectorImpl injectorImpl = new InjectorImpl() {
			{
				bind("physicsWorld", physicsWorld);
				bind("resourceManager", resourceManager);
				bind("entityBuilder", new EntityBuilder(world));
				bind("entityFactory", new EntityFactoryImpl(world));
				bind("eventManager", eventManager);
				bind("bodyBuilder", new BodyBuilder(physicsWorld));
				bind("mesh2dBuilder", new Mesh2dBuilder());
				bind("jointBuilder", new JointBuilder(physicsWorld));
			}
		};

		entityTemplates = new EntityTemplates(injectorImpl);

		// creates and registers all the controller templates

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, parameters //
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

			parameters.clear();
			Entity replayShip = entityFactory.instantiate(entityTemplates.replayShipTemplate, parameters //
					.put("replay", replay) //
					);

			parameters.clear();
			entityFactory.instantiate(entityTemplates.replayPlayerTemplate, parameters //
					.put("replay", replay) //
					.put("target", replayShip) //
					);

		}

		entityBuilder //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					@Override
					public void init(com.artemis.World world, Entity e) {
						Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
						TargetComponent targetComponent = GameComponents.getTargetComponent(mainCamera);

						Entity mainReplayShip = world.getTagManager().getEntity(Groups.MainReplayShip);
						targetComponent.setTarget(mainReplayShip);

						ReplayComponent replayComponent = mainReplayShip.getComponent(ReplayComponent.class);
						Replay replay = replayComponent.replay;

						// also starts a timer to invoke game over game state
						entityFactory.instantiate(entityTemplates.timerTemplate, new ParametersWrapper() //
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
						// mainCamera.delete();
						TargetComponent targetComponent = GameComponents.getTargetComponent(mainCamera);
						targetComponent.setTarget(null);

						nextScreen();
					}

				})) //
				.build();

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager))) //
				.build();

		entityFactory.instantiate(entityTemplates.secondCameraTemplate, new ParametersWrapper() //
				.put("camera", new CameraImpl()) //
				.put("libgdx2dCamera", secondBackgroundLayerCamera)//
				);

		// creates a new particle emitter spawner template which creates a new explosion when the ship dies.
		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);

		// create gui label..

		BitmapFont levelFont = resourceManager.getResourceValue("LevelFont");

		guiContainer.add(GuiControls.label("Playing replay, touch to continue...") //
				.position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.9f) //
				.center(0.5f, 0f) //
				.color(Colors.yellow) //
				.font(levelFont) //
				.build());

	}

	public void nextScreen() {
		game.transition(Screens.GameOver) //
				.leaveTime(0) //
				.enterTime(0) //
				.disposeCurrent(true) //
				.parameter("worldWrapper", worldWrapper) //
				.start();
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		worldWrapper.render();
		guiCamera.apply(spriteBatch);
		spriteBatch.begin();
		guiContainer.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		guiContainer.update();
		worldWrapper.update(getDeltaInMs());
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().show();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getBackgroundGameScreen().dispose();
		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void pause() {
		super.pause();
		if (Gdx.input.getInputProcessor() == inputProcessor)
			Gdx.input.setInputProcessor(null);
	}

	@Override
	public void dispose() {
		// worldWrapper.dispose();
		spriteBatch.dispose();
	}

}
