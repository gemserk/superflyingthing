package com.gemserk.games.superflyingthing.preferences;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class PlayerProfileJsonSerializer {

	Gson gson = new Gson();

	public PlayerProfileJsonSerializer() {
		gson = new GsonBuilder().registerTypeAdapter(Object.class, new JsonDeserializer<Object>() {
			@Override
			public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (value.isBoolean()) {
					return value.getAsBoolean();
				} else if (value.isNumber()) {
					return value.getAsNumber();
				} else {
					return value.getAsString();
				}
			}
		}).create();
	}

	public PlayerProfile parse(String profileJson) {
		return gson.fromJson(profileJson, PlayerProfile.class);
	}

	public Set<PlayerProfile> parseList(String profileListJson) {
		if ("".equals(profileListJson) || profileListJson == null)
			return new HashSet<PlayerProfile>();
		Type collectionType = new TypeToken<Set<PlayerProfile>>() {}.getType();
		return gson.fromJson(profileListJson, collectionType);
	}

	public String serialize(PlayerProfile profile) {
		return gson.toJson(profile);
	}
	
	public String serialize(Set<PlayerProfile> profiles) {
		return gson.toJson(profiles);
	}

}