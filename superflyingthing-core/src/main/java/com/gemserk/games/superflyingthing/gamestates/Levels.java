package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.math.Vector2;

public class Levels {
	
	private static Level level1 = new Level() {{
		name = "Learning to fly";
		w = 50f;
		h = 10f;
		obstacles = new Obstacle[]{
			new Obstacle() {{
				x = w * 0.5f;
				y = 1.4f;
				vertices = new Vector2[] { new Vector2(15f, -1.5f), new Vector2(10f, 1.5f), new Vector2(-10f, 1.5f), new Vector2(-15f, -1.5f) };
			}},
			new Obstacle() {{
				x = w * 0.5f;
				y = h - 1.4f;
				angle = 180f;
				vertices = new Vector2[] { new Vector2(15f, -1.5f), new Vector2(10f, 1.5f), new Vector2(-10f, 1.5f), new Vector2(-15f, -1.5f) };
			}},
		}; 
		items = new Level.Item[] {};
	}};
	
	private static Level level2 = new Level() {{
		name = "I'm a flying machine!";
		w = 50f;
		h = 10f;
		obstacles = new Obstacle[]{
			new Obstacle() {{
				x = w * 0.5f;
				y = 0f;
				vertices = new Vector2[] { new Vector2(15f, -1.5f), new Vector2(10f, 1.5f), new Vector2(-10f, 1.5f), new Vector2(-15f, -1.5f) };
			}},
			new Obstacle() {{
				x = w * 0.5f;
				y = h;
				angle = 180f;
				vertices = new Vector2[] { new Vector2(15f, -1.5f), new Vector2(10f, 1.5f), new Vector2(-10f, 1.5f), new Vector2(-15f, -1.5f) };
			}},
			new Obstacle() {{
				x = w * 0.5f;
				y = h * 0.5f;
				angle = 0f;
				vertices = new Vector2[] { new Vector2(5f, -0.5f), new Vector2(5f, 0.5f), new Vector2(-5f, 0.5f), new Vector2(-5f, -0.5f) };
			}},
		}; 
		items = new Level.Item[] {};
	}};
	
	public static Level level1() {
		return level1;
	}
	
	public static Level level2() {
		return level2;
	}

}
