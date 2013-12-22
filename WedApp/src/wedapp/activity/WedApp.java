package wedapp.activity;

import wedapp.library.DatabaseHandler;
import dima.wedapp.R;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WedApp extends Activity {
	
	private Button btnUser;
	private Button btnMerchant;
	private EditText inputList;
	
	private static final String TAG_LID = "id_list";
	
	private String lid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		
		btnUser = (Button) findViewById(R.id.button1);
		btnMerchant = (Button) findViewById(R.id.button2);
		inputList = (EditText) findViewById(R.id.editText1);
		
		btnMerchant.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(WedApp.this, LoginActivity.class);
	        	startActivity(intent);
			}
		});
		
		btnUser.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				lid = inputList.getText().toString();
				if(lid == null || lid.equals("")){
					Toast.makeText(getApplicationContext(), getString(R.string.insertListCode), Toast.LENGTH_SHORT).show();
				}else{
					System.out.println("LID "+lid);
					Intent intent = new Intent(WedApp.this, ListActivity.class);
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.createList();
					db.resetListTable();
					db.addList(lid);
					//intent.putExtra(TAG_LID, lid);
		        	startActivity(intent);
		        	finish();
				}
			}
		});
	}

}
