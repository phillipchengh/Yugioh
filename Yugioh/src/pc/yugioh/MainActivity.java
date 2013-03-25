package pc.yugioh;

import pc.yugioh.SuggestionFragment.OnSuggestionListener;
import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements OnSuggestionListener {

	private FragmentManager fm;
	private FragmentTransaction ft;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
        fm = getFragmentManager();
    	ft = fm.beginTransaction();
    	ft.add(R.id.mainLayout, new SuggestionFragment());
    	ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onSuggestionSelected(String suggestion) {
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra("Suggestion", suggestion);
		startActivity(intent);
	}

	@Override
	public void onSearchSelected(String search) {
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra("Search", search);
		startActivity(intent);
	}
}
