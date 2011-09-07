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
import com.badlogic.gdx.math.Vector2;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
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
			underlineSprite.draw(spriteBatch);
			
			SpriteUtils.centerOn(underlineSprite, selectedControl.getX(), selectedControl.getY() - 15f, 0f, 0.5f);
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
	private RenderLayers renderLayers;
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
		float y = height * 0f;

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

		renderLayers = new RenderLayers();

		final Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera, spriteBatch), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, backgroundLayerCamera, spriteBatch), game.getGamePreferences().isSecondBackgroundEnabled());

		World world = new com.artemis.World();
		worldWrapper = new WorldWrapper(world);
		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.init();

		EntityFactory entityFactory = new EntityFactoryImpl(world);
		Parameters parameters = new ParametersWrapper();

		EntityTemplates entityTemplates = new EntityTemplates(null, world, resourceManager, new EntityBuilder(world), new EntityFactoryImpl(world), null);

		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0f)) //
				.put("center", new Vector2(0f, 0f)) //
				.put("spriteId", "BackgroundSprite") //
				);

		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.GREEN) //
				.put("layer", (-400)) //
				.put("spatial", new SpatialImpl((Gdx.graphics.getWidth() * 0.57f), (Gdx.graphics.getHeight() * 0.23f), 160f, 160f, 86f)) //
				.put("center", new Vector2(0.5f, 0.5f)) //
				.put("spriteId", "FogSprite") //
				);
		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.RED) //
				.put("layer", (-400)) //
				.put("spatial", new SpatialImpl((Gdx.graphics.getWidth() * 0.24f), (Gdx.graphics.getHeight() * 0.68f), 120f, 120f, 189f)) //
				.put("center", new Vector2(0.5f, 0.5f)) //
				.put("spriteId", "FogSprite") //
				);

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
		String pageView = "/settings/control/" + selectedControllerType.name().toLowerCase() + "/test";
		Analytics.traker.trackPageView(pageView, pageView, null);
		game.transition(Screens.ControllersTest) //
				.parameter("controllerTest/backgroundEnabled", renderLayers.get(Layers.FirstBackground).isEnabled()) //
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
