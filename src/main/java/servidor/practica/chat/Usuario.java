package servidor.practica.chat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Usuario 
{
	String id;
	Socket socket;
	private DataOutputStream streamOut; //Con el output basta, el streamIn lo maneja cada hilo
	
	public Usuario(Socket socket, String id)
	{
		this.socket = socket;
		this.id = id;
		try {
			this.streamOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean soyUsuario(String id_)
	{
		return id.equals(id_);
	}
	
	public Boolean soyUsuario(Socket socket_)
	{
		return socket.equals(socket_);
	}
	
	public void recibirMensaje(String emisor, String mensaje){
		try {
			streamOut.writeUTF(emisor);	 
			streamOut.writeUTF(mensaje);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
