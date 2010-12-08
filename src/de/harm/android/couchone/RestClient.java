package de.harm.android.couchone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.SQLException;
import android.net.Uri;
import android.util.Pair;

public class RestClient {

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param url
	 *            the url of the collection or item
	 * @param params
	 *            for search
	 * @param method
	 *            get=1, put=2, delete=3
	 * @param jsonValues
	 * @return
	 */
	public static JSONObject request(Uri url, Pair<String, String>[] params,
			int method, JSONObject jsonValues) {

		HttpClient httpclient = new DefaultHttpClient();
		// NameValuePair pair = new BasicNameValuePair(name, value);

		// Prepare a request object
		// try {
		// url = URLEncoder.encode(url, "UTF-8");
		// } catch (UnsupportedEncodingException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		HttpRequestBase httpMethod;
		if (method == 1) {
			String u = url.toString();
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					String second = params[i].second;
					// second.replace(" ", "%20");
					// second.replace(" ", "%5C");
					second = URLEncoder.encode(second);
					String str;
					if (i == 0) {
						str = "?";
					} else {
						str = "&";
					}
					u += str + params[i].first + "=%22" + second + "%22";
				}
				// url = Uri.withAppendedPath(url, param);
				// HttpParams p = new BasicHttpParams();
				// for (int i = 0; i < params.length; i++) {
				// p.setParameter(params[i].first, params[i].second);
				// }
				// httpMethod.setParams(p);
			}
			httpMethod = new HttpGet(u);
		} else if (method == 2) {
			HttpPut put = new HttpPut(url.toString());
			try {
				HttpEntity entity = null;
				entity = new StringEntity(jsonValues.toString(), "UTF8");
				put.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpMethod = put;
		} else if (method == 3) {
			String rev = "?" + params[0].first + "=" + params[0].second;
			url = Uri.withAppendedPath(url, rev);
			httpMethod = new HttpDelete(url.toString());
			// HttpParams p = new BasicHttpParams();
			// p.setParameter(params[0].first, params[0].second);
			// httpMethod.setParams(p);
		} else {
			throw new SQLException("HTTP-Method not defined correctly " + url);
		}

		// Execute the request
		HttpResponse response;
		JSONObject json = null;
		try {
			response = httpclient.execute(httpMethod);

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();

			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String responseContent = convertStreamToString(instream);

				// A Simple JSONObject Creation
				json = new JSONObject(responseContent);

				// Closing the input stream will trigger connection release
				instream.close();
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

}
