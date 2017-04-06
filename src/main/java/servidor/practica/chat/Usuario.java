package servidor.practica.chat;

import java.net.Socket;

public class Usuario 
{
	String id;
	Socket socket;
	
	public Usuario(Socket socket, String id)
	{
		this.socket = socket;
		this.id = id;
	}
	
	public Boolean soyUsuario(String id_)
	{
		return id.equals(id_);
	}
}
