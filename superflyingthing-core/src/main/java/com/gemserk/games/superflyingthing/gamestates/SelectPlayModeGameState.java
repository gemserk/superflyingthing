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
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.resources.ResourceManager;

public class SelectPlayModeGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;

	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
	private Sprite whiteRectangleSprite;
	private Container container;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public SelectPlayModeGameState(Game game) {
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

		container = new Container();
		
		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		panel.add(GuiControls.label("Select Game Mode") //
				.position(centerX, height * 0.60f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		panel.add(GuiControls.textButton() //
				.text("Challenge") //
				.font(buttonFont) //
				.position(centerX, height * 0.4f) //
				.center(0.5f, 0.5f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.boundsOffset(30f, 30f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						challenge();
					}
				}) //
				.build());

		panel.add(GuiControls.textButton() //
				.text("Random") //
				.font(buttonFont) //
				.position(centerX, height * 0.2f) //
				.center(0.5f, 0.5f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.boundsOffset(30f, 30f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						random();
					}
				}) //
				.build());

		panel.add(GuiControls.textButton() //
				.text("Practice") //
				.font(buttonFont) //
				.position(centerX, height * 0f) //
				.center(0.5f, 0.5f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.boundsOffset(30f, 30f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						practice();
					}
				}) //
				.build());
		
		container.add(panel);

		if (Gdx.app.getType() != ApplicationType.Android)
			container.add(GuiControls.textButton() //
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

		whiteRectangleSprite = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangleSprite.setPosition(0, 0);
		whiteRectangleSprite.setSize(width, height);
		whiteRectangleSprite.setColor(0.2f, 0.2f, 0.2f, 0.3f);

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
		whiteRectangleSprite.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		inputDevicesMonitor.update();
		container.update();
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
