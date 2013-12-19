package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;
import dima.wedapp.R;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RegisterActivity extends Activity {
	
	private ProgressDialog pDialog;
	private String errMsg;
	JSONParser jsonParser = new JSONParser();
	private static String registerURL = "http://wedapp.altervista.org/create_merchant.php";
	
	Button btnRegister;
	Button btnLinkToLogin;
	EditText inputEmail;
	EditText inputPassword;
	EditText inputVAT;
	EditText inputName;
	EditText inputCity;
	EditText inputAddress;
	EditText inputBuild;
	EditText inputPhone;
	TextView registerErrorMsg;

	private static String KEY_SUCCESS = "success";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         	   getActionBar().setDisplayHomeAsUpEnabled(true);
         	}

		// Importing all assets like buttons, text fields
		inputEmail = (EditText) findViewById(R.id.registerEmail);
		inputPassword = (EditText) findViewById(R.id.registerPassword);
		inputVAT = (EditText) findViewById(R.id.registerVAT);
		inputName = (EditText) findViewById(R.id.registerName);
		inputCity = (EditText) findViewById(R.id.registerCity);
		inputAddress = (EditText) findViewById(R.id.registerAddress);
		inputBuild = (EditText) findViewById(R.id.registerBuild);
		inputPhone = (EditText) findViewById(R.id.registerPhone);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
		registerErrorMsg = (TextView) findViewById(R.id.register_error);
		
		btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new registerMerchant().execute();
			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(i);
				// Close Registration View
				finish();
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	      case android.R.id.home:
	         NavUtils.navigateUpTo(this,
	               new Intent(this, LoginActivity.class));
	         return true;
	   }
	   return super.onOptionsItemSelected(item);
	}
	
	class registerMerchant extends AsyncTask<String, String, String> {
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(RegisterActivity.this, "Loading",
					"Please wait...", true);
        }
		
		protected String doInBackground(String... args) {
			String email = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			String piva = inputVAT.getText().toString();
			String name = inputName.getText().toString();
			String city = inputCity.getText().toString();
			String address = inputAddress.getText().toString();
			String build_number = inputBuild.getText().toString();
			String phone = inputPhone.getText().toString();
			Log.d("Button", "Register");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", email));
			params.add(new BasicNameValuePair("password", password));
			params.add(new BasicNameValuePair("piva", piva));
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("city", city));
			params.add(new BasicNameValuePair("address", address));
			params.add(new BasicNameValuePair("build_number", build_number));
			params.add(new BasicNameValuePair("phone", phone));
			JSONObject json = jsonParser.makeHttpRequest(registerURL, "POST", params);
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					errMsg = "";
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						// user successfully registred
						// Store user details in SQLite DatabaseJSONObject
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						db.addUser(email, name, city, address, build_number, phone);						
						// Launch Dashboard Screen
						Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);
						// Close all views before launching Dashboard
						dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						pDialog.dismiss();
						startActivity(dashboard);
						// Close Registration Screen
						finish();
					}else{
						// Error in registration
						errMsg = "Error occured in registration";
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
					registerErrorMsg.setText(errMsg);
				}
			});
			return null;
		}
		
		protected void onPostExecute() {
    		// dismiss the dialog once done
    		pDialog.dismiss();
		}
	}
}