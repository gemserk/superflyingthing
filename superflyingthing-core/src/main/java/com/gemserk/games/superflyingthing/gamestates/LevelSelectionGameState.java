package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class LevelSelectionGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private BitmapFont titleFont;
	private Text text;

	ArrayList<TextButton> buttons;

	public LevelSelectionGameState(Game game) {
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

		text = new Text("Select Level", centerX, height * 0.9f).setColor(Color.GREEN);

		buttons = new ArrayList<TextButton>();

		if (Gdx.app.getType() != ApplicationType.Android)
			buttons.add(new TextButton(buttonFont, "Back", width * 0.95f, height * 0.05f) //
					.setNotOverColor(Color.WHITE) //
					.setOverColor(Color.GREEN) //
					.setColor(Color.WHITE) //
					.setBoundsOffset(20f, 20f) //
					.setAlignment(HAlignment.RIGHT) //
					.setButtonHandler(new ButtonHandler() {
						@Override
						public void onReleased(TextButton button) {
							game.transition(game.getSelectPlayModeScreen(), 500, 500);
						}
					}));

	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		text.draw(spriteBatch, titleFont);
		for (int i = 0; i < buttons.size(); i++)
			buttons.get(i).draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		
		for (int i = 0; i < buttons.size(); i++)
			buttons.get(i).update();

		if (Gdx.input.isKeyPressed(Keys.BACK) || Gdx.input.isKeyPressed(Keys.ESCAPE))
			game.transition(game.getSelectPlayModeScreen(), 500, 500);
	}

	@Override
	public void resume() {
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void pause() {
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
