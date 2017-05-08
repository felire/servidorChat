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
	private List<Usuario> usuariosConectados;
	private static Servidor obj = null;
	private ServerSocket socketS;
	private Thread thread;
	private List<ChatServerThread> chats;
	private List<Mensaje> mensajesPendientes;
	private HashMap<String, String> tokens;
	
	public Servidor(int puerto)
	{
		usuariosConectados = new ArrayList<Usuario>();
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
	
	public synchronized void seDesconectoUsuario(Usuario usuario)
	{
		usuariosConectados.remove(usuario);
	}

	public void mandarMensajesPendientes(Usuario usuario)//se encarga de entregar mensajes pendientes
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
		//usuario.recibirPendientes(mensajesPorMandar);
	}
	
	public synchronized Optional<Usuario> getUsuario(String id)
	{
		return usuariosConectados.stream().filter(u->u.soyUsuario(id)).findFirst();
	}
	
	public synchronized Usuario generarUsuario(String id)
	{
		Optional<Usuario> user = usuariosConectados.stream().filter(u->u.soyUsuario(id)).findFirst();
		if(user.isPresent())
		{
			return user.get();
		}
		else
		{
			Usuario usuario = new Usuario(id);
			System.out.println("agrego a " + usuario.id);
			usuariosConectados.add(usuario);
			return usuario;
		}
	}
	
	public Boolean validar(Usuario usuario, String desafio, String respuesta) throws Exception
	{
		String id = usuario.id;
		if(!tokens.containsKey(id))//primera vez que se conecta
		{
			tokens.put(id, respuesta);
			return true;
		}
		String token = tokens.get(id);
		int mid = token.length()/2;
		String mezcla = token.substring(0, mid);
		mezcla.concat(desafio);
		mezcla.concat(token.substring(mid, token.length()));
		if(Hash.sha256(mezcla).equals(respuesta))
		{
			System.out.println("valido a " + usuario.id);
		}
		return Hash.sha256(mezcla).equals(respuesta);
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
	
	public void establecerConexion(Usuario llamador, Usuario llamado)
	{
		llamador.recibir(TipoMensaje.DATOSDECONEXION.string());
		llamador.recibir(llamado.ip);
		llamador.recibir(llamado.puerto);
	}
	
	public void mensajePendiente(Mensaje mensaje)
	{
		mensajesPendientes.add(mensaje);
	}
	
	public static void main(String args[])
	{
		Servidor.obj().start();
	}
}
