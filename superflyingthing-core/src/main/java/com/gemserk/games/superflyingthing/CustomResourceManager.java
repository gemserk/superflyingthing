package com.gemserk.games.superflyingthing;

import java.util.ArrayList;

import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.resources.resourceloaders.ResourceLoader;

public class CustomResourceManager<K> extends ResourceManagerImpl<K> {
	
	private ArrayList<K> registeredResources = new ArrayList<K>();
	
	public ArrayList<K> getRegisteredResources() {
		return registeredResources;
	}

	public void add(K id, ResourceLoader resourceLoader) {
		super.add(id, resourceLoader);
		registeredResources.add(id);
	}
	
}