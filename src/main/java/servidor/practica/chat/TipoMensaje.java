package servidor.practica.chat;

public enum TipoMensaje {
	CIERROSOCKET, MEDESCONECTO, HABLARCON, 
	DATOSDECONEXION, MENSAJEPENDIENTE, NOESTADISPONIBLE, TOKEN;
	
	public String string()
	{
		return String.valueOf(this.ordinal());
	}
}
