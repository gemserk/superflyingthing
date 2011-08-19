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
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.resources.ResourceManager;

public class SettingsGameState extends GameStateImpl {

	class MultipleButtonControlWithUnderline extends Container {

		private final Color selectedColor = Colors.yellow;
		private final Color notOverColor = Color.WHITE;
		private final Color overColor = Color.GREEN;
		private final Sprite underlineSprite;

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
			SpriteUtils.centerOn(underlineSprite, textButton.getX(), textButton.getY() - 15f, 0f, 0.5f);
		}

		@Override
		public void draw(SpriteBatch spriteBatch) {
			super.draw(spriteBatch);
			underlineSprite.draw(spriteBatch);
		}

	}

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private GamePreferences gamePreferences;

	private Container container;
	private RenderLayers renderLayers;
	private WorldWrapper worldWrapper;
	private boolean toggleBackground;

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

		toggleBackground = false;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		container = new Container();

		ControllerType currentControllerType = gamePreferences.getCurrentPlayerProfile().getControllerType();
		ControllerType[] availableControllers = getAvailableControllers();

		game.getGameData().put("testControllerType", currentControllerType);

		container.add(GuiControls.label("Settings") //
				.position(centerX, height * 0.9f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		float x = width * 0.025f;
		float y = height * 0.75f;

		Sprite underlineSprite = resourceManager.getResourceValue("WhiteRectangle");
		underlineSprite.setSize(Gdx.graphics.getWidth() * 0.35f, 3f);
		underlineSprite.setColor(Colors.yellow);

		MultipleButtonControlWithUnderline multipleButtonControlWithUnderline = new MultipleButtonControlWithUnderline(underlineSprite);
		container.add(multipleButtonControlWithUnderline);

		for (int i = 0; i < availableControllers.length; i++) {
			final ControllerType controllerType = availableControllers[i];
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
							game.getGameData().put("testControllerType", controllerType);
						}
					}) //
					.build();
			multipleButtonControlWithUnderline.add(controllerTextButton);

			if (currentControllerType == controllerType)
				multipleButtonControlWithUnderline.select(controllerTextButton);

			y -= height * 0.12f;
		}

		container.add(GuiControls.textButton() //
				.text("Toggle background") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.75f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						toggleBackground();
					}
				}) //
				.build());

		container.add(GuiControls.textButton() //
				.text("Toggle FPS") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.63f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						Game.setShowFps(!Game.isShowFps());
					}
				}) //
				.build());

		container.add(GuiControls.textButton() //
				.text("Test") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.51f) //
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
		container.add(GuiControls.textButton() //
				.text("Save") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.39f) //
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
		container.add(GuiControls.textButton() //
				.text("Cancel") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.27f) //
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

		renderLayers = new RenderLayers();

		final Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera, spriteBatch), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, backgroundLayerCamera, spriteBatch), game.getGamePreferences().isSecondBackgroundEnabled());

		World world = new com.artemis.World();
		worldWrapper = new WorldWrapper(world);
		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.init();

		EntityTemplates entityTemplates = new EntityTemplates(null, world, resourceManager, new EntityBuilder(world), null, null);

		Sprite sprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(sprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0f, -999, 0f, 0f, Color.WHITE);

		sprite = resourceManager.getResourceValue("FogSprite");
		entityTemplates.staticSprite(new Sprite(sprite), Gdx.graphics.getWidth() * 0.57f, Gdx.graphics.getHeight() * 0.23f, 160f, 160f, 86f, -400, 0.5f, 0.5f, Color.GREEN);
		entityTemplates.staticSprite(new Sprite(sprite), Gdx.graphics.getWidth() * 0.24f, Gdx.graphics.getHeight() * 0.68f, 120f, 120f, 189f, -400, 0.5f, 0.5f, Color.RED);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("back", Keys.BACK, Keys.ESCAPE);
			}
		};

		Analytics.traker.trackPageView("/settings/start", "/settings/start", null);
	}

	private ControllerType[] getAvailableControllers() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return new ControllerType[] { ControllerType.ClassicController, ControllerType.AxisController, //
					ControllerType.AnalogController, ControllerType.TiltController, ControllerType.TargetController };
		else
			return new ControllerType[] { ControllerType.KeyboardController, ControllerType.AnalogKeyboardController, //
					ControllerType.AxisController, ControllerType.AnalogController, ControllerType.TargetController };
	}

	private void controllerTestBed() {
		ControllerType controllerType = (ControllerType) game.getGameData().get("testControllerType");
		String pageView = "/settings/control/" + controllerType.name().toLowerCase() + "/test";
		Analytics.traker.trackPageView(pageView, pageView, null);

		game.getGameData().put("controllerTest/backgroundEnabled", renderLayers.get(Layers.FirstBackground).isEnabled());

		game.transition(game.getControllersTestScreen()) //
				.enterTime(250) //
				.leaveTime(250) //
				.start();
	}

	private void toggleBackground() {
		toggleBackground = !toggleBackground;
		renderLayers.toggle(Layers.FirstBackground);
		renderLayers.toggle(Layers.SecondBackground);
	}

	private void save() {
		// save control type to the player profile preferences.
		PlayerProfile playerProfile = gamePreferences.getCurrentPlayerProfile();
		ControllerType controllerType = (ControllerType) game.getGameData().get("testControllerType");
		playerProfile.setControllerType(controllerType);
		gamePreferences.updatePlayerProfile(playerProfile);
		String pageView = "/settings/control/" + controllerType.name().toLowerCase() + "/save";
		Analytics.traker.trackPageView(pageView, pageView, null);

		// save background

		if (toggleBackground) {
			game.getEventManager().registerEvent(Events.toggleFirstBackground, this);
			game.getEventManager().registerEvent(Events.toggleSecondBackground, this);
		}

		back();
	}

	private void back() {
		Screen previousScreen = game.getGameData().get("previousScreen");
		game.transition(previousScreen) //
				.enterTime(250) //
				.leaveTime(250) //
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
