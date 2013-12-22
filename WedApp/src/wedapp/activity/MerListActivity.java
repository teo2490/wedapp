package wedapp.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.library.DatabaseHandler;
import wedapp.library.JSONParser;
import wedapp.library.UserFunctions;

import dima.wedapp.R;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity principale.
 * 
 * @author MarcoDuff [url=http://www.marcoduff.com/]MarcoDuff&#39;s Blog[/url]
 */
public class MerListActivity extends FragmentActivity implements MerListFragment.OnMerListFragmentItemClick {

	private ProgressDialog pDialog;
	private static final String TAG_LID = "id_list";
	private String lid;
	private String memail;
	JSONParser jsonParser = new JSONParser();
	private static String URL = "http://wedapp.altervista.org/get_list_details.php";
	private static final String TAG_PID = "pid";
	private static final String TAG_NGROOM = "n_groom";
	private static final String TAG_NBRIDE = "n_bride";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "list";
	
	String ng;
	String nb;
	
	public String getLid(){
		return lid;
	}
	
	public String getMemail(){
		return memail;
	}
	
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                if(getResources().getBoolean(R.bool.portrait_only)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
               	   getActionBar().setDisplayHomeAsUpEnabled(true);
               	}
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                //db.upgradeDatabase();
                //db.resetListTable();
                //Intent i = getIntent();
                lid = db.getListId();
                //lid = i.getStringExtra(TAG_LID);
                db.createLogin();
                db.createList();
                //db.addList(lid);
                System.out.println("QUA "+lid);
                // Mi limito a caricare il layout, � android che inserir� in modo opportuno quello portrait o landscape (o altri!).
                setContentView(R.layout.activity_mer_list);

                memail = db.getUserDetails().get("email");
               
                
                //lid = "1";
		    	new GetListDetails().execute();
                

        }
		
		@Override
		public void onRestart(){
			super.onRestart();
			finish();
			startActivity(getIntent());
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.mer_list, menu);
	        /*MenuItem item = menu.findItem(R.id.actShare);
	        ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();*/
			return true;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		   switch (item.getItemId()) {
		      case android.R.id.home:
					Intent back = new Intent(getApplicationContext(), DashboardActivity.class);
		        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	startActivity(back);
		        	finish();
	                return true;
	                
		      case R.id.actLogout:
		    	    UserFunctions us = new UserFunctions();
					us.logoutMerchant(getApplicationContext());
					Intent choose = new Intent(getApplicationContext(), WedApp.class);
		        	choose.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	startActivity(choose);
		        	finish();
	                return true;
	                
		      case R.id.actUpList:
					Intent update = new Intent(getApplicationContext(), UpdateListDetailActivity.class);
		        	update.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	startActivity(update);
		        	finish();
	                return true;
		      
		      case R.id.actAddGift:
					Intent newProduct = new Intent(getApplicationContext(), NewProductActivity.class);
		        	newProduct.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	startActivity(newProduct);
		        	finish();

		      default:
		    	  return super.onOptionsItemSelected(item);
		   }
		}

        @Override
        public void onClick(String item) {
                // Preparo l'argomento da passare al Fragment o all'Activity. Questo argomento contiene l'oggetto cliccato.
                Bundle arguments = new Bundle();
                arguments.putString(MerDetailFragment.ARGUMENT_ITEM, item);
                arguments.putString(TAG_LID, lid);
                
                // Recupero la vista detailContainer
                View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                        // Non esiste spazio per la visualizzazione del dattagli, quindi ho necessit� di lanciare una nuova activity.
                        // Carico gli arguments nell'intent di chiamata.
                        Intent intent = new Intent(this, MerDetailActivity.class);
                        intent.putExtras(arguments);
                        startActivity(intent);
                }
                else {
                        // Esiste lo spazio, procedo con la creazione del Fragment!
                        MerDetailFragment merDetailFragment = new MerDetailFragment();
                        // Imposto gli argument del fragment.
                        merDetailFragment.setArguments(arguments);
                        
                        // Procedo con la sostituzione del fragment visualizzato.
                        FragmentManager fragmentManager = this.getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                        .replace(R.id.detailContainer, merDetailFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                }
        }
        
        //Non credo serva
        class GetListDetails extends AsyncTask<String, String, String> {

    		@Override
    		protected void onPreExecute() {
    			super.onPreExecute();
    			pDialog = ProgressDialog.show(MerListActivity.this, getString(R.string.Loading),
    					getString(R.string.PleaseWait), true);
    		}
    		
    		protected String doInBackground(String... params) {
    			
    					int success;
    					try {
    						List<NameValuePair> params1 = new ArrayList<NameValuePair>();
    						params1.add(new BasicNameValuePair(TAG_PID, lid));
    						JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params1);

    						// check your log for json response
    						Log.d("Single Product Details", json.toString());
    						
    						// json success tag
    						success = json.getInt(TAG_SUCCESS);
    						if (success == 1) {
    							// successfully received product details
    							JSONArray productObj = json.getJSONArray(TAG_PRODUCT); // JSON Array
    							
    							// get first product object from JSON Array
    							JSONObject product = productObj.getJSONObject(0);

    							ng = product.getString(TAG_NGROOM);
    							nb = product.getString(TAG_NBRIDE);
    						}else{
    							// product with pid not found
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
