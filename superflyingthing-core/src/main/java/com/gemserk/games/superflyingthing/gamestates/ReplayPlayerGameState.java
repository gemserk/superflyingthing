package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.scenes.ReplayGameSceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.resources.ResourceManager;

public class ReplayPlayerGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;
	SoundPlayer soundPlayer;

	EntityBuilder entityBuilder;
	WorldWrapper worldWrapper;
	EventManager eventManager;

	private InputAdapter inputProcessor = new InputAdapter() {

		public boolean keyUp(int keycode) {
			eventManager.registerEvent(Events.gameOver, ReplayPlayerGameState.this);
//			nextScreen();
			return super.keyUp(keycode);
		};

		public boolean touchUp(int x, int y, int pointer, int button) {
			eventManager.registerEvent(Events.gameOver, ReplayPlayerGameState.this);
//			nextScreen();
			return false;
		};

	};

	@Override
	public void init() {
		Injector injector = new InjectorImpl();

		injector.bind("soundPlayer", soundPlayer);
		injector.bind("resourceManager", resourceManager);
		injector.bind("timeStepProvider", new TimeStepProviderGameStateImpl(this));

		worldWrapper = new WorldWrapper(new World());

		Level level = getParameters().get("level");
		ReplayList replayList = getParameters().get("replayList");

		SceneTemplate sceneTemplate = injector.getInstance(ReplayGameSceneTemplate.class);
		sceneTemplate.getParameters().put("backgroundEnabled", game.getGamePreferences().isFirstBackgroundEnabled());
		sceneTemplate.getParameters().put("level", level);
		sceneTemplate.getParameters().put("replayList", replayList);
		sceneTemplate.apply(worldWrapper);

		injector.injectMembers(this);
		
		eventManager.register(Events.gameOver, new EventListener() {
			@Override
			public void onEvent(Event event) {
				nextScreen();
				
				World world = worldWrapper.getWorld();
				Entity replayLabel = world.getTagManager().getEntity(Groups.ReplayLabel);
				if (replayLabel != null)
					replayLabel.delete();
			}
		});

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
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		worldWrapper.update(getDeltaInMs());
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().show();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		// game.getBackgroundGameScreen().dispose();
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
