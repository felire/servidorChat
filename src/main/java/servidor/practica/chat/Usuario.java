package servidor.practica.chat;

import java.io.DataInputStream;
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
	private DataInputStream streamIn;
		
	public Usuario(String id)
	{
		this.id = id;
	}
	
	public void cierroSocket() throws IOException
	{
		streamOut.close();
		streamIn.close();
		socket.close();
	}
	
	public void abrirSocket(Socket socket, DataOutputStream streamOut, DataInputStream streamIn)
	{
		this.socket = socket;
		this.ip = socket.getInetAddress().toString().substring(1);
		this.streamOut = streamOut;
		this.streamIn = streamIn;
	}
	
	public String leer() throws IOException
	{
		return streamIn.readUTF();
	}
	
	public void escribir(String texto)
	{
		try {
			streamOut.writeUTF(texto);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean soyUsuario(String id_)
	{
		return id.equals(id_);
	}
	
	public void recibirPendientes(ArrayList<Mensaje> mensajes)
	{
		try {
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
