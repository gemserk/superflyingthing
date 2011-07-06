package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class PauseGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private Sprite whiteRectangle;
	Container container;

	public PauseGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();
		resourceManager = new ResourceManagerImpl<String>();

		GameResources.load(resourceManager);

		container = new Container();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		Text title = new Text("Game Paused", centerX, height * 0.9f).setColor(Color.GREEN);
		title.setFont(titleFont);

		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangle.setSize(width, height);
		whiteRectangle.setColor(0f, 0f, 0f, 0.75f);

		TextButton playButton = GuiControls.textButton() //
				.position(centerX, height * 0.7f) //
				.text("Resume") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						game.transition(game.getPlayScreen(), 500, 250);
					}
				})//
				.build();

		TextButton exitButton = GuiControls.textButton() //
				.position(centerX, height * 0.5f) //
				.text("Main Menu") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						game.transition(game.getMainMenuScreen(), 500, 500);
						game.getPlayScreen().dispose();

						if (GameData.gameMode == GameData.RandomGameMode) {
							Analytics.traker.trackPageView("/challengeMode/finish", "/challengeMode/finish", null);
						} else if (GameData.gameMode == GameData.PracticeGameMode) {
							Analytics.traker.trackPageView("/finishPracticeMode", "/finishPracticeMode", null);
						} else if (GameData.gameMode == GameData.ChallengeGameMode) {
							Analytics.traker.trackPageView("/finishRandomMode", "/finishRandomMode", null);
						}

					}
				})//
				.build();

		container.add(title);
		container.add(playButton);
		container.add(exitButton);
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
		container.update();
	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
