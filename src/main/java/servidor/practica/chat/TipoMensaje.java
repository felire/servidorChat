package servidor.practica.chat;

public enum TipoMensaje {
	CIERROSOCKET, HABLARCON, DATOSDECONEXION, MENSAJEPENDIENTE, NOESTADISPONIBLE, OK, ERROR;
	
	public String string()
	{
		return String.valueOf(this.ordinal());
	}
}
