package servidor.practica.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Pendientes implements Runnable
{
	
	private List<Mensaje> mensajes;
	private Semaphore semaphore;

	public Pendientes()
	{
		mensajes = new ArrayList<Mensaje>();
		semaphore = new Semaphore(1);
	}
	
	public void addMensaje(Mensaje mensaje)
	{
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mensajes.add(mensaje);
		semaphore.release();
	}
	
	public void sacarPendiente(Mensaje mensaje)
	{
		mensajes.remove(mensaje);
	}
	
	@Override
	public void run()
	{
		List <Mensaje> mensajesClonados = new ArrayList<Mensaje>();
		while(true)
		{
			mensajesClonados.clear();
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mensajesClonados.addAll(mensajes);
			semaphore.release();
			mensajesClonados.forEach(m-> m.intentarEnviar(this));
		}
	}	
}
