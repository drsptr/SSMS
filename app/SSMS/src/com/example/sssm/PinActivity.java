package com.example.sssm;

//******************
//import directives
//******************
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.TextView;


//******************************
//PinActivity class definition
//******************************
public class PinActivity extends ActionBarActivity implements OnClickListener
{
	private String action = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		// Retrieve the operation
		Intent intent = getIntent();
		action = intent.getStringExtra("action");
				
		// Keyboard buttons
		Button keyboard[] = new Button[12];
		keyboard[0] = (Button) findViewById(R.id.ButtonKeypad1PinActivity);
		keyboard[1] = (Button) findViewById(R.id.ButtonKeypad2PinActivity);
		keyboard[2] = (Button) findViewById(R.id.ButtonKeypad3PinActivity);
		keyboard[3] = (Button) findViewById(R.id.ButtonKeypad4PinActivity);
		keyboard[4] = (Button) findViewById(R.id.ButtonKeypad5PinActivity);
		keyboard[5] = (Button) findViewById(R.id.ButtonKeypad6PinActivity);
		keyboard[6] = (Button) findViewById(R.id.ButtonKeypad7PinActivity);
		keyboard[7] = (Button) findViewById(R.id.ButtonKeypad8PinActivity);
		keyboard[8] = (Button) findViewById(R.id.ButtonKeypad9PinActivity);
		keyboard[9] = (Button) findViewById(R.id.ButtonKeypadBackPinActivity);
		keyboard[10] = (Button) findViewById(R.id.ButtonKeypad0PinActivity);
		keyboard[11] = (Button) findViewById(R.id.ButtonKeypadOkPinActivity);
		// set keyboard buttons listener
		for(int i=0; i<keyboard.length; i++)
			keyboard[i].setOnClickListener(this);
	}
	
	@Override
	public void onClick(View arg0)
	{
		Intent nextActivityIntent = new Intent(this, TelephoneNumberActivity.class);
		TextView pinTextView = (TextView) findViewById(R.id.textViewPinActivity);
		String pinText = pinTextView.getText().toString();
		
		switch(arg0.getId())
		{
			case R.id.ButtonKeypadBackPinActivity:
				if(pinText.length() > 0)
					pinText = pinText.substring(0, pinText.length() - 1);
				break;
				
			case R.id.ButtonKeypadOkPinActivity:
				if(pinText.length() != 4)
				{
					Toast.makeText(getApplicationContext(), "Il PIN deve essere di 4 cifre", Toast.LENGTH_LONG).show();
					break;
				}
				else
				{	
					if(action.equals("SEND"))
					{
						nextActivityIntent.putExtra("action","SEND");
						nextActivityIntent.putExtra("nonceA",pinText);
					}
					else
					{
						nextActivityIntent.putExtra("action","RECEIVE");
						nextActivityIntent.putExtra("nonceB",pinText);
					}
				}
				startActivity(nextActivityIntent);
				 	
				break;
				
			default:
				if(pinText.length() < 4)
				{
					Button b = (Button)arg0;
					pinText += b.getText().toString();
				}
				else
					Toast.makeText(getApplicationContext(), "Premi OK per continuare", Toast.LENGTH_LONG).show();
				break;
				
		}
		
		pinTextView.setText(pinText);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pin, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_pin, container,
					false);
			return rootView;
		}
	}

}
