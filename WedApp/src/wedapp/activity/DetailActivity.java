package wedapp.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

import dima.wedapp.R;
import dima.wedapp.R.id;
import dima.wedapp.R.layout;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailActivity extends FragmentActivity {
	
	private static final String TAG_LID = "id_list";
	private String lid;
	
		//---
        @Override
        protected void onCreate(Bundle savedInstanceState) {
               
                if(getResources().getBoolean(R.bool.portrait_only)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                	   getActionBar().setDisplayHomeAsUpEnabled(true);
                	}
       
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_detail);
                
                // Crea gli argomenti da passare al fragment in base all'intent di chiamata. 
                Bundle arguments = getIntent().getExtras();
                lid = arguments.getString(TAG_LID);
                System.out.println("Lo stamp?" + lid);
               // pid = arguments.getString(MyDetailFragment.ARGUMENT_ITEM);
                
                // Crea il fragment.
                MyDetailFragment myDetailFragment = new MyDetailFragment();
                myDetailFragment.setArguments(arguments);
                
                // Visualizza il fragment.
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                .replace(R.id.detailContainer, myDetailFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        }
        
        public void onBackPressed() {
			Intent back = new Intent(getApplicationContext(), ListActivity.class);
        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(back);
        	finish();
        	return;
    	}
        
        @Override
		public boolean onOptionsItemSelected(MenuItem item) {
		   switch (item.getItemId()) {
		      case android.R.id.home:
					Intent back = new Intent(getApplicationContext(), ListActivity.class);
		        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	startActivity(back);
		        	finish();
	                return true;
		   }
		   return super.onOptionsItemSelected(item);
		}
    }
