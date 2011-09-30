package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class MainMenuGameState extends GameStateImpl {

	Game game;
	SoundPlayer soundPlayer;
	AdWhirlViewHandler adWhirlViewHandler;
	ResourceManager<String> resourceManager;

	SpriteBatch spriteBatch;
	Sprite whiteRectangleSprite;
	Container container;
	Resource<Sound> buttonSound;

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		buttonSound = resourceManager.get("ButtonReleasedSound");

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
						soundPlayer.play(buttonSound.get());
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
						soundPlayer.play(buttonSound.get());
					}
				})//
				.build();

		TextButton aboutButton = GuiControls.textButton() //
				.id("AboutButton").position(centerX, height * 0f) //
				.text("About us") //
				.font(buttonFont) //
				.overColor(Color.GREEN) //
				.notOverColor(Color.WHITE)//
				.boundsOffset(20, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						aboutUs();
						soundPlayer.play(buttonSound.get());
					}
				})//
				.build();

		// container.add(text);
		panel.add(playButton);
		panel.add(settingsButton);
		panel.add(aboutButton);

		container.add(panel);

		whiteRectangleSprite = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangleSprite.setPosition(0, 0);
		whiteRectangleSprite.setSize(width, height);
		whiteRectangleSprite.setColor(0.2f, 0.2f, 0.2f, 0.3f);

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
