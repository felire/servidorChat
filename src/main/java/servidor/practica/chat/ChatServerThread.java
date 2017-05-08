package servidor.practica.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

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
			Usuario usuario = Servidor.obj().generarUsuario(idUsuario);
			String desafio = RandomString.generateRandomToken();
			streamOut.writeUTF(desafio);
			String respuesta = streamIn.readUTF();
			
			if (Servidor.obj().validar(usuario, desafio, respuesta) == false)
			{
				try {
					Thread.sleep(7000);// para complicar un dos
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.close();
				return;
			}
			
			usuario.abroSocket(socket, streamOut);
			usuario.puerto = streamIn.readUTF(); //puerto en el que espera conexiones
			//Servidor.obj().mandarMensajesPendientes(usuario);
			TipoMensaje tipo = null;
			String idRemitente;
			
			while(tipo != TipoMensaje.MEDESCONECTO && tipo != TipoMensaje.CIERROSOCKET)
			{
				tipo = TipoMensaje.values()[Integer.parseInt(streamIn.readUTF())];
				switch(tipo)
				{
					case CIERROSOCKET:
						usuario.cierroSocket();
						break;
					case MEDESCONECTO:
						Servidor.obj().seDesconectoUsuario(usuario);
						break;
					case HABLARCON:
						idRemitente = streamIn.readUTF();
						System.out.println("quiere hablar con " + idRemitente);
						Optional<Usuario> compañero = Servidor.obj().getUsuario(idRemitente);
						compañero.ifPresent(llamado ->
						{
							Servidor.obj().establecerConexion(usuario, llamado);
						});
						if(!compañero.isPresent())
						{
							streamOut.writeUTF(TipoMensaje.NOESTADISPONIBLE.string());
						}
						break;
					case MENSAJEPENDIENTE:
						idRemitente = streamIn.readUTF();
						String mensajePendiente = streamIn.readUTF();
						Mensaje mensaje = new Mensaje(usuario.id, idRemitente, mensajePendiente);
						Servidor.obj().mensajePendiente(mensaje);
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
		if (streamIn != null) streamIn.close();
	}
}
