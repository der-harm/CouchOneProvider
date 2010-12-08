package de.harm.android.couchone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

public class JsonProcessor {

	public static JSONObject valuesToJson(ContentValues values) {

		JSONObject json = new JSONObject();
		for (Entry<String, Object> e : values.valueSet()) {
			try {
				json.put(e.getKey(), e.getValue().toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return json;
	}

	/**
	 * returned list could be empty!!
	 * @param json
	 * @return list
	 */
	public static List<LinkedHashMap<String, String>> jsonToMap(JSONObject json) {
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();

		try {
			// get query results; result contains an array of
			// JSONObjects/documents
			if (json.has("rows")) {
				JSONArray array = json.getJSONArray("rows");

				for (int row = 0; row < array.length(); row++) {
					JSONObject obj = array.getJSONObject(row);
					result.add(putInMap(obj));
				}
			}
			// get uuid for an entity to be inserted soon
			else if (json.has("uuids")) {
				JSONArray array = json.getJSONArray("uuids");
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				map.put("uuid", array.getString(0));
				result.add(map);
			}
			// whatever, "ok":true
			else {
				result.add(putInMap(json));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private static LinkedHashMap<String, String> putInMap(JSONObject json)
			throws JSONException {

		JSONArray nameArray = json.names();
		JSONArray valArray = json.toJSONArray(nameArray);
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < valArray.length(); i++) {
			map.put(nameArray.getString(i), valArray.getString(i));
		}
		return map;
	}
}
