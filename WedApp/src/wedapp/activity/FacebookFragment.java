package wedapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

import dima.wedapp.R;

/**
 * Fragment that checks the login on Facebook and makes able to publish some content on it.
 *  
 * @author Matteo
 *
 */
public class FacebookFragment extends Fragment {
	
	private static final String TAG = "MainFragment";
	private static final String TAG_NGROOM = "n_groom";
	private static final String TAG_NBRIDE = "n_bride";
	private String groom;
	private String bride;
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
	private Button publishButton;
	
	/**
	 * It checks if an existing login on Facebook is present. If not the Login button is shown.
	 * When the user is logged, the publish button will be available.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_facebook, container, false);
		Intent i = getActivity().getIntent();
		groom = i.getStringExtra(TAG_NGROOM);
		bride = i.getStringExtra(TAG_NBRIDE);
	    
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		
	    publishButton = (Button) view.findViewById(R.id.publishButton);
	    publishButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				publishFeedDialog();		
			}
		});

	    return view;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
        
    }

	/**
	 * This method is used in order to handle a change in the login state
	 * when the fragment is already running
	 */
    @Override
    public void onResume() {
        super.onResume();
        
        // For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null &&
				(session.isOpened() || session.isClosed()) ) {
			onSessionStateChange(session, session.getState(), null);
		}
		
        uiHelper.onResume();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
    
    /**
     * This method prepare the content to publish on Facebook and show a preview to the user.
     * It handles all the action that the user can do (X button, cancel button and publish button)
     */
	private void publishFeedDialog() {
		String message = getString(R.string.messageFacebook)+" "+groom+" "+getString(R.string.and)+" "+bride;
        Bundle params = new Bundle();
        params.putString("name", message);
        params.putString("caption", "");
        params.putString("description", getString(R.string.descriptionFacebook));
        params.putString("link", "https://play.google.com/store/apps/details?id=com.anjokes.apps.jokes.it");
        params.putString("picture", "http://wedapp.altervista.org/Images/128wedding_pinkw.png");
        
        // Invoke the dialog
    	WebDialog feedDialog = (
    			new WebDialog.FeedDialogBuilder(getActivity(),
    					Session.getActiveSession(),
    					params))
    					.setOnCompleteListener(new OnCompleteListener() {

    						@Override
    						public void onComplete(Bundle values,
    								FacebookException error) {
    							if (error == null) {
    								// When the story is posted, echo the success
    				                // and the post Id.
    								final String postId = values.getString("post_id");
        							if (postId != null) {
        								Toast.makeText(getActivity(),
        										getString(R.string.postedFacebook)+" "+postId,
        										Toast.LENGTH_SHORT).show();
        							} else {
        								// User clicked the Cancel button
        								Toast.makeText(getActivity().getApplicationContext(), 
        		                                getString(R.string.cancelFacebook), 
        		                                Toast.LENGTH_SHORT).show();
        							}
    							} else if (error instanceof FacebookOperationCanceledException) {
    								// User clicked the "x" button
    								Toast.makeText(getActivity().getApplicationContext(), 
    										getString(R.string.cancelFacebook), 
    		                                Toast.LENGTH_SHORT).show();
    							} else {
    								// Generic, ex: network error
    								Toast.makeText(getActivity().getApplicationContext(), 
    										getString(R.string.errorPostingFacebook), 
    		                                Toast.LENGTH_SHORT).show();
    							}
    						}
    						
    						})
    					.build();
    	feedDialog.show();
    }
	
	/**
	 * This method handles changes in the Login state and shows/hides the Publish button wrt this state
	 * @param session
	 * @param state
	 * @param exception
	 */
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	    	Log.i(TAG, "Logged in...");
	        publishButton.setVisibility(View.VISIBLE);
	    } else if (state.isClosed()) {
	    	Log.i(TAG, "Logged out...");
	        publishButton.setVisibility(View.INVISIBLE);
	    }
	}

}
