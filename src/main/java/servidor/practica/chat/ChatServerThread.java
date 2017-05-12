package servidor.practica.chat;

import servidor.practica.mensajes.Mensaje;
import servidor.practica.mensajes.TipoMensaje;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerThread extends Thread
{
	private DataInputStream streamIn =  null;
	private DataOutputStream streamOut = null;
	private Socket socket;
	private Logger logger;
	
	public ChatServerThread(Socket socket, Logger logger)
	{
		this.socket = socket;
		this.logger = logger;
	}
	
	public int indexOf(String string, char caracter)
	{
		int posicion = string.indexOf(caracter);
		if(posicion == -1) posicion = string.length();
		return posicion;
	}
	
	private String primerSubstring(String mensaje)
	{
		return mensaje.substring(0, indexOf(mensaje, ':'));
	}
	
	private String segundoSubstring(String mensaje)
	{
		if(mensaje.indexOf(':') == -1) return "";
		return mensaje.substring(indexOf(mensaje, ':') + 1, mensaje.length());
	}
	
	private void log(Level nivel, String msg)
	{
		logger.log(nivel, socket.getPort() + ": " + msg);
	}

	public void run()
	{
		try
		{
			String idUsuario_puerto = streamIn.readUTF();//el usuario se identifica
			String idUsuario = primerSubstring(idUsuario_puerto);
			String puerto = segundoSubstring(idUsuario_puerto);

			log(Level.INFO, "Manejando al usuario: " + idUsuario);
			
			Usuario usuario = Servidor.obj().generarUsuario(idUsuario, puerto, socket, streamOut, streamIn);
			Boolean valido = Servidor.obj().autenticar(usuario);
			if(valido == false)
			{
				this.close();
				return;
			}
			
			TipoMensaje tipo = null;
			while(tipo != TipoMensaje.CIERROSOCKET)
			{
				String header_payload = usuario.leer();
				String header = primerSubstring(header_payload);
				tipo = TipoMensaje.values()[Integer.parseInt(header)];
				switch(tipo)
				{
					case CIERROSOCKET:
						usuario.cierroSocket();
						break;
					case HABLARCON:
						String idLlamado = segundoSubstring(header_payload);
						Servidor.obj().comunicar(usuario, idLlamado);
						break;
					case MENSAJEPENDIENTE:
						String idRemitente_texto = segundoSubstring(header_payload);
						String idRemitente = primerSubstring(idRemitente_texto);
						String texto = segundoSubstring(idRemitente_texto);
						Mensaje msjPendiente = new Mensaje(usuario.id, idRemitente, texto);
						Servidor.obj().addMensajePendiente(msjPendiente);
					default:
						break;
				}
			}
			log(Level.INFO, "Terminado.");
			return;
		}catch(Exception ioe) {
			log(Level.WARNING, "Error manejando usuario " + socket +  " " + ioe);
			this.close();
		}
	}
	
	public void open() throws IOException
	{
		streamIn = new DataInputStream(socket.getInputStream());
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	public void close()
	{
		log(Level.INFO, "Close() invocado.");
		try{
			if (streamIn != null) streamIn.close();
			if (streamOut != null) streamOut.close();
			if (socket != null) socket.close();
		} catch (IOException e) {
			log(Level.WARNING, "Error al cerrar el ChatServerThread" + e);
		}
	}
}
/*
 *El ChatServerThread se encarga de recibir las request del cliente y enviarselas al servidor,
 *una vez que el cliente termina, el hilo se cierra inmediatamente para causar el minimo
 *overhead posible al server.
 *Antes de aceptar sus pedidos, le pide al servidor que verifique que el usuario es quien dice ser.
 *El usuario puede mandar mas de un dato de informacion por mensaje, los distintos fragmentos estan
 *separados por ':' y se manejan con los metodos primerSubstring y segundoSubstring.
 */
