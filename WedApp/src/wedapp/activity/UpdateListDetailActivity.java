package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UpdateListDetailActivity extends Activity {

	Button btnAddList;
	EditText nGroom;
	EditText sGroom;
	EditText nBride;
	EditText sBride;
	DatePicker wDate;
	TextView errorMsg;
	
	private String nG;
	private String sG;
	private String nB;
	private String sB;
	private String wD;
	private int oldDay;
	private int oldMonth;
	private int oldYear;

	private static String KEY_SUCCESS = "success";
	private static final String TAG_NGROOM = "n_groom";
	private static final String TAG_SGROOM = "s_groom";
	private static final String TAG_NBRIDE = "n_bride";
	private static final String TAG_SBRIDE = "s_bride";
	private static final String TAG_WDATE = "w_date";
	private static final String TAG_PID = "pid";
	
	private ProgressDialog pDialog;
	private String errMsg;
	private String pid;
	JSONParser jsonParser = new JSONParser();
	private static String url_update_list_details = "http://wedapp.altervista.org/update_list.php";
	private static String url_get_list_details = "http://wedapp.altervista.org/get_list_details.php";

		// JSON Node names
		private static final String TAG_SUCCESS = "success";
		private static final String TAG_PRODUCT = "list";

	/**
	 * On creation kind of device is checked and the orientation is set.
	 * EditText, Textiew and Button are placed.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_list_detail);
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		db.createList();
		pid = db.getListId();
		
		nGroom = (EditText) findViewById(R.id.registerNameGroom);
		sGroom = (EditText) findViewById(R.id.registerSurnameGroom);
		nBride = (EditText) findViewById(R.id.registerNameBride);
		sBride = (EditText) findViewById(R.id.registerSurnameBride);
		wDate = (DatePicker) findViewById(R.id.registerDate);
		btnAddList = (Button) findViewById(R.id.btnUpListDetails);
		errorMsg = (TextView) findViewById(R.id.addListMessage);
		
		btnAddList.setOnClickListener(new View.OnClickListener() {
			/**
			 * It starts the update list phase
			 */
			@SuppressWarnings("deprecation")
			public void onClick(View view) {
				new upList().execute();
			}
		});
		
		new GetListDetails().execute();
	}
	
	class upList extends AsyncTask<String, String, String> {
		
		/**
		 * Progress dialog is shown
		 */
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(UpdateListDetailActivity.this, getString(R.string.Loading),
					getString(R.string.PleaseWait), true);
        }
		
		/**
		 * List is updated in background
		 */
		protected String doInBackground(String... args) {
			String gName = nGroom.getText().toString();
			String gSurname = sGroom.getText().toString();
			String bName = nBride.getText().toString();
			String bSurname = sBride.getText().toString();
			int day = wDate.getDayOfMonth();
			int month = wDate.getMonth() + 1; //Perché i mesi sono contati da 0!
			int year = wDate.getYear();
			String date = year+"-"+month+"-"+day;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("pid", pid));
			params.add(new BasicNameValuePair("n_groom", gName));
			params.add(new BasicNameValuePair("s_groom", gSurname));
			params.add(new BasicNameValuePair("n_bride", bName));
			params.add(new BasicNameValuePair("s_bride", bSurname));
			params.add(new BasicNameValuePair("w_date", date));
			JSONObject json = jsonParser.makeHttpRequest(url_update_list_details, "POST", params);
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					String res = json.getString(KEY_SUCCESS); 
					if(Integer.parseInt(res) == 1){
						errMsg = getString(R.string.successUpdateList);
						pDialog.dismiss();
					}else{
						// Error
						errMsg = getString(R.string.errorUpdateList);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			runOnUiThread(new Runnable() {
				public void run() {
					pDialog.dismiss();
					Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG).show();
				}
			});
			return null;
		}
		
		/**
		 * Progress dialog is dismissed
		 */
		protected void onPostExecute() {
    		// dismiss the dialog once done
    		pDialog.dismiss();
		}
	}

	class GetListDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(UpdateListDetailActivity.this, getString(R.string.Loading),
					getString(R.string.PleaseWait), true);
		}

		/**
		 * Product details are retrieved in background
		 */
		protected String doInBackground(String... params) {

		
					// Check for success tag
					int success;
					try {
						// Building Parameters
						List<NameValuePair> params1 = new ArrayList<NameValuePair>();
						params1.add(new BasicNameValuePair(TAG_PID, pid));

						// getting product details by making HTTP request
						// Note that product details url will use GET request
						JSONObject json = jsonParser.makeHttpRequest(
								url_get_list_details, "POST", params1);

						// check your log for json response
						Log.d("Single Product Details", json.toString());
						
						// json success tag
						success = json.getInt(TAG_SUCCESS);
						if (success == 1) {
							// successfully received product details
							JSONArray productObj = json
									.getJSONArray(TAG_PRODUCT); // JSON Array
							
							// get first product object from JSON Array
							JSONObject product = productObj.getJSONObject(0);

							nG = product.getString(TAG_NGROOM);
							sG = product.getString(TAG_SGROOM);
							nB = product.getString(TAG_NBRIDE);
							sB = product.getString(TAG_SBRIDE);
							wD = product.getString(TAG_WDATE);
							String tokens[] = new String[3];
							tokens = wD.split("-");
							oldYear = Integer.parseInt(tokens[0]);
							oldMonth = Integer.parseInt(tokens[1])-1; //Meno 1 perché conta da 0!!!
							oldDay = Integer.parseInt(tokens[2]);
						}else{
							// product with pid not found
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					// updating UI from Background Thread
					runOnUiThread(new Runnable() {
						public void run() {
							// display product data in EditText
							nGroom.setText(nG);
							sGroom.setText(sG);
							nBride.setText(nB);
							sBride.setText(sB);
							wDate.updateDate(oldYear, oldMonth, oldDay);
				}
			});

			return null;
		}
		/**
		 * After completing background task Dismiss the progress dialog
		 */
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
		}
	}
	
	/**
	 * Back button is set to go to the previous Activity
	 */
	public void onBackPressed() {
		Intent back = new Intent(getApplicationContext(), MerListActivity.class);
    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(back);
    	finish();
    	return;
	}
	
	/**
	 * Options menu is created
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_list_detail, menu);
		return true;
	}

	/**
	 * Logout and Home button are placed in menu
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
            case android.R.id.home:
				Intent back = new Intent(getApplicationContext(), MerListActivity.class);
	        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(back);
	        	finish();
                return true;
	        	
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
