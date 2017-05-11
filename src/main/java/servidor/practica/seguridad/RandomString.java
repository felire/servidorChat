package servidor.practica.seguridad;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString 
{
	  private static final Random RANDOM = new SecureRandom();

	  public static String generateRandomToken()
	  {
	      String letters = "abcdefghijklmnñpqrstuvwxyzABCDEFGHIJKMNÑOPQRSTUVWXYZ123456789+-@<>|¡!*/\\&#$%&()=¿?\"\'";
	      String password = "";
	      int passwordLength = RANDOM.nextInt(15) + 25;
	      for (int i = 0; i < passwordLength; i++)
	      {
	          int index = (int)(RANDOM.nextDouble()*letters.length());
	          password += letters.substring(index, index+1);
	      }
	      return password;
	  }
}
