package servidor.practica.chat;

import java.util.ArrayList;
import java.util.List;

public class Pendientes implements Runnable{
	List<Mensaje> mensajes;
	List <Mensaje> mensajesClonados; //Para borrar despues del ForEach
	Servidor server;
	public Pendientes(Servidor server_){
		server = server_;
		mensajes = new ArrayList<Mensaje>();
	}
	
	public void addMensaje(Mensaje mensaje){
		mensajes.add(mensaje);
	}
	public void sacarPendiente(Mensaje mensaje){
		mensajes.remove(mensaje);
	}
	@Override
	public void run() {
		while(true){
			mensajesClonados = new ArrayList<Mensaje>();
			mensajes.forEach(m -> mensajesClonados.add(m));
			mensajesClonados.forEach(m-> m.intentarEnviar(this, server));
		}
		
	}
	
	
}
