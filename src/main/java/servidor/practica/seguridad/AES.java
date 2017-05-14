package servidor.practica.seguridad;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES
{
	private static Key getKey(String contraseña) throws NoSuchAlgorithmException
	{
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte [] hash = sha256.digest(contraseña.getBytes());
		byte [] trim = Arrays.copyOf(hash, 16); // use only first 128 bit
		return new SecretKeySpec(trim, "AES");
	}
	
	public static String encriptar(String contraseña, String plainText) throws Exception
	{
		Key key = getKey(contraseña);
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
	    aes.init(Cipher.ENCRYPT_MODE, key);
	    byte[] ciphertext = aes.doFinal(plainText.getBytes());
	    byte[] encoded = Base64.getEncoder().encode(ciphertext);
	    return new String(encoded);
	}
	
	public static String desencriptar(String contraseña, String texto) throws Exception
	{
		Key key = getKey(contraseña);
		byte[] cipherText = Base64.getDecoder().decode(texto.getBytes());
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		aes.init(Cipher.DECRYPT_MODE, key);
	    byte[] desencriptado = aes.doFinal(cipherText);
	    return new String(desencriptado);
	}
}