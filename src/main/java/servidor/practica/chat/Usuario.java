package servidor.practica.chat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Usuario 
{
	public String id;
	public Socket socket;
	public String puerto;
	private DataOutputStream streamOut; //el streamIn lo maneja cada hilo
	
	public Boolean tieneDatosDeConexion()
	{
		return (/*ip != null && */puerto != null);
	}
	
	public Usuario(Socket socket)
	{
		this.puerto = null;
		this.socket = socket;
		try {
			this.streamOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String ip()
	{
		return socket.getInetAddress().toString();
	}
	
	public Boolean soyUsuario(String id_)
	{
		return id.equals(id_);
	}
	
	public void recibir(String mensaje)
	{
		try {
			streamOut.writeUTF(mensaje);	 
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void recibirPendiente(Mensaje mensaje)
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
