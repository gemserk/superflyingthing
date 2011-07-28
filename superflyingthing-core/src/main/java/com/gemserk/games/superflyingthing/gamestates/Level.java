package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Level {

	public static class Obstacle {

		public float x, y;
		public float angle;
		public Vector2[] vertices;
		
		public BodyType bodyType = BodyType.StaticBody;
		
		public Vector2[] path;
		
		public Obstacle() {
			
		}
		
		public Obstacle(Vector2[] vertices) {
			this.vertices = vertices;
		}

	}
	
	public static class Item {
		
		public float x,y;
		
	}
	
	public static class StartPlanet {
		
		float x;
		float y;

		public StartPlanet(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	public static class DestinationPlanet {
		
		float x;
		float y;

		public DestinationPlanet(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	public static class LaserTurret {
		
		float x;
		float y;
		float angle;

		public LaserTurret(float x, float y, float angle) {
			this.x = x;
			this.y = y;
			this.angle = angle;
		}
		
	}
	
	public static class Portal {
		
		float x;
		float y;
		String id;
		String targetPortalId;

		public Portal(float x, float y, String id, String targetPortalId) {
			this.x = x;
			this.y = y;
			this.id = id;
			this.targetPortalId = targetPortalId;
		}
		
	}

	public float w, h;
	
	public ArrayList<Obstacle> obstacles = new ArrayList<Level.Obstacle>();
	
	public ArrayList<Item> items = new ArrayList<Level.Item>();
	
	public ArrayList<LaserTurret> laserTurrets = new ArrayList<Level.LaserTurret>();
	
	public ArrayList<Portal> portals = new ArrayList<Level.Portal>();
	
	public StartPlanet startPlanet;
	
	public DestinationPlanet destinationPlanet;
	
	public String name;
	
}
