package com.gemserk.games.superflyingthing.levels;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.componentsengine.utils.Parameters;

public class Level {

	public static class Obstacle {
		
		public String id;

		public float x, y;
		public float angle;
		public Vector2[] vertices;

		public BodyType bodyType = BodyType.StaticBody;

		public Vector2[] path;

		public int startPoint = 0;

		public Obstacle() {

		}

		public Obstacle(Vector2[] vertices) {
			this.vertices = vertices;
		}
		
		public Obstacle(Vector2[] vertices, float x, float y, float angle) {
			this.vertices = vertices;
			this.x = x;
			this.y = y;
			this.angle = angle;
		}

	}

	public static class Item {

		public float x, y;
		public String id;

	}

	public static class StartPlanet {

		public float x, y;
		public Color color;

		public StartPlanet(float x, float y) {
			this.x = x;
			this.y = y;
		}

	}

	public static class DestinationPlanet {

		public float x, y;

		public DestinationPlanet(float x, float y) {
			this.x = x;
			this.y = y;
		}

	}

	public static class LaserTurret {

		public float x, y;
		public float angle;
		public int fireRate;
		public int bulletDuration;
		public int currentReloadTime;

		public LaserTurret(float x, float y, float angle, int fireRate, int bulletDuration, int currentReloadTime) {
			this.x = x;
			this.y = y;
			this.angle = angle;
			this.fireRate = fireRate;
			this.bulletDuration = bulletDuration;
			this.currentReloadTime = currentReloadTime;
		}

	}

	public static class Portal {

		public float x, y;
		public float w, h;
		public float angle;
		
		public String id;
		public String targetPortalId;

		public Portal(float x, float y, float w, float h, float angle, String id, String targetPortalId) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.angle = angle;
			this.id = id;
			this.targetPortalId = targetPortalId;
		}

	}

	public float w, h;
	public float zoom = 64f;

	public ArrayList<Obstacle> obstacles = new ArrayList<Level.Obstacle>();

	public ArrayList<Item> items = new ArrayList<Level.Item>();

	public ArrayList<LaserTurret> laserTurrets = new ArrayList<Level.LaserTurret>();

	public ArrayList<Portal> portals = new ArrayList<Level.Portal>();

	public StartPlanet startPlanet;

	public ArrayList<DestinationPlanet> destinationPlanets = new ArrayList<DestinationPlanet>();
	
	public ArrayList<Parameters> fogClouds = new ArrayList<Parameters>();
	
	public String name;

}
