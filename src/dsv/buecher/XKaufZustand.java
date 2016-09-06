package dsv.buecher;

public enum XKaufZustand {
	BESTELLT("Renunciar", "Encargado", true), VERKAUFT("Devolver", "Vendido",
			true), DEVUELTO("Devuelto", "Recomprar", false), NIX("",
			"Comprar sin reserva", false);
	public String[] s;
	public boolean active;

	public String getDisplayText(boolean b) {
		if (b)
			return s[1];
		else
			return s[0];
	}

	private XKaufZustand(String s1, String s2, boolean active) {
		s = new String[] { s1, s2 };
		this.active = active;
	}

	public boolean getActive() {
		return active;
	}

}
