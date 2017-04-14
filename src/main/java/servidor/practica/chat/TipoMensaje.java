package servidor.practica.chat;

public enum TipoMensaje {
	MEDESCONECTO, HABLARCON, PEDIDODECONEXION, 
	DATOSDECONEXION, ESTABLECERCONEXION, MENSAJEPENDIENTE, 
	NOESTADISPONIBLE;
	
	public String string()
	{
		return String.valueOf(this.ordinal());
	}
}
