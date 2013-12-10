package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import dima.wedapp.R;

import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;
import wedapp.library.UserFunctions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	Button btnLogin;
	Button btnLinkToRegister;
	EditText inputEmail;
	EditText inputPassword;
	TextView loginErrorMsg;
	private boolean isLastThread = true;
	private ProgressDialog pDialog;
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Importing all assets like buttons, text fields
		inputEmail = (EditText) findViewById(R.id.loginEmail);
		inputPassword = (EditText) findViewById(R.id.loginPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		loginErrorMsg = (TextView) findViewById(R.id.login_error);

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				new loginMerchant().execute();
			}
		});
	}
			
	class loginMerchant extends AsyncTask<String, String, String> {

		@Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
		
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
					loginErrorMsg.setText("");
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						// user successfully logged in
						// Store user details in SQLite Database
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						JSONObject json_user = json.getJSONObject("merchant");
						
						// Clear all previous data in database
						//userFunction.logoutUser(getApplicationContext());
						db.addUser(json_user.getString(KEY_EMAIL), json_user.getString(KEY_NAME), json_user.getString(KEY_CITY), json_user.getString(KEY_ADDRESS), json_user.getString(KEY_BUILD), json_user.getString(KEY_PHONE));						
						
						// Launch Dashboard Screen
						Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);
						
						// Close all views before launching Dashboard
						dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(dashboard);
						
						// Close Login Screen
						finish();
					}else{
						// Error in login
						System.out.println("fail!!!");
						loginErrorMsg.setText("Incorrect username/password");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(String file_url) {
        	if(isLastThread){
        		// dismiss the dialog once done
        		pDialog.dismiss();
        	}
        	else{
        		isLastThread=true;
        	}
        }

		// Link to Register Screen
		/*btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});*/
	}
}
