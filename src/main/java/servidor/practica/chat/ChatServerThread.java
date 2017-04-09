package servidor.practica.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatServerThread extends Thread
{
	private Socket socket = null;
	private int ID = -1;
	private DataInputStream streamIn =  null;
	private DataOutputStream streamOut = null;
	private String idUsuario;
	
	public ChatServerThread(Socket _socket)
	{
		socket = _socket;
		ID = socket.getPort();
	}

	public void run()
	{
		System.out.println("Server Thread " + ID + " running.");
		try
		{
			String id = streamIn.readUTF();
			this.idUsuario = id;
			String mensaje;
			System.out.println(id);
			Usuario usuario = new Usuario(socket, id);
			Servidor.obj().addUsuario(usuario);
			while(true)
			{
				id = streamIn.readUTF(); //A quien le quiero hablar, el id ahora no es mas el del user dueño del Socket, es de la persona a la que le hablamos
				mensaje = streamIn.readUTF(); //Mensaje que mando
				System.out.println(mensaje);
				Mensaje msj = new Mensaje(idUsuario,id, mensaje);
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
		streamIn = new DataInputStream(socket.getInputStream());
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	public void close() throws IOException
	{
		if(idUsuario != null) Servidor.obj().seDesconectoUsuario(idUsuario);
		if (socket != null) socket.close();
		if (streamIn != null) streamIn.close();
	}
}
