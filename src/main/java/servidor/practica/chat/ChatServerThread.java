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
	private Usuario usuario;
	private Socket socket;
	
	public ChatServerThread(Socket socket)
	{
		this.socket = socket;
	}

	public void run()
	{
		System.out.println("Server Thread " + usuario.socket.getPort() + " running.");
		try
		{
			Optional<Usuario> op = Servidor.obj().getUsuario(streamIn.readUTF());
			if(op.isPresent())
			{
				usuario = op.get();
				usuario.abroSocket(socket);
			}
			else
			{
				usuario = new Usuario(socket);
				usuario.puerto = streamIn.readUTF(); //si se habia desconectado nos deberia mandar su puerto
			}
			System.out.println(usuario.id);
			Servidor.obj().seConectoUsuario(usuario);
			TipoMensaje tipo = null;
			Boolean pendientesPermitidos = false;
			String idPendiente = null;
			while(tipo != TipoMensaje.MEDESCONECTO && tipo != TipoMensaje.CIERROSOCKET)
			{
				tipo = TipoMensaje.values()[Integer.parseInt(streamIn.readUTF())];
				switch(tipo)
				{
					case CIERROSOCKET:
						usuario.cierroSocket();
					case MEDESCONECTO:
						Servidor.obj().seDesconectoUsuario(usuario);
						break;
					case HABLARCON:
						pendientesPermitidos = false;
						String idCompañero = streamIn.readUTF();
						Optional<Usuario> compañero = Servidor.obj().getUsuario(idCompañero);
						compañero.ifPresent(llamado ->
						{
							Servidor.obj().establecerConexion(usuario, llamado);
						});
						if(!compañero.isPresent())
						{
							idPendiente = idCompañero;
							pendientesPermitidos = true;
							streamOut.writeUTF(TipoMensaje.NOESTADISPONIBLE.toString());
						}
						break;
					case MENSAJEPENDIENTE:
						if(pendientesPermitidos)//si no existe el usuario? se podria chequear con los tokens
						{
							String mensajePendiente = streamIn.readUTF();
							Mensaje mensaje = new Mensaje(usuario.id, idPendiente, mensajePendiente);
							Servidor.obj().mensajePendiente(mensaje);
						}
				default:
					break;
				}
			}
			this.close();
		}catch(IOException ioe) {
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
		usuario.socket.close();		
		if(usuario != null) Servidor.obj().seDesconectoUsuario(usuario);
		if (streamIn != null) streamIn.close();
	}
}
