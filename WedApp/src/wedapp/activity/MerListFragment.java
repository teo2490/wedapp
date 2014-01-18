package wedapp.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dima.wedapp.R;

import wedapp.library.JSONParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Fragment that shows the list of gifts in a list for a merchant
 * 
 * @author Matteo
 */
public class MerListFragment extends ListFragment {
	
	private boolean flagProductExists;
	private boolean flagListExists;
	
	private String lid;
	private String memail;
	
	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single product url
	private static final String url_product_list = "http://wedapp.altervista.org/get_all_gifts_in_list_for_merchant.php";
	
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_LID = "id_list";
	private static final String TAG_MEMAIL = "merchant_email";
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "price";
	private static final String TAG_PID= "id";
	private static final String TAG_GIFTS= "gifts";
	
	private String[] name;
	private String[] price;
	private String[] pid;
	

		
        /**
         * Callback interface in order to communicate with the activity that contains the fragment
         */
        public static interface OnMerListFragmentItemClick {
                public void onClick(String item);
        }
        
        /**
         * Riferimento all'activity di Callback.
         */
        private OnMerListFragmentItemClick mActivityAttached;
        

        @Override
        public void onAttach(Activity activity) {
                super.onAttach(activity);

                if(activity instanceof OnMerListFragmentItemClick) {
                        // L'activity che contiene il fragment � compatibile con l'interfacci di Callback, mi memorizzo il riferimento.
                        mActivityAttached = (OnMerListFragmentItemClick)activity;
                }
                else {
                        // L'activity non � compatibile, creo un riferimento fittizio. 
                        mActivityAttached = new OnMerListFragmentItemClick() {
                                public void onClick(String item) {}
                        };
                }
        }
        
        /**
         * Once the activity is created a task in order to get the List detail from the server in started
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);

                //Bundle bundle = this.getArguments();
                //lid = bundle.getString(TAG_LID);
                lid = ((MerListActivity)getActivity()).getLid();
                memail = ((MerListActivity)getActivity()).getMemail();
                System.out.println("OGGI: "+lid);
                System.out.println("OGGI: "+memail);
  				new GetProductList().execute();

                
                //ArrayList<Parcelable> giftList = bundle.getParcelableArrayList(MerDetailFragment.ARGUMENT_ITEM);
                
               
        }
        
        /**
         * Listener that reacts to the click on a item of the list
         */
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
    			pDialog = ProgressDialog.show(getActivity(), getString(R.string.Loading),
    					getString(R.string.PleaseWait), true);
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
    						params1.add(new BasicNameValuePair(TAG_MEMAIL, memail));

    						// getting product details by making HTTP request
    						// Note that product details url will use GET request
    						JSONObject json = jsonParser.makeHttpRequest(
    								url_product_list, "POST", params1);

    						// check your log for json response
    						Log.d("List", json.toString());
    						
    						// json success tag
    						success = json.getInt(TAG_SUCCESS);
    						if (success == 1) {
    							flagProductExists = true;
    							flagListExists = true;
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
    						} else if(success == 2) {
    							flagProductExists = false;
    							flagListExists = true;
    						} else if(success == 3) {
    							flagProductExists = false;
    							flagListExists = false;
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
    			if(flagProductExists && flagListExists){
    				setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, name));
    			} else if(!flagProductExists && flagListExists) {
    				setListAdapter(null);
    				Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noGiftMerListFragment), Toast.LENGTH_LONG).show();
    			} else {
    				Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noListMerListFragment), Toast.LENGTH_LONG).show();
    				Intent main = new Intent(getActivity().getApplicationContext(), DashboardActivity.class);
					startActivity(main);
    				getActivity().finish();
    			}
    			pDialog.dismiss();    			
    		}
    	}
}
