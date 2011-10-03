package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.components.Components.ReplayListComponent;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.entities.Groups;
import com.gemserk.games.superflyingthing.scripts.ReplayRecorderScript;

public class ReplayRecorderTemplate extends EntityTemplateImpl {

	@Override
	public void apply(Entity entity) {
		entity.addComponent(new TagComponent(Groups.ReplayRecorder));
		entity.addComponent(new ReplayListComponent(new ReplayList()));
		entity.addComponent(new ScriptComponent(new ReplayRecorderScript()));
	}

}
