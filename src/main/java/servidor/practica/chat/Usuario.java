package servidor.practica.chat;

import servidor.practica.mensajes.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Usuario 
{
	public String id;
	private Socket socket;
	private String puerto;
	private DataOutputStream streamOut;
	private DataInputStream streamIn;
		
	public Usuario(String id, String puerto)
	{
		this.id = id;
		this.puerto = puerto;
	}
	
	public String datosDeConexion()
	{
		return socket.getInetAddress().toString().substring(1) + ":" + puerto;
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
	
	public Boolean soy(String id_)
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
					String emisor_mensaje = mensaje.emisor + ":" + mensaje.mensaje;
					streamOut.writeUTF(emisor_mensaje);
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
/*
 * El usuario tiene el socket para comunicarse con el cliente,
 * sabe recibir una lista de mensajes y enviarla.
 * Tambien guarda la informacion necesaria para que otros clientes
 * se puedan comunicar con el.
 */
