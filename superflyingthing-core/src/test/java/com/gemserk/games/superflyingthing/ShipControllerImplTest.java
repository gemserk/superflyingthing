package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import com.gemserk.animation4j.interpolator.FloatInterpolator;

public class ShipControllerImplTest {

	public float value(float center, float x, float minValue, float distanceToMax) {
		float distance = Math.abs(x - center);
		if (x > center)
			return FloatInterpolator.interpolate(minValue, 1f, distance / distanceToMax);
		else if (x < center)
			return FloatInterpolator.interpolate(-minValue, -1f, distance / distanceToMax);
		return 0;
	}

	@Test
	public void test() {
		assertThat(value(400f, 0f, 0f, 400f), IsEqual.equalTo(-1f));
		assertThat(value(400f, 800f, 0f, 400f), IsEqual.equalTo(1f));
		assertThat(value(400f, 400f, 0f, 400f), IsEqual.equalTo(0f));

		assertThat(value(400f, 600f, 0f, 400f), IsEqual.equalTo(0.5f));
		assertThat(value(400f, 200f, 0f, 400f), IsEqual.equalTo(-0.5f));
		
		assertThat(value(400f, 600f, 0.5f, 400f), IsEqual.equalTo(0.75f));
		assertThat(value(400f, 200f, 0.5f, 400f), IsEqual.equalTo(-0.75f));
		
		assertThat(value(400f, 500f, 0.5f, 50f), IsEqual.equalTo(1f));
		assertThat(value(400f, 425f, 0.5f, 50f), IsEqual.equalTo(0.75f));
	}

}
