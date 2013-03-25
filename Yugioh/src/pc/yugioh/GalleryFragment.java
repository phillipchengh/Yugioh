package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GalleryFragment extends Fragment {

	private DefaultHttpClient client;
	private Activity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gallery_fragment, container, false);
		activity = getActivity();
		
		//Convert the selection string to a more convenient search term
		//Removing spaces and special characters yields better results in the query
		String search = getArguments().getString("gallery_name").replaceAll("[^A-Za-z0-9]", "");
		client = new DefaultHttpClient();
		ImageView image = (ImageView) view.findViewById(R.id.card_image);
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if (ni != null && ni.isConnected()) {
			new ImageTask(image).execute(client, search);
		} else {
			//couldn't connect
		}
		return view;
	}
	
	private class ImageTask extends AsyncTask<Object, Void, String> {

		ImageView image;
		
		public ImageTask(ImageView image) {
			this.image = image;
		}
		
		@Override
		protected String doInBackground(Object... params) {
			DefaultHttpClient client = (DefaultHttpClient) params[0];
			String search = (String) params[1], url;
			String query = "http://yugioh.wikia.com/api.php?action=query&list=allimages&format=json&ailimit=25&aifrom=" + search;
			InputStream is;
			try {
            	HttpGet get = new HttpGet(query);
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					is = entity.getContent();
					//convertStreamToString handles closing the is
					String result = convertStreamToString(is);
					JSONObject json = new JSONObject(result);
					//Get array of results
					JSONArray queries = (json.getJSONObject("query")).getJSONArray("allimages");
					url = getURL(queries, search);
				    EntityUtils.consume(entity);
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			} finally {
				client.getConnectionManager().shutdown();
			}
			return url;
		}
		
		 /* Filters:
		 * 		"Anime" - Anime images
		 * 		"-VG" - Video game images
		 * 		"-ZX-" - I don't know what it is, but I don't want it
		 */
		private boolean filter(String query_name) {
			if (!query_name.contains("Anime") &&
					!query_name.contains("-VG") &&
					!query_name.contains("-ZX-")) {
				return true;
			}
			return false;
		}
		
		//Parse through queries and get the most likely result, how this is determined is listed below
		private String getURL(JSONArray queries, String search) {
			JSONObject query_result;
			String current_url = null, query_name;
			boolean english = false;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Date current_date = null, query_date = null;
			int query_length = queries.length();
			try {
				/*
				 * The query_name must contain the card name and follows filters in filter()
				 * Next, English card names are prioritized over others
				 * Lastly, later time stamps are preferred
				 */
				for (int i = 0; i < query_length; i++) {
					query_result = queries.getJSONObject(i);
					query_name = query_result.getString("name");
					try {
						query_date = sdf.parse(query_result.getString("timestamp"));
					} catch (java.text.ParseException e) {
						return null;
					}
					//Check if correct card and follows filters
					//TODO Check if + '-' is suffice or should a regex be used
					if (query_name.contains(search + '-') && filter(query_name)) {
						//If no card is found yet, initialize values
						if (current_url == null) {
							current_url = query_result.getString("url");
							if (query_name.contains("-EN-")) {
								english = true;
							}
							current_date = query_date;
							continue;
						}
						//If current is not in English, then prioritize candidate if it is in English
						if (!english) {
							if (query_name.contains("-EN-")) {
								current_url = query_result.getString("url");
								english = true;
								current_date = query_date;
								continue;
							}
							//If candidate is a newer image, then switch
							if (query_date.after(current_date)) {
								current_url = query_result.getString("url");
								current_date = query_date;
							}
						}
						//If current is in English and candidate is not, then continue
						if (!query_name.contains("-EN-")) {
							continue;
						}
						//If candidate is a newer image, then switch
						if (query_date.after(current_date)) {
							current_url = query_result.getString("url");
							current_date = query_date;
						}
					}
				}
			} catch (JSONException e) {
				return null;
			}
			return current_url;
		}

		@Override
		protected void onPostExecute(String url) {
			if (url == null) {
				//TODO Couldn't get an image url
				return;
			}
			new DrawImageTask(image).execute(url);
		}
		
	}
	
	private class DrawImageTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView image;
		
		public DrawImageTask(ImageView image) {
			this.image = image;
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			Bitmap map = null;
			InputStream is;
			try {
				is = new java.net.URL(url).openStream();
				map = BitmapFactory.decodeStream(is);
			} catch (Exception e) {
				Log.e("Bitmap Error", e.getMessage());
			}
			return map;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			try {
				image.setImageBitmap(result);
			} catch (Exception e) {
				Log.e("Error", "Setting Bitmap Error", e);
			}
		}
		
	}
	
	 private static String convertStreamToString(InputStream is) {
	        /*
	         * To convert the InputStream to String we use the BufferedReader.readLine()
	         * method. We iterate until the BufferedReader return null which means
	         * there's no more data to read. Each line will appended to a StringBuilder
	         * and returned as String.
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
}
