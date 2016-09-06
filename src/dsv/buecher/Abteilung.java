package dsv.buecher;

import java.util.ArrayList;

public class Abteilung {
	public Abteilung(String name, ArrayList<Klasse> klassenInAbteilung) {
		this.klassen = klassenInAbteilung;
		this.name = name;
	}

	public Abteilung() {
		name = "";
		klassen = new ArrayList<Klasse>();
	}

	public String name;
	public final ArrayList<Klasse> klassen;
	private final boolean isNew = true;

	public String[] toStringArray() {
		return new String[] { name };
	}

	public boolean isNew() {
		return isNew;
	}
}
