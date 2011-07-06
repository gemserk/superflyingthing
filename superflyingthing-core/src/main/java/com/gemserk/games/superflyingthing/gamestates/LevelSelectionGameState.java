package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
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

	ArrayList<Control> controls;

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

		controls = new ArrayList<Control>();

		Sprite level1 = resourceManager.getResourceValue("WhiteRectangle");

		controls.add(GuiControls.imageButton(level1) //
				.color(0.8f, 0.8f, 0.8f, 1f) //
				.size(width * 0.1f, height * 0.1f) //
				.position(width * 0.15f, height * 0.75f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						// load level 1, then go to play screen
						GameData.level = Levels.level1();
						game.transition(game.getPlayScreen(), 500, 250);
					}
				}) //
				.build());
		
		controls.add(GuiControls.imageButton(level1) //
				.color(0.8f, 0.8f, 0.8f, 1f) //
				.size(width * 0.1f, height * 0.1f) //
				.position(width * 0.3f, height * 0.75f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						// load level 1, then go to play screen
						GameData.level = Levels.level2();
						game.transition(game.getPlayScreen(), 500, 250);
					}
				}) //
				.build());

		if (Gdx.app.getType() != ApplicationType.Android)
			controls.add(new TextButton(buttonFont, "Back", width * 0.95f, height * 0.05f) //
					.setNotOverColor(Color.WHITE) //
					.setOverColor(Color.GREEN) //
					.setColor(Color.WHITE) //
					.setBoundsOffset(20f, 20f) //
					.setAlignment(HAlignment.RIGHT) //
					.setButtonHandler(new ButtonHandler() {
						@Override
						public void onReleased() {
							game.transition(game.getSelectPlayModeScreen(), 500, 500);
						}
					}));

	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		text.draw(spriteBatch, titleFont);
		for (int i = 0; i < controls.size(); i++)
			controls.get(i).draw(spriteBatch);
		spriteBatch.end();

		// GuiControls.debugRender(scene);
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);

		for (int i = 0; i < controls.size(); i++)
			controls.get(i).update();

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