package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.World;
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
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.resources.ResourceManager;

public class SettingsGameState extends GameStateImpl {

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

		container.add(GuiControls.label("Settings") //
				.position(centerX, height * 0.9f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		Sprite underlineSprite = resourceManager.getResourceValue("WhiteRectangle");
		underlineSprite.setSize(Gdx.graphics.getWidth() * 0.35f, 3f);
		underlineSprite.setColor(Colors.yellow);

		container.add(GuiControls.textButton() //
				.text("Toggle background") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.75f) //
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
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.63f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						boolean oldShowFps = Game.isShowFps();
						String pageView = "/settings/fps/" + (oldShowFps ? "hide" : "show");
						Analytics.traker.trackPageView(pageView, pageView, null);
						Game.setShowFps(!oldShowFps);
					}
				}) //
				.build());
		container.add(GuiControls.textButton() //
				.text("Change Controller") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.51f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						controllers();
					}
				}) //
				.build());
		
		container.add(GuiControls.textButton() //
				.text("Save") //
				.font(buttonFont) //
				.center(1f, 0.5f) //
				.position(width * 0.975f, height * 0.51f) //
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
				.position(width * 0.975f, height * 0.39f) //
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

	private void controllers() {
		game.transition(Screens.ControllersSettings).start();
	}

	private void toggleBackground() {
		toggleBackground = !toggleBackground;
		renderLayers.toggle(Layers.FirstBackground);
		renderLayers.toggle(Layers.SecondBackground);
	}

	private void save() {
		// save background

		if (toggleBackground) {
			game.getEventManager().registerEvent(Events.toggleFirstBackground, this);
			game.getEventManager().registerEvent(Events.toggleSecondBackground, this);
		}

		back();
	}

	private void back() {
		String previousScreen = game.getGameData().get("previousScreen");
		game.transition(previousScreen) //
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
