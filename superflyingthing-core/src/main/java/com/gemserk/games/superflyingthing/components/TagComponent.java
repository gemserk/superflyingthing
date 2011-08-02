package com.gemserk.games.superflyingthing.components;

import com.artemis.Component;

public class TagComponent extends Component {
	
	private final String tag;
	
	public String getTag() {
		return tag;
	}
	
	public TagComponent(String tag) {
		this.tag = tag;
	}

}
