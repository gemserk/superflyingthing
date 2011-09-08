package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;


public class ShapeTest {
	
	@Test
	public void testCalculateBounds() {
		
		Vector2[] vertices = new Vector2[] {
				new Vector2(-10f, -15f), 
				new Vector2(10f, -15f),
				new Vector2(10f, 15f),
				new Vector2(-10f, 15f),
				};
		
		Shape shape = new Shape(vertices);
		
		assertThat(shape.bounds.x, IsEqual.equalTo(-10f));
		assertThat(shape.bounds.y, IsEqual.equalTo(-15f));
		assertThat(shape.bounds.width, IsEqual.equalTo(20f));
		assertThat(shape.bounds.height, IsEqual.equalTo(30f));
		
	}

}
