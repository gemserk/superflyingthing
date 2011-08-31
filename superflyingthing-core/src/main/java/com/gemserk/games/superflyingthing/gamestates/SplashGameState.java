package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.games.superflyingthing.CustomResourceManager;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.resources.progress.TaskQueue;
import com.gemserk.resources.progress.tasks.SimulateLoadingTimeRunnable;

public class SplashGameState extends com.gemserk.commons.gdx.gamestates.LoadingGameState {

	private final Game game;

	private GL10 gl;
	private SpriteBatch spriteBatch;
	private BitmapFont font;

	private ResourceManager<String> resourceManager;

	private Sprite gemserkLogo;
	private Sprite lwjglLogo;
	private Sprite libgdxLogo;
	private Sprite gemserkLogoBlur;

	private Color blurColor = new Color();

	private TimeTransition timeTransition;

	private InputAdapter inputProcessor = new InputAdapter() {
		@Override
		public boolean keyUp(int keycode) {
			mainMenu();
			return super.keyUp(keycode);
		}

		@Override
		public boolean touchUp(int x, int y, int pointer, int button) {
			mainMenu();
			return super.touchUp(x, y, pointer, button);
		}
	};

	public SplashGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		super.init();
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		int centerX = width / 2;
		int centerY = height / 2;

		gl = Gdx.graphics.getGL10();
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(1f, 1f, 0f, 1f);

		resourceManager = new ResourceManagerImpl<String>();

		GameResources.load(resourceManager);

		gemserkLogo = resourceManager.getResourceValue("GemserkLogo");
		gemserkLogoBlur = resourceManager.getResourceValue("GemserkLogoBlur");
		lwjglLogo = resourceManager.getResourceValue("LwjglLogo");
		libgdxLogo = resourceManager.getResourceValue("LibgdxLogo");

		SpriteUtils.resize(gemserkLogo, width * 0.8f);
		SpriteUtils.resize(gemserkLogoBlur, width * 0.8f);
		SpriteUtils.resize(lwjglLogo, width * 0.2f);
		SpriteUtils.resize(libgdxLogo, width * 0.2f);

		SpriteUtils.centerOn(gemserkLogo, centerX, centerY);
		SpriteUtils.centerOn(gemserkLogoBlur, centerX, centerY);
		SpriteUtils.centerOn(lwjglLogo, width * 0.85f, lwjglLogo.getHeight() * 0.5f);
		SpriteUtils.centerOn(libgdxLogo, width * 0.15f, libgdxLogo.getHeight() * 0.5f);

		Synchronizers.transition(blurColor, Transitions.transitionBuilder(new Color(0f, 0f, 1f, 0f)).end(new Color(0f, 0f, 1f, 1f)).time(1f));

		timeTransition = new TimeTransition();
		timeTransition.start(2f);

		TaskQueue taskQueue = super.getTaskQueue();

		taskQueue.add(new SimulateLoadingTimeRunnable(0));
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				CustomResourceManager<String> resourceManager = game.getResourceManager();
				ArrayList<String> registeredResources = resourceManager.getRegisteredResources();
				for (int i = 0; i < registeredResources.size(); i++) {
					String resourceId = registeredResources.get(i);
					resourceManager.get(resourceId).reload();
					// it would be nicer to have load/unload in the resource API, so I could call load() here, not reload()
				}

			}
		}, "Loading assets");
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				int levelsCount = Levels.levelsCount();
				for (int i = 1; i <= levelsCount; i++) {
					Levels.level(i);
				}
			}
		}, "Loading levels");
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				game.transition(Screens.MainMenu) //
						.leaveTime(250) //
						.enterTime(250) //
						.start();
			}
		});
	}

	private void mainMenu() {
		// game.transition(Screens.MainMenu) //
		// .disposeCurrent() //
		// .leaveTime(250) //
		// .enterTime(250) //
		// .start();
	}

	@Override
	public void render() {
		// super.render();

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		gemserkLogoBlur.setColor(blurColor);
		gemserkLogoBlur.draw(spriteBatch);
		gemserkLogo.draw(spriteBatch);
		if (Gdx.app.getType() != ApplicationType.Android)
			lwjglLogo.draw(spriteBatch);
		libgdxLogo.draw(spriteBatch);

		float percentage = getTaskQueue().getProgress().getPercentage();
		String currentTaskName = getTaskQueue().getCurrentTaskName();
		if ("".equals(currentTaskName))
			currentTaskName = "Loading ";
		SpriteBatchUtils.drawMultilineTextCentered(spriteBatch, font, currentTaskName + " - " + (int) (percentage) + "%...", // 
				Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.25f);

		spriteBatch.end();

		super.render();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());

		timeTransition.update(getDelta());

		if (!timeTransition.isFinished())
			return;

		mainMenu();
	}

	@Override
	public void resume() {
		game.getAdWhirlViewHandler().hide();
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void pause() {
		if (Gdx.input.getInputProcessor() == inputProcessor)
			Gdx.input.setInputProcessor(null);
	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
