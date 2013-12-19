package wedapp.activity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dima.wedapp.R;
import dima.wedapp.R.id;
import dima.wedapp.R.layout;

import wedapp.library.Base64;
import wedapp.library.JSONParser;
import wedapp.library.UserFunctions;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NavUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UpdateProduct extends Activity {
	
    EditText inputName;
    EditText inputPrice;
    ImageView imgView;
    Button btnPhoto;
    Button btnCreateProduct;

	Bitmap bitmap;
	
	//Strings used to get result from AsyncTask in order to update UI
	private String name;
	private String price;
	private String email;
	private String photo;
	
	private String pid;
	
	Uri imageUri;
	
	private static final int PICK_Camera_IMAGE = 2;

	// Progress Dialog
	private ProgressDialog pDialog;
	private boolean isLastThread = false;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single product url
	private static final String url_product_details = "http://wedapp.altervista.org/get_gift_details.php";
	private static final String url_update_details = "http://wedapp.altervista.org/update_gift.php";


	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "gift";
	private static final String TAG_PID = "pid";
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "price";
	private static final String TAG_PHOTO = "photo";
	private static final String TAG_EMAIL = "m_email";
	private static final String TAG_MERCHANT = "merchant";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_product);
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
       	   getActionBar().setDisplayHomeAsUpEnabled(true);
       	}
		
		 // Edit Text
        inputName = (EditText) findViewById(R.id.inputName);
        inputPrice = (EditText) findViewById(R.id.inputPrice);
        imgView = (ImageView) findViewById(R.id.ImageView);
		btnPhoto = (Button) findViewById(R.id.imguploadbtn);
		
		Intent i = getIntent();
		pid = i.getStringExtra("pid");
		
		btnPhoto.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//define the file-name to save photo taken by Camera activity
	        	String fileName = "new-photo-name.jpg";
	        	//create parameters for Intent with filename
	        	ContentValues values = new ContentValues();
	        	values.put(MediaStore.Images.Media.TITLE, fileName);
	        	values.put(MediaStore.Images.Media.DESCRIPTION,"Image captured by camera");
	        	//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
	        	imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	        	//create new Intent
	        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
	        	intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
	        	startActivityForResult(intent, PICK_Camera_IMAGE);
			}
		});
 
        // Create button
        Button btnCreateProduct = (Button) findViewById(R.id.btnCreateProduct);
 
        // button click event
        btnCreateProduct.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View view) {
            	if (bitmap == null) {
					Toast.makeText(getApplicationContext(),
							"Please select image", Toast.LENGTH_SHORT).show();
				} else {
					pDialog = ProgressDialog.show(UpdateProduct.this, "Uploading",
							"Please wait...", true);
					//Create the name of the photo by the timestamp MANCA LA CONCATENAZIONE CON ID COMMERCIANTE!
					Long tsLong = System.currentTimeMillis()/1000;
					photo = tsLong.toString();
					// Uploading photo in background thread
					new ImageGalleryTask().execute();
					// creating new product in background thread
	                new UpdateProductTask().execute();
				}
            }
        });
        
        new GetProductDetails().execute();
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri selectedImageUri = null;
		String filePath = null;
					 if (resultCode == RESULT_OK) {
		 		        //use imageUri here to access the image
		 		    	selectedImageUri = imageUri;
		 		    	/*Bitmap mPic = (Bitmap) data.getExtras().get("data");
						selectedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), mPic, getResources().getString(R.string.app_name), Long.toString(System.currentTimeMillis())));*/
				    } else if (resultCode == RESULT_CANCELED) {
		 		        Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
		 		    } else {
		 		    	Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
			}
		
			if(selectedImageUri != null){
					try {
						// OI FILE Manager
						String filemanagerstring = selectedImageUri.getPath();
			
						// MEDIA GALLERY
						String selectedImagePath = getPath(selectedImageUri);
			
						if (selectedImagePath != null) {
							filePath = selectedImagePath;
						} else if (filemanagerstring != null) {
							filePath = filemanagerstring;
						} else {
							Toast.makeText(getApplicationContext(), "Unknown path",
									Toast.LENGTH_LONG).show();
							Log.e("Bitmap", "Unknown path");
						}
			
						if (filePath != null) {
							decodeFile(filePath);
						} else {
							bitmap = null;
						}
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), "Internal error",
								Toast.LENGTH_LONG).show();
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
			}
	
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
			pDialog = ProgressDialog.show(UpdateProduct.this, "Loading",
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
					
					// updating UI from Background Thread
					runOnUiThread(new Runnable() {
						public void run() {
							// display product data in EditText
							inputName.setText(name);
							inputPrice.setText(price);
				}
			});

			return null;
		}
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			
		}
	}
	
	//AsyncTask for dowloading photos of a product
    class DownloadImage extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            
        }


        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try
            {
            //URL url = new URL( "http://a3.twimg.com/profile_images/670625317/aam-logo-v3-twitter.png");
            String dwn = "http://wedapp.altervista.org/"+photo+".bmp";
            bitmap = downloadBitmap(dwn);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(bitmap!=null)
            {
                imgView.setImageBitmap(bitmap);
            }
         // dismiss the dialog once got all details
         			pDialog.dismiss();
        }   
    }
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
                     bitmap = BitmapFactory.decodeStream(inputStream);


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

         return bitmap;
     }
     
     @SuppressWarnings("deprecation")
 	public String getPath(Uri uri) {
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		if (cursor != null) {
 			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
 			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
 			int column_index = cursor
 					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 			cursor.moveToFirst();
 			return cursor.getString(column_index);
 		} else
 			return null;
 	}
    public void decodeFile(String filePath) {
 		// Decode image size
 		BitmapFactory.Options o = new BitmapFactory.Options();
 		o.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(filePath, o);

 		// The new size we want to scale to
 		final int REQUIRED_SIZE = 1024;

 		// Find the correct scale value. It should be the power of 2.
 		int width_tmp = o.outWidth, height_tmp = o.outHeight;
 		int scale = 1;
 		while (true) {
 			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
 				break;
 			width_tmp /= 2;
 			height_tmp /= 2;
 			scale *= 2;
 		}

 		// Decode with inSampleSize
 		BitmapFactory.Options o2 = new BitmapFactory.Options();
 		o2.inSampleSize = scale;
 		bitmap = BitmapFactory.decodeFile(filePath, o2);

 		imgView.setImageBitmap(bitmap);

 	}
    
    class ImageGalleryTask extends AsyncTask<Void, Void, String> {
		@SuppressWarnings("unused")
		@Override
		protected String doInBackground(Void... unsued) {
				InputStream is;
			    BitmapFactory.Options bfo;
			    Bitmap bitmapOrg;
			    ByteArrayOutputStream bao ;
			   
			    bfo = new BitmapFactory.Options();
			    bfo.inSampleSize = 2;
			    //bitmapOrg = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + customImage, bfo);
			      
			    bao = new ByteArrayOutputStream();
			    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
				byte [] ba = bao.toByteArray();
				String ba1 = Base64.encodeBytes(ba);
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("image",ba1));
				//NOME CON TIMESTAMP
				nameValuePairs.add(new BasicNameValuePair("cmd", photo+".bmp"));
				Log.v("log_tag", System.currentTimeMillis()+".jpg");	       
				try{
				        HttpClient httpclient = new DefaultHttpClient();
				        HttpPost httppost = new 
                      //  Here you need to put your server file address - IDcommerciante concatenato a timestamp?
				        HttpPost("http://wedapp.altervista.org/upload_photo.php");
				        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				        HttpResponse response = httpclient.execute(httppost);
				        HttpEntity entity = response.getEntity();
				        is = entity.getContent();
				        Log.v("log_tag", "In the try Loop" );
				   }catch(Exception e){
				        Log.v("log_tag", "Error in http connection "+e.toString());
				   }
			return "Success";
			// (null);
		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}

		@Override
		protected void onPostExecute(String sResponse) {
			if(isLastThread){
        		// dismiss the dialog once done
        		pDialog.dismiss();
        	}
        	else{
        		isLastThread=true;
        	}
        }
	}
    
    /**
     * Background Async Task to Create new product
     * */
    class UpdateProductTask extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*
            pDialog = new ProgressDialog(NewProductActivity.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            */
        }
 
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String name = inputName.getText().toString();
            String price = inputPrice.getText().toString();
            String phName = photo;
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("price", price));
            params.add(new BasicNameValuePair("photo", phName));
            params.add(new BasicNameValuePair("id", pid));
            
             
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_update_details,
                    "POST", params);
                        
            // check log cat for response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // successfully created product MANCA!!
                    //Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    //startActivity(i);
 
                    // closing this screen
                    finish();
                } else {
                    // failed to create product
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
        	if(isLastThread){
        		// dismiss the dialog once done
        		pDialog.dismiss();
        	}
        	else{
        		isLastThread=true;
        	}
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_product, menu);
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
		         NavUtils.navigateUpTo(this,
		               new Intent(this, WedApp.class));
		         return true;
	        	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
 }
