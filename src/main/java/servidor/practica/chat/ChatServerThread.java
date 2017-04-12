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
			Optional<Usuario> compañero;
			String tipoMensaje = streamIn.readUTF();
			while(tipoMensaje != "1")
			{
				tipoMensaje = streamIn.readUTF();
				switch(tipoMensaje)
				{
					case "1":
						Servidor.obj().seDesconectoUsuario(usuario);
						break;
					case "2":
						String idCompañero, ip, puerto;
						idCompañero = streamIn.readUTF();
						ip = streamIn.readUTF();
						puerto = streamIn.readUTF();
						
						compañero = Servidor.obj().getUsuario(idCompañero);
						compañero.ifPresent(user ->
						{
							// pregunar a user su puerto e ip, si quiere hablar con id
						});
						if(!compañero.isPresent())
						{
							//responder que no esta conectado, dejar mensaje?? while(tipo != 5) ??
							//usar un tipo mensaje especial aca para dejar claro que dejas
							//de mandar mensajes pendientes??
							/* mandaria 5 si queire mandar un msj pendiente, 6 si ya no quiere mandar mas
							 *tipoMensaje = streamIn.readUTF();
							 *while(tipoMensaje != "6")
							 *{
							 *	mensaje = streamIn.readUTF();
							 *	Mensaje msj = new Mensaje(usuario.id, idcompañero, mensaje);
							 *	Servidor.obj().mensajePendiente(msj);
							 *	tipoMensaje = streamIn.readUTF();
							 *}
							 *
							 */
						}
						break;
					case "3":
						//
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
