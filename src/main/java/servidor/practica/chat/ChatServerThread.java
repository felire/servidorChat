package servidor.practica.chat;

import servidor.practica.mensajes.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatServerThread extends Thread
{
	private DataInputStream streamIn =  null;
	private DataOutputStream streamOut = null;
	private Socket socket;
	
	public ChatServerThread(Socket socket)
	{
		this.socket = socket;
	}
	
	public int indexOf(String string, char caracter)
	{
		int lastof = string.indexOf(caracter);
		if(lastof == -1) return string.length();
		else return lastof;
	}
	
	public String primerSubstring(String mensaje)
	{
		return mensaje.substring(0, indexOf(mensaje, ':'));
	}
	
	public String segundoSubstring(String mensaje)
	{
		if(mensaje.indexOf(':') == -1) return "";
		return mensaje.substring(indexOf(mensaje, ':') + 1, mensaje.length());
	}

	public void run()
	{
		System.out.println("Server Thread " + socket.getPort() + " running.");
		try
		{
			String idUsuario_puerto = streamIn.readUTF();
			String idUsuario = primerSubstring(idUsuario_puerto);
			String puerto = segundoSubstring(idUsuario_puerto);

			System.out.println("Manejando al usuario de id: " + idUsuario);
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
				String tipoMsj_resto = streamIn.readUTF();
				String tipoMsj = primerSubstring(tipoMsj_resto);
				tipo = TipoMensaje.values()[Integer.parseInt(tipoMsj)];
				switch(tipo)
				{
					case CIERROSOCKET:
						usuario.cierroSocket();
						break;
					case HABLARCON:
						String idLlamado = segundoSubstring(tipoMsj_resto);
						Servidor.obj().comunicar(usuario, idLlamado);
						break;
					case MENSAJEPENDIENTE:
						String idRemitente_texto = segundoSubstring(tipoMsj_resto);
						String idRemitente = primerSubstring(idRemitente_texto);
						String texto = segundoSubstring(idRemitente_texto);
						Mensaje msjPendiente = new Mensaje(usuario.id, idRemitente, texto);
						Servidor.obj().addMensajePendiente(msjPendiente);
					default:
						break;
				}
			}
			return;
		}catch(Exception ioe) {
			try{
				this.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void open() throws IOException
	{
		streamIn = new DataInputStream(socket.getInputStream());
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	public void close() throws IOException
	{
		if (streamIn != null) streamIn.close();
		//if (streamOut != null) streamOut.close();
	}
}
