package com.gemserk.games.superflyingthing.gamestates;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;

public class GameOverGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;

	SpriteBatch spriteBatch;
	Sprite whiteRectangle;
	Container screen;
	InputDevicesMonitorImpl<String> inputDevicesMonitor;
	WorldWrapper worldWrapper;
	Integer levelNumber;

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		float scale = Gdx.graphics.getHeight() / 480f;

		if (Gdx.graphics.getHeight() > 480f)
			scale = 1f;

		spriteBatch = new SpriteBatch();

		screen = new Container();

		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		levelNumber = getParameters().get("level", 0);

		// BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangle.setSize(width, height);
		whiteRectangle.setColor(0f, 0f, 0f, 0.25f);

		worldWrapper = getParameters().get("worldWrapper");

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {

			panel.add(GuiControls.label(String.format(Locale.US, "Your time was: %1$.2f seconds", GameInformation.gameData.travelTime)).position(centerX, height * 0.6f) //
					.font(buttonFont) //
					.build());

			{
				Sprite squareButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

				panel.add(GuiControls.imageButton(squareButtonSprite) //
						.position(centerX, height * 0.4f) //
						.center(0.5f, 0.5f) //
						.size(squareButtonSprite.getWidth() * scale, squareButtonSprite.getHeight() * scale) //
						.handler(new ButtonHandler() {
							@Override
							public void onReleased(Control control) {
								restartLevel();
							}
						})//
						.build());

				panel.add(GuiControls.textButton() //
						.position(centerX, height * 0.4f) //
						.text("Try Again") //
						.font(buttonFont) //
						.overColor(Color.WHITE) //
						.notOverColor(Colors.yellow)//
						.boundsOffset(20, 20f) //
						.build());
			}
		}

		{
			String nextLevelText = "Next Level";
			if (GameInformation.gameMode == GameInformation.ChallengeGameMode && !Levels.hasLevel(levelNumber + 1))
				nextLevelText = "Select Level";

			Sprite squareButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

			panel.add(GuiControls.imageButton(squareButtonSprite) //
					.position(centerX, height * 0.2f) //
					.center(0.5f, 0.5f) //
					.size(squareButtonSprite.getWidth() * scale, squareButtonSprite.getHeight() * scale) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							nextLevel();
						}
					})//
					.build());

			panel.add(GuiControls.textButton() //
					.position(centerX, height * 0.2f) //
					.text(nextLevelText) //
					.font(buttonFont) //
					.overColor(Color.WHITE) //
					.notOverColor(Colors.yellow)//
					.boundsOffset(20, 20f) //
					.build());
		}

		{
			Sprite squareButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

			panel.add(GuiControls.imageButton(squareButtonSprite) //
					.position(centerX, height * 0f) //
					.center(0.5f, 0.5f) //
					.size(squareButtonSprite.getWidth() * scale, squareButtonSprite.getHeight() * scale) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							mainMenu();
						}
					})//
					.build());

			panel.add(GuiControls.textButton() //
					.position(centerX, height * 0f) //
					.text("Main Menu") //
					.font(buttonFont) //
					.overColor(Color.WHITE) //
					.notOverColor(Colors.yellow)//
					.boundsOffset(20, 20f) //
					.build());
		}

		screen.add(panel);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("mainMenu", Keys.BACK, Keys.ESCAPE);
				monitorKey("restartLevel", Keys.R);
				monitorKeys("nextLevel", Keys.SPACE, Keys.ENTER, Keys.N);
			}
		};

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "deaths", "Level finished", GameInformation.gameData.deaths);
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "stars", "Level finished", GameInformation.gameData.currentItems);
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "fps", "Level finished", GameInformation.gameData.averageFPS);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackEvent("/random", "deaths", "Level finished", GameInformation.gameData.deaths);
			Analytics.traker.trackEvent("/random", "stars", "Level finished", GameInformation.gameData.currentItems);
			Analytics.traker.trackEvent("/random", "fps", "Level finished", GameInformation.gameData.averageFPS);
		}
	}

	private void nextLevel() {
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode && !Levels.hasLevel(levelNumber + 1)) {
			// GameInformation.level = 0;
			game.transition(Screens.LevelSelection) //
					.disposeCurrent() //
					.start();

		} else {
			// GameInformation.level++;
			game.transition(Screens.Play) //
					.disposeCurrent() //
					.parameter("level", levelNumber + 1) //
					.leaveTransitionHandler(new TransitionHandler() {
						@Override
						public void onEnd() {
							game.getPlayScreen().restart();
						}
					}).start();
		}
	}

	private void mainMenu() {
		game.transition(Screens.MainMenu) //
				.disposeCurrent() //
				.start();

		// I hate this code here...
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + levelNumber + "/exit", "/challenge/" + levelNumber + "/exit", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/exit", "/practice/exit", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/exit", "/random/exit", null);
		}
	}

	private void restartLevel() {
		game.transition(Screens.Play) //
				.disposeCurrent() //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().restart();
					}
				}).start();

		// I hate this code here...
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + levelNumber + "/tryagain", "/challenge/" + levelNumber + "/tryagain", null);
		}
	}

	@Override
	public void resume() {
		game.getAdWhirlViewHandler().show();
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (worldWrapper != null)
			worldWrapper.render();

		spriteBatch.begin();
		whiteRectangle.draw(spriteBatch);
		screen.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {

		// if (worldWrapper != null)
		// worldWrapper.update(getDeltaInMs());

		Synchronizers.synchronize(getDelta());
		inputDevicesMonitor.update();
		screen.update();

		if (inputDevicesMonitor.getButton("mainMenu").isReleased())
			mainMenu();

		if (inputDevicesMonitor.getButton("restartLevel").isReleased())
			restartLevel();

		if (inputDevicesMonitor.getButton("nextLevel").isReleased())
			nextLevel();
	}

	@Override
	public void dispose() {

		if (worldWrapper != null)
			worldWrapper.dispose();

		spriteBatch.dispose();
		spriteBatch = null;
	}

}
