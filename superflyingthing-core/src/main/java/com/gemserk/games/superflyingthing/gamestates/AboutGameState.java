package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.World;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.commons.utils.BrowserUtils;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.scenes.EmptySceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.resources.ResourceManager;

public class AboutGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;
	BrowserUtils browserUtils;

	Container guiContainer;
	SpriteBatch spriteBatch;
	InputDevicesMonitorImpl<String> inputDevicesMonitor;
	WorldWrapper worldWrapper;

	@Override
	public void init() {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		spriteBatch = new SpriteBatch();
		guiContainer = new Container();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont instructionsFont = resourceManager.getResourceValue("InstructionsFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");
		
		guiContainer.add(GuiControls.label("ABOUT US") //
				.position(width * 0.5f, height * 0.95f) //
				.center(0.5f, 0.5f) //
				.font(titleFont) //
				.color(1f, 0f, 0f, 1f) //
				.build());

		guiContainer.add(GuiControls.label("Gemserk Studios is an independent game \n" + //
				"development company. We make games\n" + //
				"since 2010, want more information?") //
				.position(width * 0.5f, height * 0.70f) //
				.center(0.5f, 0.5f) //
				.font(instructionsFont) //
				.color(1f, 0f, 0f, 1f) //
				.build());

		guiContainer.add(GuiControls.textButton() //
				.id("VisitBlogButton") //
				.text("Visit our Blog") //
				.position(width * 0.5f, height * 0.47f) //
				.center(0.5f, 0.5f) //
				.font(buttonFont) //
				.overColor(0f, 0f, 1f, 1f) //
				.notOverColor(1f, 1f, 0f, 1f) //
				.boundsOffset(40f, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						browserUtils.open(Game.blogUrl);
						Analytics.traker.trackPageView("/about/blog", "/about/blog", null);
					}
				}) //
				.build());

		guiContainer.add(GuiControls.textButton() //
				.id("MoreGamesButton") //
				.text("More games") //
				.position(width * 0.5f, height * 0.32f) //
				.center(0.5f, 0.5f) //
				.font(buttonFont) //
				.overColor(0f, 0f, 1f, 1f) //
				.notOverColor(1f, 1f, 0f, 1f) //
				.boundsOffset(40f, 20f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						if (Gdx.app.getType() == ApplicationType.Android)
							browserUtils.open(Game.androidMoreGamesUrl);
						else
							browserUtils.open(Game.desktopMoreGamesUrl);
						Analytics.traker.trackPageView("/about/moreGames", "/about/moreGames", null);
					}
				}) //
				.build());

		if (Gdx.app.getType() != ApplicationType.Android)
			guiContainer.add(GuiControls.textButton() //
					.id("BackButton") //
					.text("Back") //
					.position(width * 0.95f, height * 0.05f) //
					.center(1f, 0.5f) //
					.font(buttonFont) //
					.notOverColor(Color.WHITE) //
					.overColor(Color.GREEN) //
					.boundsOffset(40f, 20f) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							mainMenu();
						}
					}) //
					.build());

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();

		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("back", Keys.BACK, Keys.ESCAPE);
			}
		};

		worldWrapper = new WorldWrapper(new World());

		Injector injector = new InjectorImpl() {
			{
				bind("resourceManager", resourceManager);
				bind("timeStepProvider", new TimeStepProviderGameStateImpl(AboutGameState.this));
			}
		};
		
		SceneTemplate sceneTemplate = injector.getInstance(EmptySceneTemplate.class);
		sceneTemplate.apply(worldWrapper);

		worldWrapper.update(1);

		Analytics.traker.trackPageView("/about", "/about", null);
	}

	private void mainMenu() {
		game.transition(Screens.MainMenu)//
				.disposeCurrent() //
				.start();
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		worldWrapper.render();
		spriteBatch.begin();
		guiContainer.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		worldWrapper.update(getDeltaInMs());

		Synchronizers.synchronize(getDelta());
		guiContainer.update();
		inputDevicesMonitor.update();

		if (inputDevicesMonitor.getButton("back").isReleased())
			mainMenu();
	}

	@Override
	public void resume() {
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().show();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}
}
