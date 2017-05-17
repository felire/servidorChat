package servidor.practica.chat;

import servidor.practica.mensajes.Mensaje;
import servidor.practica.mensajes.TipoMensaje;
import servidor.practica.seguridad.AES;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * El usuario tiene el socket para comunicarse con el cliente,
 * sabe recibir una lista de mensajes y enviarla.
 * Tambien guarda la informacion necesaria para que otros clientes
 * se puedan comunicar con el.
 */

public class Usuario 
{
	public String id;
	private Socket socket;
	private String puerto;
	private DataOutputStream streamOut;
	private DataInputStream streamIn;
	private Logger logger;
	private String token;

	public Usuario(String id, String puerto, Logger logger)
	{
		this.id = id;
		this.puerto = puerto;
		this.logger = logger;
		this.token = null;
	}
	
	public Boolean tieneToken()
	{
		return (token != null);
	}
	
	public void setToken(String token)
	{
		if(this.token == null) this.token = token; //nunca sobre escribimos el token
	}
	
	public String datosDeConexion()
	{
		return TipoMensaje.DATOSDECONEXION.string() + ":" + socket.getInetAddress().toString().substring(1) + ":" + puerto;
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
		String mensaje;
		try {
			mensaje = streamIn.readUTF();
			if(this.tieneToken())
			{
				mensaje = AES.desencriptar(token, mensaje);
			}
			log(Level.FINEST, "El usuario " + id + " nos mando: " + mensaje);
			return mensaje;
		} catch (Exception e) {
			log(Level.WARNING, "Fallo de lectura " + e);
			return null;
		}
	}
	
	public void escribir(String texto)
	{
		try {
			log(Level.FINEST, "Le mandamos al usuario " + id + " " + texto);
			if(this.tieneToken())
			{
				texto = AES.encriptar(token, texto);
			}
			streamOut.writeUTF(texto);
		} catch (Exception e) {
			log(Level.WARNING, "Fallo envio del texto: " + texto + " " + e);
		}
	}
	
	public Boolean soy(String id_)
	{
		return id.equals(id_);
	}
	
	public void recibirPendientes(ArrayList<Mensaje> mensajes)
	{
		String cantidad = String.valueOf(mensajes.size());
		escribir(cantidad);
		mensajes.forEach(mensaje ->
		{
			escribir(mensaje.contenido());		
		});
	}
}

