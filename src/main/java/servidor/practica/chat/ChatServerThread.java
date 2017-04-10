package servidor.practica.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatServerThread extends Thread
{
	private DataInputStream streamIn =  null;
	private DataOutputStream streamOut = null;
	private Usuario usuario;
	
	public ChatServerThread(Socket socket)
	{
		this.usuario = new Usuario(socket);
	}

	public void run()
	{
		System.out.println("Server Thread " + usuario.socket.getPort() + " running.");
		try
		{
			usuario.id = streamIn.readUTF();
			System.out.println(usuario.id);
			Servidor.obj().seConectoUsuario(usuario);
			String id, mensaje;
			while(true)
			{
				id = streamIn.readUTF(); //id de la persona con la que hablamos
				mensaje = streamIn.readUTF(); //Mensaje que mando
				System.out.println(mensaje);
				Mensaje msj = new Mensaje(usuario.id, id, mensaje);
				Servidor.obj().enviarMensaje(msj);
			}
		}catch(IOException ioe) {
			try{
				this.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void open() throws IOException
	{
		streamIn = new DataInputStream(usuario.socket.getInputStream());
		streamOut = new DataOutputStream(usuario.socket.getOutputStream());
	}
	
	public void close() throws IOException
	{
		usuario.socket.close();		
		if(usuario != null) Servidor.obj().seDesconectoUsuario(usuario);
		if (streamIn != null) streamIn.close();
	}
}
