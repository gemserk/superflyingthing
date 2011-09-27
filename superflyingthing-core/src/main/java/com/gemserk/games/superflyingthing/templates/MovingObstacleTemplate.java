package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.FixtureDefBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.graphics.ShapeUtils;
import com.gemserk.commons.gdx.graphics.Triangulator;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.components.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.scripts.MovingObstacleScript;
import com.gemserk.resources.ResourceManager;

public class MovingObstacleTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;
	Mesh2dBuilder mesh2dBuilder;

	@Override
	public void apply(Entity entity) {

		Float x = parameters.get("x");
		Float y = parameters.get("y");
		Float angle = parameters.get("angle");
		Vector2[] vertices = parameters.get("vertices");
		Vector2[] points = parameters.get("points");
		Integer startPoint  = parameters.get("startPoint");
		
		Triangulator triangulator = ShapeUtils.triangulate(vertices);

		FixtureDef[] fixtureDefs = new FixtureDef[triangulator.getTriangleCount()];
		FixtureDefBuilder fixtureDefBuilder = new FixtureDefBuilder();

		for (int i = 0; i < triangulator.getTriangleCount(); i++) {
			Vector2[] v = new Vector2[3];
			for (int p = 0; p < 3; p++) {
				float[] pt = triangulator.getTrianglePoint(i, p);
				v[p] = new Vector2(pt[0], pt[1]);

				mesh2dBuilder.color(1f, 0f, 0f, 1f);
				mesh2dBuilder.vertex(pt[0], pt[1]);
			}
			fixtureDefs[i] = fixtureDefBuilder //
					.polygonShape(v) //
					.restitution(0f) //
					.categoryBits(CategoryBits.MovingObstacleCategoryBits) //
					.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.ObstacleCategoryBits & ~CategoryBits.MovingObstacleCategoryBits)) //
					.build();
		}

		Body body = bodyBuilder //
				.mass(500f) //
				.inertia(1f) //
				.fixtures(fixtureDefs) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.angle(angle) //
				.userData(entity) //
				.build();

		entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		entity.addComponent(new ShapeComponent(mesh2dBuilder.build()));
		entity.addComponent(new RenderableComponent(-59));
		entity.addComponent(new Components.DamageComponent(6000f));
		entity.addComponent(new ScriptComponent(new MovingObstacleScript(points, startPoint)));

	}

}