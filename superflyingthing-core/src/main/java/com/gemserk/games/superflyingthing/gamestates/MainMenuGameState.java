package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.adwhirl.AdWhirlViewHandler;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.ImageButton;
import com.gemserk.commons.gdx.gui.Panel;
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
	EventManager eventManager;

	SpriteBatch spriteBatch;
	Container screen;

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		float scale = Gdx.graphics.getHeight() / 480f;
		
		float scaleX = Gdx.graphics.getWidth() / 800f;
		float scaleY = Gdx.graphics.getHeight() / 480f;

		if (Gdx.graphics.getHeight() > 480f)
			scale = 1f;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");
		BitmapFont versionFont = resourceManager.getResourceValue("VersionFont");

		screen = new Container();

		final Container settingsPanel = new Container();

		{
			Sprite panelBackgroundSprite = resourceManager.getResourceValue(GameResources.Sprites.LeftPanelBackground);

			settingsPanel.add(GuiControls.imageButton(panelBackgroundSprite) //
					.position(0f, 0f) //
					.center(0f, 0f) //
					.color(1f, 1f, 1f, 1f) //
					.size(panelBackgroundSprite.getWidth() * scaleY, panelBackgroundSprite.getHeight() * scaleY) //
					.build());

			{
				Sprite toggleBackgroundSprite = resourceManager.getResourceValue(GameResources.Sprites.ToggleBackgroundButton);
				final String disabledOverlayId = "BackgroundDisabledOverlay";

				settingsPanel.add(GuiControls.imageButton(toggleBackgroundSprite) //
						.position(55f * scaleX, 300 * scaleY) //
						.center(0.5f, 0.5f) //
						.color(1f, 1f, 1f, 1f) //
						.size(toggleBackgroundSprite.getWidth() * scaleY * 0.5f, toggleBackgroundSprite.getHeight() * scaleY * 0.5f) //
						.handler(new ButtonHandler() {
							@Override
							public void onReleased(Control control) {
								eventManager.registerEvent(Events.toggleFirstBackground, this);
								eventManager.registerEvent(Events.toggleSecondBackground, this);

								ImageButton disabledOverlay = settingsPanel.findControl(disabledOverlayId);
								disabledOverlay.setColor(1f, 1f, 1f, game.getGamePreferences().isFirstBackgroundEnabled() ? 1f : 0f);
							}
						}) //
						.build());

				Sprite disabledOverlay = resourceManager.getResourceValue(GameResources.Sprites.DisabledButtonOverlay);

				settingsPanel.add(GuiControls.imageButton(disabledOverlay) //
						.id(disabledOverlayId) //
						.position(55f * scaleX, 300 * scaleY) //
						.center(0.5f, 0.5f) //
						.color(1f, 1f, 1f, game.getGamePreferences().isFirstBackgroundEnabled() ? 0f : 1f) //
						.size(disabledOverlay.getWidth() * scaleY * 0.5f, disabledOverlay.getHeight() * scaleY * 0.5f) //
						.build());
			}

			{
				Sprite toggleSoundSprite = resourceManager.getResourceValue(GameResources.Sprites.ToggleSoundsButton);
				final String disabledOverlayId = "SoundDisabledOverlay";

				settingsPanel.add(GuiControls.imageButton(toggleSoundSprite) //
						.position(55f * scaleX, 180f * scaleY) //
						.center(0.5f, 0.5f) //
						.color(1f, 1f, 1f, 1f) //
						.size(toggleSoundSprite.getWidth() * scaleY * 0.5f, toggleSoundSprite.getHeight() * scaleY * 0.5f) //
						.handler(new ButtonHandler() {
							@Override
							public void onReleased(Control control) {
								if (soundPlayer.isMuted())
									soundPlayer.unmute();
								else
									soundPlayer.mute();

								ImageButton disabledOverlay = settingsPanel.findControl(disabledOverlayId);
								disabledOverlay.setColor(1f, 1f, 1f, soundPlayer.isMuted() ? 1f : 0f);
							}

						}) //
						.build());

				Sprite disabledOverlay = resourceManager.getResourceValue(GameResources.Sprites.DisabledButtonOverlay);

				settingsPanel.add(GuiControls.imageButton(disabledOverlay) //
						.id(disabledOverlayId) //
						.position(55f * scaleX, 180f * scaleY) //
						.center(0.5f, 0.5f) //
						.color(1f, 1f, 1f, soundPlayer.isMuted() ? 1f : 0f) //
						.size(disabledOverlay.getWidth() * scaleY * 0.5f, disabledOverlay.getHeight() * scaleY * 0.5f) //
						.build());
			}

		}

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

		{
			Sprite squareButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

			panel.add(GuiControls.imageButton(squareButtonSprite) //
					.position(centerX, height * 0.3f) //
					.center(0.5f, 0.5f) //
					.size(squareButtonSprite.getWidth() * scale, squareButtonSprite.getHeight() * scale) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							game.transition(Screens.SelectPlayMode) //
									.disposeCurrent() //
									.start();
						}
					})//
					.build());

			panel.add(GuiControls.textButton() //
					.position(centerX, height * 0.3f) //
					.text("Play") //
					.font(buttonFont) //
					.overColor(Color.WHITE) //
					.notOverColor(Colors.yellow)//
					.build());
		}

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

		{
			Sprite squareButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

			panel.add(GuiControls.imageButton(squareButtonSprite) //
					.position(centerX, height * 0.1f) //
					.center(0.5f, 0.5f) //
					.size(squareButtonSprite.getWidth() * scale, squareButtonSprite.getHeight() * scale) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							aboutUs();
						}
					})//
					.build());

			panel.add(GuiControls.textButton() //
					.id("AboutButton") //
					.position(centerX, height * 0.1f) //
					.text("About us") //
					.font(buttonFont) //
					.overColor(Color.WHITE) //
					.notOverColor(Colors.yellow)//
					.boundsOffset(40, 20f) //
					.build());
		}

		// container.add(text);

		// panel.add(settingsButton);

		screen.add(settingsPanel);
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
