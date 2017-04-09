package servidor.practica.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Pendientes implements Runnable{
	
	List<Mensaje> mensajes;
	List <Mensaje> mensajesClonados; //Para borrar despues del ForEach
	Semaphore semaphore;
	
	public Pendientes(){
		mensajes = new ArrayList<Mensaje>();
		semaphore = new Semaphore(1);
	}
	
	public void addMensaje(Mensaje mensaje){
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mensajes.add(mensaje);
		semaphore.release();
	}
	public void sacarPendiente(Mensaje mensaje){
		mensajes.remove(mensaje);
	}
	@Override
	public void run() {
		while(true){
			mensajesClonados = new ArrayList<Mensaje>();
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mensajes.forEach(m -> mensajesClonados.add(m));
			semaphore.release();
			mensajesClonados.forEach(m-> m.intentarEnviar(this));
		}
		
	}
	
	
}
