package servidor.practica.chat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Usuario 
{
	public String id;
	public Socket socket;
	public String ip;
	public String puerto;
	private DataOutputStream streamOut;
		
	public Usuario(String id)
	{
		this.id = id;
	}
	
	public void cierroSocket() throws IOException
	{
		socket.close();
		streamOut.close();
	}
	
	public void abroSocket(Socket socket, DataOutputStream stream)
	{
		this.socket = socket;
		this.ip = socket.getInetAddress().toString().substring(1);
		this.streamOut = stream;
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
	
	public void recibirPendientes(ArrayList<Mensaje> mensajes)
	{
		try {
			System.out.println("mando un " + String.valueOf(mensajes.size()));
			streamOut.writeUTF(String.valueOf(mensajes.size()));
			mensajes.forEach(mensaje ->
			{
				try {
					streamOut.writeUTF(mensaje.emisor);
					streamOut.writeUTF(mensaje.mensaje);
				} catch (IOException e) {
					e.printStackTrace();
				}			
			});	 
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
