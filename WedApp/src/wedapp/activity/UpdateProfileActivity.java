package wedapp.activity;

import dima.wedapp.R;
import dima.wedapp.R.layout;
import dima.wedapp.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class UpdateProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_profile);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_profile, menu);
		return true;
	}

}
