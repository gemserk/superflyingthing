package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.World;
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
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Panel;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.time.TimeStepProvider;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.scenes.EmptySceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.resources.ResourceManager;

public class SettingsGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private Container container;
	// private RenderLayers renderLayers;
	private WorldWrapper worldWrapper;

	private boolean toggleBackground;
	private boolean showReplay;
	private SceneTemplate sceneTemplate;

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

		toggleBackground = false;
		showReplay = game.getGamePreferences().isShowReplay();

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		container = new Container();

		Panel panel = new Panel(0, game.getAdsMaxArea().height + height * 0.15f);

		panel.add(GuiControls.label("Settings") //
				.position(centerX, height * 0.6f) //
				.color(Color.GREEN) //
				.font(titleFont) //
				.build());

		Sprite underlineSprite = resourceManager.getResourceValue("WhiteRectangle");
		underlineSprite.setSize(Gdx.graphics.getWidth() * 0.35f, 3f);
		underlineSprite.setColor(Colors.yellow);

		panel.add(GuiControls.textButton() //
				.text("Toggle background") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.48f) //
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

		panel.add(GuiControls.textButton() //
				.text("Toggle FPS") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.36f) //
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
		panel.add(GuiControls.textButton() //
				.id("ShowReplay") //
				.text(showReplay ? "Show Replay: On" : "Show Replay: Off") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.24f) //
				.boundsOffset(20f, 20f) //
				.notOverColor(Color.WHITE) //
				.overColor(Color.GREEN) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						toggleSaveReplay();
					}
				}) //
				.build());
		panel.add(GuiControls.textButton() //
				.text("Change Controller") //
				.font(buttonFont) //
				.center(0f, 0.5f) //
				.position(width * 0.025f, height * 0.12f) //
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

		final TimeStepProvider timeStepProvider = new TimeStepProviderGameStateImpl(this);

		Injector injectorImpl = new InjectorImpl() {
			{
				configureField("resourceManager", resourceManager);
				configureField("timeStepProvider", timeStepProvider);
			}
		};
		
		Provider provider = new ProviderImpl(injectorImpl);

		sceneTemplate = provider.getInstance(EmptySceneTemplate.class);
		sceneTemplate.getParameters().put("backgroundEnabled", game.getGamePreferences().isFirstBackgroundEnabled());
		sceneTemplate.apply(worldWrapper);

		worldWrapper.update(1);
	}

	private void controllers() {
		game.transition(Screens.ControllersSettings).start();
	}

	private void toggleBackground() {
		toggleBackground = !toggleBackground;

		boolean backgroundEnabled = game.getGamePreferences().isFirstBackgroundEnabled();

		if (toggleBackground)
			backgroundEnabled = !backgroundEnabled;

		worldWrapper.dispose();

		worldWrapper = new WorldWrapper(new World());

		sceneTemplate.getParameters().put("backgroundEnabled", backgroundEnabled);
		sceneTemplate.apply(worldWrapper);

		worldWrapper.update(1);
	}

	private void toggleSaveReplay() {
		showReplay = !showReplay;
		TextButton saveReplaysButton = container.findControl("ShowReplay");
		saveReplaysButton.setText(showReplay ? "Show Replay: On" : "Show Replay: Off");
	}

	private void save() {
		// save background

		if (toggleBackground) {
			game.getEventManager().registerEvent(Events.toggleFirstBackground, this);
			game.getEventManager().registerEvent(Events.toggleSecondBackground, this);
		}

		if (showReplay != game.getGamePreferences().isShowReplay()) {
			game.getGamePreferences().setShowReplay(showReplay);

			String pageView = "/settings/showreplay/" + (showReplay ? "enabled" : "disabled");
			Analytics.traker.trackPageView(pageView, pageView, null);
		}

		back();
	}

	private void back() {
		String previousScreen = getParameters().get("previousScreen");
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
