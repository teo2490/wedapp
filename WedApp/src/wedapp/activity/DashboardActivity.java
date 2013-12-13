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
	Button btnGift;
	Button btnList;
	TextView txtWelcome;
	
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
        	btnLogout = (Button) findViewById(R.id.btnLogout);
        	btnGift = (Button) findViewById(R.id.btnGift);
        	txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        	
        	String wlcMsg = "Hi "+db.getUserDetails().get("name")+" "+db.getUserDetails().get("city");
        	txtWelcome.setText(wlcMsg);

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
        	
        	btnGift.setOnClickListener(new View.OnClickListener() {
        		
				public void onClick(View v) {
					Intent newGift = new Intent(getApplicationContext(), NewProductActivity.class);
					newGift.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(newGift);
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
