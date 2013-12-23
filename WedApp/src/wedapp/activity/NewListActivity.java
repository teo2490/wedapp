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
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewListActivity extends Activity {
	
	Button btnAddList;
	EditText nGroom;
	EditText sGroom;
	EditText nBride;
	EditText sBride;
	DatePicker wDate;
	TextView errorMsg;

	private static String KEY_SUCCESS = "success";
	private boolean flagEmptyFields = false;
	
	private ProgressDialog pDialog;
	private String errMsg;
	private String color;
	JSONParser jsonParser = new JSONParser();
	private static String URL = "http://wedapp.altervista.org/create_list.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_list);
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		nGroom = (EditText) findViewById(R.id.registerNameGroom);
		sGroom = (EditText) findViewById(R.id.registerSurnameGroom);
		nBride = (EditText) findViewById(R.id.registerNameBride);
		sBride = (EditText) findViewById(R.id.registerSurnameBride);
		wDate = (DatePicker) findViewById(R.id.registerDate);
		btnAddList = (Button) findViewById(R.id.btnAddList);
		errorMsg = (TextView) findViewById(R.id.addListMessage);
		
		btnAddList.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View view) {
				new addList().execute();
				if(flagEmptyFields){
					btnAddList.setClickable(false);
					btnAddList.setEnabled(false);
					btnAddList.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_color));
				}
			}
		});
	}
	
	class addList extends AsyncTask<String, String, String> {
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(NewListActivity.this, getString(R.string.Loading),
					getString(R.string.PleaseWait), true);
        }
		
		protected String doInBackground(String... args) {
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			db.createLogin();
			String gName = nGroom.getText().toString();
			String gSurname = sGroom.getText().toString();
			String bName = nBride.getText().toString();
			String bSurname = sBride.getText().toString();
			int day = wDate.getDayOfMonth();
			int month = wDate.getMonth() + 1; //Perché i mesi partono da 0!!
			int year = wDate.getYear();
			String date = year+"-"+month+"-"+day;
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
						flagEmptyFields = true;
						errMsg = getString(R.string.successCreationNewList)+" "+codeList;
						color = "#fc6c85";
						pDialog.dismiss();
					}else{
						// Error in registration
						flagEmptyFields = false;
						errMsg = getString(R.string.errorCreationNewList);
						color = "#e52b50";
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			runOnUiThread(new Runnable() {
				public void run() {
					// display errMsg in TextView
					if(flagEmptyFields){
						nGroom.setText("");
						sGroom.setText("");
						nBride.setText("");
						sBride.setText("");
						//wDate.setText("");
					}
					errorMsg.setText(errMsg);
					errorMsg.setTextColor(Color.parseColor(color));
					pDialog.dismiss();
				}
			});
			return null;
		}
		
		protected void onPostExecute() {
    		// dismiss the dialog once done
    		pDialog.dismiss();
		}
	}
	
	public void onBackPressed() {
		Intent back = new Intent(getApplicationContext(), DashboardActivity.class);
    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(back);
    	finish();
    	return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_list, menu);
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
