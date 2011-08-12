package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.resources.ResourceManager;

public class MainMenuGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;

	Container container;
	private Sprite whiteRectangleSprite;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public MainMenuGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");
		BitmapFont versionFont = resourceManager.getResourceValue("VersionFont");

		container = new Container();

		container.add(GuiControls.label("Super Flying Thing") //
				.position(centerX, height * 0.9f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		String version = game.getGameData().get("version");
		container.add(GuiControls.label("v" + version) //
				.position(centerX, height * 0.85f) //
				.color(Color.WHITE) //
				.font(versionFont) //
				.build());

		TextButton playButton = GuiControls.textButton() //
				.position(centerX, height * 0.7f) //
				.text("Play") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						game.transition(game.getSelectPlayModeScreen(), 500, 500);
					}
				})//
				.build();

		TextButton settingsButton = GuiControls.textButton() //
				.position(centerX, height * 0.5f) //
				.text("Settings") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						settings();
					}
				})//
				.build();

		TextButton exitButton = GuiControls.textButton() //
				.position(centerX, height * 0.3f) //
				.text("Exit") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						Gdx.app.exit();
					}
				})//
				.build();

		// container.add(text);
		container.add(playButton);
		container.add(settingsButton);
		if (Gdx.app.getType() != ApplicationType.Applet)
			container.add(exitButton);

		whiteRectangleSprite = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangleSprite.setPosition(0, 0);
		whiteRectangleSprite.setSize(width, height);
		whiteRectangleSprite.setColor(0.2f, 0.2f, 0.2f, 0.3f);

		game.getBackgroundGameScreen().init();
	}

	private void settings() {
		game.getGameData().put("previousScreen", game.getMainMenuScreen());
		game.transition(game.getSettingsScreen(), 250, 250, true);
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		game.getBackgroundGameScreen().render(delta);
		spriteBatch.begin();
		whiteRectangleSprite.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		container.update();
		game.getBackgroundGameScreen().update(delta);
	}

	@Override
	public void show() {
		super.show();
		game.getAdWhirlViewHandler().show();
		game.getBackgroundGameScreen().show();
	}

	@Override
	public void resume() {
		super.resume();
		game.getBackgroundGameScreen().resume();
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
