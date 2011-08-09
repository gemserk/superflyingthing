package com.gemserk.games.superflyingthing.scripts;

import com.gemserk.animation4j.interpolator.FloatInterpolator;

public class ControllerUtils {

	public static float calculateDirectionWithVariableSensibility(float currentValue, float direction, float minValue, float delta, float speed) {
		if (direction == 0)
			return 0f;
		if (currentValue <= 0 && direction > 0)
			return minValue;
		if (currentValue >= 0 && direction < 0)
			return -minValue;
		float newValue = currentValue + direction * delta * speed;
		if (newValue > 1f)
			return 1f;
		if (newValue < -1f)
			return -1f;
		return newValue;
	}

	public static float value(float center, int x, float minValue, float distanceToMin, float distanceToMax) {
		float distance = Math.abs(x - center);

		if (distance < distanceToMin) {
			if (x > center)
				return minValue;
			else if (x < center)
				return -minValue;
		}

		if (x > center)
			return FloatInterpolator.interpolate(minValue, 1f, (distance - distanceToMin) / (distanceToMax - distanceToMin));
		else if (x < center)
			return FloatInterpolator.interpolate(-minValue, -1f, (distance - distanceToMin) / (distanceToMax - distanceToMin));
		return 0;
	}

}