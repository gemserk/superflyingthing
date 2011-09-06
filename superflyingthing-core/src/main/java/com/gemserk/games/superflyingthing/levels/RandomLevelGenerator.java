package com.gemserk.games.superflyingthing.levels;

import java.util.ArrayList;
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

		float minx = 10000f;
		float maxx = -10000f;

		float miny = 10000f;
		float maxy = -10000f;

		ArrayList<Shape> obstacles = new RandomLevelTileBasedGenerator().generateLevel();
		for (int i = 0; i < obstacles.size(); i++) {
			Shape obstacleShape = obstacles.get(i);

			obstacleShape.x += 7.5f;
			obstacleShape.y += 7.5f;

			Obstacle obstacle = new Obstacle(obstacleShape.vertices, obstacleShape.x, obstacleShape.y, 0f);
			obstacle.id = "obstacle-" + UUID.randomUUID();
			level.obstacles.add(obstacle);

			if (obstacleShape.bounds.x + obstacleShape.x < minx)
				minx = obstacleShape.bounds.x + obstacleShape.x;

			if (obstacleShape.bounds.x + obstacleShape.bounds.width + obstacleShape.x > maxx)
				maxx = obstacleShape.bounds.x + obstacleShape.bounds.width + obstacleShape.x;

			if (obstacleShape.bounds.y < miny)
				miny = obstacleShape.bounds.y;

			if (obstacleShape.bounds.y + obstacleShape.bounds.height > maxy)
				maxy = obstacleShape.bounds.y + obstacleShape.bounds.height;

		}

		level.w = maxx - minx;
		level.h = maxy - miny;

		System.out.println("level size: " + level.w + "x" + level.h);

		// level.w = MathUtils.random(40f, 200f);
		// level.h = MathUtils.random(8f, 16f);

		Shape startTile = obstacles.get(0);
		Shape endTile = obstacles.get(obstacles.size() - 1);

		level.startPlanet = new StartPlanet(startTile.x, startTile.y);
		level.destinationPlanets.add(new DestinationPlanet(endTile.x, endTile.y));

		level.startPlanet.color = getRandomColor();

		float obstacleX = 12f;

		// while (obstacleX < level.w - 17f) {
		// Obstacle obstacle1 = new Obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, level.h), MathUtils.random(0f, 359f));
		// Obstacle obstacle2 = new Obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, level.h), MathUtils.random(0f, 359f));
		//
		// obstacle1.id = "obstacle-" + UUID.randomUUID();
		// obstacle2.id = "obstacle-" + UUID.randomUUID();
		//
		// level.obstacles.add(obstacle1);
		// level.obstacles.add(obstacle2);
		// obstacleX += 8f;
		// }

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