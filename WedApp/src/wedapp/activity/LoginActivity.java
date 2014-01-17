package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dima.wedapp.R;

import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoginActivity extends Activity {
	Button btnLogin;
	Button btnLinkToRegister;
	EditText inputEmail;
	EditText inputPassword;
	TextView loginErrorMsg;
	private ProgressDialog pDialog;
	private String errMsg;
	
	JSONParser jsonParser = new JSONParser();
	private static String loginURL = "http://wedapp.altervista.org/login.php";
	
	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_EMAIL = "email";
	private static String KEY_NAME = "name";
	private static String KEY_CITY = "city";
	private static String KEY_ADDRESS = "address";
	private static String KEY_BUILD = "build_number";
	private static String KEY_PHONE = "phone";
	
	/**
	 * On creation kind of device is checked and the orientation is set.
	 * EditText, Textiew and Button are placed.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }

		// Importing all assets like buttons, text fields
		inputEmail = (EditText) findViewById(R.id.loginEmail);
		inputPassword = (EditText) findViewById(R.id.loginPassword);
		loginErrorMsg = (TextView) findViewById(R.id.login_error);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {
			/**
			 * The AsyncTask is executed
			 */
			public void onClick(View view) {
				new loginMerchant().execute();
			}
		});
		
		// Link to Register Screen
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
			/**
			 * Registration activity is started
			 */
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});
	}
	
	/**
	 * Back button is set to go to previous activity
	 */
	public void onBackPressed() {
		Intent back = new Intent(getApplicationContext(), WedApp.class);
    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(back);
    	finish();
    	return;
	}
	
	/**
	 * Button home is shown in the option bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	      case android.R.id.home:
				Intent back = new Intent(getApplicationContext(), WedApp.class);
	        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(back);
	        	finish();
                return true;
	   }
	   return super.onOptionsItemSelected(item);
	}
			
	class loginMerchant extends AsyncTask<String, String, String> {
		
		/**
		 * A progress dialog is shown
		 */
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(LoginActivity.this, getString(R.string.Loading),
					getString(R.string.PleaseWait), true);
        }
		
		/**
		 * Data from database are retrieved in order to do the login
		 */
		protected String doInBackground(String... args) {
			String email = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			Log.d("Button", "Login");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", email));
			params.add(new BasicNameValuePair("password", password));
			JSONObject json = jsonParser.makeHttpRequest(loginURL, "POST", params);
			
			// check for login response
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					errMsg = "";
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						// user successfully logged in
						// Store user details in SQLite Database
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						db.createLogin();
						JSONArray merchantObj = json.getJSONArray("merchant");
						JSONObject json_user = merchantObj.getJSONObject(0);
						
						// Clear all previous data in database
						//userFunction.logoutUser(getApplicationContext());
						db.addUser(json_user.getString(KEY_EMAIL), json_user.getString(KEY_NAME), json_user.getString(KEY_CITY), json_user.getString(KEY_ADDRESS), json_user.getString(KEY_BUILD), json_user.getString(KEY_PHONE));						
						
						// Launch Dashboard Screen
						Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);
						
						// Close all views before launching Dashboard
						dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						pDialog.dismiss();
						startActivity(dashboard);
						
						// Close Login Screen
						finish();
					}else{
						// Error in login
						errMsg = getString(R.string.errorLogin);
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
					loginErrorMsg.setText(errMsg);
				}
			});
			return null;
		}
		
		/**
		 * the progress dialog is dismissed
		 */
		protected void onPostExecute() {
        		// dismiss the dialog once done
        		pDialog.dismiss();
        }
	}
}
