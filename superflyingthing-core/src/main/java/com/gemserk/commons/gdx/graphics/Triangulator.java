package com.gemserk.commons.gdx.graphics;

public interface Triangulator {

	/**
	 * Upate the triangles
	 */
	boolean triangulate();

	/**
	 * Add a point to the polygon
	 */
	void addPolyPoint(float x, float y);

	/**
	 * @see org.newdawn.slick.geom.Triangulator#getTriangleCount()
	 */
	int getTriangleCount();

	/**
	 * @see org.newdawn.slick.geom.Triangulator#getTrianglePoint(int, int)
	 */
	float[] getTrianglePoint(int tri, int i);

}