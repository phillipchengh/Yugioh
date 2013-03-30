package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InfoFragment extends Fragment {

	private Activity activity;
	private HashMap<String, String> map;
	private String[] MonsterInfo = {
			
	};
	private String[] SpellTrapInfo = {
			
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.gallery_fragment, container, false);
		activity = getActivity();
		String rdf = getArguments().getString("rdf");
		String gallery_name = getArguments().getString("gallery_name");
		int card_type = getArguments().getInt("card_type");
		
		return view;
	}
	
	private void loadMap(String rdf, int card_type) {
		BufferedReader rdf = new BufferedReader(new StringReader(result));
		String line, image_name = null;
		try {
			while ((line = rdf.readLine()) != null) {
				if (line.contains("Card_Image")) {
					image_name = getCardImageUrl(line);
					if (card_type == 1 || card_type == 2) {
						break;
					}
				}
				if (line.contains("&wiki;Monster_Card")) {
					card_type = 0;
					//card_image should already have been found
					break;
				} else if (line.contains("&wiki;Spell_Card")) {
					card_type = 1;
					//continue for card_image
				} else if (line.contains("&wiki;Trap_Card")) {
					card_type = 2;
					//continue for card_image
				} else if (line.contains("&wiki;Category-3ASeries")) {
					setupArchseries(result);
					archseries = true;
					break;
				} else if (line.contains("&wiki;Category-3AArchetypes")) {
					setupArchseries(result);
					archseries = true;
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("InfoFragment", "IO error", e);
		}
	}
	
	
	}
}
