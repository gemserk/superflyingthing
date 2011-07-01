package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class SelectPlayModeGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private BitmapFont titleFont;
	private Text text;
	private TextButton challengeModeButton;
	private TextButton practiceModeButton;
	private TextButton randomModeButton;

	public SelectPlayModeGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		int centerX = width / 2;
		int centerY = height / 2;

		spriteBatch = new SpriteBatch();
		resourceManager = new ResourceManagerImpl<String>();

		GameResources.load(resourceManager);

		titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		text = new Text("Select Mode", centerX, height * 0.9f).setColor(Color.GREEN);

		practiceModeButton = new TextButton(buttonFont, "Practice", centerX, height * 0.7f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE);

		challengeModeButton = new TextButton(buttonFont, "Challenge", centerX, height * 0.5f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE);

		randomModeButton = new TextButton(buttonFont, "Random", centerX, height * 0.3f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE);
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		text.draw(spriteBatch, titleFont);

		practiceModeButton.draw(spriteBatch);
		challengeModeButton.draw(spriteBatch);
		randomModeButton.draw(spriteBatch);

		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);

		practiceModeButton.update();
		challengeModeButton.update();
		randomModeButton.update();

		if (practiceModeButton.isReleased()) {
			// instantiate in some way the game world
			PlayGameState.gameMode = PlayGameState.PracticeGameMode;
			game.transition(game.getPlayScreen(), 500, 250);
		}

		if (challengeModeButton.isReleased()) {
			PlayGameState.gameMode = PlayGameState.ChallengeGameMode;
			game.transition(game.getPlayScreen(), 500, 250);
		}
		
		if (randomModeButton.isReleased()) {
			PlayGameState.gameMode = PlayGameState.RandomGameMode;
			game.transition(game.getPlayScreen(), 500, 250);
		}

	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
