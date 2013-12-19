package wedapp.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wedapp.library.JSONParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Fragment che si occupa della visualizzazione di una lista di String.
 * 
 * @author MarcoDuff [url=http://www.marcoduff.com/]MarcoDuff&#39;s Blog[/url]
 */
public class MyListFragment extends ListFragment {
	
	private String lid;
	
	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single product url
	private static final String url_product_list = "http://wedapp.altervista.org/get_all_gifts_in_list.php";
	
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_LID = "id_list";
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "price";
	private static final String TAG_PID= "id";
	private static final String TAG_GIFTS= "gifts";
	
	private String[] name;
	private String[] price;
	private String[] pid;
	

		
        /**
         * Interfaccia di Callback per comunicare con l'activity che contiene il Fragment.
         */
        public static interface OnMyListFragmentItemClick {
                public void onClick(String item);
        }
        
        /**
         * Riferimento all'activity di Callback.
         */
        private OnMyListFragmentItemClick mActivityAttached;
        

        @Override
        public void onAttach(Activity activity) {
                super.onAttach(activity);

                if(activity instanceof OnMyListFragmentItemClick) {
                        // L'activity che contiene il fragment è compatibile con l'interfacci di Callback, mi memorizzo il riferimento.
                        mActivityAttached = (OnMyListFragmentItemClick)activity;
                }
                else {
                        // L'activity non è compatibile, creo un riferimento fittizio. 
                        mActivityAttached = new OnMyListFragmentItemClick() {
                                public void onClick(String item) {}
                        };
                }
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);

                //Bundle bundle = this.getArguments();
                //lid = bundle.getString(TAG_LID);
                lid = ((ListActivity)getActivity()).getLid();
                System.out.println("QUI "+lid);
  				new GetProductList().execute();

                
                //ArrayList<Parcelable> giftList = bundle.getParcelableArrayList(MyDetailFragment.ARGUMENT_ITEM);
                
               
        }
        
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
                super.onListItemClick(l, v, position, id);
                // Richiamo il metodo id callback
                mActivityAttached.onClick(pid[position]);
        }
        
        /**
    	 * Background Async Task to Get complete product details
    	 * */
    	class GetProductList extends AsyncTask<String, String, String> {

    		/**
    		 * Before starting background thread Show Progress Dialog
    		 * */
    		@Override
    		protected void onPreExecute() {
    			super.onPreExecute();
    			pDialog = ProgressDialog.show(getActivity(), "Loading",
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
    						params1.add(new BasicNameValuePair(TAG_LID, lid));

    						// getting product details by making HTTP request
    						// Note that product details url will use GET request
    						JSONObject json = jsonParser.makeHttpRequest(
    								url_product_list, "POST", params1);

    						// check your log for json response
    						Log.d("List", json.toString());
    						
    						// json success tag
    						success = json.getInt(TAG_SUCCESS);
    						if (success == 1) {
    							// successfully received product details
    							JSONArray productObj = json
    									.getJSONArray(TAG_GIFTS); // JSON Array

    							// creating new HashMap
    			                HashMap<String, String> map = new HashMap<String, String>();
    			                name = new String[productObj.length()];
    			                pid = new String[productObj.length()];
    			                price = new String[productObj.length()];
    			                
    							for(int i=0; i<productObj.length(); i++){
	    							// get first product object from JSON Array
	    							JSONObject product = productObj.getJSONObject(i);
	    							
	    							System.out.println(product.getString(TAG_NAME));
	
	    							String pi = product.getString(TAG_PID);
	    							String n = product.getString(TAG_NAME);
	    							String p = product.getString(TAG_PRICE);
	    							
	    							name[i] = n;
	    							price[i] = p;
	    							pid[i] = pi;
    							}
    						}
    						else{
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
    			setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, name));
    			pDialog.dismiss();    			
    		}
    	}
}
