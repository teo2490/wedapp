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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UpdateProfileActivity extends Activity {

	private ProgressDialog pDialog;
	private String errMsg;
	JSONParser jsonParser = new JSONParser();
	private static String updateURL = "http://wedapp.altervista.org/update_merchant.php";
	
	Button btnUpdate;
	EditText inputEmail;
	EditText inputPassword;
	EditText inputCity;
	EditText inputAddress;
	EditText inputBuild;
	EditText inputPhone;
	TextView update_error;

	private static String KEY_SUCCESS = "success";
	
	protected void onCreate(Bundle savedInstanceState) {
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		db.createLogin();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_profile);
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
       	   getActionBar().setDisplayHomeAsUpEnabled(true);
       	}

		inputEmail = (EditText) findViewById(R.id.updateEmail);
		inputPassword = (EditText) findViewById(R.id.updatePassword);
		inputCity = (EditText) findViewById(R.id.updateCity);
		inputAddress = (EditText) findViewById(R.id.updateAddress);
		inputBuild = (EditText) findViewById(R.id.updateBuild);
		inputPhone = (EditText) findViewById(R.id.updatePhone);
		btnUpdate = (Button) findViewById(R.id.btnUpdate);
		update_error = (TextView) findViewById(R.id.update_error);
		
		/* EditText precompilate */
		inputEmail.setText(db.getUserDetails().get("email"));
		inputCity.setText(db.getUserDetails().get("city"));
		inputAddress.setText(db.getUserDetails().get("address"));
		inputBuild.setText(db.getUserDetails().get("build_number"));
		inputPhone.setText(db.getUserDetails().get("phone"));
		
		btnUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new updateMerchant().execute();
			}
		});
	}
	
	public void onBackPressed() {
		Intent back = new Intent(getApplicationContext(), DashboardActivity.class);
    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(back);
    	finish();
    	return;
	}
	
	class updateMerchant extends AsyncTask<String, String, String> {
		
		
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(UpdateProfileActivity.this, getString(R.string.Loading),
					getString(R.string.PleaseWait), true);
        }
		
		protected String doInBackground(String... args) {
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			db.createLogin();
			String newEmail = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			String city = inputCity.getText().toString();
			String address = inputAddress.getText().toString();
			String build_number = inputBuild.getText().toString();
			String phone = inputPhone.getText().toString();
			String oldEmail = db.getUserDetails().get("email");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", newEmail));
			params.add(new BasicNameValuePair("password", password));
			params.add(new BasicNameValuePair("city", city));
			params.add(new BasicNameValuePair("address", address));
			params.add(new BasicNameValuePair("build_number", build_number));
			params.add(new BasicNameValuePair("phone", phone));
			params.add(new BasicNameValuePair("email_src", oldEmail));
			JSONObject json = jsonParser.makeHttpRequest(updateURL, "POST", params);
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					errMsg = getString(R.string.successUpdateProfile);
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						String name = db.getUserDetails().get("name");
						db.resetLoginTables();
						/*json = jsonParser.makeHttpRequest(detailsURL, "POST", params);
						JSONArray merchantObj = json.getJSONArray("merchant");
						JSONObject json_user = merchantObj.getJSONObject(0);*/
						db.addUser(newEmail, name, city, address, build_number, phone);
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
						errMsg = getString(R.string.errorUpdateProfile);
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
					update_error.setText(errMsg);
				}
			});
			return null;
		}
		
		protected void onPostExecute() {
    		// dismiss the dialog once done
    		pDialog.dismiss();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_profile, menu);
		return true;
	}

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
            case android.R.id.home:
				Intent back = new Intent(getApplicationContext(), DashboardActivity.class);
	        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(back);
	        	finish();
                return true;
            
	        	
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
