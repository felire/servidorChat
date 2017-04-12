package servidor.practica.chat;

public class Mensaje
{
	public String emisor;
	public String receptor;
	public String mensaje;
	
	public Mensaje(String emisor, String receptor, String mensaje)
	{
		this.emisor = emisor;
		this.receptor = receptor;
		this.mensaje = mensaje;
	}
}