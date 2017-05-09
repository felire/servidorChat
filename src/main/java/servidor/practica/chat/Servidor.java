package servidor.practica.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Servidor implements Runnable
{
	private List<Usuario> usuarios;
	private static Servidor obj = null;
	private ServerSocket socketS;
	private Thread thread;
	private List<ChatServerThread> chats;
	private List<Mensaje> mensajesPendientes;
	private HashMap<String, String> tokens;
	
	public Servidor(int puerto)
	{
		usuarios = new ArrayList<Usuario>();
		chats = new ArrayList<ChatServerThread>();
		mensajesPendientes = new ArrayList<Mensaje>();
		tokens = new HashMap<String, String>();
		thread = null;
		try 
		{
			socketS = new ServerSocket(puerto);
			System.out.println("Arranca server..");
			this.start();
		}
		catch (IOException e) 
		{
			System.out.println("Error al crear serverSocket" + e);
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
			System.out.println("Error " + e);
		}
	}
		
	public void run()
	{
		System.out.println("Waiting for clients ..."); 
		while (thread != null)
		{
			try
	        {  
	            this.addThread(socketS.accept());
	        }
	        catch(IOException ie)
	        {  
	        	System.out.println("Acceptance Error: " + ie);
	        }
	    }
	}
	
	private void addThread(Socket socket)
	{
		System.out.println("Client accepted: " + socket);
		ChatServerThread peticionCliente = new ChatServerThread(socket);
		chats.add(peticionCliente);
	    try
	    {
	    	peticionCliente.open();
	    	peticionCliente.start();
	    }
	    catch(IOException ioe)
	    {
	    	System.out.println("Error opening thread: " + ioe);
	    	chats.remove(peticionCliente);
	    	try {
				peticionCliente.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
		
	public synchronized Optional<Usuario> getUsuario(String id)
	{
		return usuarios.stream().filter(u->u.soyUsuario(id)).findFirst();
	}
	
	public synchronized Usuario generarUsuario(String id, Socket socket, DataOutputStream streamOut, DataInputStream streamIn)
	{
		Optional<Usuario> user = getUsuario(id);
		Usuario usuario;
		if(user.isPresent())
		{
			usuario = user.get();
		}
		else
		{
			usuario = new Usuario(id);
			usuarios.add(usuario);
		}
		usuario.abrirSocket(socket, streamOut, streamIn);
		return usuario;
	}
	
	public Boolean autenticar(Usuario usuario) throws Exception
	{
		String token;
		if(tokens.containsKey(usuario.id) == false)//primera vez que se conecta
		{
			token = usuario.leer();
			tokens.put(usuario.id, token);
			usuario.escribir(TipoMensaje.OK.string());
			usuario.puerto = usuario.leer(); //puerto en el que espera conexiones
			return true;
		}
		String desafio = RandomString.generateRandomToken();

		token = tokens.get(usuario.id);
		int mid = token.length()/2;
		String mezcla = token.substring(0, mid);
		mezcla.concat(desafio);
		mezcla.concat(token.substring(mid, token.length()));
		
		usuario.escribir(desafio);	
		String respuesta = usuario.leer();
		
		Boolean autenticado = Hash.sha256(mezcla).equals(respuesta);
		if(autenticado == false)
		{
			usuario.escribir(TipoMensaje.ERROR.string());
			return false;
		}
		usuario.escribir(TipoMensaje.OK.string());
		usuario.puerto = usuario.leer(); //puerto en el que espera conexiones
		Servidor.obj().mandarMensajesPendientes(usuario);
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
	
	public void comunicar(Usuario usuario) throws IOException
	{
		String idRemitente = usuario.leer();
		Optional<Usuario> compañero = Servidor.obj().getUsuario(idRemitente);
		compañero.ifPresent(llamado ->
		{
			usuario.escribir(TipoMensaje.DATOSDECONEXION.string());
			usuario.escribir(llamado.ip);
			usuario.escribir(llamado.puerto);
		});
		if(!compañero.isPresent())
		{
			usuario.escribir(TipoMensaje.NOESTADISPONIBLE.string());
		}
	}
	
	public void addMensajePendiente(Usuario usuario) throws IOException
	{
		String idRemitente = usuario.leer();
		String mensajePendiente = usuario.leer();
		Mensaje mensaje = new Mensaje(usuario.id, idRemitente, mensajePendiente);
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
			try {
				chat.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void main(String args[])
	{
		Servidor.obj().start();
	}
}
