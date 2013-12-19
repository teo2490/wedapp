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

import dima.wedapp.R;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity principale.
 * 
 * @author MarcoDuff [url=http://www.marcoduff.com/]MarcoDuff&#39;s Blog[/url]
 */
public class ListActivity extends FragmentActivity implements MyListFragment.OnMyListFragmentItemClick {

	private static final String TAG_LID = "id_list";
	private String lid;
	
	public String getLid(){
		return lid;
	}
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
               	   getActionBar().setDisplayHomeAsUpEnabled(true);
               	}
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                db.upgradeDatabase();
                //db.resetListTable();
                Intent i = getIntent();
                lid = i.getStringExtra(TAG_LID);
                db.createList();
                db.addList(lid);
                System.out.println("QUA "+lid);
                // Mi limito a caricare il layout, è android che inserirà in modo opportuno quello portrait o landscape (o altri!).
                setContentView(R.layout.activity_list);
                
                
                //lid = "1";
                
                if(getResources().getBoolean(R.bool.portrait_only)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                

        }
		
		@Override
		public void onRestart(){
			super.onRestart();
			finish();
			startActivity(getIntent());
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		   switch (item.getItemId()) {
		      case android.R.id.home:
		         NavUtils.navigateUpTo(this,
		               new Intent(this, WedApp.class));
		         return true;
		   }
		   return super.onOptionsItemSelected(item);
		}

        @Override
        public void onClick(String item) {
                // Preparo l'argomento da passare al Fragment o all'Activity. Questo argomento contiene l'oggetto cliccato.
                Bundle arguments = new Bundle();
                arguments.putString(MyDetailFragment.ARGUMENT_ITEM, item);
                arguments.putString(TAG_LID, lid);
                
                // Recupero la vista detailContainer
                View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                        // Non esiste spazio per la visualizzazione del dattagli, quindi ho necessità di lanciare una nuova activity.
                        // Carico gli arguments nell'intent di chiamata.
                        Intent intent = new Intent(this, DetailActivity.class);
                        intent.putExtras(arguments);
                        startActivity(intent);
                }
                else {
                        // Esiste lo spazio, procedo con la creazione del Fragment!
                        MyDetailFragment myDetailFragment = new MyDetailFragment();
                        // Imposto gli argument del fragment.
                        myDetailFragment.setArguments(arguments);
                        
                        // Procedo con la sostituzione del fragment visualizzato.
                        FragmentManager fragmentManager = this.getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                        .replace(R.id.detailContainer, myDetailFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                }
        }
        
}