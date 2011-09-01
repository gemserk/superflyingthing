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
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.resources.ResourceManager;

public class PauseGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private Sprite whiteRectangle;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
	private Integer levelNumber;
	private Container container;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public PauseGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		levelNumber = getParameters().get("level");

		spriteBatch = new SpriteBatch();
		
		container = new Container();

		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangle.setSize(width, height);
		whiteRectangle.setColor(0f, 0f, 0f, 0.5f);

		panel.add(GuiControls.label("Game Paused") //
				.position(centerX, height * 0.60f) //
				.color(Color.GREEN) //
				.font(titleFont)//
				.build());

		panel.add(GuiControls.textButton() //
				.position(centerX, height * 0.48f) //
				.text("Resume") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						resumeLevel();
					}
				})//
				.build());
		panel.add(GuiControls.textButton() //
				.position(centerX, height * 0.36f) //
				.text("Instructions") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						instructions();
					}
				})//
				.build());
		panel.add(GuiControls.textButton() //
				.position(centerX, height * 0.24f) //
				.text("Settings") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						settings();
					}
				})//
				.build());
		panel.add(GuiControls.textButton() //
				.position(centerX, height * 0.12f) //
				.text("Restart") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						restartLevel();
					}
				})//
				.build());
		panel.add(GuiControls.textButton() //
				.position(centerX, height * 0f) //
				.text("Main Menu") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						mainMenu();
					}
				})//
				.build());
		
		container.add(panel);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("resume", Keys.BACK, Keys.ESCAPE, Keys.SPACE, Keys.ENTER);
			}
		};
	}

	private void instructions() {
		game.getGamePreferences().setTutorialEnabled(true);
		game.transition(Screens.Instructions) //
				.leaveTime(500) //
				.enterTime(0) //
				.disposeCurrent() //
				.start();
	}

	private void settings() {
		// sets previous screen....
		game.transition(Screens.Settings) //
				.disposeCurrent(false) //
				.parameter("previousScreen", Screens.Pause)//
				.start();
	}

	private void restartLevel() {
		game.transition(Screens.Play) //
				.disposeCurrent() //
				.parameter("level", levelNumber) //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().restart();
					}
				}).start();

		Gdx.app.log("SuperFlyingThing", "Restarting level " + levelNumber);

		// I hate this code here...
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "deaths", "Level not finished", GameInformation.gameData.deaths);
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "stars", "Level not finished", GameInformation.gameData.currentItems);
			Analytics.traker.trackPageView("/challenge/" + levelNumber + "/restart", "/challenge/" + levelNumber + "/restart", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/restart", "/practice/restart", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/restart", "/random/restart", null);
		}
	}

	private void mainMenu() {
		game.transition(Screens.MainMenu) //
				.disposeCurrent() //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().dispose();
					}
				}).start();

		// I hate this code here...
		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "deaths", "Level not finished", GameInformation.gameData.deaths);
			Analytics.traker.trackEvent("/challenge/" + levelNumber, "stars", "Level not finished", GameInformation.gameData.currentItems);
			Analytics.traker.trackPageView("/challenge/" + levelNumber + "/exit", "/challenge/" + levelNumber + "/exit", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/exit", "/practice/exit", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/exit", "/random/exit", null);
		}
	}

	private void resumeLevel() {
		game.transition(Screens.Play) //
				.disposeCurrent() //
				.start();
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
		super.resume();
		game.getAdWhirlViewHandler().show();
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		game.getPlayScreen().render();
		spriteBatch.begin();
		whiteRectangle.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		inputDevicesMonitor.update();
		container.update();
		if (inputDevicesMonitor.getButton("resume").isReleased())
			resumeLevel();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
