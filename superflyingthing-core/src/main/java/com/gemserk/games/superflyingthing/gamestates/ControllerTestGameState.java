package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventListenerManager;
import com.gemserk.commons.artemis.events.EventListenerManagerImpl;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.TagComponent;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.CameraScript;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.systems.TagSystem;
import com.gemserk.games.superflyingthing.templates.ControllerTemplates;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.games.superflyingthing.templates.UserMessageTemplate;
import com.gemserk.resources.ResourceManager;

public class ControllerTestGameState extends GameStateImpl {

	private class ControllerTestModeScript extends ScriptJavaImpl {
		private int starsCollected = 0;

		@Override
		public void init(com.artemis.World world, Entity e) {
			eventListenerManager.register(Events.itemTaken, new EventListener() {
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
			game.transition(game.getSettingsScreen()).enterTime(250) //
					.leaveTime(250) //
					.disposeCurrent() //
					.start();
		}

		private void checkShipOutsideBounds(com.artemis.World world, Entity e) {
			Entity ship = world.getTagManager().getEntity(Groups.ship);
			Spatial shipSpatial = ComponentWrapper.getSpatial(ship);

			if (isShipInsideBounds(shipSpatial.getX(), shipSpatial.getY()))
				return;

			game.transition(game.getSettingsScreen()).enterTime(250) //
					.leaveTime(250) //
					.disposeCurrent() //
					.start();
		}

		private boolean isShipInsideBounds(float x, float y) {
			if ((x < -10f) || (x > 10f) || (y > 8f) || (y < -8f))
				return false;
			return true;
		}
	}

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

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
	private EventListenerManager eventListenerManager;

	private Container container;
	private Libgdx2dCamera guiCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public ControllerTestGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		container = new Container();
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();
		eventListenerManager = new EventListenerManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		guiCamera = new Libgdx2dCameraTransformImpl();

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		renderLayers.add(new RenderLayerSpriteBatchImpl(-1000, -100, backgroundLayerCamera, spriteBatch));
		renderLayers.add(new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(new RenderLayerSpriteBatchImpl(-50, 100, worldCamera, spriteBatch));
		renderLayers.add(new RenderLayerSpriteBatchImpl(100, 100000, guiCamera));

		world = new com.artemis.World();
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
		parameters = new ParametersWrapper();

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory);

		ControllerTemplates controllerTemplates = new ControllerTemplates();
		controllerTemplates.keyboardControllerTemplate = new ControllerTemplates.KeyboardControllerTemplate();
		controllerTemplates.androidClassicControllerTemplate = new ControllerTemplates.AndroidClassicControllerTemplate();
		controllerTemplates.axisControllerTemplate = new ControllerTemplates.AxisControllerTemplate(resourceManager);
		controllerTemplates.analogControllerTemplate = new ControllerTemplates.AnalogControllerTemplate(resourceManager);
		controllerTemplates.tiltAndroidControllerTemplate = new ControllerTemplates.TiltAndroidControllerTemplate();
		controllerTemplates.analogKeyboardControllerTemplate = new ControllerTemplates.AnalogKeyboardControllerTemplate();

		gameData = new GameData();

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager, eventListenerManager))) //
				.build();

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ControllerTestModeScript())).build();

		// create world

		float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;
		Camera camera = new CameraImpl(0f, 0f, cameraZoom, 0f);

		final ShipController controller = new ShipController();

		parameters.put("spatial", new SpatialImpl(-5f, 0f, 0.8f, 0.8f, 0f));
		parameters.put("controller", controller);

		entityFactory.instantiate(entityTemplates.getShipTemplate(), parameters);

		parameters.clear();
		parameters.put("camera", camera);
		parameters.put("libgdxCamera", worldCamera);
		parameters.put("script", new CameraScript(eventManager, eventListenerManager));
		parameters.put("spatial", new SpatialImpl(0f, 0f, 1f, 1f, 0f));
		entityFactory.instantiate(entityTemplates.getCameraTemplate(), parameters);

		parameters.put("controller", controller);
		entityFactory.instantiate(controllerTemplates.keyboardControllerTemplate, parameters);

		entityTemplates.star(-3f, 3f, new Scripts.StarScript(eventManager));
		entityTemplates.star(-3f, -3f, new Scripts.StarScript(eventManager));
		entityTemplates.star(3f, 3f, new Scripts.StarScript(eventManager));
		entityTemplates.star(3f, -3f, new Scripts.StarScript(eventManager));

		parameters.clear();
		entityFactory.instantiate(new UserMessageTemplate(container, resourceManager), //
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
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().hide();
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		worldWrapper.render();

		guiCamera.apply(spriteBatch);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		worldWrapper.update(delta);
		container.update();
		inputDevicesMonitor.update();

		if (inputDevicesMonitor.getButton("back").isReleased()) {
			game.transition(game.getSettingsScreen()).enterTime(250) //
					.leaveTime(250) //
					.disposeCurrent() //
					.start();
		}
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}