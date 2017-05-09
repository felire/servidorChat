package servidor.practica.chat;

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
	
	public synchronized Optional<Usuario> getUsuario(String id)
	{
		return usuarios.stream().filter(u->u.soyUsuario(id)).findFirst();
	}
	
	public synchronized Usuario generarUsuario(String id)
	{
		Optional<Usuario> user = usuarios.stream().filter(u->u.soyUsuario(id)).findFirst();
		if(user.isPresent())
		{
			return user.get();
		}
		else
		{
			Usuario usuario = new Usuario(id);
			usuarios.add(usuario);
			return usuario;
		}
	}
	
	public synchronized Boolean autenticar(Usuario usuario) throws Exception
	{
		String token;
		if(tokens.containsKey(usuario.id) == false)//primera vez que se conecta
		{
			token = usuario.streamIn.readUTF();
			tokens.put(usuario.id, token);
			usuario.streamOut.writeUTF(TipoMensaje.OK.string());
			usuario.puerto = usuario.streamIn.readUTF(); //puerto en el que espera conexiones
			System.out.println("el puerto es " + usuario.puerto );
			return true;
		}
		String desafio = RandomString.generateRandomToken();
		usuario.streamOut.writeUTF(desafio);	
		String respuesta = usuario.streamIn.readUTF();
		
		token = tokens.get(usuario.id);
		int mid = token.length()/2;
		String mezcla = token.substring(0, mid);
		mezcla.concat(desafio);
		mezcla.concat(token.substring(mid, token.length()));
		Boolean autenticado = Hash.sha256(mezcla).equals(respuesta);
		if(autenticado == false)
		{
			usuario.streamOut.writeUTF(TipoMensaje.ERROR.string());
			return false;
		}
		usuario.streamOut.writeUTF(TipoMensaje.OK.string());
		usuario.puerto = usuario.streamIn.readUTF(); //puerto en el que espera conexiones
		Servidor.obj().mandarMensajesPendientes(usuario);
		return true;
	}
	
	public void comunicar(Usuario usuario) throws IOException
	{
		String idRemitente = usuario.streamIn.readUTF();
		Optional<Usuario> compañero = Servidor.obj().getUsuario(idRemitente);
		compañero.ifPresent(llamado ->
		{
			try {
				usuario.streamOut.writeUTF(TipoMensaje.DATOSDECONEXION.string());
				usuario.streamOut.writeUTF(llamado.ip);
				usuario.streamOut.writeUTF(llamado.puerto);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		if(!compañero.isPresent())
		{
			usuario.streamOut.writeUTF(TipoMensaje.NOESTADISPONIBLE.string());
		}
	}
	
	public void addMensajePendiente(Usuario usuario) throws IOException
	{
		String idRemitente = usuario.streamIn.readUTF();
		String mensajePendiente = usuario.streamIn.readUTF();
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
