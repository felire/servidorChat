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
	public List<Usuario> usuariosConectados;
	private static Servidor obj = null;
	private ServerSocket socketS;
	private Thread thread;
	private List<ChatServerThread> chats;
	private Pendientes mensajesPendientes;
	private Thread threadPendientes;
	private Semaphore semaforo;
	
	public static Servidor obj()
	{
		if(obj == null)
		{
			obj = new Servidor(2023);
		}
		return obj;
	}
	
	public static Servidor obj(int puerto)
	{
		if(obj == null)
		{
			obj = new Servidor(puerto);
		}
		return obj;
	}
	
	public Servidor(int puerto)
	{
		usuariosConectados = new ArrayList<Usuario>();
		chats = new ArrayList<ChatServerThread>();
		mensajesPendientes = new Pendientes();
		threadPendientes = new Thread(mensajesPendientes);
		semaforo = new Semaphore(1);
		threadPendientes.start();
		try 
		{
			socketS = new ServerSocket(puerto);
		}
		catch (IOException e) 
		{
			System.out.println("Error " + e);
		}
		System.out.println("Arranca server..");
		this.start();
	}
	
	public void run()
	{
		while (thread != null)
		{  
			try
	        {  
				System.out.println("Waiting for a client ..."); 
	            addThread(socketS.accept());
	        }
	        catch(IOException ie)
	        {  
	        	System.out.println("Acceptance Error: " + ie);
	        }
	    }
	}
	
	public void addThread(Socket socket)
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
	    }
	}
	
	public void seDesconectoUsuario(String id)
	{
		this.getUsuario(id).ifPresent(usr ->
		{
			try {
				semaforo.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			usuariosConectados.remove(usr);
			semaforo.release();
		});
	}

	public void addUsuario(Usuario usuario)
	{
		try {
			semaforo.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.usuariosConectados.add(usuario);
		semaforo.release();
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
	}
	
	public static void main(String args[])
	{
		Servidor.obj(2023).start();
	}

	public void enviarMensaje(String idEmisor,String idReceptor, String mensaje) {
		Optional<Usuario> opUsuario = this.getUsuario(idReceptor);
		opUsuario.ifPresent(usr -> usr.recibirMensaje(idEmisor, mensaje));
		if(!opUsuario.isPresent())
		{
			Mensaje mensajeP = new Mensaje(idEmisor,idReceptor,mensaje);
			mensajesPendientes.addMensaje(mensajeP);
		}
	}
}
