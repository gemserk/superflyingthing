package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.gdx.box2d.Contact;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.HealthComponent;

public class ObstacleScript extends ScriptJavaImpl {

	@Override
	public void update(com.artemis.World world, Entity e) {
		Physics physics = ComponentWrapper.getPhysics(e);
		if (physics == null)
			return;
		Contact contact = physics.getContact();
		for (int i = 0; i < contact.getContactCount(); i++) {
			if (!contact.isInContact(i))
				continue;
			Entity otherEntity = (Entity) contact.getUserData(i);
			if (otherEntity == null)
				continue;
			
			HealthComponent healthComponent = otherEntity.getComponent(HealthComponent.class);
			if (healthComponent == null)
				return;
			healthComponent.getHealth().setCurrent(0f);
		}
	}
	
}
