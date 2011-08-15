package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.box2d.Contact;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.PortalComponent;

public class PortalScript extends ScriptJavaImpl {

	// Could be converted to be a system.

	private static final Vector2 direction = new Vector2();
	
	private static final Class<PortalComponent> portalComponentClass = PortalComponent.class;

	@Override
	public void update(com.artemis.World world, Entity e) {
		PortalComponent portalComponent = e.getComponent(portalComponentClass);

		Physics physics = ComponentWrapper.getPhysics(e);
		Contact contact = physics.getContact();

		Spatial portalSpatial = ComponentWrapper.getSpatial(e);
		portalSpatial.setAngle(portalSpatial.getAngle() + 0.25f * world.getDelta());

		Entity portal = world.getTagManager().getEntity(portalComponent.getTargetPortalId());
		if (portal == null)
			return;

		for (int i = 0; i < contact.getContactCount(); i++) {
			if (!contact.isInContact(i))
				continue;
			Entity e2 = (Entity) contact.getUserData(i);
			if (e2 == null)
				continue;

			// this should not happen
			if (e2 == e)
				continue;

			Gdx.app.log("SuperFlyingThing", "Teleporting entity " + e2.getUniqueId() + " to " + portalComponent.getTargetPortalId());

			// start transition of e2 to target portal,
			Spatial targetPortalSpatial = ComponentWrapper.getSpatial(portal);
			Spatial entitySpatial = ComponentWrapper.getSpatial(e2);

			PortalComponent targetPortalComponent = portal.getComponent(portalComponentClass);
			direction.set(targetPortalSpatial.getWidth() * 0.5f, 0f).rotate(targetPortalComponent.getOutAngle());

			entitySpatial.setPosition(targetPortalSpatial.getX() + direction.x, //
					targetPortalSpatial.getY() + direction.y);
			entitySpatial.setAngle(targetPortalComponent.getOutAngle());
			
			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(e2);
			if (movementComponent != null) {
				movementComponent.getDirection().set(1f, 0f).rotate(targetPortalComponent.getOutAngle());
			}

			return;
		}
	}

}