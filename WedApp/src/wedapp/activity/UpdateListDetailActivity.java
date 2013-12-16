package wedapp.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.library.JSONParser;
import dima.wedapp.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UpdateListDetailActivity extends Activity {

	Button btnAddList;
	Button btnLinkToHome;
	EditText nGroom;
	EditText sGroom;
	EditText nBride;
	EditText sBride;
	EditText wDate;
	TextView errorMsg;
	
	private String nG;
	private String sG;
	private String nB;
	private String sB;
	private String wdate;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_list_detail);
		
		Intent i = getIntent();
		pid = i.getStringExtra("pid");
		
		nGroom = (EditText) findViewById(R.id.registerNameGroom);
		sGroom = (EditText) findViewById(R.id.registerSurnameGroom);
		nBride = (EditText) findViewById(R.id.registerNameBride);
		sBride = (EditText) findViewById(R.id.registerSurnameBride);
		wDate = (EditText) findViewById(R.id.registerDate);
		btnAddList = (Button) findViewById(R.id.btnUpListDetails);
		btnLinkToHome = (Button) findViewById(R.id.btnLinkToHome);
		errorMsg = (TextView) findViewById(R.id.addListMessage);
		
		btnAddList.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View view) {
				new upList().execute();
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
		
		new GetListDetails().execute();
	}
	
	class upList extends AsyncTask<String, String, String> {
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(UpdateListDetailActivity.this, "Loading",
					"Please wait...", true);
        }
		
		protected String doInBackground(String... args) {
			String gName = nGroom.getText().toString();
			String gSurname = sGroom.getText().toString();
			String bName = nBride.getText().toString();
			String bSurname = sBride.getText().toString();
			String date = wDate.getText().toString();
			/*System.out.println(gName);
			System.out.println(gSurname);
			System.out.println(bName);
			System.out.println(bSurname);
			System.out.println(date);
			System.out.println(mEmail);*/
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
						errMsg = "List correctly updated!";
						pDialog.dismiss();
					}else{
						// Error
						errMsg = "Error occured in updating list";
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			runOnUiThread(new Runnable() {
				public void run() {
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

	/**
	 * Background Async Task to Get complete product details
	 * */
	class GetListDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(UpdateListDetailActivity.this, "Loading",
					"Please wait...", true);
		}

		/**
		 * Getting product details in background thread
		 * */
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
							wdate = product.getString(TAG_WDATE);
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
							wDate.setText(wdate);
				}
			});

			return null;
		}
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
		}
	}

}
