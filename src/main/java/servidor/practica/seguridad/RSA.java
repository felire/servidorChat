package servidor.practica.seguridad;

import javax.crypto.Cipher;
import java.security.*;

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
    public static byte[] encrypt(String plaintext, PublicKey key)
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
        return ciphertext;
    }

    /**
     * Returns the plaintext of a given RSA-encrypted string and a private RSA key.
     * @param ciphertext an RSA-encrypted byte array
     * @param key a private RSA key
     * @return plaintext
     */
    public static String decrypt(byte[] ciphertext, PrivateKey key){
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
}