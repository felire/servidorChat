package servidor.practica.seguridad;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Created by Gabriel Wittes on 3/15/2016.
 * A class to encrypt and decrypt RSA plaintext and ciphertext, as well as to generate RSA key pairs.
 */
public class RSA 
{
    /**
     * Returns a new RSA key pair.
     * @return public and private RSA keys
     */
    public static KeyPair generateKeyPair()
    {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Returns an RSA-encrypted byte array given a plaintext string and a public RSA key.
     * @param plaintext a plaintext string
     * @param key a public RSA key
     * @return ciphertext
     */
    public static String encrypt(String plaintext, PublicKey key)
    {
        byte[] ciphertext = null;
        try {
            //Cipher cipher = Cipher.getInstance("RSA/ECB/OASP");
        	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ciphertext = cipher.doFinal(plaintext.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] cipherEncoded = Base64.getEncoder().encode(ciphertext);
        return new String(cipherEncoded);
    }

    /**
     * Returns the plaintext of a given RSA-encrypted string and a private RSA key.
     * @param ciphertext an RSA-encrypted byte array
     * @param key a private RSA key
     * @return plaintext
     */
    public static String decrypt(String text, PrivateKey key){
    	byte[] ciphertext = Base64.getDecoder().decode((text.getBytes()));
        byte[] plaintext = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            plaintext = cipher.doFinal(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(plaintext);
    }
	
    public static String savePublicKey(PublicKey publ) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = fact.getKeySpec(publ, X509EncodedKeySpec.class);
        byte[] tokenBase64 = Base64.getEncoder().encode(spec.getEncoded());
        return new String(tokenBase64);
    }
	
	public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException 
    {
		byte[] data = Base64.getDecoder().decode((stored.getBytes()));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		return fact.generatePublic(spec);
     }
}










