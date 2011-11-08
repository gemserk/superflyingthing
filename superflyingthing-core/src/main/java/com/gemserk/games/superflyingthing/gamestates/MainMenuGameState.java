package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.adwhirl.AdWhirlViewHandler;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;

public class MainMenuGameState extends GameStateImpl {

	Game game;
	SoundPlayer soundPlayer;
	AdWhirlViewHandler adWhirlViewHandler;
	ResourceManager<String> resourceManager;

	SpriteBatch spriteBatch;
	Container screen;

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		float scale = Gdx.graphics.getHeight() / 480f;

		if (Gdx.graphics.getHeight() > 480f)
			scale = 1f;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");
		BitmapFont versionFont = resourceManager.getResourceValue("VersionFont");

		screen = new Container();

		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		panel.add(GuiControls.label("Super Flying Thing") //
				.position(centerX, height * 0.60f) //
				.color(Colors.yellow) //
				.font(titleFont) //
				.build());

		String version = game.getGameData().get("version");
		panel.add(GuiControls.label("v" + version) //
				.position(centerX, height * 0.55f) //
				.color(Color.WHITE) //
				.font(versionFont) //
				.build());

		Sprite playButtonBackgroundSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

		panel.add(GuiControls.imageButton(playButtonBackgroundSprite) //
				.position(centerX, height * 0.3f) //
				.center(0.5f, 0.5f) //
				.size(playButtonBackgroundSprite.getWidth() * scale, playButtonBackgroundSprite.getHeight() * scale) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						game.transition(Screens.SelectPlayMode) //
								.disposeCurrent() //
								.start();
					}
				})//
				.build());

		TextButton playButton = GuiControls.textButton() //
				.position(centerX, height * 0.3f) //
				.text("Play") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.build();

		// TextButton settingsButton = GuiControls.textButton() //
		// .position(centerX, height * 0.2f) //
		// .text("Settings") //
		// .font(buttonFont) //
		// .overColor(Color.GREEN) //
		// .notOverColor(Color.WHITE)//
		// .boundsOffset(40, 20f) //
		// .handler(new ButtonHandler() {
		// @Override
		// public void onReleased(Control control) {
		// settings();
		// }
		// })//
		// .build();

		Sprite aboutButtonBackgroundSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

		panel.add(GuiControls.imageButton(aboutButtonBackgroundSprite) //
				.position(centerX, height * 0.1f) //
				.center(0.5f, 0.5f) //
				.size(aboutButtonBackgroundSprite.getWidth() * scale, aboutButtonBackgroundSprite.getHeight() * scale) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						aboutUs();
					}
				})//
				.build());

		TextButton aboutButton = GuiControls.textButton() //
				.id("AboutButton") //
				.position(centerX, height * 0.1f) //
				.text("About us") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(40, 20f) //
				// .handler(new ButtonHandler() {
				// @Override
				// public void onReleased(Control control) {
				// aboutUs();
				// }
				// })//
				.build();

		// container.add(text);
		panel.add(playButton);
		// panel.add(settingsButton);
		panel.add(aboutButton);

		screen.add(panel);

		Screen backgroundGameScreen = game.getBackgroundGameScreen();
		backgroundGameScreen.init();
		game.getEventManager().registerEvent(Events.previewRandomLevel, this);
	}

	private void aboutUs() {
		game.transition(Screens.About) //
				.disposeCurrent(true) //
				.start();
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
		screen.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		screen.update();
		game.getBackgroundGameScreen().setDelta(getDelta());
		game.getBackgroundGameScreen().update();
	}

	@Override
	public void show() {
		super.show();
		adWhirlViewHandler.show();
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
