package servidor.practica.chat;

import servidor.practica.mensajes.Mensaje;
import servidor.practica.seguridad.AES;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Usuario 
{
	public String id;
	private Socket socket;
	private String puerto;
	public DataOutputStream streamOut;
	public DataInputStream streamIn;
	private Logger logger;
	private String token;

	public Usuario(String id, String puerto, Logger logger)
	{
		this.id = id;
		this.puerto = puerto;
		this.logger = logger;
	}
	
	public void setToken(String token)
	{
		this.token = token;
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
	
	private void log(Level nivel, String msg)
	{
		logger.log(nivel, id + ": " + msg);
	}
	
	public String leer()
	{
		try {
			String ciphertext = streamIn.readUTF();
			System.out.println("user " + id + " nos mando " + AES.desencriptar(token, ciphertext));
			return AES.desencriptar(token, ciphertext);
		} catch (Exception e) {
			log(Level.SEVERE, "Fallo de lectura " + e);
		}
		return null;
	}
	
	public void escribir(String texto)
	{
		try {
			String ciphertext = AES.encriptar(token, texto);
			System.out.println("user " + id + " le mandamos " + texto);
			streamOut.writeUTF(ciphertext);
		} catch (Exception e) {
			log(Level.SEVERE, "Fallo envio del texto: " + texto + " " + e);
		}
	}
	
	public Boolean soy(String id_)
	{
		return id.equals(id_);
	}
	
	public void recibirPendientes(ArrayList<Mensaje> mensajes)
	{
		escribir(String.valueOf(mensajes.size()));
		mensajes.forEach(mensaje ->
		{
			String emisor_mensaje = mensaje.emisor + ":" + mensaje.mensaje;
			escribir(emisor_mensaje);		
		});
	}
}
/*
 * El usuario tiene el socket para comunicarse con el cliente,
 * sabe recibir una lista de mensajes y enviarla.
 * Tambien guarda la informacion necesaria para que otros clientes
 * se puedan comunicar con el.
 */
