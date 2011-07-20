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
import com.gemserk.resources.ResourceManager;

public class PauseGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private Sprite whiteRectangle;
	Container container;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

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

		spriteBatch = new SpriteBatch();

		container = new Container();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangle.setSize(width, height);
		whiteRectangle.setColor(0f, 0f, 0f, 0.25f);

		container.add(GuiControls.label("Game Paused") //
				.position(centerX, height * 0.9f) //
				.color(Color.GREEN) //
				.font(titleFont)//
				.build());
		container.add(GuiControls.textButton() //
				.position(centerX, height * 0.7f) //
				.text("Resume") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						resumeLevel();
					}
				})//
				.build());
		container.add(GuiControls.textButton() //
				.position(centerX, height * 0.55f) //
				.text("Instructions") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						game.getGamePreferences().setTutorialEnabled(true);
						game.transition(game.getInstructionsScreen(), 500, 0);
					}
				})//
				.build());
		container.add(GuiControls.textButton() //
				.position(centerX, height * 0.40f) //
				.text("Restart") //
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
		container.add(GuiControls.textButton() //
				.position(centerX, height * 0.25f) //
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
				monitorKeys("resume", Keys.BACK, Keys.ESCAPE, Keys.SPACE, Keys.ENTER);
			}
		};
	}

	private void restartLevel() {
		game.transition(game.getPlayScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().restart();
					}
				}).start();
	}

	private void mainMenu() {
		game.transition(game.getMainMenuScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.leaveTransitionHandler(new TransitionHandler() {
					@Override
					public void onEnd() {
						game.getPlayScreen().dispose();
					}
				}).start();

		if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/challengeMode/finish", "/challengeMode/finish", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/finishPracticeMode", "/finishPracticeMode", null);
		} else if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/finishRandomMode", "/finishRandomMode", null);
		}
	}
	
	private void resumeLevel() {
		game.transition(game.getPlayScreen(), 500, 250);
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
		Synchronizers.synchronize(delta);
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
