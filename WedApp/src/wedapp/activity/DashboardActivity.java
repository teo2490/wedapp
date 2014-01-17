package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;
import wedapp.library.UserFunctions;
import dima.wedapp.R;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardActivity extends Activity {
	
	Button btnLogout;
	Button btnNewList;
	Button btnDeleteList;
	Button btnUpList;
	Button btnUpProfile;
	EditText listToDelete;
	EditText listToUpdate;
	TextView txtWelcome;
	//TextView txtMessage;
	
	private static String KEY_SUCCESS = "success";
	private static final String TAG_LID = "id_list";
	
	final Context context = this;
	
	private ProgressDialog pDialog;
	private String message;
	private boolean flagDelete;
	JSONParser jsonParser = new JSONParser();
	private static String deleteURL = "http://wedapp.altervista.org/delete_list.php";
	//private static String updateURL = "http://wedapp.altervista.org/update_list.php";
	
	/**
	 * Back button is disabled
	 */
	@Override
	public void onBackPressed()
	{
		return;
	}
	/**
	 * Use this to check if merchant is logged.
	 * 
	 * @param context
	 * @return boolean true if merchant is logged, false otherwise			
	 */
	private boolean isMerchantLogged(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.createLogin();
		int count = db.getRowLoginCount();
		if(count > 0){
			return true;
		}
		return false;
	}

	/**
	 * On creation the Internet is checked.
	 * Kind of device is checked and the orientation is set.
	 * EditText, Textiew and Button are placed.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Controllo connessione ON
		Networking nw = new Networking();
        if(!nw.isNetworkAvailable(getApplicationContext())){
            Toast.makeText(this, getString(R.string.noInternet), Toast.LENGTH_LONG).show();
            finish();
        }

        //ActionBar ab = getActionBar();
        //ab.setHomeButtonEnabled(true);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		if(isMerchantLogged(getApplicationContext())){
			setContentView(R.layout.activity_dashboard);
			
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			db.createLogin();
        	//btnLogout = (Button) findViewById(R.id.btnLogout);
        	//btnNewList = (Button) findViewById(R.id.btnNewList);
        	btnDeleteList = (Button) findViewById(R.id.btnDeleteList);
        	btnUpList = (Button) findViewById(R.id.btnUpList);
        	btnUpProfile = (Button) findViewById(R.id.btnUpProfile);
        	//btnGift = (Button) findViewById(R.id.btnGift);
        	listToDelete = (EditText) findViewById(R.id.listToDelete);
        	listToUpdate = (EditText) findViewById(R.id.listToUpdate);
        	txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        	//txtMessage = (TextView) findViewById(R.id.txtMessage);
        	
        	String wlcMsg = getString(R.string.welcomeDashboard)+" "+db.getUserDetails().get("name")+" "+db.getUserDetails().get("city");
        	txtWelcome.setText(wlcMsg);
        	
        	/*btnNewList.setOnClickListener(new View.OnClickListener() {		
    			public void onClick(View view) {
    				Intent newList = new Intent(getApplicationContext(), NewListActivity.class);
    	        	newList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	        	startActivity(newList);
    	        	finish();
    			}
    		});*/
        	
        	btnUpList.setOnClickListener(new View.OnClickListener() {
        		/**
        		 * It starts the "update list" phase
        		 */
				public void onClick(View view) {
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.createList();
					db.resetListTable();
					
					String lid = listToUpdate.getText().toString();
					db.addList(lid);
					
					if(lid.equals("")){
						Toast.makeText(getApplicationContext(), getString(R.string.noListCodeDashboard), Toast.LENGTH_SHORT).show();
					}
					else{
						Intent i = new Intent(getApplicationContext(), MerListActivity.class);
						i.putExtra(TAG_LID, lid);
						startActivity(i);
						finish();
					}
				}
			});
        	
        	btnDeleteList.setOnClickListener(new View.OnClickListener() {
        		/**
        		 * The "delete list" phase is started
        		 */
				public void onClick(View view) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			 
						// set title
						alertDialogBuilder.setTitle(getString(R.string.confirm));
			 
						// set dialog message
						alertDialogBuilder
							.setMessage(getString(R.string.confirmD))
							.setCancelable(false)
							.setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									new deleteList().execute();
									dialog.cancel();
								}
							  })
							.setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									// if this button is clicked, just close
									// the dialog box and do nothing
									dialog.cancel();
								}
							});
			 
							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder.create();
			 
							// show it
							alertDialog.show();
				}
			});
        	
        	btnUpProfile.setOnClickListener(new View.OnClickListener() {
        		/**
        		 * It starts the "update profile" phase
        		 */
				public void onClick(View view) {
					Intent upProfile = new Intent(getApplicationContext(), UpdateProfileActivity.class);
					upProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(upProfile);
					finish();
				}
			});
        	
        	/*btnLogout.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View view) {
    				logoutMerchant(getApplicationContext());
    				Intent choose = new Intent(getApplicationContext(), WedApp.class);
    	        	choose.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	        	startActivity(choose);
    	        	finish();
    			}
    		});*/
        	
        	
        	
        	/*btnGift.setOnClickListener(new View.OnClickListener() {
        		
				public void onClick(View v) {
					Intent newGift = new Intent(getApplicationContext(), NewProductActivity.class);
					newGift.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(newGift);
					finish();
				}
			});*/
        	
		} else {
			// user is not logged in show login screen
        	Intent choose = new Intent(getApplicationContext(), WedApp.class);
        	choose.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(choose);
        	finish();
		}
	}

	/**
	 * Options menu is created
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dashboard, menu);
        /*MenuItem item = menu.findItem(R.id.actShare);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();*/
		return true;
	}
	
	/**
	 * Logout and AddList button are placed in menu bar
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		UserFunctions us = new UserFunctions();
        switch (item.getItemId()) {
        
            case R.id.actLogout:
				us.logoutMerchant(getApplicationContext());
				Intent choose = new Intent(getApplicationContext(), WedApp.class);
	        	choose.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(choose);
	        	finish();
                return true;
                
            case R.id.actAddList:
				Intent newList = new Intent(getApplicationContext(), NewListActivity.class);
	        	newList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(newList);
	        	finish();
	        	
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class Networking {
    	 /**
    	  * Use to check if there is an Internet connection
    	  * 
    	  * @param context
    	  * @return boolean return true if the application can access the Internet, false otherwise
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
    
    class deleteList extends AsyncTask<String, String, String> {
		
    	/**
    	 * Progress dialog is created
    	 */
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(DashboardActivity.this, getString(R.string.Loading),
					getString(R.string.PleaseWait), true);
        }
		
		/**
		 * List is deleted in background
		 */
		@Override
		protected String doInBackground(String... args) {
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			db.createLogin();
			String code = listToDelete.getText().toString();
			String email = db.getUserDetails().get("email");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("id", code));
			params.add(new BasicNameValuePair("merchant_email", email));
			JSONObject json = jsonParser.makeHttpRequest(deleteURL, "POST", params);
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						flagDelete = true;
						message = getString(R.string.listDelDashboard);
						pDialog.dismiss();
					}else{
						flagDelete = false;
						message = getString(R.string.delErrorDashboard);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					// display errMsg in TextView
					pDialog.dismiss();
					if(flagDelete){
						listToDelete.setText("");
					}
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				}
			});
			return null;
		}
    	
    }
}
