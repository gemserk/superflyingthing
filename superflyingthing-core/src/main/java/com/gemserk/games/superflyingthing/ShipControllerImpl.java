package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;

public class ShipControllerImpl implements ShipController {

	private final Libgdx2dCamera libgdx2dCamera;

	private final Vector2 position;

	public ShipControllerImpl(Libgdx2dCamera libgdx2dCamera) {
		this.libgdx2dCamera = libgdx2dCamera;
		this.position = new Vector2(0f, 0f);
	}

	@Override
	public boolean isDown() {
		return Gdx.input.isTouched();
	}

	@Override
	public Vector2 getPosition() {
		position.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
		libgdx2dCamera.unproject(position);
		return position;
	}

	@Override
	public float getMovementDirection() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return getMovementDirectionAndroid();
		else
			return getMovementDirectionPC();
	}

	private float getMovementDirectionPC() {
		return movementDirection;
	}

	private float getMovementDirectionAndroid() {
		return movementDirection;
		// for (int i = 0; i < 5; i++) {
		// if (!Gdx.input.isTouched(i))
		// continue;
		//
		// return -value(Gdx.graphics.getWidth() * 0.5f, Gdx.input.getX(i), 0.2f, Gdx.graphics.getWidth() * 0.375f);
		// }
		// return 0f;
	}

	public float value(float center, int x, float minValue, float distanceToMax) {
		float distance = Math.abs(x - center);
		if (x > center)
			return FloatInterpolator.interpolate(minValue, 1f, distance / distanceToMax);
		else if (x < center)
			return FloatInterpolator.interpolate(-minValue, -1f, distance / distanceToMax);
		return 0;
	}

	float movementDirection = 0f;

	public float calculateDirectionWithVariableSensibility(float currentValue, float direction, float minValue, float delta, float speed) {
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

	@Override
	public void update(int delta) {

		if (Gdx.app.getType() != ApplicationType.Android) {
			float direction = 0f;

			if (Gdx.input.isKeyPressed(Keys.LEFT))
				direction = 1f;
			else if (Gdx.input.isKeyPressed(Keys.RIGHT))
				direction = -1f;

			movementDirection = calculateDirectionWithVariableSensibility(movementDirection, direction, 0.3f, 0.001f * delta, 2f);
		} else {

			for (int i = 0; i < 5; i++) {
				if (!Gdx.input.isTouched(i))
					continue;

				float direction = 0f;

				if (Gdx.input.getX(i) < Gdx.graphics.getWidth() * 0.5f)
					direction = 1f;
				else if (Gdx.input.getX(i) > Gdx.graphics.getWidth() * 0.5f)
					direction = -1f;

				movementDirection = calculateDirectionWithVariableSensibility(movementDirection, direction, 0.3f, 0.001f * delta, 2f);

				return;
			}

			movementDirection = 0f;

		}
	}

}