package wedapp.activity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


import dima.wedapp.R;

import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;
import wedapp.library.Base64;
import wedapp.library.UserFunctions;
 
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
 
/**
 * Activity called in order to create a new gift associated to a defined list.
 * 
 * @author Matteo
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewProductActivity extends Activity {
 
    // Progress Dialog
    private ProgressDialog pDialog;
 
    JSONParser jsonParser = new JSONParser();
    EditText inputName;
    EditText inputPrice;
    EditText inputDesc;
    private ImageView imgView;
	private Button btnPhoto;
	private Bitmap bitmap;
	private String photoName;
	Uri imageUri;
	private static final int PICK_Camera_IMAGE = 2;
	private static final String TAG_LID = "id_list";
	private boolean isLastThread = true;
	
	private String memail;
	private String lid;
	//private ProgressDialog dialog;
 
    // url to create new product
    private static String url_create_product = "http://wedapp.altervista.org/create_gift.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
 
    /**
     * At the creation a form for the creation of a new gift is displayed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
 
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db.createList();
        db.createLogin();
        memail = db.getUserDetails().get("email");
        lid = db.getListId();
        // Edit Text
        inputName = (EditText) findViewById(R.id.inputName);
        inputPrice = (EditText) findViewById(R.id.inputPrice);
        imgView = (ImageView) findViewById(R.id.ImageView);
		btnPhoto = (Button) findViewById(R.id.imguploadbtn);
		
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
							getString(R.string.noImage), Toast.LENGTH_SHORT).show();
				} else {
					pDialog = ProgressDialog.show(NewProductActivity.this, getString(R.string.Loading),
							getString(R.string.PleaseWait), true);
					//Create the name of the photo by the timestamp MANCA LA CONCATENAZIONE CON ID COMMERCIANTE!
					Long tsLong = System.currentTimeMillis()/1000;
					photoName = tsLong.toString();
					// Uploading photo in background thread
					new ImageGalleryTask().execute();
					// creating new product in background thread
	                new CreateNewProduct().execute();
				}
            }
        });
    }
    
    /**
     * It chechs the input of the form
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri selectedImageUri = null;
		String filePath = null;
					 if (resultCode == RESULT_OK) {
		 		        //use imageUri here to access the image
		 		    	selectedImageUri = imageUri;
		 		    	/*Bitmap mPic = (Bitmap) data.getExtras().get("data");
						selectedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), mPic, getResources().getString(R.string.app_name), Long.toString(System.currentTimeMillis())));*/
				    } else if (resultCode == RESULT_CANCELED) {
		 		        Toast.makeText(this, getString(R.string.noImage), Toast.LENGTH_SHORT).show();
		 		    } else {
		 		    	Toast.makeText(this, getString(R.string.noImage), Toast.LENGTH_SHORT).show();
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
							Toast.makeText(getApplicationContext(), getString(R.string.unknownPath),
									Toast.LENGTH_LONG).show();
							Log.e("Bitmap", "Unknown path");
						}
			
						if (filePath != null) {
							decodeFile(filePath);
						} else {
							bitmap = null;
						}
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), getString(R.string.internalError),
								Toast.LENGTH_LONG).show();
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
			}
	
	}
 
    /**
     * Background Async Task to prepare and send a request in order to create a new gift
     * 
     */
    class CreateNewProduct extends AsyncTask<String, String, String> {
 
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
         * Creating request and handling response
         * */
        protected String doInBackground(String... args) {
            String name = inputName.getText().toString();
            String price = inputPrice.getText().toString();
            String phName = photoName;
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("price", price));
            params.add(new BasicNameValuePair("photo", phName));
            params.add(new BasicNameValuePair("id_list", lid));
            params.add(new BasicNameValuePair("m_email", memail));
             
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                    "POST", params);
                        
            // check log cat for response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // successfully created product MANCA!!
                    Intent i = new Intent(getApplicationContext(), MerListActivity.class);
                    //i.putExtra(TAG_LID, lid);
                    startActivity(i);
 
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
         * After completing background task (if there aren't other background tasks running),
         * dismiss the progress dialog
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
    
    /**
     * Background Async Task to upload the photo of the new gift
     * 
     */
    class ImageGalleryTask extends AsyncTask<Void, Void, String> {
    	
    	/**
         * Uploading photo and handling response
         * 
         * */
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
				nameValuePairs.add(new BasicNameValuePair("cmd", photoName+".bmp"));
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
		
		/**
         * After completing background task (if there aren't other background tasks running),
         * dismiss the progress dialog
         * **/
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
     * Get the path of the taken picture
     * @param uri
     * @return
     */
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

	/**
	 * Gets the stream from the camera and decodes it into a Bitmap image. Then it shows the image in the ImageView
	 * @param filePath
	 */
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
	
	/**
     * By pressing the phisical back button, it will be shown the MerListActivity
     */
	public void onBackPressed() {
		Intent back = new Intent(getApplicationContext(), MerListActivity.class);
    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(back);
    	finish();
    	return;
	}
	
	/**
     * It creates the menu
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_product, menu);
		return true;
	}

	/**
     * By pressing the back button in the action bar, it will be shown the MerListActivity
     * By pressing the Logout button in the action bar, the session is removed and the WedApp activity is shown
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
