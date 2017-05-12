package servidor.practica.mensajes;

public enum TipoMensaje {
	CIERROSOCKET, HABLARCON, DATOSDECONEXION, MENSAJEPENDIENTE, NOESTADISPONIBLE, OK, ERROR;
	
	public String string()
	{
		return String.valueOf(this.ordinal());
	}
}
/*
 * de esta manera identificamos que es lo que quiere el usuario, y que tipo de respuesta
 * el usuario esta recibiendo del servidor.
 */
