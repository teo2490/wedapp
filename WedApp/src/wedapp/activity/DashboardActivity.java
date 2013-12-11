package wedapp.activity;

import wedapp.library.DatabaseHandler;
import dima.wedapp.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class DashboardActivity extends Activity {
	
	Button btnLogout;
	
	private boolean isMerchantLogged(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		int count = db.getRowCount();
		if(count > 0){
			return true;
		}
		return false;
	}
	
	public void logoutMerchant(Context context){
		DatabaseHandler db = new DatabaseHandler(context);
		db.resetTables();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(isMerchantLogged(getApplicationContext())){
			setContentView(R.layout.activity_dashboard);
        	btnLogout = (Button) findViewById(R.id.btnLogout);
        	
        	btnLogout.setOnClickListener(new View.OnClickListener() {
    			
    			public void onClick(View view) {
    				logoutMerchant(getApplicationContext());
    				Intent choose = new Intent(getApplicationContext(), WedApp.class);
    	        	choose.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	        	startActivity(choose);
    	        	finish();
    			}
    		});
		} else {
			// user is not logged in show login screen
        	Intent choose = new Intent(getApplicationContext(), WedApp.class);
        	choose.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(choose);
        	finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dashboard, menu);
		return true;
	}

}
