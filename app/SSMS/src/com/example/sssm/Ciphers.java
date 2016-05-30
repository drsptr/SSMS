package com.example.sssm;


//******************
// import directives
//******************
import javax.crypto.Cipher;
import java.security.Key;
import javax.crypto.spec.IvParameterSpec;
import android.util.Base64;
	

//**************************
// cipher classes definition
//**************************
	// AsymmetricCipher class
class AsymmetricCipher
{
	private Cipher cipher;
	
	public AsymmetricCipher(String xform) throws Exception
	{
		this.cipher = Cipher.getInstance(xform);
	}
			
	public String encrypt(String plainText, Key key) throws Exception 
	{
		String cipherText = "";
		// encrypt
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes());
		cipherText = Base64.encodeToString(cipherTextBytes, Base64.DEFAULT);
		return cipherText;
	}

	public String decrypt(String cipherText, Key key) throws Exception 
	{
		String plainText = "";
		byte[] cipherTextBytes = Base64.decode(cipherText, Base64.DEFAULT);
		// decrypt
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
		// decode into a plaintext string
		for(int i=0; i<plainTextBytes.length; i++)
			plainText += (char)plainTextBytes[i];
		return plainText;
	}
}
	// SymmetricCipher class
class SymmetricCipher
{
	private Cipher cipher;
	private IvParameterSpec ips;
	private byte[] IV;
	private Key key;
	
	public SymmetricCipher(Key key, String xform, byte[] IV) throws Exception
	{
		this.cipher = Cipher.getInstance(xform);
		this.IV = IV;
		this.key = key;
		
		if(this.IV != null)
			this.ips = new IvParameterSpec(IV);
	}
	
	public void setKey(Key key)
	{
		this.key = key;
	}
	
	public String encrypt(String plainText) throws Exception 
	{
		String cipherText = "";
		// encrypt
		if(IV != null)
			cipher.init(Cipher.ENCRYPT_MODE, key, ips);
		else
			cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes());
		cipherText = Base64.encodeToString(cipherTextBytes, Base64.DEFAULT);
		return cipherText;
	}

	public String decrypt(String cipherText) throws Exception 
	{
		String plainText = "";
		byte[] cipherTextBytes = Base64.decode(cipherText, Base64.DEFAULT);
		// decode into bytes
		if(IV != null)
			cipher.init(Cipher.DECRYPT_MODE, key, ips);
		else
			cipher.init(Cipher.DECRYPT_MODE, key);
		
		byte[] plainTextBytes =  cipher.doFinal(cipherTextBytes);
		// decode into a plaintext string
		for(int i=0; i<plainTextBytes.length; i++)
			plainText += (char)plainTextBytes[i];
		return plainText;
	}
}