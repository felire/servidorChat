package servidor.practica.chat;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager 
{
	private Logger logger;
	private Semaphore lock;
	
	public LogManager(String path)
	{
		lock = new Semaphore(1);
		logger = Logger.getLogger("MyLog");  
	    FileHandler fh;
	    try {
	    	try{
	    		fh = new FileHandler(path);//"/tmp/logFile.log"
	    	} catch (NoSuchFileException e){
	    		System.out.println("No se pudo abrir el archivo de logs: /tmp/logFile.log");
	    		return;
	    	}
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();
	        fh.setFormatter(formatter);
	    } catch (SecurityException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    logger.setLevel(Level.INFO);
	    logger.setUseParentHandlers(false); //para que no aparezca nada en la consola
	}
	
	public void log(Level lv, String texto)
	{
		try {
			lock.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
				}
		logger.log(lv, texto);
		lock.release();
	}
}
