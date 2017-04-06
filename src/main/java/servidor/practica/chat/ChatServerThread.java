package servidor.practica.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatServerThread extends Thread
{
	private Socket socket = null;
	private Servidor server = null;
	private int ID = -1;
	private DataInputStream streamIn =  null;
	private DataOutputStream streamOut = null;
	
	public ChatServerThread(Servidor _server, Socket _socket)
	{
		server = _server;
		socket = _socket;
		ID = socket.getPort();
	}

	public void run()
	{
		System.out.println("Server Thread " + ID + " running.");
		try
		{
			String id = streamIn.readUTF();
			System.out.println(id);
			Usuario usuario = new Usuario(socket, id);
			server.addUsuario(usuario);
			while (true)
			{
				streamIn.readUTF(); //Queda  a la espera de nuevos mensajes.
			}
		}
		catch(IOException ioe) 
		{
			System.out.println("Error : " + ioe);
		}
	}
	
	public void open() throws IOException
	{
		streamIn = new DataInputStream(socket.getInputStream());
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	public void close() throws IOException
	{
		if (socket != null)
		{
			socket.close();
		}
		if (streamIn != null)
		{
			streamIn.close();
		}
	}
}
