//package de.harm.android.couchone;
//
//import java.util.LinkedHashMap;
//
//import org.json.JSONException;
//
//import se.msc.android.droidcouch.CouchException;
//import se.msc.android.droidcouch.CouchJsonDocument;
//import android.content.ContentProvider;
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.database.MatrixCursor;
//import android.database.SQLException;
//import android.net.Uri;
//
//public class Provider_bak extends ContentProvider {
//
//	/**
//	 * Field variables
//	 */
//	// private CouchDatabase dbContacts;
//
//	/**
//	 * Implementations for the extended ContentProvider
//	 */
//
//	@Override
//	public boolean onCreate() {
//		// try {
//		// CouchServer server = new CouchServer(CouchConstants.HOST,
//		// CouchConstants.PORT);
//		// dbContacts = server.GetNewDatabase(CouchConstants.dbName);
//		// } catch (Exception e) {
//		// System.err.println("No connection to couchOne database!!");
//		// System.err.println(e);
//		// }
//		// return (dbContacts == null) ? false : true;
//		return true;
//	}
//
//	@Override
//	public String getType(Uri url) {
//		if (isCollectionUri(url)) {
//			return (this.getCollectionType());
//		}
//		return (this.getSingleType());
//	}
//
//	private String getCollectionType() {
//		return ("vnd.android.cursor.dir/vnd.harm.android.couchone.contacts");
//	}
//
//	private String getSingleType() {
//		return ("vnd.android.cursor.item/vnd.harm.android.couchone.contacts");
//	}
//
//	@Override
//	public Cursor query(Uri urL, String[] projection, String selection,
//			String[] selectionArgs, String sortOrder) {
//
//		MatrixCursor cursor = null;
//
//		if (this.isCollectionUri(urL)) {
//			// cursor.addRow(dbContacts.GetAllDocuments(CouchDocument.class));
//			LinkedHashMap<String, String> response = RestClient.request(this
//					.buildContactQuery(urL));
//			cursor = new MatrixCursor((String[]) response.keySet().toArray());
//			cursor.addRow(response.values());
//			cursor.setNotificationUri(this.getContext().getContentResolver(),
//					urL);
//		}
//		// TOTO: enthält liste mit CouchDocuments
//		// TOTO anderes format
//		return cursor;
//	}
//
//	@Override
//	public Uri insert(Uri urL, ContentValues values) {
//		ContentValues insertValues;
//		// see difference between urL and urI
//
//		// values must not be null, in case create a new instance
//		if (values != null) {
//			insertValues = values;
//		} else {
//			insertValues = new ContentValues();
//		}
//
//		// single NEW entry has to be passed as COLLECTION, as it has no ID yet
//		if (!this.isCollectionUri(urL)) {
//			// TODO: exception
//		}
//
//		// check for all required attributes being available
//		for (String attr : CouchConstants.dbRequiredAttributes) {
//			if (!insertValues.containsKey(attr)) {
//				throw new IllegalArgumentException("Unknown Uri " + urL);
//			}
//		}
//
//		// TODO: populateDefaultValues(values)
//
//		// get CouchDocument as Json and save
//		// ICouchDocument doc = this.getContentValuesAsJson(values);
//		// try {
//		// doc = dbContacts.SaveDocument(doc);
//		// System.out.println("Insert: " + doc.toString());
//		// } catch (CouchException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//
//		LinkedHashMap<String, String> response = RestClient
//				.request(buildContactQuery(null));
//
//		// attention: identifier might be a string instead of long
//		if (response.containsKey("_id")) {
//			Uri urI = Uri.withAppendedPath(CouchConstants.CONTENT_URI,
//					response.get("_id"));
//			super.getContext().getContentResolver().notifyChange(urI, null);
//			return urI;
//		}
//		throw new SQLException("Failed to insert row into " + urL);
//	}
//
//	/**
//	 * NO collection Uri!!
//	 */
//	@Override
//	public int delete(Uri urI, String rev, String[] noArgs) {
//		int count;
//		String id = "";
//		if (!this.isCollectionUri(urI)) {
//			// TODO: what happens without an rev??
//			id = urI.getPathSegments().get(1);
//			String str = urI.getLastPathSegment();
//			// try {
//			// dbContacts.DeleteDocument(id, rev);
//			// System.out.println("Delete: ID: " + id + " +Rev: " + rev);
//			// } catch (CouchException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// }
//
//			LinkedHashMap<String, String> response = RestClient
//					.request(buildContactQuery(urI));
//			if (response.containsKey("ok") && response.get("ok").equals("true")) {
//				getContext().getContentResolver().notifyChange(urI, null);
//				return 1;
//			}
//			throw new SQLException("Failed to delete row into " + urI);
//		}
//	}
//
//	@Override
//	public int update(Uri url, ContentValues values, String where,
//			String[] whereArgs) {
//
//		int count = 0;
//
//		if (this.isCollectionUri(url)) {
//			count = 0;
//			// count = dbContacts.update(getTableName(), values, where,
//			// whereArgs);
//		} else {
//			// String id = url.getPathSegments().get(1);
//			// ICouchDocument oldDoc = null;
//			// try {
//			// oldDoc = dbContacts.GetDocument(id);
//			// } catch (CouchException e1) {
//			// // TODO Auto-generated catch block
//			// e1.printStackTrace();
//			// }
//
//			// values shuld include the ID of the old object
//			// TODO verify id and rev
//			// ICouchDocument doc = this.getContentValuesAsJson(values);
//			// newDoc.Id(oldDoc.Id());
//			// try {
//			// doc = dbContacts.SaveDocument(doc);
//			// count = 1;
//			// } catch (CouchException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// }
//
//			RestClient.request(url);
//		}
//		getContext().getContentResolver().notifyChange(url, null);
//		return count;
//	}
//
//	private boolean isCollectionUri(Uri uri) {
//		return (uri.getPath().equals(CouchConstants.uriCollection)) ? true
//				: false;
//	}
//
//	private CouchJsonDocument getContentValuesAsJson(ContentValues values) {
//
//		CouchJsonDocument couchJson = new CouchJsonDocument();
//
//		// TODO maybe remove rev from values
//		for (String key : CouchConstants.dbAttributes) {
//			if (values.containsKey(key)) {
//				try {
//					couchJson.Obj.put(key, values.getAsString(key));
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//
//		// ICouchDocument jsonDoc = null;
//		// try {
//		// jsonDoc = dbContacts.CreateDocument(couchJson);
//		// } catch (CouchException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//
//		return couchJson;
//	}
//
//	private String buildContactQuery(Uri uri) {
//		String result;
//		if (uri == null) {
//			result = CouchConstants.HOST + "/" + CouchConstants.dbName + "/";
//		} else {
//			result = CouchConstants.HOST + "/" + CouchConstants.dbName + "/"
//					+ uri.getLastPathSegment();
//		}
//		return result;
//	}
//}
