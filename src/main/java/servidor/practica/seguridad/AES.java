package servidor.practica.seguridad;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
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
	
	public static byte [] encriptar(String contraseña, String plainText) throws Exception
	{
		Key key = getKey(contraseña);
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
	    aes.init(Cipher.ENCRYPT_MODE, key);
	    return  aes.doFinal(plainText.getBytes());
	}
	
	public static String desencriptar(String contraseña, byte [] cipherText) throws Exception
	{
		Key key = getKey(contraseña);
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		aes.init(Cipher.DECRYPT_MODE, key);
	    byte[] desencriptado = aes.doFinal(cipherText);
	    return new String(desencriptado);
	}

   public static void main(String[] args) throws Exception {

      // Generamos una clave de 128 bits adecuada para AES
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(128);
      Key key = keyGenerator.generateKey();
      
      //key = new SecretKeySpec(bytesLeidosDelFichero, "AES"); //recuperar la llave

      System.out.println(new String(key.getEncoded()));
      
      // Ver como se puede guardar esta clave en un fichero y recuperarla
      // posteriormente en la clase RSAAsymetricCrypto.java

      // Texto a encriptar
      String texto = "Este es el texto que queremos encriptarr";

      // Se obtiene un cifrador AES
      Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");

      // Se inicializa para encriptacion y se encripta el texto,
      // que debemos pasar como bytes.
      aes.init(Cipher.ENCRYPT_MODE, key);
      byte[] encriptado = aes.doFinal(texto.getBytes());

      // Se escribe byte a byte en hexadecimal el texto
      // encriptado para ver su pinta.
      for (byte b : encriptado) {
         System.out.print(Integer.toHexString(0xFF & b));
      }
      System.out.println();

      // Se iniciliza el cifrador para desencriptar, con la
      // misma clave y se desencripta
      aes.init(Cipher.DECRYPT_MODE, key);
      byte[] desencriptado = aes.doFinal(encriptado);

      // Texto obtenido, igual al original.
      System.out.println(new String(desencriptado));
   }
}