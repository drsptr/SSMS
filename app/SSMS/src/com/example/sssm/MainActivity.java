package com.example.sssm;

//******************
// import directives
//******************
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.content.Intent;


//******************************
// MainActivity class definition
//******************************
public class MainActivity extends ActionBarActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) 
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        // Define buttons onClick() behavior
        Button sendSMS = (Button) findViewById(R.id.sendButtonMainActivity);
        Button receiveSMS = (Button) findViewById(R.id.receiveButtonMainActivity);
        
        sendSMS.setOnClickListener(this);
        receiveSMS.setOnClickListener(this);        
    }


    @Override
   	public void onClick(View arg0)
   	{
    	Intent nextActivityIntent = new Intent(this, PinActivity.class);
    	
    	switch(arg0.getId())
   		{
   			case R.id.sendButtonMainActivity:
   				nextActivityIntent.putExtra("action","SEND");
   				break;
   			
   			case R.id.receiveButtonMainActivity:
   				nextActivityIntent.putExtra("action","RECEIVE");
   				break;
   		}
    	
    	startActivity(nextActivityIntent);
   	}
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
