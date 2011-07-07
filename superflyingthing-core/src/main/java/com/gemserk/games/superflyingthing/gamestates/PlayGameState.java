package com.gemserk.games.superflyingthing.gamestates;

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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.interpolator.function.InterpolationFunctions;
import com.gemserk.animation4j.transitions.Transition;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.event.TransitionEventHandler;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityBuilder;
import com.gemserk.games.entities.EntityLifeCycleHandler;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.entities.EntityManagerImpl;
import com.gemserk.games.superflyingthing.Behaviors.CallTriggerIfEntityDeadBehavior;
import com.gemserk.games.superflyingthing.Behaviors.CallTriggerIfNoShipBehavior;
import com.gemserk.games.superflyingthing.Behaviors.FixCameraTargetBehavior;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;
import com.gemserk.games.superflyingthing.EntityTemplates;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.PhysicsContactListener;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.games.superflyingthing.Trigger;
import com.gemserk.games.superflyingthing.gamestates.Level.Obstacle;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class PlayGameState extends GameStateImpl implements EntityLifeCycleHandler {

	// temporal

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	EntityManager entityManager;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean resetPressed;
	boolean done;
	Container container;
	private Libgdx2dCamera guiCamera;

	public PlayGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		resetPressed = false;
		spriteBatch = new SpriteBatch();

		physicsWorld = new World(new Vector2(), false);
		physicsWorld.setContactListener(new PhysicsContactListener());

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		guiCamera = new Libgdx2dCameraTransformImpl();

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		resourceManager = new ResourceManagerImpl<String>();
		GameResources.load(resourceManager);
		container = new Container();

		if (GameData.gameMode == GameData.RandomGameMode) {
			new RandomMode().create(this);
			Analytics.traker.trackPageView("/startChallengeMode", "/startChallengeMode", null);
		} else if (GameData.gameMode == GameData.PracticeGameMode) {
			new PracticeMode().create(this);
			Analytics.traker.trackPageView("/startPracticeMode", "/startPracticeMode", null);
		} else if (GameData.gameMode == GameData.ChallengeGameMode) {
			new ChallengeMode().create(this);
			Analytics.traker.trackPageView("/startRandomMode", "/startRandomMode", null);
		}

		done = false;
	}

	class ChallengeMode {

		EntityBuilder entityBuilder = new EntityBuilder();

		void loadLevel(final EntityManager entityManager, EntityTemplates templates, Level level) {
			float worldWidth = level.w;
			float worldHeight = level.h;

			float x = worldWidth * 0.5f;
			float y = worldHeight * 0.5f;

			Camera camera = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f);

			entityManager.add(startPlanet);
			entityManager.add(entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new Trigger() {
				@Override
				protected void onTrigger(Entity e) {
					gameFinished();
					triggered();
				}
			}));

			Entity cameraEntity = entityTemplates.camera(camera);
			entityManager.add(cameraEntity);

			cameraEntity.addBehavior(new Behavior() {
				@Override
				public void update(int delta, Entity e) {
					Camera camera = ComponentWrapper.getCamera(e);
					worldCamera.move(camera.getX(), camera.getY());
					worldCamera.zoom(camera.getZoom());
					worldCamera.rotate(camera.getAngle());
				}
			});

			for (int i = 0; i < level.obstacles.length; i++) {
				Obstacle o = level.obstacles[i];
				entityManager.add(entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians));
			}

			for (int i = 0; i < level.items.length; i++) {
				Level.Item item = level.items[i];
				entityManager.add(entityTemplates.diamond(item.x, item.y, 0.2f));
			}

			entityManager.add(entityTemplates.boxObstacle(x, 0f, worldWidth, 0.1f, 0f));
			entityManager.add(entityTemplates.boxObstacle(x, worldHeight, worldWidth, 0.1f, 0f));
			entityManager.add(entityTemplates.boxObstacle(0, y, 0.1f, worldHeight, 0f));
			entityManager.add(entityTemplates.boxObstacle(worldWidth, y, 0.1f, worldHeight, 0f));

			Entity game = entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component("entityDeadTrigger", new Trigger() {
						@Override
						public void onTrigger(Entity e) {
							GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
							entityManager.remove(gameDataComponent.ship);

							Spatial superSheepSpatial = ComponentWrapper.getSpatial(gameDataComponent.ship);
							Entity deadSuperSheepEntity = entityTemplates.deadShip(superSheepSpatial);
							entityManager.add(deadSuperSheepEntity);

							gameDataComponent.ship = null;
						}
					}) //
					.behavior(new CallTriggerIfEntityDeadBehavior()) //
					.component("noEntityTrigger", new Trigger() {
						@Override
						public void onTrigger(Entity e) {
							GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
							Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.startPlanet);
							Entity ship = entityTemplates.ship(spatial.getX(), spatial.getY() + 2f, new Vector2(1f, 0f));
							entityManager.add(ship);
							AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
							attachmentComponent.setEntity(ship);
							gameDataComponent.ship = ship;
						}
					}) //
					.behavior(new CallTriggerIfNoShipBehavior()) //
					.behavior(new FixCameraTargetBehavior()) //
					.build();
			entityManager.add(game);

			BitmapFont font = resourceManager.getResourceValue("GameFont");

			Text levelNameText = GuiControls.label(level.name).position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.9f) //
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
			World physicsWorld = p.physicsWorld;
			ResourceManager<String> resourceManager = p.resourceManager;

			final EntityManager entityManager = new EntityManagerImpl(p);
			final EntityTemplates entityTemplates = new EntityTemplates(physicsWorld, entityManager, resourceManager);

			p.entityManager = entityManager;
			p.entityTemplates = entityTemplates;

			if (GameData.level != null)
				loadLevel(entityManager, entityTemplates, GameData.level);

			// simulate a step to put everything on their places
			entityManager.update(1);
			physicsWorld.step(1, 1, 1);
			entityManager.update(1);
		}
	}
	
	class RandomMode {

		EntityBuilder entityBuilder = new EntityBuilder();

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

			final EntityManager entityManager = new EntityManagerImpl(p);
			final EntityTemplates entityTemplates = new EntityTemplates(physicsWorld, entityManager, resourceManager);

			p.entityManager = entityManager;
			p.entityTemplates = entityTemplates;

			float worldWidth = MathUtils.random(30f, 150f);
			float worldHeight = MathUtils.random(10f, 20f);

			Gdx.app.log("SuperFlyingThing", "new world generated with size " + worldWidth + ", " + worldHeight);

			Camera camera = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			float obstacleX = 12f;

			while (obstacleX < worldWidth - 17f) {
				entityManager.add(entityTemplates.obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f)));
				entityManager.add(entityTemplates.obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f)));
				obstacleX += 8f;
			}

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

				entityManager.add(entityTemplates.diamond(x, y, w));
			}

			Entity cameraEntity = entityTemplates.camera(camera);
			entityManager.add(cameraEntity);

			cameraEntity.addBehavior(new Behavior() {
				@Override
				public void update(int delta, Entity e) {
					Camera camera = ComponentWrapper.getCamera(e);
					worldCamera.move(camera.getX(), camera.getY());
					worldCamera.zoom(camera.getZoom());
					worldCamera.rotate(camera.getAngle());
				}
			});

			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f);

			entityManager.add(startPlanet);
			entityManager.add(entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new Trigger() {
				@Override
				protected void onTrigger(Entity e) {
					gameFinished();
					triggered();
				}
			}));

			float x = worldWidth * 0.5f;
			float y = worldHeight * 0.5f;

			entityManager.add(entityTemplates.boxObstacle(x, 0f, worldWidth, 0.1f, 0f));
			entityManager.add(entityTemplates.boxObstacle(x, worldHeight, worldWidth, 0.1f, 0f));
			entityManager.add(entityTemplates.boxObstacle(0, y, 0.1f, worldHeight, 0f));
			entityManager.add(entityTemplates.boxObstacle(worldWidth, y, 0.1f, worldHeight, 0f));

			Entity game = entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component("entityDeadTrigger", new Trigger() {
						@Override
						public void onTrigger(Entity e) {
							GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
							entityManager.remove(gameDataComponent.ship);

							Spatial superSheepSpatial = ComponentWrapper.getSpatial(gameDataComponent.ship);
							Entity deadSuperSheepEntity = entityTemplates.deadShip(superSheepSpatial);
							entityManager.add(deadSuperSheepEntity);

							gameDataComponent.ship = null;

							Analytics.traker.trackPageView("/randomMode/shipDead", "/randomMode/shipDead", null);
						}
					}) //
					.behavior(new CallTriggerIfEntityDeadBehavior()) //
					.component("noEntityTrigger", new Trigger() {
						@Override
						public void onTrigger(Entity e) {
							GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
							Spatial spatial = ComponentWrapper.getSpatial(gameDataComponent.startPlanet);
							Entity ship = entityTemplates.ship(spatial.getX(), spatial.getY() + 2f, new Vector2(1f, 0f));
							entityManager.add(ship);
							AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
							attachmentComponent.setEntity(ship);
							gameDataComponent.ship = ship;
						}
					}) //
					.behavior(new CallTriggerIfNoShipBehavior()) //
					.behavior(new FixCameraTargetBehavior()) //
					.build();
			entityManager.add(game);

			// simulate a step to put everything on their places
			entityManager.update(1);
			physicsWorld.step(1, 1, 1);
			entityManager.update(1);

			// if R and MENU doesn't generate N levels per second
			// Analytics.traker.trackPageView("/randomMode/newLevel", "/randomMode/newLevel", null);
		}
	}

	class PracticeMode {

		EntityBuilder entityBuilder = new EntityBuilder();

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

			final EntityManager entityManager = new EntityManagerImpl(p);
			final EntityTemplates entityTemplates = new EntityTemplates(physicsWorld, entityManager, resourceManager);

			p.entityManager = entityManager;
			p.entityTemplates = entityTemplates;

			float worldWidth = MathUtils.random(40f, 40f);
			float worldHeight = MathUtils.random(15f, 15f);

			Gdx.app.log("SuperFlyingThing", "new world generated with size " + worldWidth + ", " + worldHeight);

			Camera camera = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			float obstacleX = 12f;

			while (obstacleX < worldWidth - 17f) {
				entityManager.add(entityTemplates.obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f)));
				entityManager.add(entityTemplates.obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f)));
				obstacleX += 8f;
			}

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

				entityManager.add(entityTemplates.diamond(x, y, w));
			}

			Entity cameraEntity = entityTemplates.camera(camera);
			entityManager.add(cameraEntity);

			cameraEntity.addBehavior(new Behavior() {
				@Override
				public void update(int delta, Entity e) {
					Camera camera = ComponentWrapper.getCamera(e);
					worldCamera.move(camera.getX(), camera.getY());
					worldCamera.zoom(camera.getZoom());
					worldCamera.rotate(camera.getAngle());
				}
			});

			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f);

			entityManager.add(startPlanet);
			entityManager.add(entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new Trigger() {
			}));

			float x = worldWidth * 0.5f;
			float y = worldHeight * 0.5f;

			entityManager.add(entityTemplates.boxObstacle(x, 0f, worldWidth, 0.1f, 0f));
			entityManager.add(entityTemplates.boxObstacle(x, worldHeight, worldWidth, 0.1f, 0f));
			entityManager.add(entityTemplates.boxObstacle(0, y, 0.1f, worldHeight, 0f));
			entityManager.add(entityTemplates.boxObstacle(worldWidth, y, 0.1f, worldHeight, 0f));

			Entity game = entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component("noEntityTrigger", new Trigger() {
						@Override
						public void onTrigger(Entity e) {
							GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
							Entity ship = entityTemplates.ship(5f, 6f, new Vector2(1f, 0f));
							entityManager.add(ship);
							AttachmentComponent attachmentComponent = gameDataComponent.startPlanet.getComponent(AttachmentComponent.class);
							attachmentComponent.setEntity(ship);
							gameDataComponent.ship = ship;
						}
					}) //
					.behavior(new CallTriggerIfNoShipBehavior()) //
					.behavior(new FixCameraTargetBehavior()) //
					.build();
			entityManager.add(game);

			// simulate a step to put everything on their places
			entityManager.update(1);
			physicsWorld.step(1, 1, 1);
			entityManager.update(1);
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

	@Override
	public void init(Entity e) {

	}

	@Override
	public void dispose(Entity e) {
		diposeJoints(e);
		disposeBody(e);
	}

	private void disposeBody(Entity e) {
		Physics physics = ComponentWrapper.getPhysics(e);
		if (physics == null)
			return;

		Body body = physics.getBody();
		body.setUserData(null);

		com.gemserk.commons.gdx.box2d.Contact contact = physics.getContact();

		// removes contact from the other entity
		for (int i = 0; i < contact.getContactCount(); i++) {
			if (!contact.isInContact(i))
				continue;

			Body otherBody = contact.getBody(i);
			if (otherBody == null)
				continue;

			Entity otherEntity = (Entity) otherBody.getUserData();
			if (otherEntity == null)
				continue;

			Physics otherPhysics = ComponentWrapper.getPhysics(otherEntity);
			otherPhysics.getContact().removeContact(body);
		}

		physicsWorld.destroyBody(body);
		Gdx.app.log("SuperFlyingThing", "removing body from physics world");
	}

	private void diposeJoints(Entity e) {
		AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);
		if (entityAttachment == null)
			return;
		if (entityAttachment.getJoint() == null)
			return;
		physicsWorld.destroyJoint(entityAttachment.getJoint());
		Gdx.app.log("SuperFlyingThing", "removing joints from physics world");
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		worldCamera.apply(spriteBatch);
		renderEntities(spriteBatch);
	}

	void renderEntities(SpriteBatch spriteBatch) {
		spriteBatch.begin();
		for (int i = 0; i < entityManager.entitiesCount(); i++)
			renderEntitySprite(entityManager.get(i));
		spriteBatch.end();

		// box2dCustomDebugRenderer.render();

		for (int i = 0; i < entityManager.entitiesCount(); i++) {
			Entity e = entityManager.get(i);
			renderMovementDebug(e);
			renderAttachmentDebug(e);
			renderEntityWithShape(e);
		}

		guiCamera.apply(spriteBatch);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	private void renderEntitySprite(Entity e) {
		Spatial spatial = ComponentWrapper.getSpatial(e);
		if (spatial == null)
			return;
		SpriteComponent spriteComponent = ComponentWrapper.getSprite(e);
		if (spriteComponent == null)
			return;
		Sprite sprite = spriteComponent.getSprite();
		sprite.setSize(spatial.getWidth(), spatial.getHeight());
		sprite.setColor(spriteComponent.getColor());
		Vector2 position = spatial.getPosition();
		SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, spatial.getAngle());
	}

	private void renderEntityWithShape(Entity e) {
		ShapeComponent shapeComponent = e.getComponent(ShapeComponent.class);
		if (shapeComponent != null) {
			Spatial spatial = ComponentWrapper.getSpatial(e);
			if (spatial == null)
				return;
			ImmediateModeRendererUtils.drawPolygon(shapeComponent.getVertices(), spatial.getX(), spatial.getY(), spatial.getAngle(), shapeComponent.color);
		}
	}

	private void renderAttachmentDebug(Entity e) {
		Spatial spatial = ComponentWrapper.getSpatial(e);
		if (spatial == null)
			return;
		AttachmentComponent attachmentComponent = e.getComponent(AttachmentComponent.class);
		if (attachmentComponent == null)
			return;
		Vector2 position = spatial.getPosition();
		ImmediateModeRendererUtils.drawSolidCircle(position, spatial.getWidth() * 0.5f, Color.BLUE);
	}

	private void renderMovementDebug(Entity e) {
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

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		container.update();

		if (Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK)) {
			game.transition(game.getPauseScreen(), 500, 500, false);
		}

		if (!resetPressed)
			resetPressed = Gdx.input.isKeyPressed(Keys.R) || Gdx.input.isKeyPressed(Keys.MENU);

		if (resetPressed && !Gdx.input.isKeyPressed(Keys.R) && !Gdx.input.isKeyPressed(Keys.MENU))
			done = true;

		if (done) {

			if (GameData.gameMode == GameData.ChallengeGameMode) {
				game.transition(game.getLevelSelectionScreen(), 500, 500);
				Analytics.traker.trackPageView("/challengeMode/finishLevel", "/challengeMode/finishLevel", null);
			} else {
				// restart game state...
				dispose();
				init();
			}
		}

		physicsWorld.step(delta * 0.001f, 3, 3);
		entityManager.update(delta);
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