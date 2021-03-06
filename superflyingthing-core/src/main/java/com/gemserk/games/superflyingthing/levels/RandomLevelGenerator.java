package com.gemserk.games.superflyingthing.levels;

import java.util.ArrayList;
import java.util.UUID;

import org.w3c.dom.Document;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.StartPlanet;

public class RandomLevelGenerator {

	private final static Color[] planetColors = new Color[] { Color.BLUE, Color.RED, Colors.darkGreen, Colors.darkMagenta, Colors.darkYellow };

	private final Shape[] shapes = new Shape[] { //
	new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
			new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
			new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
	};

	/**
	 * The SVG with the tiles specification for the random tiles generation.
	 */
	private final Document document;

	private Shape getRandomShape() {
		return shapes[MathUtils.random(shapes.length - 1)];
	}

	private Color getRandomColor() {
		return planetColors[MathUtils.random(planetColors.length - 1)];
	}

	public RandomLevelGenerator(Document document) {
		this.document = document;
	}

	public Level generateRandomLevel() {

		Level level = new Level();

		level.zoom = 64f;

		float minx = 10000f;
		float maxx = -10000f;

		float miny = 10000f;
		float maxy = -10000f;

		float startX = 5f;
		float startY = 5f;

		int maxLevelDepth = MathUtils.random(7, 7);

		ArrayList<Shape> obstacles = new RandomLevelTileBasedGenerator().generateLevel(document, maxLevelDepth);
		for (int i = 0; i < obstacles.size(); i++) {
			Shape obstacleShape = obstacles.get(i);

			obstacleShape.x += startX;
			obstacleShape.y += startY;

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

		Shape startTile = obstacles.get(0);
		Shape endTile = obstacles.get(obstacles.size() - 1);

		level.startPlanet = new StartPlanet(startTile.x - 1.5f, startTile.y);
		level.destinationPlanets.add(new DestinationPlanet(endTile.x + 1.5f, endTile.y));

		level.startPlanet.color = getRandomColor();

		// int maxItems = 10;
		//
		// for (int i = 0; i < maxItems; i++) {
		// Item item = new Item();
		// item.x = MathUtils.random(10f, level.w - 10f);
		// item.y = MathUtils.random(1f, level.h - 1f);
		// level.items.add(item);
		// }

		Levels.generateRandomClouds(level, 6);

		return level;
	}

}