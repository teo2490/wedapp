package wedapp.activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dima.wedapp.R;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FacebookActivity extends FragmentActivity {
	
	private FacebookFragment facebookFragment;
	private String lid;
	
	private static final String TAG_LID = "id_list";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
     	   getActionBar().setDisplayHomeAsUpEnabled(true);
     	}
        
        Intent in = getIntent();
        lid = in.getStringExtra(TAG_LID);
        
        if (savedInstanceState == null) {
        	// Add the fragment on initial activity setup
        	facebookFragment = new FacebookFragment();
            getSupportFragmentManager()
            .beginTransaction()
            .add(android.R.id.content, facebookFragment)
            .commit();
        } else {
        	// Or set the fragment from restored state info
        	facebookFragment = (FacebookFragment) getSupportFragmentManager()
        	.findFragmentById(android.R.id.content);
        }
        

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "teo2490.fbprova", //E' giusto questo?? o va cambiato?????????
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                System.out.println(Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
        } catch (NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.facebook, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	      case android.R.id.home:
				Intent back = new Intent(getApplicationContext(), ListActivity.class)/*.putExtra(TAG_LID, lid)*/;
	        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(back);
	        	finish();
                return true;
	   }
	   return super.onOptionsItemSelected(item);
	}
}
