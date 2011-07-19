package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;

public class ShipControllerImpl implements ShipController {

	private final Libgdx2dCamera libgdx2dCamera;

	private final Vector2 position;

	public ShipControllerImpl(Libgdx2dCamera libgdx2dCamera) {
		this.libgdx2dCamera = libgdx2dCamera;
		this.position = new Vector2(0f, 0f);
	}

	/* (non-Javadoc)
	 * @see com.gemserk.games.superflyingthing.ShipController#isDown()
	 */
	@Override
	public boolean isDown() {
		return Gdx.input.isTouched();
	}

	/* (non-Javadoc)
	 * @see com.gemserk.games.superflyingthing.ShipController#getPosition()
	 */
	@Override
	public Vector2 getPosition() {
		position.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
		libgdx2dCamera.unproject(position);
		return position;
	}

	/* (non-Javadoc)
	 * @see com.gemserk.games.superflyingthing.ShipController#getMovementDirection()
	 */
	@Override
	public float getMovementDirection() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return getMovementDirectionAndroid();
		else
			return getMovementDirectionPC();
	}

	private float getMovementDirectionPC() {
		float movementDirection = 0f;

		if (Gdx.input.isKeyPressed(Keys.LEFT))
			movementDirection += 1f;

		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			movementDirection -= 1f;

		return movementDirection;
	}

	private float getMovementDirectionAndroid() {

		for (int i = 0; i < 5; i++) {
			if (!Gdx.input.isTouched(i))
				continue;

			float x = Gdx.input.getX(i);
			float d = Gdx.graphics.getWidth() * 0.5f - x;
			d /= Gdx.graphics.getWidth() * 0.5f;

			if (d > 0 && d < 0.2f)
				d = 0.2f;

			if (d < 0 && d > -0.2f)
				d = -0.2f;

			if (d > 1f)
				d = 1f;

			if (d < -1f)
				d = -1f;

			return d;
		}

		return 0f;
	}

	/* (non-Javadoc)
	 * @see com.gemserk.games.superflyingthing.ShipController#update(int)
	 */
	@Override
	public void update(int delta) {

	}

}