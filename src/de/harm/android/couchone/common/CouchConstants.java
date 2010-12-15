package de.harm.android.couchone.common;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public final class CouchConstants implements BaseColumns {
	// https://github.com/apage43

	// CouchOne server settings
	public static final String HOST = "https://harm.couchone.com";
	public static final int PORT = 6984;
	public static final String DB_Name = "test2";

	private static final String DESIGN_DOC = "/_design/test";
	public static final String VIEW_Default = DESIGN_DOC + "/_view/fn-ln_kv";
	private static final String VIEW_Address = DESIGN_DOC
			+ "/_view/fnlnaddress";

	public static final String[] REQUIRED_ATTRIBUTES = new String[] { Contact.FIRSTNAME };

	// ContentProvider settings
	public static final String AUTHORITY = "de.harm.android.couchone.provider.Provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DB_Name);

	public static final String COLLECTION_TYPE = "vnd.android.cursor.dir/vnd.harm.android.couchone.contacts";
	public static final String SINGLE_TYPE = "vnd.android.cursor.item/vnd.harm.android.couchone.contacts";

	private static final int URI_COLLECTION = 1;
	private static final int URI_ENTITY = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, DB_Name, URI_COLLECTION);
		uriMatcher.addURI(AUTHORITY, DB_Name + "/#", URI_ENTITY);
	}

	public static boolean isCollectionUri(Uri uri) {
		Log.i("harmsProviderCouchConstants",
				"accessed isCollectionUri method with uri: " + uri.toString());
		return (uriMatcher.match(uri) == URI_COLLECTION) ? true : false;
	}

	public final class Contact {
		public static final String FIRSTNAME = "firstname";
		public static final String LASTNAME = "lastname";
		public static final String AGE = "age";

		public final class Address {
			public static final String TYPE = "type";
			public static final String STREET = "street";
			public static final String ZIP = "zip";
			public static final String CITY = "city";
			public static final String COUNTRY = "coutry";
		}

		public final class Phone {
			public static final String TYPE = "type";
			public static final String NUMBER = "number";
		}

		public class eMail {
			public static final String TYPE = "type";
			public static final String EMAIL = "email";
		}
	}
}
