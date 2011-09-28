package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.scenes.BackgroundSceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.resources.ResourceManager;

public class BackgroundGameState extends GameStateImpl {

	private final Game game;
	SpriteBatch spriteBatch;

	ResourceManager<String> resourceManager;
	boolean done;

	private WorldWrapper worldWrapper;

	private Container guiContainer;

	private TimeTransition restartTimeTransition;

	private Integer previewLevelNumber;

	RenderLayers renderLayers;
	EntityBuilder entityBuilder;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public BackgroundGameState(Game game) {
		this.game = game;
	}

	@Handles
	public void toggleFirstBackground(Event e) {
		if (renderLayers != null)
			renderLayers.toggle(Layers.FirstBackground);
	}

	@Handles
	public void toggleSecondBackground(Event e) {
		if (renderLayers != null)
			renderLayers.toggle(Layers.SecondBackground);
	}

	@Override
	public void init() {
		if (previewLevelNumber != null)
			reloadLevel(previewLevelNumber);
		else
			reloadLevel(MathUtils.random(1, 8));
	}

	@Handles(ids = Events.previewLevel)
	public void previewLevel(Event event) {
		dispose();
		previewLevelNumber = (Integer) event.getSource();
		reloadLevel(previewLevelNumber);
	}

	@Handles(ids = Events.previewRandomLevel)
	public void previewRandomLevel(Event event) {
		previewLevelNumber = null;
	}

	void reloadLevel(int levelNumber) {
		restartTimeTransition = null;

		spriteBatch = new SpriteBatch();

		worldWrapper = new WorldWrapper(new World());

		Injector injector = new Injector() {
			{
				add("resourceManager", resourceManager);
				add("timeStepProvider", new TimeStepProviderGameStateImpl(BackgroundGameState.this));
				add("entityBuilder", new EntityBuilder(worldWrapper.getWorld()));
			}
		};

		Provider provider = new ProviderImpl(injector);

		SceneTemplate sceneTemplate = provider.get(BackgroundSceneTemplate.class);
		sceneTemplate.getParameters().put("levelNumber", levelNumber);
		sceneTemplate.apply(worldWrapper);
		
		injector.injectMembers(this);

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ScriptJavaImpl() {

			private World world;

			@Override
			public void init(com.artemis.World world, Entity e) {
				this.world = world;
			}

			@Handles(ids = Events.destinationPlanetReached)
			public void gameOverWhenShipDestroyed(Event event) {
				gameFinished();
			}

			@Handles(ids = Events.gameStarted)
			public void resetCameraZoomWhenGameStarted(Event event) {
				Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
				CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(mainCamera);
				Camera camera = cameraComponent.getCamera();
				camera.setZoom(Gdx.graphics.getWidth() * 24f / 800f);
			}

		})).build();

		worldWrapper.update(1);

		//

		BitmapFont font = resourceManager.getResourceValue("VersionFont");
		
		guiContainer = new Container();

		guiContainer.add(GuiControls.label("Preview level " + levelNumber + "...") //
				.id("PreviewLevelNumberLabel") //
				.center(0f, 1f) //
				.font(font) //
				.position(Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.02f + game.getAdsMaxArea().getHeight()) //
				.build());
	}

	private void gameFinished() {
		restartTimeTransition = new TimeTransition();
		restartTimeTransition.start(4f);
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
		Synchronizers.synchronize(getDelta());
		guiContainer.update();

		if (restartTimeTransition != null) {
			restartTimeTransition.update(getDelta());
			if (restartTimeTransition.isFinished())
				game.getBackgroundGameScreen().restart();
		}

		worldWrapper.update(getDeltaInMs());
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		worldWrapper.dispose();
	}

}