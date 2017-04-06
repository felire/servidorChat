package servidor.practica.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor implements Runnable
{
	public List<Usuario> usuariosConectados;
	private ServerSocket socketS;
	private Thread thread;
	private List<ChatServerThread> chats;
	
	public Servidor(int puerto)
	{
		usuariosConectados = new ArrayList<Usuario>();
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
		ChatServerThread peticionCliente = new ChatServerThread(this, socket);
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
	
	public void addUsuario(Usuario usuario)
	{
		usuariosConectados.add(usuario);
	}
	
	public Usuario getUsuario(String id)
	{
		return this.usuariosConectados.stream().filter(usr->usr.soyUsuario(id)).findFirst().orElseGet(null);
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
		Servidor server  = new Servidor(2023);
		server.start();
	}
}
