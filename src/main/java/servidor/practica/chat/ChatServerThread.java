package servidor.practica.chat;

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

	public void run()
	{
		System.out.println("Server Thread " + socket.getPort() + " running.");
		try
		{
			String idUsuario_puerto = streamIn.readUTF();
			String idUsuario = idUsuario_puerto.substring(0, idUsuario_puerto.indexOf(":"));
			String puerto = idUsuario_puerto.substring(idUsuario_puerto.indexOf(":") +1, idUsuario_puerto.length());

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
				tipo = TipoMensaje.values()[Integer.parseInt(tipoMsj_resto.substring(0, tipoMsj_resto.indexOf(":")))];
				switch(tipo)
				{
					case CIERROSOCKET:
						usuario.cierroSocket();
						break;
					case HABLARCON:
						String idLlamado = tipoMsj_resto.substring(tipoMsj_resto.indexOf(":") +1, tipoMsj_resto.length());
						Servidor.obj().comunicar(usuario, idLlamado);
						break;
					case MENSAJEPENDIENTE:
						String idRemitente_texto = tipoMsj_resto.substring(tipoMsj_resto.indexOf(":") +1, tipoMsj_resto.length());
						
						String idRemitente = idRemitente_texto.substring(0, idRemitente_texto.indexOf(":"));
						String texto = idRemitente_texto.substring(idRemitente_texto.indexOf(":") +1, idRemitente_texto.length());
						
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
