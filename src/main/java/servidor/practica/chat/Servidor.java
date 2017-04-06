package servidor.practica.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Servidor implements Runnable{
	public List<Usuario> usuariosConectados;
	private ServerSocket socketS;
	private Socket socketC;
	private Thread thread;
	private ChatServerThread peticionCliente;
	
	public Servidor(int puerto){
		usuariosConectados = new ArrayList<Usuario>();
		try {
			socketS = new ServerSocket(puerto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Arranca server..");
		this.start();
	}
	@Override
	public void run() {
		while (thread != null)
	      {  try
	         {  System.out.println("Waiting for a client ..."); 
	            addThread(socketS.accept());
	         }
	         catch(IOException ie)
	         {  System.out.println("Acceptance Error: " + ie); }
	      }		
	}
	
	public void addThread(Socket socket)
	   { 
		  System.out.println("Client accepted: " + socket);
	      peticionCliente = new ChatServerThread(this, socket);
	      try
	      {  peticionCliente.open();
	      	 peticionCliente.start();
	      }
	      catch(IOException ioe)
	      {  System.out.println("Error opening thread: " + ioe); }
	   }
	
	public void addUsuario(Usuario usuario){
		this.usuariosConectados.add(usuario);
	}
	
	public Usuario darUsuario(String id){
		List<Usuario> lista = this.usuariosConectados.stream().filter(u->u.soyUsuario(id)).collect(Collectors.toList());
		if(lista.size() == 0){
			return null;
		}
		else{
			return lista.get(0);
		}
		
	}
	public void start()
	   {  if (thread == null)
	      {  thread = new Thread(this); 
	         thread.start();
	      }
	   }
	
	public void stop()
	   {  if (thread != null)
	      {  thread.stop(); 
	         thread = null;
	      }
	   }
	
	public static void main(String args[])
	   {  Servidor server = null;
	         server = new Servidor(2023);
	   }
}
