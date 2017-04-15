package servidor.practica.chat;

public enum TipoMensaje {
	CIERROSOCKET, MEDESCONECTO, HABLARCON, 
	DATOSDECONEXION, MENSAJEPENDIENTE, NOESTADISPONIBLE;
	
	public String string()
	{
		return String.valueOf(this.ordinal());
	}
}
