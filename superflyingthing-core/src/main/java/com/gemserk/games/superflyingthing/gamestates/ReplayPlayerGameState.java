package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.scripts.LaserGunScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.DestinationPlanetScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StartPlanetScript;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.games.superflyingthing.templates.UserMessageTemplate;
import com.gemserk.resources.ResourceManager;

public class ReplayPlayerGameState extends GameStateImpl {

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean resetPressed;
	Container container;
	private Libgdx2dCamera guiCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	EntityTemplates entityTemplates;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private WorldWrapper worldWrapper;
	private EntityFactory entityFactory;
	private Parameters parameters;

	GameData gameData;
	private JointBuilder jointBuilder;

	private EventManager eventManager;

	EntityTemplate userMessageTemplate;

	private RenderLayers renderLayers;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public ReplayPlayerGameState(Game game) {
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
		resetPressed = false;
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		jointBuilder = new JointBuilder(physicsWorld);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		guiCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		final Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		secondBackgroundLayerCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), game.getGamePreferences().isSecondBackgroundEnabled());
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		world = new com.artemis.World();
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
		parameters = new ParametersWrapper();
		// add render and all stuff...
		GameInformation.worldWrapper = worldWrapper;

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		container = new Container();

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory, eventManager);

		// creates and registers all the controller templates
		gameData = new GameData();
		GameInformation.gameData = gameData;

		userMessageTemplate = new UserMessageTemplate(container, resourceManager);

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		// loadLevelForChallengeMode();

		Level level = game.getGameData().get("level");
		ReplayList replayList = game.getGameData().get("replayList");

		loadLevel(level, true);

		ArrayList<Replay> replays = replayList.getReplays();

		for (int i = 0; i < replays.size(); i++) {

			// could be a flag on the replay itself instead checking the last one.
			boolean mainReplay = i == replays.size() - 1;

			parameters.clear();
			Entity replayShip = entityFactory.instantiate(entityTemplates.getReplayShipTemplate(), parameters //
					.put("mainReplay", mainReplay) //
					);

			parameters.clear();
			entityFactory.instantiate(entityTemplates.getReplayPlayerTemplate(), parameters //
					.put("replay", replays.get(i)) //
					.put("target", replayShip) //
					);

			// if (i == replays.size() - 1)
			// replayShip.addComponent(new TagComponent(Groups.MainReplayShip));

		}

		entityBuilder //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					@Override
					public void init(com.artemis.World world, Entity e) {
						Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
						TargetComponent targetComponent = mainCamera.getComponent(TargetComponent.class);

						Entity mainReplayShip = world.getTagManager().getEntity(Groups.MainReplayShip);
						targetComponent.setTarget(mainReplayShip);

						// also starts a timer to invoke game over game state
					}

				})) //
				.build();

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager))) //
				.build();

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorPointerDown("continue", 0);
			}
		};

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
		entityFactory.instantiate(entityTemplates.getParticleEmitterSpawnerTemplate());

	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		createWorldLimits(worldWidth, worldHeight, 0.2f);
	}

	private void generateRandomClouds(float width, float height, int count) {
		Sprite sprite = resourceManager.getResourceValue("FogSprite");

		Color[] colors = new Color[] { Colors.yellow, Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Colors.magenta };

		for (int i = 0; i < count; i++) {

			float x = MathUtils.random(0, width);
			float y = MathUtils.random(0, height);

			float w = MathUtils.random(50, 80f);
			float h = w;

			float angle = MathUtils.random(0, 359f);

			Color color = new Color(colors[MathUtils.random(0, colors.length - 1)]);
			color.a = 0.3f;

			entityTemplates.staticSprite(new Sprite(sprite), x, y, w, h, angle, -200, 0.5f, 0.5f, color);
		}
	}

	private void createWorldLimits(float worldWidth, float worldHeight, float offset) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;
		float limitWidth = 0.1f;
		entityTemplates.boxObstacle(centerX, -offset, worldWidth + 1, limitWidth, 0f);
		entityTemplates.boxObstacle(centerX, worldHeight + offset, worldWidth + 1, limitWidth, 0f);
		entityTemplates.boxObstacle(-offset, centerY, limitWidth, worldHeight + 1, 0f);
		entityTemplates.boxObstacle(worldWidth + offset, centerY, limitWidth, worldHeight + 1, 0f);
	}

	void loadLevel(Level level, boolean shipInvulnerable) {
		float worldWidth = level.w;
		float worldHeight = level.h;

		float cameraZoom = Gdx.graphics.getWidth() * level.zoom / 800f;

		final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

		final ShipController controller = new ShipController();

		entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventManager));

		for (int i = 0; i < level.destinationPlanets.size(); i++) {
			DestinationPlanet destinationPlanet = level.destinationPlanets.get(i);
			entityTemplates.destinationPlanet(destinationPlanet.x, destinationPlanet.y, 1f, new DestinationPlanetScript(eventManager, jointBuilder, entityFactory, entityTemplates.getPlanetFillAnimationTemplate()));
		}

		parameters.clear();
		entityFactory.instantiate(entityTemplates.getCameraTemplate(), parameters //
				.put("camera", camera) //
				.put("libgdxCamera", worldCamera) //
				.put("spatial", new SpatialImpl(level.startPlanet.x, level.startPlanet.y, 1f, 1f, 0f)) //
				);

		for (int i = 0; i < level.obstacles.size(); i++) {
			Obstacle o = level.obstacles.get(i);
			if (o.bodyType == BodyType.StaticBody)
				entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			else {
				entityTemplates.movingObstacle(o.vertices, o.path, o.startPoint, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			}
		}

		for (int i = 0; i < level.items.size(); i++) {
			Level.Item item = level.items.get(i);
			entityTemplates.star(item.x, item.y, new StarScript(eventManager));
		}

		for (int i = 0; i < level.laserTurrets.size(); i++) {
			LaserTurret laserTurret = level.laserTurrets.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.getLaserGunTemplate(), parameters //
					.put("position", new Vector2(laserTurret.x, laserTurret.y)) //
					.put("angle", laserTurret.angle) //
					.put("fireRate", laserTurret.fireRate) //
					.put("bulletDuration", laserTurret.bulletDuration) //
					.put("currentReloadTime", laserTurret.currentReloadTime) //
					.put("script", new LaserGunScript(entityFactory)) //
					);
		}

		for (int i = 0; i < level.portals.size(); i++) {
			Portal portal = level.portals.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.getPortalTemplate(), parameters //
					.put("id", portal.id) //
					.put("targetPortalId", portal.targetPortalId) //
					.put("spatial", new SpatialImpl(portal.x, portal.y, portal.w, portal.h, portal.angle)) //
					);
		}

		createWorldLimits(worldWidth, worldHeight);

		generateRandomClouds(worldWidth, worldHeight, 6);
	}

	void loadLevelForChallengeMode() {
		if (Levels.hasLevel(GameInformation.level)) {
			Level level = Levels.level(GameInformation.level);
			loadLevel(level, false);
		}
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		guiCamera.apply(spriteBatch);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		GamePreferences gamePreferences = game.getGamePreferences();
		if (gamePreferences.isTutorialEnabled()) {
			game.transition(game.getInstructionsScreen(), 0, 300, false);
			return;
		}

		inputDevicesMonitor.update();
		Synchronizers.synchronize(getDelta());
		container.update();

		// if (inputDevicesMonitor.getButton("pause").isReleased())
		// game.transition(game.getPauseScreen(), 200, 300, false);

		worldWrapper.update(getDeltaInMs());
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().show();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getBackgroundGameScreen().dispose();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void dispose() {
		worldWrapper.dispose();
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}
