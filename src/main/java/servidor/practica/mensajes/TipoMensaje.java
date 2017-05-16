package servidor.practica.mensajes;

/**
 * de esta manera identificamos que es lo que quiere el usuario, y que tipo de respuesta
 * el usuario esta recibiendo del servidor.
 */

public enum TipoMensaje {
	CIERROCONEXION, HABLARCON, DATOSDECONEXION, MENSAJEPENDIENTE, NOESTADISPONIBLE, ERROR;
	
	public String string()
	{
		return String.valueOf(this.ordinal());
	}
}
