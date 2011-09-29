package com.gemserk.games.superflyingthing.components;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Component;

public class PropertiesComponent extends Component {
	
	public final Map<String, Object> properties ;
	
	public PropertiesComponent(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public PropertiesComponent() {
		this(new HashMap<String, Object>());
	}
	
}
