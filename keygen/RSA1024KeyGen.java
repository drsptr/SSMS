// import directives
import java.io.*;
import java.security.spec.*;
import javax.crypto.Cipher;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Key;
import java.security.KeyFactory;


class AE
{
	public static void main(String[] unused) throws Exception 
	{
		// Local vars
			String str = "";
		
		// Key-pair Generation
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair kp = kpg.generateKeyPair();
			PublicKey pubKey = kp.getPublic();
			PrivateKey privKey = kp.getPrivate();
			
		// Store Key into files
		try
		{
			// Public Key
				X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKey.getEncoded());
				FileOutputStream fos = new FileOutputStream("./public.key");
				fos.write(x509EncodedKeySpec.getEncoded());
				fos.close();
			// Private Key
				PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privKey.getEncoded());
				fos = new FileOutputStream("./private.key");
				fos.write(pkcs8EncodedKeySpec.getEncoded());
				fos.close();
		}
		catch (IOException e) {}
	}
}