package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.LabelComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;

public class LevelLoader {

	private final Parameters parameters = new ParametersWrapper();
	private final EntityTemplates entityTemplates;
	private final World physicsWorld;
	private final EntityFactory entityFactory;
	private final Libgdx2dCamera worldCamera;

	// used if we want to avoid item removal check
	private final boolean shouldRemoveCollidingItems;

	// used to check if items are inside obstacles to remove them from the level.
	private boolean insideObstacle;

	public LevelLoader(EntityTemplates entityTemplates, EntityFactory entityFactory, World physicsWorld, Libgdx2dCamera worldCamera, boolean shouldRemoveCollidingItems) {
		this.entityTemplates = entityTemplates;
		this.entityFactory = entityFactory;
		this.physicsWorld = physicsWorld;
		this.worldCamera = worldCamera;
		this.shouldRemoveCollidingItems = shouldRemoveCollidingItems;
	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		createWorldLimits(worldWidth, worldHeight, 2f);
	}

	private void createWorldLimits(float worldWidth, float worldHeight, float offset) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;
		float limitWidth = 0.1f;
		entityFactory.instantiate(entityTemplates.boxObstacleTemplate, new ParametersWrapper() //
				.put("id", "worldLimit1") //
				.put("x", centerX) //
				.put("y", (-offset)) //
				.put("angle", 0f) //
				.put("w", (worldWidth + 1)) //
				.put("h", limitWidth) //
				);
		entityFactory.instantiate(entityTemplates.boxObstacleTemplate, new ParametersWrapper() //
				.put("id", "worldLimit2") //
				.put("x", centerX) //
				.put("y", (worldHeight + offset)) //
				.put("angle", 0f) //
				.put("w", (worldWidth + 1)) //
				.put("h", limitWidth) //
				);
		entityFactory.instantiate(entityTemplates.boxObstacleTemplate, new ParametersWrapper() //
				.put("id", "worldLimit3") //
				.put("x", (-offset)) //
				.put("y", centerY) //
				.put("angle", 0f) //
				.put("w", limitWidth) //
				.put("h", (worldHeight + 1)) //
				);
		entityFactory.instantiate(entityTemplates.boxObstacleTemplate, new ParametersWrapper() //
				.put("id", "worldLimit4") //
				.put("x", (worldWidth + offset)) //
				.put("y", centerY) //
				.put("angle", 0f) //
				.put("w", limitWidth) //
				.put("h", (worldHeight + 1)) //
				);
	}

	public void loadLevel(Level level) {
		float worldWidth = level.w;
		float worldHeight = level.h;

		float cameraZoom = Gdx.graphics.getWidth() * level.zoom / 800f;

		final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

		final ShipController controller = new ShipController();

		parameters.clear();

		Entity startPlanet = entityFactory.instantiate(entityTemplates.startPlanetTemplate, new ParametersWrapper() //
				.put("x", level.startPlanet.x) //
				.put("y", level.startPlanet.y) //
				.put("radius", 1f) //
				.put("controller", controller) //
				);

		entityFactory.instantiate(entityTemplates.planetFillAnimationTemplate, parameters //
				.put("owner", startPlanet) //
				.put("color", level.startPlanet.color) //
				);

		for (int i = 0; i < level.destinationPlanets.size(); i++) {
			DestinationPlanet destinationPlanet = level.destinationPlanets.get(i);
			entityFactory.instantiate(entityTemplates.destinationPlanetTemplate, new ParametersWrapper() //
					.put("x", destinationPlanet.x) //
					.put("y", destinationPlanet.y) //
					.put("radius", 1f) //
					);
		}

		parameters.clear();
		entityFactory.instantiate(entityTemplates.cameraTemplate, parameters //
				.put("camera", camera) //
				.put("libgdxCamera", worldCamera) //
				.put("spatial", new SpatialImpl(level.startPlanet.x, level.startPlanet.y, 1f, 1f, 0f)) //
				);

		for (int i = 0; i < level.obstacles.size(); i++) {
			Obstacle o = level.obstacles.get(i);
			if (o.bodyType == BodyType.StaticBody) {
				entityFactory.instantiate(entityTemplates.staticObstacleTemplate, new ParametersWrapper() //
						.put("id", o.id) //
						.put("x", o.x) //
						.put("y", o.y) //
						.put("angle", (o.angle * MathUtils.degreesToRadians)) //
						.put("vertices", o.vertices) //
						);
			} else {
				entityFactory.instantiate(entityTemplates.movingObstacleTemplate, new ParametersWrapper() //
						.put("id", o.id) //
						.put("x", o.x) //
						.put("y", o.y) //
						.put("angle", (o.angle * MathUtils.degreesToRadians)) //
						.put("vertices", o.vertices) //
						.put("points", o.path) //
						.put("startPoint", o.startPoint) //
						);
			}
		}

		int j = 0;
		while (j < level.items.size()) {
			final Level.Item item = level.items.get(j);

			float x = item.x;
			float y = item.y;
			float w = 0.3f;
			float h = 0.3f;

			if (shouldRemoveCollidingItems) {
				insideObstacle = false;

				physicsWorld.QueryAABB(new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {

						Entity entity = (Entity) fixture.getBody().getUserData();
						if (entity != null) {
							LabelComponent labelComponent = entity.getComponent(LabelComponent.class);
							if (labelComponent != null) {
								Gdx.app.log("SuperFlyingThing", "Removing item " + item.id + " because it is colliding with " + labelComponent.label);
							}
						}

						insideObstacle = true;
						return false;
					}
				}, x - w * 0.5f, y - h * 0.5f, x + w * 0.5f, y + h * 0.5f);

				if (insideObstacle) {
					// Gdx.app.log("SuperFlyingThing", "Removing item " + item.id + " because is colliding with obstacle");
					level.items.remove(j);
					continue;
				}
			}

			entityFactory.instantiate(entityTemplates.starTemplate, new ParametersWrapper() //
					.put("x", item.x) //
					.put("y", item.y) //
					.put("id", item.id) //
					);

			j++;
		}

		for (int i = 0; i < level.laserTurrets.size(); i++) {
			LaserTurret laserTurret = level.laserTurrets.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.laserGunTemplate, parameters //
					.put("position", new Vector2(laserTurret.x, laserTurret.y)) //
					.put("angle", laserTurret.angle) //
					.put("fireRate", laserTurret.fireRate) //
					.put("bulletDuration", laserTurret.bulletDuration) //
					.put("currentReloadTime", laserTurret.currentReloadTime) //
					);
		}

		for (int i = 0; i < level.portals.size(); i++) {
			Portal portal = level.portals.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.portalTemplate, parameters //
					.put("id", portal.id) //
					.put("targetPortalId", portal.targetPortalId) //
					.put("spatial", new SpatialImpl(portal.x, portal.y, portal.w, portal.h, portal.angle)) //
					);
		}

		for (int i = 0; i < level.fogClouds.size(); i++)
			entityFactory.instantiate(entityTemplates.staticSpriteTemplate, level.fogClouds.get(i));

		createWorldLimits(worldWidth, worldHeight);

		// default Player controller (with no script)

		entityFactory.instantiate(new EntityTemplateImpl() {
			@Override
			public void apply(Entity entity) {
				entity.addComponent(new TagComponent(Groups.PlayerController));
				entity.addComponent(new ControllerComponent(controller));
			}
		});

	}

}