package wedapp.activity;

import java.sql.Date;
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
	Button btnLinkToHome;
	EditText nGroom;
	EditText sGroom;
	EditText nBride;
	EditText sBride;
	EditText wDate;
	TextView errorMsg;

	private static String KEY_SUCCESS = "success";
	
	private ProgressDialog pDialog;
	private String errMsg;
	JSONParser jsonParser = new JSONParser();
	private static String URL = "http://wedapp.altervista.org/create_list.php";

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
		btnLinkToHome = (Button) findViewById(R.id.btnLinkToHome);
		errorMsg = (TextView) findViewById(R.id.addListMessage);
		
		btnAddList.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new addList().execute();
			}
		});
		
		btnLinkToHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
				startActivity(i);
				// Close Registration View
				finish();
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
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			String gName = nGroom.getText().toString();
			String gSurname = sGroom.getText().toString();
			String bName = nBride.getText().toString();
			String bSurname = sBride.getText().toString();
			String date = wDate.getText().toString();
			String mEmail = db.getUserDetails().get("email");
			/*System.out.println(gName);
			System.out.println(gSurname);
			System.out.println(bName);
			System.out.println(bSurname);
			System.out.println(date);
			System.out.println(mEmail);*/
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("n_groom", gName));
			params.add(new BasicNameValuePair("s_groom", gSurname));
			params.add(new BasicNameValuePair("n_bride", bName));
			params.add(new BasicNameValuePair("s_bride", bSurname));
			params.add(new BasicNameValuePair("w_date", date));
			params.add(new BasicNameValuePair("m_email", mEmail));
			JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						int codeList = json.getInt("list");
						errMsg = "List correctly created! The list code is: " + codeList;
						pDialog.dismiss();
					}else{
						// Error in registration
						errMsg = "Error occured in adding list";
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			runOnUiThread(new Runnable() {
				public void run() {
					// display errMsg in TextView
					pDialog.dismiss();
					errorMsg.setText(errMsg);
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
		getMenuInflater().inflate(R.menu.new_list, menu);
		return true;
	}

}
