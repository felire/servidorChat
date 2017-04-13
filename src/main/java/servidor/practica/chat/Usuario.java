package servidor.practica.chat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Usuario 
{
	public String id;
	public Socket socket;
	private DataOutputStream streamOut; //el streamIn lo maneja cada hilo
	
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
	
	public Usuario(Socket socket)
	{
		this.socket = socket;
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
	
	public void recibir(Mensaje mensaje)
	{
		try {
			streamOut.writeUTF(mensaje.emisor);	 
			streamOut.writeUTF(mensaje.mensaje);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
