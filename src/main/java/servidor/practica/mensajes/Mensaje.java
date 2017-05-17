package servidor.practica.mensajes;

import servidor.practica.chat.Usuario;

public class Mensaje
{
	private String emisor;
	private String receptor;
	private String mensaje;
	
	public Mensaje(Usuario emisor, String receptor, String mensaje)
	{
		this.emisor = emisor.id;
		this.receptor = receptor;
		this.mensaje = mensaje;
	}
	
	public String contenido()
	{
		return emisor + ":" + mensaje;
	}
	
	public Boolean esPara(Usuario usuario)
	{
		return receptor.equals(usuario.id);
	}
}
