package pc.yugioh;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.util.DisplayMetrics;
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
		
		String gallery_name = getArguments().getString("gallery_name");
		client = new DefaultHttpClient();
		ImageView image = (ImageView) view.findViewById(R.id.card_image);
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if (ni != null && ni.isConnected()) {
			new ImageTask(image).execute(client, gallery_name);
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
			String gallery_name = (String) params[1], url;
			String query = "http://yugioh.wikia.com/api.php?action=query&list=allimages&format=json&ailimit=25&aifrom=" + gallery_name;
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
					url = getURL(queries);
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
		
		private String getURL(JSONArray queries) {
			String url = null;
			try {
				JSONObject query_result = queries.getJSONObject(0);
				url = query_result.getString("url");
			} catch (JSONException e) {
				return null;
			}
			return url;
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
	

	private static class FlushedInputStream extends FilterInputStream {
	    public FlushedInputStream(InputStream inputStream) {
	    super(inputStream);
	    }

	    @Override
	    public long skip(long n) throws IOException {
	        long totalBytesSkipped = 0L;
	        while (totalBytesSkipped < n) {
	            long bytesSkipped = in.skip(n - totalBytesSkipped);
	            if (bytesSkipped == 0L) {
	                  int byteValue = read();
	                  if (byteValue < 0) {
	                      break;  // we reached EOF
	                  } else {
	                      bytesSkipped = 1; // we read one byte
	                  }
	           }
	           totalBytesSkipped += bytesSkipped;
	        }
	        return totalBytesSkipped;
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
				map = BitmapFactory.decodeStream(new FlushedInputStream(is));
			} catch (Exception e) {
				Log.e("Bitmap Error", e.getMessage());
			}
			return map;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			try {
				int height, width;
				if (result.getHeight() >= 2048 || result.getWidth() >= 2048) {
					height = 2048;
					width = (int) ((2.5/3.375)*2048.0);
					result = Bitmap.createScaledBitmap(result, width, height, true);
				}
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
