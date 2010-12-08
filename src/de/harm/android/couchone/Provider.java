package de.harm.android.couchone;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import de.harm.android.couchone.common.CouchConstants;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Pair;

public class Provider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	public boolean testConnection() {
		JSONObject response = RestClient.request(this.buildQuery(null, null),
				null, 1, null);
		List<LinkedHashMap<String, String>> map = JsonProcessor
				.jsonToMap(response);
		// TODO equals
		if (map.contains("db_name")
				&& map.get(0).get("db_name")
						.equalsIgnoreCase(CouchConstants.DB_Name)) {
			return true;
		} else {
			return false;
		}
		// return true;
	}

	@Override
	public String getType(Uri url) {
		if (CouchConstants.isCollectionUri(url)) {
			return (CouchConstants.COLLECTION_TYPE);
		}
		return (CouchConstants.SINGLE_TYPE);
	}

	/**
	 * TODO query per where args oder views
	 * 
	 * @param uri
	 *            get collection or item
	 * @param view
	 */
	@Override
	public Cursor query(Uri uri, String[] noProjection, String view,
			String[] selectionArgs, String noSortOrder) {

		MatrixCursor cursor = null;

		Pair<String, String>[] params = this.buildParams(selectionArgs);

		JSONObject response = RestClient.request(this.buildQuery(uri, view),
				params, 1, null);
		List<LinkedHashMap<String, String>> list = JsonProcessor
				.jsonToMap(response);
		// TODO: sind die spalten bei mehreren ergebnissen immer gleich?
		// the keys used as column names
		if (list.isEmpty()) {
			cursor = new MatrixCursor(new String[] { "" });
		} else {
			cursor = new MatrixCursor((String[]) list.get(0).keySet()
					.toArray(new String[list.get(0).keySet().size()]));
			for (LinkedHashMap<String, String> map : list) {
				cursor.addRow(map.values());
			}
		}
		// TODO:
		// cursor.setNotificationUri(super.getContext().getContentResolver(),
		// uri);

		return cursor;
	}

	/**
	 * @param urL
	 *            collection Url - database to insert the document
	 * @param values
	 *            the values to be inserted
	 * @return the Uri including the id of the document
	 */
	@Override
	public Uri insert(Uri urL, ContentValues values) {

		// single NEW entry has to be passed as COLLECTION, as it has no ID
		// yet
		// POST method is less secure than PUT as insert could be redone in
		// case of an connection problem. So its also possible to add a
		// document twice unintenionaly
		if (values != null && CouchConstants.isCollectionUri(urL)) {

			// check for all required attributes being available
			this.checkForAllRequiredAttributes(values);

			// TODO: populateDefaultValues(values)

			// as we use the PUT method the id has to be set in advanced
			JSONObject response = RestClient.request(this.getUUIDs(), null, 1,
					null);
			List<LinkedHashMap<String, String>> list = JsonProcessor
					.jsonToMap(response);
			String uuid = "";
			if (!list.isEmpty()) {
				uuid = list.get(0).get("uuid");
			}
			Uri urI = Uri.withAppendedPath(urL, uuid);

			return this.saveJSON(urI, JsonProcessor.valuesToJson(values));
		}
		throw new SQLException("Failed to insert row into " + urL);
	}

	/**
	 * check for all required attributes being available
	 * 
	 * @param values
	 *            ContentValues to be saved
	 * @return boolean
	 */
	private boolean checkForAllRequiredAttributes(ContentValues values) {
		// TODO: rest attr == null
		// leeren string zum l�schen eines zellinhalts?
		for (String attr : CouchConstants.REQUIRED_ATTRIBUTES) {
			if (!values.containsKey(attr) || values.get(attr) == null) {
				throw new IllegalArgumentException(
						"Not all required attributes are set!! ");
			}
		}
		return true;
	}

	private Uri saveJSON(Uri urI, JSONObject json) {

		JSONObject response = RestClient.request(this.buildQuery(urI, null),
				null, 2, json);
		List<LinkedHashMap<String, String>> map = JsonProcessor
				.jsonToMap(response);

		if (map.get(0).containsKey("id")
				&& map.get(0).get("id").equals(urI.getPathSegments().get(1))) {
			// super.getContext().getContentResolver().notifyChange(urI,
			// null);
			return urI;
		}
		throw new SQLException("Failed to insert row into " + urI);
	}

	/**
	 * two possibilities to delete a document (not implemented yet)
	 * 
	 * @param urI
	 *            item Uri - has to include the _id & the _rev
	 * @param noWhere
	 *            not to be set as is preset to "_rev"
	 * @param noWhereArgs
	 *            the revision of the document at the first place in the array
	 * @return 1 document deleted successfully
	 */
	@Override
	public int delete(Uri urI, String noWhere, String[] noWhereArgs) {

		if (!CouchConstants.isCollectionUri(urI)) {

			JSONObject response = RestClient.request(
					this.buildQuery(urI, null), null, 1, null);

			String rev = null;
			try {
				rev = response.getString("_rev");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Pair<String, String>[] param = new Pair[1];
			param[0] = new Pair<String, String>("rev", rev);
			response = RestClient.request(this.buildQuery(urI, null), param, 3,
					null);

			List<LinkedHashMap<String, String>> list = JsonProcessor
					.jsonToMap(response);

			if (!list.isEmpty() && list.get(0).containsKey("ok")
					&& list.get(0).get("ok").equals("true")) {
				// super.getContext().getContentResolver().notifyChange(urI,
				// null);
				return 1;
			}
		}
		throw new SQLException("Failed to delete row into " + urI);
	}

	/**
	 * two possibilities to update a document (not implemented yet)
	 * 
	 * @param urI
	 *            item Uri - has to include the _id & the _rev
	 * @param noWhere
	 *            not to be set as is preset to "_rev"
	 * @param rev
	 *            the revision of the document at the first place in the array
	 * @return 1 document deleted successfully
	 */
	@Override
	public int update(Uri urI, ContentValues values, String noWhere,
			String[] noWhereArgs) {

		this.checkForAllRequiredAttributes(values);

		JSONObject response = RestClient.request(this.buildQuery(urI, null),
				null, 1, null);

		// TODO: adresse 2. ebene??
		Iterator<Entry<String, Object>> it = values.valueSet().iterator();
		do {
			Entry<String, Object> e = it.next();
			try {
				response.put(e.getKey(), e.getValue());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} while (it.hasNext());

		this.saveJSON(urI, response);
		return 1;
	}

	/**
	 * 
	 * @param uri
	 *            the Uri - collection or item Uri
	 * @param rev
	 *            necessary for delete operation only
	 * @return the request string
	 */
	private Uri buildQuery(Uri uri, String view)
	// String[] selectionArgs)
	{
		StringBuilder result = new StringBuilder();

		result.append(CouchConstants.HOST + ":" + CouchConstants.PORT + "/"
				+ CouchConstants.DB_Name);
		// result.append(CouchConstants.HOST + "/" + CouchConstants.DB_Name);

		if (uri != null)
			if (CouchConstants.isCollectionUri(uri)) {
				if (view == null) {
					result.append(CouchConstants.VIEW_Default);
				} else {
					result.append(view);
				}

				// if (selectionArgs != null) {
				// if (selectionArgs.length == 1) {
				// result.append("?key=\"" + selectionArgs[0] + "\"");
				// } else if (selectionArgs.length == 2) {
				// result.append("?startkey=\"" + selectionArgs[0]
				// + "\"&endkey=\"" + selectionArgs[1] + "\"");
				// }
				// }
			} else {
				// TODO getlastseg
				result.append("/" + uri.getPathSegments().get(1));
			}
		return Uri.parse(result.toString());
	}

	private Pair<String, String>[] buildParams(String[] selectionArgs) {
		Pair<String, String>[] params = null;

		if (selectionArgs != null) {
			if (selectionArgs.length == 1) {
				// params[0] = new Pair<String, String>("key",
				// selectionArgs[0]);
				params = new Pair[] {
						new Pair<String, String>("startkey", selectionArgs[0]),
						new Pair<String, String>("endkey", selectionArgs[0]
								+ "\\u9999") };
			} else if (selectionArgs.length == 2) {
				params = new Pair[] {
						new Pair<String, String>("startkey", selectionArgs[0]),
						new Pair<String, String>("endkey", selectionArgs[1]) };
			} else {
				// TODO: key/value??
				// for (int i = 0; i < selectionArgs.length; i++) {
				//
				// }
			}
		}
		return params;
	}

	private Uri getUUIDs() {
		return Uri.parse(CouchConstants.HOST + ":" + CouchConstants.PORT
				+ "/_uuids?count=1");
	}

}
