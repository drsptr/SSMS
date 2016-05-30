package com.example.sssm;


//******************
// import directives
//******************
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


//*********************
// key storage class
//*********************
	// KeyStorage class
class KeyStorage
{
	private String publicKeyPath;
	private String privateKeyPath;
	private String sharedKeyPath;
	private String publicKeyFilename;
	private String privateKeyFilename;
	private String sharedKeyFilename;
	private String algorithm;
	
	// **constructors**
	public KeyStorage(String publicKeyPath, String privateKeyPath, String publicKeyFilename, String privateKeyFilename)
	{
		this.publicKeyPath = publicKeyPath;
		this.privateKeyPath = privateKeyPath;
		this.publicKeyFilename = publicKeyFilename;
		this.privateKeyFilename = privateKeyFilename;
	}
	
	public KeyStorage(String sharedKeyPath, String sharedKeyFilename, String algorithm)
	{
		this.sharedKeyPath = sharedKeyPath;
		this.sharedKeyFilename = sharedKeyFilename;
		this.algorithm = algorithm;
	}
	
	// **asymmetric**
	public KeyPair loadKeyPair()
	{
		return new KeyPair(this.loadPublicKey(),this.loadPrivateKey());
	}
	
	public PrivateKey loadPrivateKey()
	{
		PrivateKey privateKeyR = null;
		
		try
		{
			// Read Private Key.
			File filePrivateKey = new File(privateKeyPath + privateKeyFilename);
			FileInputStream fis = new FileInputStream(privateKeyPath + privateKeyFilename);
			byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			fis.read(encodedPrivateKey);
			fis.close();
			
			// Reconstruct
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
			privateKeyR = keyFactory.generatePrivate(privateKeySpec);
		}
		catch(Exception e) {}
		
		return privateKeyR;
	}
	
	public PublicKey loadPublicKey()
	{
		PublicKey publicKeyR = null;
		
		try
		{
			// Read Public Key.
			File filePublicKey = new File(publicKeyPath + publicKeyFilename);
			FileInputStream fis = new FileInputStream(publicKeyPath + publicKeyFilename);
			byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
			fis.read(encodedPublicKey);
			fis.close();
			
			// Reconstruct
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			publicKeyR = keyFactory.generatePublic(publicKeySpec);
		}
		catch(Exception e) {}
		
		return publicKeyR;
	}
	
	//**symmetric**
	public void saveSharedKey(SecretKey key) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(sharedKeyPath + sharedKeyFilename);
		fos.write(key.getEncoded());
		fos.close();
	}
	
	public SecretKey loadSharedKey() throws Exception
	{
		File fileSharedKey = new File(sharedKeyPath + sharedKeyFilename);
		FileInputStream fis = new FileInputStream(sharedKeyPath + sharedKeyFilename);
		byte[] encodedSharedKey = new byte[(int) fileSharedKey.length()];
		fis.read(encodedSharedKey);
		fis.close();
		return new SecretKeySpec(encodedSharedKey, algorithm);
	}
}