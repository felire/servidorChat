package servidor.practica.chat;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString 
{
	  private static final Random RANDOM = new SecureRandom();
	  /** Length of password. @see #generateRandomToken() */
	  public static final int PASSWORD_LENGTH = RANDOM.nextInt(15) + 25;
	  /**
	   * Generate a random String suitable for use as a temporary password.
	   *
	   * @return String suitable for use as a temporary password
	   * @since 2.4
	   */
	  public static String generateRandomToken()
	  {
	      String letters = "abcdefghijklmnñpqrstuvwxyzABCDEFGHIJKMNÑOPQRSTUVWXYZ123456789+-@<>|¡!*/\\&#$%&()=¿?\"\'";

	      String pw = "";
	      for (int i=0; i<PASSWORD_LENGTH; i++)
	      {
	          int index = (int)(RANDOM.nextDouble()*letters.length());
	          pw += letters.substring(index, index+1);
	      }
	      return pw;
	  }
}