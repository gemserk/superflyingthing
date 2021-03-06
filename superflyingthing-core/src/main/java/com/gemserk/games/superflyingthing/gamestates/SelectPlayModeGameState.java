package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Application.ApplicationType;
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
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;

public class SelectPlayModeGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;

	SpriteBatch spriteBatch;
	InputDevicesMonitorImpl<String> inputDevicesMonitor;
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

		screen = new Container();

		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		// panel.add(GuiControls.label("Select Game Mode") //
		// .position(centerX, height * 0.60f) //
		// .color(Color.GREEN) //
		// .font(titleFont) //
		// .build());

		Sprite challengeButtonBackgroundSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

		panel.add(GuiControls.imageButton(challengeButtonBackgroundSprite) //
				.position(centerX, height * 0.3f) //
				.center(0.5f, 0.5f) //
				.size(challengeButtonBackgroundSprite.getWidth() * scale, challengeButtonBackgroundSprite.getHeight() * scale) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						challenge();
					}
				}) //
				.build());

		panel.add(GuiControls.textButton() //
				.text("Challenge") //
				.font(buttonFont) //
				.position(centerX, height * 0.3f) //
				.center(0.5f, 0.5f) //
				.overColor(Color.WHITE) //
				.notOverColor(Colors.yellow)//
				.boundsOffset(30f, 30f) //
				.build());

		Sprite randomButtonBackgroundSprite = resourceManager.getResourceValue(GameResources.Sprites.SquareButton);

		panel.add(GuiControls.imageButton(randomButtonBackgroundSprite) //
				.position(centerX, height * 0.1f) //
				.center(0.5f, 0.5f) //
				.size(randomButtonBackgroundSprite.getWidth() * scale, randomButtonBackgroundSprite.getHeight() * scale) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						random();
					}
				}) //
				.build());

		panel.add(GuiControls.textButton() //
				.text("Random") //
				.font(buttonFont) //
				.position(centerX, height * 0.1f) //
				.center(0.5f, 0.5f) //
				.overColor(Color.WHITE) //
				.notOverColor(Colors.yellow)//
				.boundsOffset(30f, 30f) //
				.build());

		screen.add(panel);

		if (Gdx.app.getType() != ApplicationType.Android)
			screen.add(GuiControls.textButton() //
					.text("Back") //
					.font(buttonFont) //
					.position(width * 0.98f, height * 0.05f) //
					.center(1f, 0.5f) //
					.notOverColor(Color.WHITE) //
					.overColor(Color.GREEN) //
					.boundsOffset(30f, 30f) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							back();
						}
					}) //
					.build());

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				if (Gdx.app.getType() == ApplicationType.Android)
					monitorKey("back", Keys.BACK);
				else
					monitorKey("back", Keys.ESCAPE);
			}
		};

		game.getBackgroundGameScreen().init();
	}

	private void challenge() {
		GameInformation.gameMode = GameInformation.ChallengeGameMode;
		game.transition(Screens.LevelSelection) //
				.disposeCurrent(true) //
				.start();
		Analytics.traker.trackPageView("/challenge/selected", "/challenge/selected", null);
	}

	private void random() {
		GameInformation.gameMode = GameInformation.RandomGameMode;
		game.transition(Screens.Play) //
				.disposeCurrent(true) //
				.start();
		Analytics.traker.trackPageView("/random/selected", "/random/selected", null);
	}

	private void practice() {
		GameInformation.gameMode = GameInformation.PracticeGameMode;
		game.transition(Screens.Play) //
				.disposeCurrent(true) //
				.start();
		Analytics.traker.trackPageView("/practice/selected", "/practice/selected", null);
	}

	private void back() {
		game.transition(Screens.MainMenu) //
				.disposeCurrent(true) //
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
		inputDevicesMonitor.update();
		screen.update();
		if (inputDevicesMonitor.getButton("back").isReleased())
			game.transition(Screens.MainMenu) //
					.disposeCurrent() //
					.start();
		game.getBackgroundGameScreen().setDelta(getDelta());
		game.getBackgroundGameScreen().update();
	}

	@Override
	public void show() {
		super.show();
		game.getBackgroundGameScreen().show();
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().show();
		game.getBackgroundGameScreen().resume();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
