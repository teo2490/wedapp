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
import dima.wedapp.R.layout;
import dima.wedapp.R.menu;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductDetailActivity extends Activity {
	
	TextView txtName;
	TextView txtPrice;
	TextView txtMerchant;
	TextView txtEmail;
	TextView txtPhone;
	Button btnDrive;
	Button btnReserve;
	ImageView imgPhoto;

	//Strings used to get result from AsyncTask in order to update UI
	private String name;
	private String price;
	private String mname;
	private String email;
	private String phone;
	private String address;
	
	private String pid;

	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single product url
	private static final String url_product_details = "http://wedapp.altervista.org/get_gift_details.php";
	
	private static final String url_merchant_details = "http://wedapp.altervista.org/get_merchant_details.php";


	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "gift";
	private static final String TAG_PID = "pid";
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "price";
	private static final String TAG_PHOTO = "photo";
	private static final String TAG_EMAIL = "m_email";
	private static final String TAG_MNAME = "name";
	private static final String TAG_PHONE = "phone";
	private static final String TAG_MERCHANT = "merchant";
	private static final String TAG_CITY = "city";
	private static final String TAG_ADDR = "address";
	private static final String TAG_BUILD = "build_number";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_detail);
		
		btnReserve = (Button) findViewById(R.id.buttonReserve);
		btnDrive = (Button) findViewById(R.id.buttonDrive);
		
		btnDrive.setOnClickListener(new View.OnClickListener() {
        	@Override
			public void onClick(View v) {
        		//Per ottenere la stringa si concatenano tutti i dati sull'indirizzo presenti nel DB con dei + in mezzo
        		//e si usa il metodo replaceAll("a","b") per togliere spazi e mettere +
        		//String t = address.replaceAll(" ", "+");
        		String uri = "geo:0,0?q="+address;
        		//controllo se Google Maps e' installato
        		if(isGoogleMapsInstalled())
        	    {
        			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        			intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        			startActivity(intent);
        	    }else{
        	    	Toast.makeText(getApplicationContext(),"Installa Google Maps, per favore!",Toast.LENGTH_LONG).show();
        	    }
        	}
        	});
				
				/* DA ATTIVARE QUANDO CI SARA' L'ACTIVITY PRECEDENTE (LISTA)
				// getting product details from intent
				Intent i = getIntent();
				
				// getting product id (pid) from intent
				pid = i.getStringExtra(TAG_PID);
				*/
				pid = "7";

				// Getting complete product details in background thread
				new GetProductDetails().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_detail, menu);
		return true;
	}
	
	/**
	 * Background Async Task to Get complete product details
	 * */
	class GetProductDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(ProductDetailActivity.this, "Loading",
					"Please wait...", true);
		}

		/**
		 * Getting product details in background thread
		 * */
		protected String doInBackground(String... params) {

		
					// Check for success tag
					int success;
					int msuccess;
					try {
						// Building Parameters
						List<NameValuePair> params1 = new ArrayList<NameValuePair>();
						params1.add(new BasicNameValuePair(TAG_PID, pid));

						// getting product details by making HTTP request
						// Note that product details url will use GET request
						JSONObject json = jsonParser.makeHttpRequest(
								url_product_details, "POST", params1);

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

							// product with this pid found
							txtName = (TextView) findViewById(R.id.textName);
							txtPrice = (TextView) findViewById(R.id.textPrice);
							txtMerchant = (TextView) findViewById(R.id.textMerchant);
							txtEmail = (TextView) findViewById(R.id.textEmail);
							txtPhone = (TextView) findViewById(R.id.textPhone);
							imgPhoto = (ImageView) findViewById(R.id.imageView1);
							
							System.out.println(product.getString(TAG_NAME));

							name = product.getString(TAG_NAME);
							price = product.getString(TAG_PRICE);
							
							// Building Parameters Merchant
							List<NameValuePair> mparams = new ArrayList<NameValuePair>();
							mparams.add(new BasicNameValuePair("m_email", product.getString(TAG_EMAIL)));

							// getting product details by making HTTP request
							// Note that product details url will use GET request
							JSONObject jsonm = jsonParser.makeHttpRequest(
									url_merchant_details, "POST", mparams);

							// check your log for json response
							Log.d("Single Merchant Details", jsonm.toString());
							
							// json success tag
							msuccess = jsonm.getInt(TAG_SUCCESS);
							if (msuccess == 1) {
								// successfully received merchant details
								JSONArray merchantObj = jsonm
										.getJSONArray(TAG_MERCHANT); // JSON Array
								
								// get first merchant object from JSON Array
								JSONObject merchant = merchantObj.getJSONObject(0);
							
								mname = merchant.getString(TAG_MNAME);
								email = product.getString(TAG_EMAIL);
								phone = merchant.getString(TAG_PHONE);
								
								String city = merchant.getString(TAG_CITY);
								String addr = merchant.getString(TAG_ADDR);
								String build = merchant.getString(TAG_BUILD);
								
								address = city+"+"+addr+"+"+build;
							}

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
							txtName.setText(name);
							txtPrice.setText(price);
							//display merchant data in EditText
							txtMerchant.setText(mname);
							txtEmail.setText(email);
							txtPhone.setText(phone);
							Linkify.addLinks(txtPhone, Linkify.PHONE_NUMBERS);
				}
			});

			return null;
		}


		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once got all details
			pDialog.dismiss();
		}
	}
	
	//Controlla che Google Maps sia installato.
    public boolean isGoogleMapsInstalled()
    {
        try
        {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return true;
        } 
        catch(PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }


}
