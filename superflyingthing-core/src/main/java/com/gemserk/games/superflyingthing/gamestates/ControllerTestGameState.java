package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.CameraUpdateSystem;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.PreviousStateSpatialSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.systems.TextLocationUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.entities.Tags;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.templates.ControllerTemplates;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.UserMessageTemplate;
import com.gemserk.resources.ResourceManager;

public class ControllerTestGameState extends GameStateImpl {

	private class ControllerTestModeScript extends ScriptJavaImpl {
		private int starsCollected = 0;
		boolean settingsCalled = false;

		@Override
		public void init(com.artemis.World world, Entity e) {
			eventManager.register(Events.itemTaken, new EventListener() {
				@Override
				public void onEvent(Event event) {
					starsCollected++;
				}
			});
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			checkStarsCollected(world, e);
			checkShipOutsideBounds(world, e);
		}

		private void checkStarsCollected(com.artemis.World world, Entity e) {
			if (starsCollected < 4)
				return;
			if (settingsCalled)
				return;
			settings();
			settingsCalled = true;
		}

		private void checkShipOutsideBounds(com.artemis.World world, Entity e) {
			Entity ship = world.getTagManager().getEntity(Tags.Ship);
			Spatial shipSpatial = GameComponents.getSpatial(ship);

			float limitX = 8.5f;
			int limitY = 6;

			if (shipSpatial.getX() > limitX)
				shipSpatial.setPosition(-limitX, shipSpatial.getY());
			else if (shipSpatial.getX() < -limitX)
				shipSpatial.setPosition(limitX, shipSpatial.getY());

			if (shipSpatial.getY() > limitY)
				shipSpatial.setPosition(shipSpatial.getX(), -limitY);
			else if (shipSpatial.getY() < -limitY)
				shipSpatial.setPosition(shipSpatial.getX(), limitY);

		}

	}

	Game game;
	ResourceManager<String> resourceManager;
	
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	boolean done;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private EntityFactory entityFactory;
	private WorldWrapper worldWrapper;
	private Parameters parameters;

	GameData gameData;

	private EventManager eventManager;

	private Container container;
	private Libgdx2dCamera hudCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
	private ControllerTemplates controllerTemplates;

	private boolean inputEnabled = false;

	@Override
	public void init() {
		container = new Container();
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		hudCamera = new Libgdx2dCameraTransformImpl();

		RenderLayers renderLayers = new RenderLayers();

		Boolean backgroundEnabled = getParameters().get("controllerTest/backgroundEnabled");

		// renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -100, backgroundLayerCamera, spriteBatch), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -100, backgroundLayerCamera, spriteBatch), backgroundEnabled);
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));

		// used by the controllers to draw stuff, could be removed later
		renderLayers.add(Layers.Hud, new RenderLayerSpriteBatchImpl(200, 10000, hudCamera));

		world = new com.artemis.World();
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
		parameters = new ParametersWrapper();

		worldWrapper.addUpdateSystem(new PreviousStateSpatialSystem());
		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		worldWrapper.addRenderSystem(new CameraUpdateSystem(new TimeStepProviderGameStateImpl(this)));
		worldWrapper.addRenderSystem(new SpriteUpdateSystem(new TimeStepProviderGameStateImpl(this)));
		worldWrapper.addRenderSystem(new TextLocationUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		Injector injector = new InjectorImpl() {
			{
				bind("physicsWorld", physicsWorld);
				bind("resourceManager", resourceManager);
				bind("entityBuilder", new EntityBuilder(world));
				bind("entityFactory", new EntityFactoryImpl(world));
				bind("eventManager", eventManager);
				bind("bodyBuilder", new BodyBuilder(physicsWorld));
				bind("mesh2dBuilder", new Mesh2dBuilder());
				bind("jointBuilder", new JointBuilder(physicsWorld));
			}
		};

		entityTemplates = new EntityTemplates(injector);

		controllerTemplates = new ControllerTemplates();
		controllerTemplates.keyboardControllerTemplate = new ControllerTemplates.KeyboardControllerTemplate();
		controllerTemplates.androidClassicControllerTemplate = new ControllerTemplates.AndroidClassicControllerTemplate();
		controllerTemplates.axisControllerTemplate = new ControllerTemplates.AxisControllerTemplate(resourceManager);
		controllerTemplates.analogControllerTemplate = new ControllerTemplates.AnalogControllerTemplate(resourceManager);
		controllerTemplates.tiltAndroidControllerTemplate = new ControllerTemplates.TiltAndroidControllerTemplate();
		controllerTemplates.analogKeyboardControllerTemplate = new ControllerTemplates.AnalogKeyboardControllerTemplate();
		controllerTemplates.targetControllerTemplate = new ControllerTemplates.TargetControllerTemplate();
		controllerTemplates.remoteClassicControllerTemplate = new ControllerTemplates.RemoteClassicControllerTemplate();

		gameData = new GameData();

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, parameters //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager))) //
				.build();

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ControllerTestModeScript())).build();

		// create world

		float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;
		Camera camera = new CameraImpl(0f, 0f, cameraZoom, 0f);

		final ShipController controller = new ShipController();

		entityFactory.instantiate(entityTemplates.shipTemplate, //
				parameters //
						.put("spatial", new SpatialImpl(-5f, 0f, 0.8f, 0.8f, 0f)) //
						.put("controller", controller) //
						.put("maxLinearSpeed", 3.5f) //
						.put("maxAngularVelocity", 360f) //
				);

		parameters.clear();
		entityFactory.instantiate(entityTemplates.cameraTemplate, //
				parameters.put("camera", camera) //
						.put("libgdxCamera", worldCamera) //
						.put("spatial", new SpatialImpl(0f, 0f, 1f, 1f, 0f))//
				);

		parameters.clear();
		ControllerType testControllerType = getParameters().get("testControllerType");
		entityFactory.instantiate(controllerTemplates.getControllerTemplate(testControllerType), //
				parameters.put("controller", controller));

		entityFactory.instantiate(entityTemplates.starTemplate, new ParametersWrapper() //
				.put("x", -3f) //
				.put("y", 3f) //
				.put("id", "star1") //
				);
		entityFactory.instantiate(entityTemplates.starTemplate, new ParametersWrapper() //
				.put("x", -3f) //
				.put("y", -3f) //
				.put("id", "star2") //
				);
		entityFactory.instantiate(entityTemplates.starTemplate, new ParametersWrapper() //
				.put("x", 3f) //
				.put("y", 3f) //
				.put("id", "star3") //
				);
		entityFactory.instantiate(entityTemplates.starTemplate, new ParametersWrapper() //
				.put("x", 3f) //
				.put("y", -3f) //
				.put("id", "star4") //
				);

		parameters.clear();
		
		EntityTemplate userMessageTemplate = injector.getInstance(UserMessageTemplate.class);
		
		entityFactory.instantiate(userMessageTemplate, //
				parameters //
						.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.9f)) //
						.put("text", "Collect all the stars") //
				);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("back", Keys.BACK, Keys.ESCAPE);
			}
		};

		inputEnabled = true;
	}

	private void settings() {
		inputEnabled = false;
		game.transition(Screens.ControllersSettings) //
				.disposeCurrent() //
				.start();
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().hide();
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		worldWrapper.render();

		hudCamera.apply(spriteBatch);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		worldWrapper.update(getDeltaInMs());
		container.update();

		if (inputEnabled) {
			inputDevicesMonitor.update();
			if (inputDevicesMonitor.getButton("back").isReleased())
				settings();
		}
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}