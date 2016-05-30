package com.example.sssm;

import java.io.File;

import javax.crypto.SecretKey;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class ReceiveTextActivity extends ActionBarActivity implements OnClickListener
{
	private String nonceB, destPhone;
	private SecretKey sharedKey;
	private SymmetricCipher sc;
	private final byte[] IV = { 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d, 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive_text);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		// set onClick listener
		Button[] buttons = new Button[3];
		buttons[0] = (Button) findViewById(R.id.okButtonMsg1ReceiveTextActivity);
		buttons[1] = (Button) findViewById(R.id.okButtonMsg2ReceiveTextActivity);
		buttons[2] = (Button) findViewById(R.id.endButtonReceiveTextActivity);
		for(int i=0; i<buttons.length; i++)
			buttons[i].setOnClickListener(this);
		// recover my nonce, destPhone and myPhone
		Intent intent = getIntent();
		nonceB = intent.getStringExtra("nonceB");
		destPhone = intent.getStringExtra("destPhone");
		String myPhone = intent.getStringExtra("myPhone");
		// recover the shared key
		File path = Environment.getExternalStorageDirectory();
		String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
		KeyStorage keyS = new KeyStorage(keysPath, myPhone + "_" + destPhone + ".key", "AES");
		try
		{
			sharedKey = keyS.loadSharedKey();
			// generate the symmetric cipher
			sc = new SymmetricCipher(sharedKey, "AES/CBC/PKCS5Padding", IV);
		}
		catch(Exception e)
		{
			// if an exception occours during the protocol execution, the app returns on the main activity
			Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
			Intent nextActivityIntent = new Intent(this, MainActivity.class);
			startActivity(nextActivityIntent);
		}
	}
	
	private int receiveMsg2() throws Exception
	{
		String plainText, cipherText;
		
		// retrieve cipher text SMS: it is the last received message from the sender whose phone number is equal to 'destPhone'
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		// check if there messages; if no messages are found, it returns -1
		if(!cursor.moveToFirst())
			return -1;
		// scroll all messages and find the last sent by 'destPhone'; if no message from destPhone is sent, it returns -2
		for(;;)
		{
			String tmpSender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
			if(tmpSender.equals(destPhone))
			{
				// message found! it retrieves the cipher text and breaks the loop
				cipherText = cursor.getString(cursor.getColumnIndexOrThrow("body"));
				break;
			}
			// try the next message; if no more messages are available, it returns -2
			if(!cursor.moveToNext())
				return -2;
		}
		// decrypt
		plainText = sc.decrypt(cipherText);
		// check for nonce equality; if they are different, it returns -3
		if(!plainText.equals(nonceB))
			return -3;
		return 0;
	}
	
	public void onClick(View arg0)
	{
		TextView txtView = (TextView) findViewById(R.id.textViewReceiveTextActivity);
		Intent nextActivityIntent = null;
		
		switch(arg0.getId())
		{
			// receive the last message of the protocol
			case R.id.okButtonMsg1ReceiveTextActivity:
				try
				{
					int tmp = receiveMsg2();
					String errorString = "";
					
					if(tmp!=0)
					{
						switch(tmp)
						{
							case -1:
								errorString = "Errore! Nessun messaggio presente.";
								break;
								
							case -2:
								errorString = "Errore! Nessun messaggio inviato da " + destPhone + ".";
								break;
								
							case -3:
								errorString = "Errore sul nonce!";
								break;
						}
						Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
						nextActivityIntent = new Intent(this, MainActivity.class);
						startActivity(nextActivityIntent);
						return;
					}
				}
				catch(Exception e)
				{
					// if an exception occours during the protocol execution, the app returns on the main activity
					Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
					nextActivityIntent = new Intent(this, MainActivity.class);
					startActivity(nextActivityIntent);
					return;
				}
				// the handshake is done
				Toast.makeText(getApplicationContext(), "Handshake finito!", Toast.LENGTH_SHORT).show();
				// set the text of the TextView and the visibility of the buttons
				txtView.setText("Attendi il messaggio e poi premi OK per leggere il messaggio");
				arg0.setVisibility(View.INVISIBLE);
				findViewById(R.id.okButtonMsg2ReceiveTextActivity).setVisibility(View.VISIBLE);
				break;
				
			
			case R.id.okButtonMsg2ReceiveTextActivity:
				// Once the user press on this button, we have only to read the last message received, decrypt and show it
				String cipherText, plainText;
				
				// retrieve cipher text SMS: it is the last received message from the sender whose phone number is equal to 'destPhone'
				Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
				// check if there messages; if no messages are found, it returns -1
				if(!cursor.moveToFirst())
					return;
				// scroll all messages and find the last sent by 'destPhone'; if no message from destPhone is sent, it returns -2
				for(;;)
				{
					String tmpSender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
					if(tmpSender.equals(destPhone))
					{
						// message found! it retrieves the cipher text and breaks the loop
						cipherText = cursor.getString(cursor.getColumnIndexOrThrow("body"));
						break;
					}
					// try the next message; if no more messages are available, it returns -2
					if(!cursor.moveToNext())
						return;
				}
				
				// decrypt the ciphertext
				try
				{
					plainText = sc.decrypt(cipherText);
				}
				catch(Exception e)
				{
					// if an exception occours during the protocol execution, the app returns on the main activity
					Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
					nextActivityIntent = new Intent(this, MainActivity.class);
					startActivity(nextActivityIntent);
					return;
				}
				
				// set the text of the TextView and the visibility of the buttons
				txtView.setText(plainText);
				arg0.setVisibility(View.INVISIBLE);
				findViewById(R.id.endButtonReceiveTextActivity).setVisibility(View.VISIBLE);
				break;
				
				
			case R.id.endButtonReceiveTextActivity:
				nextActivityIntent = new Intent(this, MainActivity.class);
				startActivity(nextActivityIntent);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.receive_text, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_receive_text,
					container, false);
			return rootView;
		}
	}

}
