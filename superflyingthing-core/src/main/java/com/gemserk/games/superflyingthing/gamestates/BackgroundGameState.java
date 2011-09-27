package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.TimeTransition;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.CameraUpdateSystem;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.PreviousStateSpatialSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.controllers.BasicAIShipControllerScript;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class BackgroundGameState extends GameStateImpl {

	private final Game game;
	SpriteBatch spriteBatch;

	Libgdx2dCamera worldCamera;
	Libgdx2dCamera guiCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean done;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private EntityFactory entityFactory;
	private WorldWrapper worldWrapper;
	private Parameters parameters;

	GameData gameData;

	private EventManager eventManager;

	private Container guiContainer;

	private TimeTransition restartTimeTransition;
	private RenderLayers renderLayers;
	private Libgdx2dCamera backgroundLayerCamera;
	private Libgdx2dCamera secondBackgroundLayerCamera;

	private Integer previewLevelNumber;

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
		long startNanoTime = System.nanoTime();
		Gdx.app.log("SuperFlyingThing", "BackgroundGameState start loading");

		if (previewLevelNumber != null)
			reloadLevel(previewLevelNumber);
		else
			reloadLevel(MathUtils.random(1, 8));

		Gdx.app.log("SuperFlyingThing", "BackgroundGameState finished loading - " + (System.nanoTime() - startNanoTime) / 1000000 + " ms");
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

		eventManager = new EventManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		guiContainer = new Container();

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		guiCamera = new Libgdx2dCameraTransformImpl();

		backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		secondBackgroundLayerCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera, spriteBatch), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera, spriteBatch), game.getGamePreferences().isSecondBackgroundEnabled());
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		world = new com.artemis.World();
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
		parameters = new ParametersWrapper();
		// add render and all stuff...

		worldWrapper.addUpdateSystem(new PreviousStateSpatialSystem());
		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));

		worldWrapper.addRenderSystem(new CameraUpdateSystem(new TimeStepProviderGameStateImpl(this)));
		worldWrapper.addRenderSystem(new SpriteUpdateSystem(new TimeStepProviderGameStateImpl(this)));
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory, eventManager);

		gameData = new GameData();

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, parameters //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		Resource<Level> levelResource = resourceManager.get(Levels.levelId(levelNumber)); 
		Level level = levelResource.get();
		
		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, false).loadLevel(level);

		createWorld(levelNumber);

	}

	void createWorld(int levelNumber) {
		entityBuilder.component(new ScriptComponent(new BasicAIShipControllerScript(physicsWorld))).build();

		entityBuilder //
				.component(new GameDataComponent()) //
				.component(new ScriptComponent(new Scripts.GameScript(eventManager, //
						entityTemplates, entityFactory, gameData, false))) //
				.build();

		// loadLevel(entityTemplates, Levels.level(13));

		BitmapFont font = resourceManager.getResourceValue("VersionFont");

		guiContainer.add(GuiControls.label("Preview level " + levelNumber + "...") //
				.position(Gdx.graphics.getWidth() * 0.025f, Gdx.graphics.getHeight() * 0.2f) //
				.center(0f, 0.5f) //
				.color(1f, 1f, 1f, 1f) //
				.font(font) //
				.build());

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager))) //
				.build();

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ScriptJavaImpl() {

			@Override
			public void init(com.artemis.World world, Entity e) {
				eventManager.registerEvent(Events.gameStarted, e);
			}

			@Handles(ids = Events.destinationPlanetReached)
			public void destinationPlanetReached(Event e) {
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

		entityBuilder //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					@Override
					public void update(com.artemis.World world, Entity e) {
						Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
						CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(mainCamera);
						Camera camera = cameraComponent.getCamera();
						secondBackgroundLayerCamera.move(camera.getX(), camera.getY());
						secondBackgroundLayerCamera.zoom(camera.getZoom() * 0.25f);
						secondBackgroundLayerCamera.rotate(camera.getAngle());
					}

				})) //
				.build();

		// creates a new particle emitter spawner template which creates a new explosion when the ship dies.
		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);
	}

	private void gameFinished() {
		restartTimeTransition = new TimeTransition();
		restartTimeTransition.start(4f);
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		if (Game.isShowBox2dDebug())
			box2dCustomDebugRenderer.render();

		guiCamera.apply(spriteBatch);
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
		physicsWorld.dispose();
	}

}