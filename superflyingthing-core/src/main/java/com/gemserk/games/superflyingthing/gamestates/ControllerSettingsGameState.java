package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.World;
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
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.ObjectConfigurator;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.scenes.EmptySceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.resources.ResourceManager;

public class ControllerSettingsGameState extends GameStateImpl {

	class MultipleButtonControlWithUnderline extends Container {

		private final Color selectedColor = Colors.yellow;
		private final Color notOverColor = Color.WHITE;
		private final Color overColor = Color.GREEN;
		private final Sprite underlineSprite;

		private Control selectedControl;

		public MultipleButtonControlWithUnderline(Sprite underlineSprite) {
			this.underlineSprite = underlineSprite;
		}

		public void add(Control control) {
			if (control instanceof TextButton) {

				final TextButton textButton = (TextButton) control;
				final ButtonHandler buttonHandler = textButton.getButtonHandler();

				textButton.setNotOverColor(notOverColor);
				textButton.setOverColor(overColor);

				textButton.setButtonHandler(new ButtonHandler() {
					@Override
					public void onPressed(Control control) {
						buttonHandler.onPressed(control);
					}

					@Override
					public void onReleased(Control control) {
						select(textButton);
						buttonHandler.onReleased(control);
					}

				});

				super.add(textButton);
			}
		}

		private void select(final TextButton textButton) {
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

			selectedControl = textButton;

		}

		@Override
		public void draw(SpriteBatch spriteBatch) {
			super.draw(spriteBatch);

			if (selectedControl != null) {
				SpriteUtils.centerOn(underlineSprite, selectedControl.getX(), selectedControl.getY() - 15f, 0f, 0.5f);
				underlineSprite.draw(spriteBatch);
			}

		}

		@Override
		public void setPosition(float x, float y) {
			for (int i = 0; i < getControls().size(); i++) {
				Control control = getControls().get(i);
				control.setPosition(x + control.getX(), y + control.getY());
			}
		}

	}

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private Container container;
	// private RenderLayers renderLayers;
	private WorldWrapper worldWrapper;

	private ControllerType selectedControllerType;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public ControllerSettingsGameState(Game game) {
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

		selectedControllerType = game.getGamePreferences().getCurrentPlayerProfile().getControllerType();
		ControllerType[] availableControllers = getAvailableControllers();

		panel.add(GuiControls.label("Settings") //
				.position(centerX, height * 0.6f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		float x = width * 0.025f;
		float y = height * 0.12f;

		Sprite underlineSprite = resourceManager.getResourceValue("WhiteRectangle");
		underlineSprite.setSize(Gdx.graphics.getWidth() * 0.35f, 3f);
		underlineSprite.setColor(Colors.yellow);

		MultipleButtonControlWithUnderline multipleButtonControlWithUnderline = new MultipleButtonControlWithUnderline(underlineSprite);

		for (int i = 0; i < availableControllers.length; i++) {
			final ControllerType controllerType = availableControllers[availableControllers.length - i - 1];
			TextButton controllerTextButton = GuiControls.textButton() //
					.text(controllerType.name()) //
					.font(buttonFont) //
					.center(0f, 0.5f) //
					.position(x, y) //
					.boundsOffset(20f, 20f) //
					.notOverColor(Color.WHITE) //
					.overColor(Color.GREEN) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							selectedControllerType = controllerType;
						}
					}) //
					.build();
			multipleButtonControlWithUnderline.add(controllerTextButton);

			if (selectedControllerType == controllerType)
				multipleButtonControlWithUnderline.select(controllerTextButton);

			y += height * 0.12f;
		}

		panel.add(multipleButtonControlWithUnderline);

		panel.add(GuiControls.textButton() //
				.text("Test") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.36f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						controllerTestBed();
					}
				}) //
				.build());
		panel.add(GuiControls.textButton() //
				.text("Save") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.24f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						save();
					}
				}) //
				.build());
		panel.add(GuiControls.textButton() //
				.text("Cancel") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.12f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						back();
					}
				}) //
				.build());

		container.add(panel);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("back", Keys.BACK, Keys.ESCAPE);
			}
		};

		Analytics.traker.trackPageView("/settings/start", "/settings/start", null);

		worldWrapper = new WorldWrapper(new World());

		ObjectConfigurator objectConfigurator = new ObjectConfigurator() {
			{
				add("resourceManager", resourceManager);
				add("timeStepProvider", new TimeStepProviderGameStateImpl(ControllerSettingsGameState.this));
			}
		};
		
		Provider provider = new ProviderImpl(objectConfigurator);

		SceneTemplate sceneTemplate = provider.get(EmptySceneTemplate.class);
		sceneTemplate.getParameters().put("backgroundEnabled", game.getGamePreferences().isFirstBackgroundEnabled());
		sceneTemplate.apply(worldWrapper);

		worldWrapper.update(1);
	}

	private ControllerType[] getAvailableControllers() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return new ControllerType[] { ControllerType.ClassicController, ControllerType.AnalogController, ControllerType.TargetController };
		else
			return new ControllerType[] { ControllerType.KeyboardController, ControllerType.TargetController, ControllerType.RemoteClassicController };
	}

	private void controllerTestBed() {
		String pageView = "/settings/control/" + selectedControllerType.name().toLowerCase() + "/test";
		Analytics.traker.trackPageView(pageView, pageView, null);
		game.transition(Screens.ControllersTest) //
				.parameter("controllerTest/backgroundEnabled", game.getGamePreferences().isFirstBackgroundEnabled()) //
				.parameter("testControllerType", selectedControllerType) //
				.start();
	}

	private void save() {
		// save control type to the player profile preferences.
		PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();
		playerProfile.setControllerType(selectedControllerType);
		game.getGamePreferences().updatePlayerProfile(playerProfile);

		String pageView = "/settings/control/" + selectedControllerType.name().toLowerCase() + "/save";
		Analytics.traker.trackPageView(pageView, pageView, null);

		back();
	}

	private void back() {
		game.transition(Screens.Settings) //
				.disposeCurrent() //
				.start();
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		// gui camera...
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());

		container.update();
		inputDevicesMonitor.update();

		worldWrapper.update(getDeltaInMs());

		if (inputDevicesMonitor.getButton("back").isReleased())
			back();

	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().show();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
