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
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.resources.ResourceManager;

public class SettingsGameState extends GameStateImpl {

	class MultipleButtonControl extends Container {

		private final Color selectedColor = Colors.yellow;
		private final Color notOverColor = Color.WHITE;
		private final Color overColor = Color.GREEN;

		@Override
		public void add(Control control) {
			add(control, false);
		}

		public void add(Control control, boolean selected) {
			if (control instanceof TextButton) {

				final TextButton textButton = (TextButton) control;
				final ButtonHandler buttonHandler = textButton.getButtonHandler();

				textButton.setNotOverColor(notOverColor);
				textButton.setOverColor(overColor);

				if (selected)
					textButton.setNotOverColor(selectedColor);

				textButton.setButtonHandler(new ButtonHandler() {
					@Override
					public void onPressed() {
						buttonHandler.onPressed();
					}

					@Override
					public void onReleased() {

						for (int i = 0; i < getControls().size(); i++) {
							Control otherControl = getControls().get(i);
							if (otherControl == textButton)
								continue;

							if (!(otherControl instanceof TextButton))
								continue;

							TextButton otherTextButton = (TextButton) otherControl;
							otherTextButton.setNotOverColor(notOverColor);
						}

						textButton.setNotOverColor(selectedColor);

						buttonHandler.onReleased();
					}
				});

				super.add(textButton);
			}
		}

	}

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private GamePreferences gamePreferences;

	private Container container;
	private Sprite whiteRectangleSprite;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void setGamePreferences(GamePreferences gamePreferences) {
		this.gamePreferences = gamePreferences;
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

		MultipleButtonControl multipleButtonControl = new MultipleButtonControl();
		container.add(multipleButtonControl);

		ControllerType currentControllerType = getCurrentControllerType();
		ControllerType[] availableControllers = getAvailableControllers();

		game.getGameData().put("testControllerType", currentControllerType);

		container.add(GuiControls.label("Settings") //
				.position(centerX, height * 0.9f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		float y = height * 0.75f;

		for (int i = 0; i < availableControllers.length; i++) {
			final ControllerType controllerType = availableControllers[i];
			multipleButtonControl.add(GuiControls.textButton() //
					.text(controllerType.name()) //
					.font(buttonFont) //
					.center(0f, 0.5f) //
					.position(width * 0.05f, y) //
					.boundsOffset(20f, 20f) //
					.notOverColor(Color.WHITE) //
					.overColor(Color.GREEN) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased() {
							game.getGameData().put("testControllerType", controllerType);
						}
					}) //
					.build(), currentControllerType == controllerType);
			y -= height * 0.15f;
		}

		container.add(GuiControls.textButton() //
				.text("Test") //
				.font(buttonFont) //
				.center(0.5f, 0.5f) //
				.position(width * 0.85f, height * 0.6f) //
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
		container.add(GuiControls.textButton() //
				.text("Save") //
				.font(buttonFont) //
				.center(0.5f, 0.5f) //
				.position(width * 0.85f, height * 0.45f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased() {
						save();
					}
				}) //
				.build());

		if (Gdx.app.getType() != ApplicationType.Android)
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
				monitorKeys("back", Keys.BACK, Keys.ESCAPE);
			}
		};
	}

	private ControllerType[] getAvailableControllers() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return new ControllerType[] { ControllerType.ClassicController, ControllerType.AxisController, //
					ControllerType.AnalogController, ControllerType.TiltController };
		else
			return new ControllerType[] { ControllerType.KeyboardController, ControllerType.AnalogKeyboardController, //
					ControllerType.AxisController, ControllerType.AnalogController };
	}

	private ControllerType getCurrentControllerType() {
		ControllerType controllerType = gamePreferences.getCurrentPlayerProfile().getControllerType();

		if (controllerType != null)
			return controllerType;

		if (Gdx.app.getType() == ApplicationType.Android)
			return ControllerType.ClassicController;
		else
			return ControllerType.KeyboardController;
	}

	private void controllerTestBed() {
		game.transition(game.getControllersTestScreen()).enterTime(250) //
				.leaveTime(250) //
				.start();
	}

	private void save() {
		// save control type to the player profile preferences.
		PlayerProfile playerProfile = gamePreferences.getCurrentPlayerProfile();
		playerProfile.setControllerType((ControllerType) game.getGameData().get("testControllerType"));
		gamePreferences.updatePlayerProfile(playerProfile);

		back();
	}

	private void back() {
		Screen previousScreen = game.getGameData().get("previousScreen");
		game.transition(previousScreen).enterTime(250) //
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
		inputDevicesMonitor.update();

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
