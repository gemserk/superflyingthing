package com.gemserk.games.superflyingthing.levels;

import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.Item;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.StartPlanet;

public class RandomLevelGenerator {

	private final static Color[] planetColors = new Color[] { Color.BLUE, Color.RED, Colors.darkGreen, Colors.darkMagenta, Colors.darkYellow };

	private final Shape[] shapes = new Shape[] { //
	new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
			new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
			new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
	};

	private Shape getRandomShape() {
		return shapes[MathUtils.random(shapes.length - 1)];
	}

	private Color getRandomColor() {
		return planetColors[MathUtils.random(planetColors.length - 1)];
	}

	public Level generateRandomLevel() {

		Level level = new Level();

		level.w = MathUtils.random(30f, 250f);
		level.h = MathUtils.random(5f, 15f);

		level.startPlanet = new StartPlanet(5f, level.h * 0.5f);
		level.destinationPlanets.add(new DestinationPlanet(level.w - 5f, level.h * 0.5f));

		level.startPlanet.color = getRandomColor();

		float obstacleX = 12f;

		while (obstacleX < level.w - 17f) {
			Obstacle obstacle1 = new Obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, level.h), MathUtils.random(0f, 359f));
			Obstacle obstacle2 = new Obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, level.h), MathUtils.random(0f, 359f));

			obstacle1.id = "obstacle-" + UUID.randomUUID();
			obstacle2.id = "obstacle-" + UUID.randomUUID();

			level.obstacles.add(obstacle1);
			level.obstacles.add(obstacle2);
			obstacleX += 8f;
		}

		for (int i = 0; i < 10; i++) {
			Item item = new Item();
			item.x = MathUtils.random(10f, level.w - 10f);
			item.y = MathUtils.random(1f, level.h - 1f);
			level.items.add(item);
		}

		Levels.generateRandomClouds(level, 6);

		return level;
	}

}