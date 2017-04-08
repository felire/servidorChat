package servidor.practica.chat;

public class Mensaje {
	String emisor;
	String receptor;
	String mensaje;
	
	public Mensaje(String emisor, String receptor, String mensaje){
		this.emisor = emisor;
		this.receptor = receptor;
		this.mensaje = mensaje;
	}
	
	public void intentarEnviar(Pendientes pendientes, Servidor server){
		Usuario usuarioReceptor = server.getUsuario(receptor);
		if(usuarioReceptor == null){
			
		}else{
			System.out.println("Encontrado!!!!!");
			usuarioReceptor.recibirMensaje(emisor, mensaje);
			pendientes.sacarPendiente(this);
		}
	}
}
