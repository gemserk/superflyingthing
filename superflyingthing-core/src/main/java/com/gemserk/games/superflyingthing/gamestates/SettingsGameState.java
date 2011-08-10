package com.gemserk.games.superflyingthing.gamestates;

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
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.resources.ResourceManager;

public class SettingsGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	// ControllerType selectedControllerType = ControllerType.ClassicController;

	Container container;
	private Sprite whiteRectangleSprite;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public SettingsGameState(Game game) {
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

		container.add(GuiControls.label("Settings") //
				.position(centerX, height * 0.9f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		container.add(GuiControls.textButton() //
				.text("Classic Controller") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.05f, height * 0.75f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						GameInformation.controllerType = ControllerType.ClassicController;
					}
				}) //
				.build());
		container.add(GuiControls.textButton() //
				.text("Axis Controller") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.05f, height * 0.60f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						GameInformation.controllerType = ControllerType.AxisController;
					}
				}) //
				.build());
		container.add(GuiControls.textButton() //
				.text("Analog Controller") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.05f, height * 0.45f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						GameInformation.controllerType = ControllerType.AnalogController;
					}
				}) //
				.build());
		container.add(GuiControls.textButton() //
				.text("Tilt Controller") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.05f, height * 0.3f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						GameInformation.controllerType = ControllerType.TiltController;
					}
				}) //
				.build());
		
		container.add(GuiControls.textButton() //
				.text("Apply") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.4f, height * 0.2f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						// save current selected controller type
					}
				}) //
				.build());
		container.add(GuiControls.textButton() //
				.text("Test") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.6f, height * 0.2f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						controllerTestBed();
					}
				}) //
				.build());

		container.add(new TextButton(buttonFont, "Back", width * 0.95f, height * 0.05f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE) //
				.setBoundsOffset(40f, 40f) //
				.setAlignment(HAlignment.RIGHT) //
				.setButtonHandler(new ButtonHandler() {
					@Override
					public void onReleased() {
						back();
					}
				}));

		whiteRectangleSprite = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangleSprite.setPosition(0, 0);
		whiteRectangleSprite.setSize(width, height);
		whiteRectangleSprite.setColor(0.2f, 0.2f, 0.2f, 0.3f);

		game.getBackgroundGameScreen().init();

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				if (Gdx.app.getType() == ApplicationType.Android)
					monitorKey("back", Keys.BACK);
				else
					monitorKey("back", Keys.ESCAPE);
			}
		};
	}

	private void controllerTestBed() {
		game.transition(game.getControllersTestScreen()).enterTime(250) //
				.leaveTime(250) //
				.disposeCurrent() //
				.start();
	}

	private void back() {
		game.transition(game.getMainMenuScreen()).enterTime(250) //
				.leaveTime(250) //
				.disposeCurrent() //
				.start();
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

		if (inputDevicesMonitor.getButton("back").isReleased())
			back();
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
