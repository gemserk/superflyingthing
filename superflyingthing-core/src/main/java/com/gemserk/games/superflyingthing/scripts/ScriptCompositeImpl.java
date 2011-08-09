package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.gemserk.commons.artemis.Script;
import com.gemserk.commons.artemis.ScriptJavaImpl;

public class ScriptCompositeImpl extends ScriptJavaImpl {

	private Bag<Script> scripts = new Bag<Script>();

	public ScriptCompositeImpl(Script requiredScript, Script... scripts) {
		this.scripts.add(requiredScript);
		if (scripts != null)
			for (int i = 0; i < scripts.length; i++)
				this.scripts.add(scripts[i]);
	}

	@Override
	public void update(World world, Entity e) {
		for (int i = 0; i < scripts.size(); i++)
			scripts.get(i).update(world, e);
	}

}
