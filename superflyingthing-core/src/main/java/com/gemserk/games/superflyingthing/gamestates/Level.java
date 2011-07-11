package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Level {

	public static class Obstacle {

		public float x, y;
		public float angle;
		public Vector2[] vertices;
		
		public Obstacle() {
			
		}
		
		public Obstacle(Vector2[] vertices) {
			this.vertices = vertices;
		}

	}
	
	public static class Item {
		
		public float x,y;
		
	}

	public float w, h;
	
	ArrayList<Obstacle> obstacles = new ArrayList<Level.Obstacle>();
	
	ArrayList<Item> items = new ArrayList<Level.Item>();
	
	String name;
	
}
