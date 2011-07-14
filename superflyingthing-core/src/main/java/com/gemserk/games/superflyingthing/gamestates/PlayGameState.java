package com.gemserk.games.superflyingthing.gamestates;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.interpolator.function.InterpolationFunctions;
import com.gemserk.animation4j.transitions.Transition;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.event.TransitionEventHandler;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.entities.EntityBuilder;
import com.gemserk.games.superflyingthing.Behaviors.CallTriggerIfEntityDeadBehavior;
import com.gemserk.games.superflyingthing.Behaviors.CallTriggerIfNoShipBehavior;
import com.gemserk.games.superflyingthing.Behaviors.FixCameraTargetBehavior;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.GameData;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;
import com.gemserk.games.superflyingthing.Components.TriggerComponent;
import com.gemserk.games.superflyingthing.EntityTemplates;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.GamePreferences;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.games.superflyingthing.Trigger;
import com.gemserk.games.superflyingthing.Triggers;
import com.gemserk.games.superflyingthing.gamestates.Level.Obstacle;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.ShapeRenderSystem;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class PlayGameState extends GameStateImpl {

	// temporal

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean resetPressed;
	boolean done;
	Container container;
	private Libgdx2dCamera guiCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private WorldWrapper worldWrapper;

	GameData gameData;
	private Text itemsTakenLabel;

	public PlayGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		resetPressed = false;
		spriteBatch = new SpriteBatch();

		physicsWorld = new World(new Vector2(), false);
		// physicsWorld.setContactListener(new PhysicsContactListener());

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		renderLayers.add(new RenderLayer(-1000, -100, backgroundLayerCamera));
		renderLayers.add(new RenderLayer(-100, 100, worldCamera));

		world = new com.artemis.World();
		worldWrapper = new WorldWrapper(world);
		// add render and all stuff...

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new SpriteRendererSystem(renderLayers));

		worldWrapper.addRenderSystem(new ShapeRenderSystem(ShapeComponent.class));
		
		worldWrapper.addRenderSystem(new EntityProcessingSystem(SpatialComponent.class, MovementComponent.class) {
			@Override
			protected void process(Entity e) {
				Spatial spatial = ComponentWrapper.getSpatial(e);
				if (spatial == null)
					return;
				Vector2 position = spatial.getPosition();
				MovementComponent movementComponent = e.getComponent(MovementComponent.class);
				if (movementComponent == null)
					return;
				Vector2 direction = movementComponent.getDirection();
				float x = position.x + direction.tmp().mul(0.5f).x;
				float y = position.y + direction.tmp().mul(0.5f).y;
				ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
			}
		});
		
		worldWrapper.addRenderSystem(new ParticleEmitterSystem(worldCamera));
		
		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		guiCamera = new Libgdx2dCameraTransformImpl();

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		resourceManager = new ResourceManagerImpl<String>();
		GameResources.load(resourceManager);
		container = new Container();

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder);

		gameData = new GameData();

		BitmapFont font = resourceManager.getResourceValue("GameFont");

		itemsTakenLabel = GuiControls.label("") //
				.position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.95f) //
				.font(font) //
				.color(1f, 1f, 1f, 1f) //
				.build();

		container.add(itemsTakenLabel);

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			new RandomMode().create(this);
			Analytics.traker.trackPageView("/startChallengeMode", "/startChallengeMode", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			new PracticeMode().create(this);
			Analytics.traker.trackPageView("/startPracticeMode", "/startPracticeMode", null);
		} else if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			new ChallengeMode().create(this);
			Analytics.traker.trackPageView("/startRandomMode", "/startRandomMode", null);
		}

		done = false;

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("pause", Keys.BACK, Keys.ESCAPE);
				monitorKeys("restart", Keys.MENU, Keys.R);
			}
		};

	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;

		entityTemplates.boxObstacle(centerX, -0.2f, worldWidth, 0.1f, 0f);
		entityTemplates.boxObstacle(centerX, worldHeight + 0.2f, worldWidth, 0.1f, 0f);
		entityTemplates.boxObstacle(-0.2f, centerY, 0.1f, worldHeight, 0f);
		entityTemplates.boxObstacle(worldWidth + 0.2f, centerY, 0.1f, worldHeight, 0f);
	}

	class ChallengeMode {

		void loadLevel(EntityTemplates templates, Level level) {
			float worldWidth = level.w;
			float worldHeight = level.h;

			Camera camera = new CameraRestrictedImpl(0f, 0f, 40f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			Entity startPlanet = entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f);

			entityTemplates.destinationPlanet(level.destinationPlanet.x, level.destinationPlanet.y, 1f, new Trigger() {
				@Override
				protected void onTrigger(Entity e) {
					gameFinished();
					triggered();
				}
			});

			Entity cameraEntity = entityTemplates.camera(camera, worldCamera, level.startPlanet.x, level.startPlanet.y);

			for (int i = 0; i < level.obstacles.size(); i++) {
				Obstacle o = level.obstacles.get(i);
				if (o.bodyType == BodyType.StaticBody)
					entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
				else {
					entityTemplates.movingObstacle(o.vertices, o.path, o.x, o.y, o.angle * MathUtils.degreesToRadians);
				}
			}

			for (int i = 0; i < level.items.size(); i++) {
				Level.Item item = level.items.get(i);
				entityTemplates.diamond(item.x, item.y, 0.2f, new Trigger() {
					@Override
					protected void onTrigger(Entity e) {
						gameData.currentItems++;
						itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
					}
				});
			}

			gameData.totalItems = level.items.size();
			if (gameData.totalItems > 0)
				itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			createWorldLimits(worldWidth, worldHeight);
			
			entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component(new TriggerComponent(new HashMap<String, Trigger>() {
						{
							put(Triggers.entityDeadTrigger, new Trigger() {
								@Override
								public void onTrigger(Entity e) {
									GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
									
									Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.ship);
									entityTemplates.explosionEffect(spatial.getX(), spatial.getY());

									world.deleteEntity(gameDataComponent.ship);
									// I don't like the world.createEntity() and explicit call to e.refresh() :( !!
									gameDataComponent.ship = null;
									gameData.deaths++;
									
									
								}
							});
							put(Triggers.noEntityTrigger, new Trigger() {
								@Override
								public void onTrigger(Entity e) {
									GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
									Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.startPlanet);
									Entity ship = entityTemplates.ship(spatial.getX(), spatial.getY() + 2f, new Vector2(1f, 0f));
									// I don't like the world.createEntity() !!
									// world.add(ship);
									AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
									attachmentComponent.setEntity(ship);
									gameDataComponent.ship = ship;
									// PlayGameState.this.game.transition(PlayGameState.this.game.getGameOverScreen(), 500, 500, false);
									// triggered();
								}
							});
						}
					})) //
					.component(new ScriptComponent(new ScriptJavaImpl() {

						Behavior callTriggerIfEntityDeadBehavior = new CallTriggerIfEntityDeadBehavior();
						Behavior callTriggerIfNoShipBehavior = new CallTriggerIfNoShipBehavior();
						Behavior fixCameraTargetBehavior = new FixCameraTargetBehavior();

						@Override
						public void update(com.artemis.World world, Entity e) {
							callTriggerIfEntityDeadBehavior.update(world.getDelta(), e);
							callTriggerIfNoShipBehavior.update(world.getDelta(), e);
							fixCameraTargetBehavior.update(world.getDelta(), e);
						}

					})).build();

			BitmapFont font = resourceManager.getResourceValue("GameFont");

			Text levelNameText = GuiControls.label("Level " + (GameInformation.level + 1) + ": " + level.name).position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.9f) //
					.font(font) //
					.color(1f, 1f, 1f, 1f) //
					.build();

			container.add(levelNameText);

			Synchronizers.transition(levelNameText.getColor(), Transitions.transitionBuilder(levelNameText.getColor()) //
					.end(new Color(1f, 1f, 1f, 0f)) //
					.functions(InterpolationFunctions.linear(), InterpolationFunctions.linear(), InterpolationFunctions.linear(), InterpolationFunctions.easeOut()) //
					.time(3000));
		}

		void create(PlayGameState p) {
			p.entityTemplates = entityTemplates;

			if (Levels.hasLevel(GameInformation.level)) {
				Level level = Levels.level(GameInformation.level);
				loadLevel(entityTemplates, level);
			}

			// simulate a step to put everything on their places
			worldWrapper.update(1);
			worldWrapper.update(1);
		}
	}

	class RandomMode {

		boolean insideObstacle;

		private final Shape[] shapes = new Shape[] { //
		new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
				new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
				new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
		};

		private Shape getRandomShape() {
			return shapes[MathUtils.random(shapes.length - 1)];
		}

		void create(PlayGameState p) {
			World physicsWorld = p.physicsWorld;
			ResourceManager<String> resourceManager = p.resourceManager;

			float worldWidth = MathUtils.random(30f, 150f);
			float worldHeight = MathUtils.random(10f, 20f);

			Gdx.app.log("SuperFlyingThing", "new world generated with size " + worldWidth + ", " + worldHeight);

			Camera camera = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			float obstacleX = 12f;

			while (obstacleX < worldWidth - 17f) {
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				obstacleX += 8f;
			}

			int itemsCount = 0;

			for (int i = 0; i < 10; i++) {
				float x = MathUtils.random(10f, worldWidth - 10f);
				float y = MathUtils.random(2f, worldHeight - 2f);
				float w = 0.2f;
				float h = 0.2f;

				insideObstacle = false;

				physicsWorld.QueryAABB(new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {
						insideObstacle = true;
						return false;
					}
				}, x - w, y - h, x + w, y + h);

				if (insideObstacle)
					continue;

				entityTemplates.diamond(x, y, w, new Trigger() {
					@Override
					protected void onTrigger(Entity e) {
						gameData.currentItems++;
						itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
					}
				});

				itemsCount++;
			}

			gameData.totalItems = itemsCount;
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			Entity cameraEntity = entityTemplates.camera(camera, worldCamera, 5f, worldHeight * 0.5f);

			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f);

			entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new Trigger() {
				@Override
				protected void onTrigger(Entity e) {
					gameFinished();
					triggered();
				}
			});

			createWorldLimits(worldWidth, worldHeight);

			entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component(new TriggerComponent(new HashMap<String, Trigger>() {
						{
							put(Triggers.entityDeadTrigger, new Trigger() {
								@Override
								public void onTrigger(Entity e) {
									GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
									
									Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.ship);
									entityTemplates.explosionEffect(spatial.getX(), spatial.getY());
									
									world.deleteEntity(gameDataComponent.ship);

									Spatial superSheepSpatial = ComponentWrapper.getSpatial(gameDataComponent.ship);
									entityTemplates.deadShip(superSheepSpatial);

									gameDataComponent.ship = null;
									gameData.deaths++;

									Analytics.traker.trackPageView("/randomMode/shipDead", "/randomMode/shipDead", null);
									
									TargetComponent targetComponent = gameDataComponent.camera.getComponent(TargetComponent.class);
									targetComponent.setTarget(null);
									
									
								}
							});
							put(Triggers.noEntityTrigger, new Trigger() {
								@Override
								public void onTrigger(Entity e) {
									GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
									Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.startPlanet);
									Entity ship = entityTemplates.ship(spatial.getX(), spatial.getY() + 2f, new Vector2(1f, 0f));
									AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
									attachmentComponent.setEntity(ship);
									gameDataComponent.ship = ship;
								}
							});
						}
					})) //
					.component(new ScriptComponent(new ScriptJavaImpl() {

						Behavior callTriggerIfEntityDeadBehavior = new CallTriggerIfEntityDeadBehavior();
						Behavior callTriggerIfNoShipBehavior = new CallTriggerIfNoShipBehavior();
						Behavior fixCameraTargetBehavior = new FixCameraTargetBehavior();

						@Override
						public void update(com.artemis.World world, Entity e) {
							callTriggerIfEntityDeadBehavior.update(world.getDelta(), e);
							callTriggerIfNoShipBehavior.update(world.getDelta(), e);
							fixCameraTargetBehavior.update(world.getDelta(), e);
						}

					})).build();

			// simulate a step to put everything on their places

			worldWrapper.update(1);
			worldWrapper.update(1);

			// if R and MENU doesn't generate N levels per second
			// Analytics.traker.trackPageView("/randomMode/newLevel", "/randomMode/newLevel", null);
		}
	}

	class PracticeMode {

		boolean insideObstacle;

		private final Shape[] shapes = new Shape[] { //
		new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
				new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
				new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
		};

		private Shape getRandomShape() {
			return shapes[MathUtils.random(shapes.length - 1)];
		}

		void create(PlayGameState p) {
			World physicsWorld = p.physicsWorld;
			ResourceManager<String> resourceManager = p.resourceManager;

			p.entityTemplates = entityTemplates;

			float worldWidth = MathUtils.random(40f, 40f);
			float worldHeight = MathUtils.random(15f, 15f);

			Gdx.app.log("SuperFlyingThing", "new world generated with size " + worldWidth + ", " + worldHeight);

			Camera camera = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			float obstacleX = 12f;

			while (obstacleX < worldWidth - 17f) {
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				obstacleX += 8f;
			}

			int itemsCount = 0;

			for (int i = 0; i < 10; i++) {
				float x = MathUtils.random(10f, worldWidth - 10f);
				float y = MathUtils.random(2f, worldHeight - 2f);
				float w = 0.2f;
				float h = 0.2f;

				insideObstacle = false;

				physicsWorld.QueryAABB(new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {
						insideObstacle = true;
						return false;
					}
				}, x - w, y - h, x + w, y + h);

				if (insideObstacle)
					continue;

				entityTemplates.diamond(x, y, w, new Trigger() {
					@Override
					protected void onTrigger(Entity e) {
						gameData.currentItems++;
						itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
					}
				});

				itemsCount++;
			}

			gameData.totalItems = itemsCount;
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			Entity cameraEntity = entityTemplates.camera(camera, worldCamera, 5f, worldHeight * 0.5f);
			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f);

			entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new Trigger() {
				@Override
				protected void onTrigger(Entity e) {
					gameFinished();
					triggered();
				}
			});

			createWorldLimits(worldWidth, worldHeight);

			entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component(new TriggerComponent(new HashMap<String, Trigger>() {
						{
							put(Triggers.noEntityTrigger, new Trigger() {
								@Override
								public void onTrigger(Entity e) {
									GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
									Entity ship = entityTemplates.ship(5f, 6f, new Vector2(1f, 0f));
									AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
									attachmentComponent.setEntity(ship);
									gameDataComponent.ship = ship;
								}
							});
						}
					})) //
					.component(new ScriptComponent(new ScriptJavaImpl() {

						Behavior callTriggerIfNoShipBehavior = new CallTriggerIfNoShipBehavior();
						Behavior fixCameraTargetBehavior = new FixCameraTargetBehavior();

						@Override
						public void update(com.artemis.World world, Entity e) {
							callTriggerIfNoShipBehavior.update(world.getDelta(), e);
							fixCameraTargetBehavior.update(world.getDelta(), e);
						}

					})).build();

			// simulate a step to put everything on their places
			worldWrapper.update(1);
			worldWrapper.update(1);
		}
	}

	private void gameFinished() {
		BitmapFont font = resourceManager.getResourceValue("GameFont");

		Text message = GuiControls.label("Great Job!").position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f) //
				.font(font) //
				.color(1f, 1f, 1f, 1f) //
				.build();

		container.add(message);

		Synchronizers.transition(message.getColor(), Transitions.transitionBuilder(message.getColor()) //
				.end(new Color(1f, 1f, 1f, 0f)) //
				.functions(InterpolationFunctions.linear(), InterpolationFunctions.linear(), InterpolationFunctions.linear(), InterpolationFunctions.easeOut()) //
				.time(1500), new TransitionEventHandler<Color>() {
			@Override
			public void onTransitionFinished(Transition<Color> transition) {
				done = true;
			}
		});

		// Analytics.traker.trackPageView("/randomMode/finishLevel", "/randomMode/finishLevel", null);
	}

	// private void diposeJoints(Entity e) {
	// AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);
	// if (entityAttachment == null)
	// return;
	// if (entityAttachment.getJoint() == null)
	// return;
	// physicsWorld.destroyJoint(entityAttachment.getJoint());
	// Gdx.app.log("SuperFlyingThing", "removing joints from physics world");
	// }

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		if (Game.isShowBox2dDebug())
			box2dCustomDebugRenderer.render();

		guiCamera.apply(spriteBatch);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {

		GamePreferences gamePreferences = game.getGamePreferences();
		if (gamePreferences.isTutorialEnabled()) {
			gamePreferences.setTutorialEnabled(false);
			game.transition(game.getInstructionsScreen(), 0, 300, false);
			return;
		}

		inputDevicesMonitor.update();
		Synchronizers.synchronize(delta);
		container.update();

		if (inputDevicesMonitor.getButton("restart").isReleased())
			done = true;

		if (inputDevicesMonitor.getButton("pause").isReleased())
			game.transition(game.getPauseScreen(), 200, 300, false);

		if (done) {
			if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
				Analytics.traker.trackPageView("/challengeMode/finishLevel", "/challengeMode/finishLevel", null);

				game.transition(game.getGameOverScreen(), 0, 300, false);

				// if (!Levels.hasLevel(GameInformation.level + 1))
				// game.transition(game.getLevelSelectionScreen(), 200, 300);
				// else {
				// GameInformation.level++;
				// game.getPlayScreen().restart();
				// }
				
			} else {
				game.getPlayScreen().restart();
			}
		}

		worldWrapper.update(delta);
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().hide();
		super.resume();
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void pause() {
		game.getAdWhirlViewHandler().show();
		super.pause();
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		physicsWorld.dispose();
		resourceManager.unloadAll();
	}

}