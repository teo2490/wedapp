package wedapp.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.activity.ProductDetailActivity.DownloadImage;
import wedapp.library.JSONParser;

import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import dima.wedapp.R;
import dima.wedapp.R.layout;
import dima.wedapp.R.menu;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ReservationActivity extends Activity {

	// set to PaymentActivity.ENVIRONMENT_LIVE to move real money.
    // set to PaymentActivity.ENVIRONMENT_SANDBOX to use your test credentials from https://developer.paypal.com
    // set to PaymentActivity.ENVIRONMENT_NO_NETWORK to kick the tires without communicating to PayPal's servers.
    private static final String CONFIG_ENVIRONMENT = PaymentActivity.ENVIRONMENT_SANDBOX;
    
    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "AUK5NxC1PzfUyaNPB9N5WwcG4TWnwSiWpeZKmj-56VaxsO0so9QkvMRyBsVS";
    // when testing in sandbox, this is likely the -facilitator email address. EMAIL DI CHI RICEVE IL PAGAMENTO !!
    private static final String CONFIG_RECEIVER_EMAIL = "wedmerchant@gmail.com"; 
    
    private static final String TAG_PID = "pid";
    private static final String TAG_SUCCESS = "success";
    private String gid;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    EditText inputName;
    EditText inputSurname;
    EditText inputEmail;
    
 // url to add a reservation
    private static String url_add_reservation = "http://wedapp.altervista.org/add_reservation.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        
        Intent i = getIntent();
        gid = i.getStringExtra(TAG_PID);
        System.out.println(gid);
        
        // Edit Text
        inputName = (EditText) findViewById(R.id.inputName);
        inputSurname = (EditText) findViewById(R.id.inputSurname);
        inputEmail = (EditText) findViewById(R.id.inputEmail);
        
        Intent intent = new Intent(this, PayPalService.class);
        
        intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, CONFIG_ENVIRONMENT);
        intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
        intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL, CONFIG_RECEIVER_EMAIL);
        
        startService(intent);
    }

    public void onBuyPressed(View pressed) {
        PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal("1.00"), "USD", "Gift Reservation");
        
        Intent intent = new Intent(this, PaymentActivity.class);
        
        intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, CONFIG_ENVIRONMENT);
        intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
        intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL, CONFIG_RECEIVER_EMAIL);
        
        // It's important to repeat the clientId here so that the SDK has it if Android restarts your 
        // app midway through the payment UI flow.
        intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "AUK5NxC1PzfUyaNPB9N5WwcG4TWnwSiWpeZKmj-56VaxsO0so9QkvMRyBsVS");
        //In EXTRA_PAYER_ID ci va email di chi compra!
        intent.putExtra(PaymentActivity.EXTRA_PAYER_ID, "wedbuyer@gmail.com");
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        
        startActivityForResult(intent, 0);
    }
    
    public void onFinishPressed(View pressed) {
    	finish();
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample", confirm.toJSONObject().toString(4));
                    System.out.println(confirm.toJSONObject().toString(4));
                    Toast.makeText(getApplicationContext(), "Pagamento completato", Toast.LENGTH_LONG).show();

                    // TODO: send 'confirm' to your server for verification.
                    // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                    // for more details.
                    //Aggiungere una riga nella tabella delle reservation
                    new AddReservation().execute();

                } catch (JSONException e) {
                    Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    Toast.makeText(getApplicationContext(), "ERRORE!", Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
            Toast.makeText(getApplicationContext(), "Pagamento cancellato", Toast.LENGTH_LONG).show();
        }
        else if (resultCode == PaymentActivity.RESULT_PAYMENT_INVALID) {
            Log.i("paymentExample", "An invalid payment was submitted. Please see the docs.");
            Toast.makeText(getApplicationContext(), "ERRORE!", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reservation, menu);
		return true;
	}
	
	/**
	 * Background Async Task to Get complete product details
	 * */
	class AddReservation extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(ReservationActivity.this, "Loading",
					"Please wait...", true);
		}

		/**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String name = inputName.getText().toString();
            String surname = inputSurname.getText().toString();
            String email = inputEmail.getText().toString();
 
            System.out.println(gid);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("surname", surname));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("id_gift", gid));
             
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_add_reservation,
                    "POST", params);
                        
            // check log cat for response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                	//Toast.makeText(getApplicationContext(), "Reservation Confirmed", Toast.LENGTH_LONG).show();
                    // closing this screen
                    finish();
                } else {
                	//Toast.makeText(getApplicationContext(), "Reservation Failed", Toast.LENGTH_LONG).show();
                    // failed to add reservation
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
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