package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.math.Vector2;

public class Level {

	public class Obstacle {

		public float x, y;
		public float angle;
		public Vector2[] vertices;

	}
	
	public class Item {
		
		public float x,y;
		
	}

	public float w, h;
	
	Obstacle[] obstacles;
	
	Item[] items;
	
	String name;

}
