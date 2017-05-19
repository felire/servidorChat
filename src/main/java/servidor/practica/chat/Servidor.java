package servidor.practica.chat;

import servidor.practica.mensajes.Mensaje;
import servidor.practica.mensajes.TipoMensaje;
import servidor.practica.seguridad.Hash;
import servidor.practica.seguridad.RSA;
import servidor.practica.seguridad.RandomString;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.NoSuchFileException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * El Servidor hace de repositorio de clientes, se encarga de autenticarlos, de guardar y mandar los
 * mensajes pendientes y de pasar los datos de conexion para que los clientes puedan comunicarse entre si
 */

public class Servidor implements Runnable
{
	private List<Usuario> usuarios;
	private static Servidor obj = null;
	private ServerSocket socketS;
	private Thread thread;
	private List<Mensaje> mensajesPendientes;
	private Logger logger;
	private KeyPair keyPair;
	
	public Servidor(int puerto)
	{
		usuarios = new ArrayList<Usuario>();
		mensajesPendientes = new ArrayList<Mensaje>();
		thread = null;
		keyPair = RSA.generateKeyPair();
		logger = Logger.getLogger("MyLog");  
	    FileHandler fh;
	    try { 
	    	try{
	    		fh = new FileHandler("/tmp/logFile.log");  
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
	    //logger.setUseParentHandlers(false); //para que no aparezca nada en la consola
		try 
		{
			socketS = new ServerSocket(puerto);
			log(Level.INFO, "Arranca el server..");
			this.start();
		}
		catch (IOException e) 
		{
			log(Level.SEVERE,"Error al crear serverSocket " + e);
		}
	}
	
	public static Servidor obj() 
	{
		if(obj == null)
		{
			obj = new Servidor(2023);//el puerto por defecto es 2023
		}
		return obj;
	}
	
	public void setPort(int puerto)
	{
		try 
		{
			socketS.close();
			socketS = new ServerSocket(puerto);
		}
		catch (IOException e) 
		{
			log(Level.SEVERE, "Error al cambiar el puerto del server: " + e);
		}
	}
		
	public void run()
	{
		log(Level.FINE, "Waiting for clients ...");
		while (thread != null)
		{
			try
			{
				this.addThread(socketS.accept());
			}
			catch(IOException ie)
			{
				log(Level.WARNING, "Error al aceptar conexion al server socket: " + ie);
			}
		}
	}
	
	private void addThread(Socket socket)
	{
		log(Level.FINE, "Client accepted: " + socket);
		ChatServerThread peticionCliente = new ChatServerThread(socket, logger);
	    try
	    {
	    	peticionCliente.open();
	    	peticionCliente.start();
	    }
	    catch(IOException ioe)
	    {
	    	log(Level.WARNING, "Error al abrir thread" + ioe);
	    	peticionCliente.close();
	    }
	}
	
	private void log(Level nivel, String msg)
	{
		logger.log(nivel, msg);
	}
	
	private synchronized Optional<Usuario> getUsuario(String id)
	{
		return usuarios.stream().filter(usuario->usuario.soy(id)).findFirst();
	}
	
	public Usuario generarUsuario(String id, String puerto, Socket socket, DataOutputStream streamOut, DataInputStream streamIn)
	{
		Optional<Usuario> user = getUsuario(id);
		Usuario usuario;
		if(user.isPresent())
		{
			usuario = user.get();
		}
		else
		{
			usuario = new Usuario(id, puerto, logger);
			synchronized(this)
			{
				usuarios.add(usuario);
			}
			log(Level.INFO,"Agrego al usuario " + id);
		}
		usuario.abrirSocket(socket, streamOut, streamIn);
		return usuario;
	}
	
	/**
	 * mandamos el desafio encriptado con el token del usuario con AES128, el lo decripta
	 * y lo encripta con la clave publica del server, despues le hace el sha256 y lo envia encriptado
	 * con el toquen con AES128. Se puede hacer de muchas maneras esto, la idea es que solo el usuario
	 * real pueda resolver el desafio.
	 */
	private String resolverDesafio(Usuario usuario, String desafio) throws Exception
	{
		String respuesta = RSA.encrypt(desafio, keyPair.getPublic());
		return Hash.sha256(respuesta);
	}
		
	public Boolean autenticar(Usuario usuario) throws Exception
	{
		if(usuario.tieneToken())
		{
			String desafio = RandomString.generateRandomToken();
			usuario.escribir(desafio);
			String respuestaUsuario = usuario.leer();
			String respuestaEsperada = resolverDesafio(usuario, desafio);
			
			Boolean autenticado = respuestaEsperada.equals(respuestaUsuario);
			if(autenticado == false)
			{
				log(Level.SEVERE, "Fallo de autenticacion, usuario: " + usuario.id + ". Se esperaba: " + respuestaEsperada + " y se obtuvo: " + respuestaUsuario);
				return false;
			}
		}
		else//primera vez que se conecta
		{
			String publica = RSA.savePublicKey(keyPair.getPublic());//le pasamos la clave publica
			usuario.escribir(publica);
			String tokenEncriptado = usuario.leer();
			String token = RSA.decrypt(tokenEncriptado, keyPair.getPrivate());
			usuario.setToken(token);
		}
		mandarMensajesPendientes(usuario);
		return true;
	}
	
	private void mandarMensajesPendientes(Usuario usuario)
	{
		ArrayList<Mensaje> pendientes = new ArrayList<Mensaje>();
		mensajesPendientes.removeIf(msj -> 
		{
			if(msj.esPara(usuario))
			{
				pendientes.add(msj);
				return true;
			}
			else return false;
		});
		usuario.recibirPendientes(pendientes);
	}
	
	public void comunicar(Usuario usuario, String idRemitente)
	{
		Optional<Usuario> compañero = getUsuario(idRemitente);
		compañero.ifPresent(llamado ->
		{
			String datosDeConexion = llamado.datosDeConexion();
			usuario.escribir(datosDeConexion);
		});
		if(!compañero.isPresent())
		{
			log(Level.SEVERE, "Se intento contactar al usuario de id: " + idRemitente + " y este no existe");
			usuario.escribir(TipoMensaje.NOESTADISPONIBLE.string());
		}
	}
	
	public void addMensajePendiente(Mensaje mensaje) throws IOException
	{
		mensajesPendientes.add(mensaje);
	}
	
	public void start()
	{
		if (thread == null)
	    {
			thread = new Thread(this); //se encarga de llamar al metodo run()
	        thread.start();
	    }
	}
	
	public void stop()
	{
		if (thread != null)
	    {
			thread.interrupt();
	        thread = null;
	    }
	}
	
	public static void main(String args[]) throws Exception
	{
		Servidor.obj().start();
	}
}
