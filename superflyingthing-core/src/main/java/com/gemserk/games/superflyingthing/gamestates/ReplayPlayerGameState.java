package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.scenes.ReplayGameSceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.resources.ResourceManager;

public class ReplayPlayerGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;

	// SpriteBatch spriteBatch;
	// Libgdx2dCamera worldCamera;

	// World physicsWorld;
	// Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	boolean resetPressed;

	// Container guiContainer;
	// Libgdx2dCamera guiCamera;

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
		
		Injector injector = new InjectorImpl();
		
		injector.bind("resourceManager", resourceManager);
		injector.bind("timeStepProvider", new TimeStepProviderGameStateImpl(this));

		world = new World();
		worldWrapper = new WorldWrapper(world);
		
		Level level = getParameters().get("level");
		ReplayList replayList = getParameters().get("replayList");

		SceneTemplate sceneTemplate = injector.getInstance(ReplayGameSceneTemplate.class);
		sceneTemplate.getParameters().put("backgroundEnabled", game.getGamePreferences().isFirstBackgroundEnabled());
		sceneTemplate.getParameters().put("level", level);
		sceneTemplate.getParameters().put("replayList", replayList);
		sceneTemplate.apply(worldWrapper);

		injector.injectMembers(this);

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
		// guiCamera.apply(spriteBatch);
		// spriteBatch.begin();
		// guiContainer.draw(spriteBatch);
		// spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		// guiContainer.update();
		worldWrapper.update(getDeltaInMs());
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().show();
		super.resume();
		Gdx.input.setCatchBackKey(true);
//		game.getBackgroundGameScreen().dispose();
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
		// spriteBatch.dispose();
	}

}
