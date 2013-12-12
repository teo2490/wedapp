package wedapp.activity;

import wedapp.library.DatabaseHandler;
import dima.wedapp.R;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardActivity extends Activity {
	
	Button btnLogout;
	TextView nameText;
	TextView cityText;
	
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
		//Controllo connessione ON
		Networking nw = new Networking();
        if(!nw.isNetworkAvailable(getApplicationContext())){
            Toast.makeText(this, "L'applicazione necessita di una connessione dati!", Toast.LENGTH_LONG).show();
            finish();
        }
		
		if(isMerchantLogged(getApplicationContext())){
			setContentView(R.layout.activity_dashboard);
			
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        	nameText = (TextView) findViewById(R.id.nameText);
        	cityText = (TextView) findViewById(R.id.cityText);
        	btnLogout = (Button) findViewById(R.id.btnLogout);

        	/*nameText.setText(db.getReadableDatabase());
        	cityText.setText("test");*/
        	
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

    public class Networking {
      	 /*
      	 *@return boolean return true if the application can access the internet
      	 */
      	 public boolean isNetworkAvailable(Context context) {
      	     ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      	     if (connectivity != null) {
      	        NetworkInfo[] info = connectivity.getAllNetworkInfo();
      	        if (info != null) {
      	           for (int i = 0; i < info.length; i++) {
      	              if (info[i].getState() == NetworkInfo.State.CONNECTED) {
      	                 return true;
      	              }
      	           }
      	        }
      	     }
      	     return false;
      	  }
      	}
}
