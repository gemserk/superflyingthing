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
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.resources.ResourceManager;

public class MainMenuGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;

	private Sprite whiteRectangleSprite;
	private Container container;

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
		
		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		panel.add(GuiControls.label("Super Flying Thing") //
				.position(centerX, height * 0.60f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		String version = game.getGameData().get("version");
		panel.add(GuiControls.label("v" + version) //
				.position(centerX, height * 0.55f) //
				.color(Color.WHITE) //
				.font(versionFont) //
				.build());

		TextButton playButton = GuiControls.textButton() //
				.position(centerX, height * 0.4f) //
				.text("Play") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						game.transition(Screens.SelectPlayMode) //
								.disposeCurrent() //
								.start();
					}
				})//
				.build();

		TextButton settingsButton = GuiControls.textButton() //
				.position(centerX, height * 0.2f) //
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
				.build();

		TextButton exitButton = GuiControls.textButton() //
				.position(centerX, height * 0f) //
				.text("Exit") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						Gdx.app.exit();
					}
				})//
				.build();

		// container.add(text);
		panel.add(playButton);
		panel.add(settingsButton);
		if (Gdx.app.getType() != ApplicationType.Applet)
			panel.add(exitButton);
		
		container.add(panel);

		whiteRectangleSprite = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangleSprite.setPosition(0, 0);
		whiteRectangleSprite.setSize(width, height);
		whiteRectangleSprite.setColor(0.2f, 0.2f, 0.2f, 0.3f);

		Screen backgroundGameScreen = game.getBackgroundGameScreen();
		backgroundGameScreen.init();
		game.getEventManager().registerEvent(Events.previewRandomLevel, this);
	}

	private void settings() {
		game.transition(Screens.Settings) //
				.disposeCurrent(true) //
				.parameter("previousScreen", Screens.MainMenu)//
				.start();
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		game.getBackgroundGameScreen().setDelta(getDelta());
		game.getBackgroundGameScreen().render();
		spriteBatch.begin();
		whiteRectangleSprite.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		container.update();
		game.getBackgroundGameScreen().setDelta(getDelta());
		game.getBackgroundGameScreen().update();
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
