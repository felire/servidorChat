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
	
	public ChatServerThread(Socket socket)
	{
		this.usuario = new Usuario(socket);
	}

	public void run()
	{
		System.out.println("Server Thread " + usuario.socket.getPort() + " running.");
		try
		{
			usuario.id = streamIn.readUTF();
			System.out.println(usuario.id);
			Servidor.obj().seConectoUsuario(usuario);
			TipoMensaje tipo = null;
			Boolean pendientesPermitidos = false;
			String idPendiente = null;
			while(tipo != TipoMensaje.MEDESCONECTO)
			{
				tipo = TipoMensaje.values()[Integer.parseInt(streamIn.readUTF())];
				switch(tipo)
				{
					case MEDESCONECTO:
						Servidor.obj().seDesconectoUsuario(usuario);
						break;
					case HABLARCON:
						pendientesPermitidos = false;
						String idCompañero = streamIn.readUTF();
						usuario.puerto = streamIn.readUTF();
						
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
					case DATOSDECONEXION://nos manda sus datos
						pendientesPermitidos = false;
						usuario.puerto = streamIn.readUTF();
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
		streamIn = new DataInputStream(usuario.socket.getInputStream());
		streamOut = new DataOutputStream(usuario.socket.getOutputStream());
	}
	
	public void close() throws IOException
	{
		usuario.socket.close();		
		if(usuario != null) Servidor.obj().seDesconectoUsuario(usuario);
		if (streamIn != null) streamIn.close();
	}
}
