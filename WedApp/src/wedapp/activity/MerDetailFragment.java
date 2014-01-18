package wedapp.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.activity.ReservationActivity;
import wedapp.library.JSONParser;
import dima.wedapp.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment that show the details of a gift for a merchant.
 * 
 * @author Matteo
 */
public class MerDetailFragment extends Fragment {
	
	//DA QUI
	Bitmap image;
	
	//Strings used to get result from AsyncTask in order to update UI
	private String name;
	private String price;
	private String mname;
	private String email;
	private String phone;
	private String address;
	private String photo;
	
	private String pid;
	
	private String message;

	// Progress Dialog
	private ProgressDialog pDialog;
	private boolean isLastThread = false;
	
	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single product url
	private static final String url_product_details = "http://wedapp.altervista.org/get_gift_details.php";
	private static final String url_delete_product = "http://wedapp.altervista.org/delete_gift.php";
	


	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "gift";
	private static final String TAG_PID = "pid";
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "price";
	private static final String TAG_PHOTO = "photo";
	private static final String TAG_ID = "id";
        /**
         * Chiave per l'argomento che viene passato.
         */
        public static final String ARGUMENT_ITEM = "ITEM";
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                super.onCreateView(inflater, container, savedInstanceState);
                View view = inflater.inflate(R.layout.activity_mer_detail_fragment,
                        container, false);
                
				//txtPrice.setText(price);
				//display merchant data in EditText
//				txtMerchant.setText(mname);
//				txtEmail.setText(email);
//				txtPhone.setText(phone);
//				Linkify.addLinks(txtPhone, Linkify.PHONE_NUMBERS);
                
               
				
                return view;
        }
        
        /**
         * Once the activity is created, it is shown the UI and is started the asynctask in order to get the 
         * details of the gift
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                Bundle bundle = this.getArguments();
                pid = bundle.getString(ARGUMENT_ITEM);
				//pid = "7";
                Button btnEdit = (Button) getView().findViewById(R.id.buttonEdit);
        		Button btnDelete = (Button) getView().findViewById(R.id.buttonDelete);
        		
        		btnEdit.setOnClickListener(new View.OnClickListener() {
                	@Override
        			public void onClick(View v) {	
                		Intent i = new Intent(getActivity().getApplicationContext(), UpdateProduct.class);
                		i.putExtra(TAG_PID, pid);
                		startActivity(i);
                	}
                	});
        		
        		btnDelete.setOnClickListener(new View.OnClickListener() {
                	@Override
        			public void onClick(View v) {
                		new DeleteGift().execute();
                	}
                	});
				new GetProductDetails().execute();
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
    			pDialog = ProgressDialog.show(getActivity(), getString(R.string.Loading),
    					getString(R.string.PleaseWait), true);    			
    		}

    		/**
    		 * Getting product details in background thread and startin the asynctask in order to download the image
    		 * of the gift
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

    							
    							
    							System.out.println(product.getString(TAG_NAME));

    							name = product.getString(TAG_NAME);
    							price = product.getString(TAG_PRICE);
    							photo = product.getString(TAG_PHOTO);
    							
    							new DownloadImage().execute();
    						}else{
    							// product with pid not found
    						}
    					} catch (JSONException e) {
    						e.printStackTrace();
    					}
    					

    			return null;
    		}


    		/**
    		 * After completing background task (if this is the last tag active) dismiss the progress dialog
    		 * 
    		 */
    		protected void onPostExecute(String file_url) {
    			if(isLastThread){
    			pDialog.dismiss();
    			}else{
    				isLastThread = true;
    			}
    			TextView txtName = (TextView) getView().findViewById(R.id.textName);
                TextView txtPrice = (TextView) getView().findViewById(R.id.textPrice);
				
				txtName.setText(name);
				txtPrice.setText(price);
    		}
    	}
    	
    	

        /**
         * Background asyncTask for downloadin the image of a gift from the server
         * @author Matteo
         *
         */
        class DownloadImage extends AsyncTask<Void,Void,Void>
        {

            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();
                
            }

            /**
             * Downloading the image from the server
             */
            @Override
            protected Void doInBackground(Void... params) {
                // TODO Auto-generated method stub
                try
                {
                //URL url = new URL( "http://a3.twimg.com/profile_images/670625317/aam-logo-v3-twitter.png");
                String dwn = "http://wedapp.altervista.org/"+photo+".bmp";
                image = downloadBitmap(dwn);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            /**
    		 * After completing background task (if this is the last tag active) dismiss the progress dialog
    		 * 
    		 */
            @Override
            protected void onPostExecute(Void result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                if(image!=null)
                {
                    //imgPhoto.setImageBitmap(image);
                }
             // dismiss the dialog once got all details
                if(isLastThread){
                	pDialog.dismiss();
                }else{
                	isLastThread = true;
                }
             			
             			ImageView imgPhoto = (ImageView) getView().findViewById(R.id.imageView1);
             			imgPhoto.setImageBitmap(image);
            }   
            
            
        }
        
        /**
         * This method get the stream from HTTP and converti it in Bitmap format
         * @param url
         * @return The bitmap image of a gift
         */
         private Bitmap downloadBitmap(String url) {
             // initilize the default HTTP client object
             final DefaultHttpClient client = new DefaultHttpClient();

             //forming a HttoGet request 
             final HttpGet getRequest = new HttpGet(url);
             try {

                 HttpResponse response = client.execute(getRequest);

                 //check 200 OK for success
                 final int statusCode = response.getStatusLine().getStatusCode();

                 if (statusCode != HttpStatus.SC_OK) {
                     Log.w("ImageDownloader", "Error " + statusCode + 
                             " while retrieving bitmap from " + url);
                     return null;

                 }

                 final HttpEntity entity = response.getEntity();
                 if (entity != null) {
                     InputStream inputStream = null;
                     try {
                         // getting contents from the stream 
                         inputStream = entity.getContent();

                         // decoding stream data back into image Bitmap that android understands
                         image = BitmapFactory.decodeStream(inputStream);


                     } finally {
                         if (inputStream != null) {
                             inputStream.close();
                         }
                         entity.consumeContent();
                     }
                 }
             } catch (Exception e) {
                 // You Could provide a more explicit error message for IOException
                 getRequest.abort();
                 Log.e("ImageDownloader", "Something went wrong while" +
                         " retrieving bitmap from " + url + e.toString());
             } 

             return image;
         }
         
         /**
     	 * Background Async Task to Delete a Gift
     	 * */
     	class DeleteGift extends AsyncTask<String, String, String> {

     		/**
     		 * Before starting background thread Show Progress Dialog
     		 * */
     		@Override
     		protected void onPreExecute() {
     			super.onPreExecute();
     			pDialog = ProgressDialog.show(getActivity(), getString(R.string.Loading),
     					getString(R.string.PleaseWait), true);    			
     		}

     		/**
     		 * It prepares the request to the server in order to delete a gift and it handles the response
     		 */
     		protected String doInBackground(String... params) {

     		
     					// Check for success tag
     					int success;
     				
     					try {
     						// Building Parameters
     						List<NameValuePair> params1 = new ArrayList<NameValuePair>();
     						params1.add(new BasicNameValuePair(TAG_ID, pid));

     						// getting product details by making HTTP request
     						// Note that product details url will use GET request
     						JSONObject json = jsonParser.makeHttpRequest(
     								url_delete_product, "POST", params1);

     						// check your log for json response
     						Log.d("Single Product Details", json.toString());
     						
     						// json success tag
     						success = json.getInt(TAG_SUCCESS);
     						if (success == 1) {
     							Intent i = new Intent(getActivity().getApplicationContext(), MerListActivity.class);
     							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     							message =  getString(R.string.suc_delete);
     							startActivity(i);
     							getActivity().finish();
     						}else{
     							// product with pid not found
     							message =  getString(R.string.fail_delete);
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
     			Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();

     		}
     	}
         
     	/**
     	 * This method checks if Google Maps App is installed
     	 * @return true if it is installed, false otherwise
     	 */
         public boolean isGoogleMapsInstalled()
         {
             try
             {
                 ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
                 return true;
             } 
             catch(PackageManager.NameNotFoundException e)
             {
                 return false;
             }
         }


}
