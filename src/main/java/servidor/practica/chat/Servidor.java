package servidor.practica.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class Servidor implements Runnable
{
	private List<Usuario> usuariosConectados;
	private static Servidor obj = null;
	private ServerSocket socketS;
	private Thread thread;
	private List<ChatServerThread> chats;
	private List<Mensaje> mensajesPendientes;
	private Semaphore semaforo;
	
	public Servidor(int puerto)
	{
		usuariosConectados = new ArrayList<Usuario>();
		chats = new ArrayList<ChatServerThread>();
		mensajesPendientes = new ArrayList<Mensaje>();
		semaforo = new Semaphore(1);
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
		while (thread != null)
		{
			try
	        {  
				System.out.println("Waiting for a client ..."); 
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
	
	public void seDesconectoUsuario(Usuario usuario)
	{
		try {
			semaforo.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		usuariosConectados.remove(usuario);
		semaforo.release();
	}

	public void seConectoUsuario(Usuario usuario)//se encarga de entregar mensajes pendientes
	{
		try {
			semaforo.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		usuariosConectados.add(usuario);
		semaforo.release();
		mensajesPendientes.removeIf(msj -> 
		{
			if(msj.receptor.equals(usuario.id))
			{
				usuario.recibir(msj);
				return true;
			}
			return false;
		});
	}
	
	public Optional<Usuario> getUsuario(String id)
	{
		try {
			semaforo.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Optional<Usuario> usr = usuariosConectados.stream().filter(u->u.soyUsuario(id)).findFirst();
		semaforo.release();
		return usr;
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
	
	public void enviarMensaje(Mensaje mensaje) 
	{
		Optional<Usuario> opUsuario = this.getUsuario(mensaje.receptor);
		opUsuario.ifPresent(usr -> usr.recibir(mensaje));
		if(!opUsuario.isPresent())
		{
			mensajesPendientes.add(mensaje);
		}
	}
	
	public static void main(String args[])
	{
		Servidor.obj().start();
	}

}
