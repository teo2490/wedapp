package wedapp.activity;

import dima.wedapp.R;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
				System.out.println("LID "+lid);
				Intent intent = new Intent(WedApp.this, ListActivity.class);
				intent.putExtra(TAG_LID, lid);
	        	startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
