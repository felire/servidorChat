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
			String idUsuario = streamIn.readUTF();
			System.out.println("el id es " + idUsuario);
			Usuario usuario = Servidor.obj().generarUsuario(idUsuario);
			usuario.abrirSocket(socket, streamOut, streamIn);
			Boolean valido = Servidor.obj().autenticar(usuario);
			if(valido == false)
			{
				this.close();
				return;
			}
			
			TipoMensaje tipo = null;
			while(tipo != TipoMensaje.CIERROSOCKET)
			{
				tipo = TipoMensaje.values()[Integer.parseInt(streamIn.readUTF())];
				switch(tipo)
				{
					case CIERROSOCKET:
						usuario.cierroSocket();
						break;
					case HABLARCON:
						Servidor.obj().comunicar(usuario);
						break;
					case MENSAJEPENDIENTE:
						Servidor.obj().addMensajePendiente(usuario);
					default:
						break;
				}
			}
			this.close();
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
		//if (streamIn != null) streamIn.close();
		//if (streamOut != null) streamOut.close();
	}
}
