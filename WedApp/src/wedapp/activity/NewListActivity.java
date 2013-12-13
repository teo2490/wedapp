package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.activity.RegisterActivity.registerMerchant;
import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;
import dima.wedapp.R;
import dima.wedapp.R.layout;
import dima.wedapp.R.menu;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewListActivity extends Activity {
	
	Button btnAddList;
	Button btnGoBack;
	EditText nGroom;
	EditText sGroom;
	EditText nBride;
	EditText sBride;
	EditText wDate;
	TextView errorMsg;
	
	DatabaseHandler db = new DatabaseHandler(getApplicationContext());

	private static String KEY_SUCCESS = "success";
	private static String KEY_GNAME = "groomName";
	private static String KEY_GSURNAME = "groomSurname";
	private static String KEY_BNAME = "brideName";
	private static String KEY_BSURNAME = "brideSurname";
	private static String KEY_WDATE = "weddingdate";
	
	private ProgressDialog pDialog;
	private String errMsg;
	JSONParser jsonParser = new JSONParser();
	private static String URL = "http://wedapp.altervista.org/createList.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_list);
		
		nGroom = (EditText) findViewById(R.id.registerNameGroom);
		sGroom = (EditText) findViewById(R.id.registerSurnameGroom);
		nBride = (EditText) findViewById(R.id.registerNameBride);
		sBride = (EditText) findViewById(R.id.registerSurnameBride);
		wDate = (EditText) findViewById(R.id.registerDate);
		btnAddList = (Button) findViewById(R.id.btnAddList);
		btnGoBack = (Button) findViewById(R.id.btnLinkToHome);
		errorMsg = (TextView) findViewById(R.id.addListError);
		
		btnAddList.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new addList().execute();
			}
		});
	}
	
	class addList extends AsyncTask<String, String, String> {
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(NewListActivity.this, "Loading",
					"Please wait...", true);
        }
		
		protected String doInBackground(String... args) {
			String gName = nGroom.getText().toString();
			String gSurname = sGroom.getText().toString();
			String bName = nBride.getText().toString();
			String bSurname = sBride.getText().toString();
			String date = wDate.getText().toString();
			String mEmail = db.getUserDetails().get("email");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("gName", gName));
			params.add(new BasicNameValuePair("gSurname", gSurname));
			params.add(new BasicNameValuePair("bName", gName));
			params.add(new BasicNameValuePair("ssurname", gSurname));
			params.add(new BasicNameValuePair("date", date));
			params.add(new BasicNameValuePair("mEmail", mEmail));
			JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
			//Andare avanti da qui!!!!!!
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					errMsg = "";
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						JSONArray listObj = json.getJSONArray("code");
						JSONObject json_list = listObj.getJSONObject(0);
						db.addUser(json_list.getString(KEY_EMAIL), json_user.getString(KEY_NAME), json_user.getString(KEY_CITY), json_user.getString(KEY_ADDRESS), json_user.getString(KEY_BUILD), json_user.getString(KEY_PHONE));						
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
						errMsg = "Error occured in adding list";
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_list, menu);
		return true;
	}

}
