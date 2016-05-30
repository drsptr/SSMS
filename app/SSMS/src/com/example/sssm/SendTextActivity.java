package com.example.sssm;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
import android.widget.EditText;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class SendTextActivity extends ActionBarActivity implements OnClickListener
{
	private String nonceA, destPhone;
	private SecretKey sharedKey;
	private SymmetricCipher sc;
	private final byte[] IV = { 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d, 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_text);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		// set onClick listener
		Button[] buttons = new Button[2];
		buttons[0] = (Button) findViewById(R.id.okButtonSendTextActivity);
		buttons[1] = (Button) findViewById(R.id.inviaButtonSendTextActivity);
		for(int i=0; i<buttons.length; i++)
			buttons[i].setOnClickListener(this);
		// recover my nonce
		Intent intent = getIntent();
		nonceA = intent.getStringExtra("nonceA");
		destPhone = intent.getStringExtra("destPhone");
	}
	
	private int sendMsg2() throws Exception
	{
		AsymmetricCipher ac = new AsymmetricCipher("RSA/ECB/PKCS1Padding");
		KeyStorage myAsymStorage;
		PrivateKey myKey;
		String cipherText;
		
		// retrieve my private key
		File path = Environment.getExternalStorageDirectory();
		String keysPath = path.getAbsolutePath() + "/SSMSkeys/";
		myAsymStorage = new KeyStorage("", keysPath, "", "private.key");
		myKey = myAsymStorage.loadPrivateKey();
		
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
		// decrypt first and split the message;
		// msgFields[0] contains my nonce (A's nonce)
		// msgFields[1] contains B's nonce
		// msgFields[2] contains B's phone number
		// msgFields[3] contains the session key encoded in bytes expressed with their integer representation and separated by white spaces
		String plainText = ac.decrypt(cipherText, myKey);
		String[] msgFields = plainText.split("[\\x7C]"); // 'x7C' ASCII code for vertical bar '|'
		// check for sender equality; if destPhone is not equal to the number contained in the message, it returns -3
		if(!destPhone.equals(msgFields[2]))
			return -3;
		// check for nonce equality; if nonceA is not equal to that contained in the message, it returns -4
		if(!msgFields[0].equals(nonceA))
			return -4;
		
		//reconstruct the shared key
		String[] bytesString = msgFields[3].split(" ");
		byte[] bytes = new byte[bytesString.length];
		for(int i=0; i<bytes.length; i++)
		    bytes[i] = Byte.parseByte(bytesString[i]);
		sharedKey = new SecretKeySpec(bytes, "AES");
		// generate the symmetric cipher
		sc = new SymmetricCipher(sharedKey, "AES/CBC/PKCS5Padding", IV);
		
		// prepare SMS text
			// prepare text to encrypt
		plainText = new String(msgFields[1]);
			// encrypt plaintext
		cipherText = sc.encrypt(plainText);
			// send message
		SmsManager smanager = SmsManager.getDefault();
		ArrayList<String> parts = smanager.divideMessage(cipherText);
		smanager.sendMultipartTextMessage(destPhone, null, parts, null, null);
		return 0;
	}
	
	@Override
	public void onClick(View arg0)
	{
		TextView txtView = (TextView) findViewById(R.id.textViewSendTextActivity);
		EditText edit = (EditText) findViewById(R.id.editTextSendTextActivity);
		Intent nextActivityIntent = null;
		
		switch(arg0.getId())
		{
			case R.id.okButtonSendTextActivity:
				try
				{
					String errorString = "";
					int tmp = sendMsg2();
					
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
								errorString = "Errore sul mittente!";
								break;
								
							case -4:
								errorString = "Errore sul nonce!";
								break;
						}
						Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
						nextActivityIntent = new Intent(this, MainActivity.class);
						startActivity(nextActivityIntent);
						break;
					}
				}
				catch(Exception e)
				{
					// if an exception occours during the protocol execution, the app returns on the main activity
					Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
					nextActivityIntent = new Intent(this, MainActivity.class);
					startActivity(nextActivityIntent);
				}
				arg0.setVisibility(View.INVISIBLE);
				txtView.setVisibility(View.INVISIBLE);
				findViewById(R.id.inviaButtonSendTextActivity).setVisibility(View.VISIBLE);
				edit.setVisibility(View.VISIBLE);
				Toast.makeText(getApplicationContext(), "Handshake finito!", Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.inviaButtonSendTextActivity:
				try
				{
					// retrieve the text of the message written
					String plainText = edit.getText().toString();
					// encrypt the plain text
					String cipherText = sc.encrypt(plainText);
					SmsManager smanager = SmsManager.getDefault();
					ArrayList<String> parts = smanager.divideMessage(cipherText);
					smanager.sendMultipartTextMessage(destPhone, null, parts, null, null);
					Toast.makeText(getApplicationContext(), "Messaggio inviato!", Toast.LENGTH_SHORT).show();
					nextActivityIntent = new Intent(this, MainActivity.class);
				}
				catch(Exception e) 
				{
					// if an exception occours during the protocol execution, the app returns on the main activity
					Toast.makeText(getApplicationContext(), "Si è verificato un errore!", Toast.LENGTH_LONG).show();
					nextActivityIntent = new Intent(this, MainActivity.class);
				}
				startActivity(nextActivityIntent);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_text, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_send_text,
					container, false);
			return rootView;
		}
	}
}
