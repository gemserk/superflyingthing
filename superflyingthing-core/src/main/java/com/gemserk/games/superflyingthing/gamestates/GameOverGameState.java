package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.resources.ResourceManager;

public class GameOverGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private Sprite whiteRectangle;
	Container container;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public GameOverGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		container = new Container();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangle.setSize(width, height);
		whiteRectangle.setColor(0f, 0f, 0f, 0.25f);

		// container.add(GuiControls.label("YOUR SCORE HERE") //
		// .position(centerX, height * 0.9f) //
		// .color(Color.GREEN) //
		// .font(titleFont)//
		// .build());

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode)
			container.add(GuiControls.textButton() //
					.position(centerX, height * 0.7f) //
					.text("Try Again") //
					.font(buttonFont) //
					.overColor(Color.GREEN) //
					.notOverColor(Color.WHITE)//
					.boundsOffset(20, 20f) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased() {
							restartLevel();
						}
					})//
					.build());

		String nextLevelText = "Next Level";
		if (!Levels.hasLevel(GameInformation.level + 1) && GameInformation.gameMode == GameInformation.ChallengeGameMode)
			nextLevelText = "Select Level";

		container.add(GuiControls.textButton() //
				.position(centerX, height * 0.5f) //
				.text(nextLevelText) //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						nextLevel();
					}
				})//
				.build());

		container.add(GuiControls.textButton() //
				.position(centerX, height * 0.3f) //
				.text("Main Menu") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						mainMenu();
					}
				})//
				.build());

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("mainMenu", Keys.BACK, Keys.ESCAPE);
				monitorKeys("restartLevel", Keys.R);
				monitorKeys("nextLevel", Keys.SPACE, Keys.ENTER, Keys.N);
			}
		};

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackEvent("/challenge/" + (GameInformation.level + 1), "deaths", "Level finished", GameInformation.gameData.deaths);
			Analytics.traker.trackEvent("/challenge/" + (GameInformation.level + 1), "stars", "Level finished", GameInformation.gameData.currentItems);
		}
	}

	private void nextLevel() {
		if (!Levels.hasLevel(GameInformation.level + 1) && GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			GameInformation.level = 0;
			game.transition(game.getLevelSelectionScreen()) //
					.disposeCurrent() //
					.leaveTime(250) //
					.enterTime(250) //
					.leaveTransitionHandler(new TransitionHandler() {
						@Override
						public void onEnd() {
							game.getPlayScreen().dispose();
						}
					}).start();

		} else {
			GameInformation.level++;
			game.transition(game.getPlayScreen()) //
					.leaveTime(250) //
					.enterTime(250) //
					.disposeCurrent() //
					.leaveTransitionHandler(new TransitionHandler() {
						@Override
						public void onEnd() {
							game.getPlayScreen().restart();
						}
					}).start();
		}
	}

	private void mainMenu() {
		game.transition(game.getMainMenuScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.disposeCurrent() //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().dispose();
					}
				}).start();

		// I hate this code here...
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/exit", "/challenge/" + (GameInformation.level + 1) + "/exit", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/exit", "/practice/exit", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/exit", "/random/exit", null);
		}
	}

	private void restartLevel() {
		game.transition(game.getPlayScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.disposeCurrent() //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().restart();
					}
				}).start();

		// I hate this code here...
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/tryagain", "/challenge/" + (GameInformation.level + 1) + "/tryagain", null);
		}
	}

	@Override
	public void show() {
		super.show();
		game.getPlayScreen().show();
	}

	@Override
	public void hide() {
		super.hide();
		game.getPlayScreen().hide();
	}

	@Override
	public void resume() {
		game.getAdWhirlViewHandler().show();
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		game.getPlayScreen().render(delta);

		spriteBatch.begin();
		whiteRectangle.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {

		if (GameInformation.worldWrapper != null)
			GameInformation.worldWrapper.update(delta);

		Synchronizers.synchronize(delta);
		inputDevicesMonitor.update();
		container.update();

		if (inputDevicesMonitor.getButton("mainMenu").isReleased())
			mainMenu();

		if (inputDevicesMonitor.getButton("restartLevel").isReleased())
			restartLevel();

		if (inputDevicesMonitor.getButton("nextLevel").isReleased())
			nextLevel();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
