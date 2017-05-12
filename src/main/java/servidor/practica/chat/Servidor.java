package servidor.practica.chat;

import servidor.practica.mensajes.Mensaje;
import servidor.practica.mensajes.TipoMensaje;
import servidor.practica.seguridad.Hash;
import servidor.practica.seguridad.RandomString;
import servidor.practica.seguridad.AES;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Servidor implements Runnable
{
	private List<Usuario> usuarios;
	private static Servidor obj = null;
	private ServerSocket socketS;
	private Thread thread;
	private List<ChatServerThread> chats;
	private List<Mensaje> mensajesPendientes;
	private HashMap<String, String> tokens;
	private Logger logger;
	
	public Servidor(int puerto)
	{
		usuarios = new ArrayList<Usuario>();
		chats = new ArrayList<ChatServerThread>();
		mensajesPendientes = new ArrayList<Mensaje>();
		tokens = new HashMap<String, String>();
		thread = null;
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
			logger.info("Arranca el server..");
			this.start();
		}
		catch (IOException e) 
		{
			log(Level.SEVERE,"Error al crear serverSocket" + e);
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
		log(Level.INFO, "Waiting for clients ...");
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
		log(Level.INFO, "Client accepted: " + socket);
		ChatServerThread peticionCliente = new ChatServerThread(socket, logger);
		chats.add(peticionCliente);
	    try
	    {
	    	peticionCliente.open();
	    	peticionCliente.start();
	    }
	    catch(IOException ioe)
	    {
	    	log(Level.WARNING, "Error al abrir thread" + ioe);
	    	chats.remove(peticionCliente);
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
	
	public synchronized Usuario generarUsuario(String id, String puerto, Socket socket, DataOutputStream streamOut, DataInputStream streamIn)
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
			usuarios.add(usuario);
			log(Level.INFO,"Agrego al usuario " + id);
		}
		usuario.abrirSocket(socket, streamOut, streamIn);
		return usuario;
	}
	
	private String resolverDesafio(Usuario usuario, String desafio) throws Exception
	{
		String token = tokens.get(usuario.id);
		int mid = token.length()/2;
		String mezcla = token.substring(0, mid);
		mezcla.concat(desafio);
		mezcla.concat(token.substring(mid, token.length()));
		return Hash.sha256(mezcla);
	}
	
	private String decodeToken(String tokenBase64)
	{
		byte[] encoded = tokenBase64.getBytes();
		byte[] decoded = Base64.getDecoder().decode(encoded);
		return new String(decoded);
	}
	
	public Boolean autenticar(Usuario usuario) throws Exception
	{
		String token;
		if(tokens.containsKey(usuario.id) == false)//primera vez que se conecta
		{
			String tokenBase64 = usuario.leer();//manda su token encodeado en base 64
			token = decodeToken(tokenBase64);
			tokens.put(usuario.id, token);
			usuario.escribir(TipoMensaje.OK.string());
			return true;
		}
		String desafio = RandomString.generateRandomToken();
		usuario.escribir(desafio);	
		String respuestaUsuario = usuario.leer();
		String respuestaEsperada = resolverDesafio(usuario, desafio);
		
		Boolean autenticado = respuestaEsperada.equals(respuestaUsuario);
		if(autenticado == false)
		{
			log(Level.SEVERE, "Fallo de autenticacion, usuario: " + usuario.id + ". Se esperaba: " + respuestaEsperada + " y se obtuvo: " + respuestaUsuario);
			usuario.escribir(TipoMensaje.ERROR.string());
			return false;
		}
		usuario.escribir(TipoMensaje.OK.string());
		mandarMensajesPendientes(usuario);
		return true;
	}
	
	private void mandarMensajesPendientes(Usuario usuario)
	{
		ArrayList<Mensaje> mensajesPorMandar = new ArrayList<Mensaje>();
		mensajesPendientes.removeIf(msj -> 
		{
			if(msj.receptor.equals(usuario.id))
			{
				mensajesPorMandar.add(msj);
				return true;
			}
			return false;
		});
		usuario.recibirPendientes(mensajesPorMandar);
	}
	
	public void comunicar(Usuario usuario, String idRemitente)
	{
		Optional<Usuario> compañero = getUsuario(idRemitente);
		compañero.ifPresent(llamado ->
		{
			String tipoMsj_ip_puerto = TipoMensaje.DATOSDECONEXION.string() + ":" + llamado.datosDeConexion();
			usuario.escribir(tipoMsj_ip_puerto);
		});
		if(!compañero.isPresent())
		{
			log(Level.WARNING, "Se intento contactar al usuario de id: " + idRemitente + " y este no existe");
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
		chats.forEach(chat -> 
		{
			chat.close();
		});
	}
	
	public static void main(String args[]) throws Exception
	{
		//Servidor.obj().start();
		String plain = "Este es el texto que queremos encriptar";
		byte [] rta = AES.encriptar("pepitooooooooooo", plain);  //16
		System.out.println(rta);
		String pplain = AES.desencriptar("pepitooooooooooo", rta);
		System.out.println(pplain);
		
	}
}
/*
 * El Servidor hace de repositorio de clientes, se encarga de autenticarlos, de guardar y mandar los
 * mensajes pendientes y de pasar los datos de conexion para que los clientes puedan comunicarse entre si
 */
