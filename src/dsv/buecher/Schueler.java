package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Schueler implements ToArrayPassable {
	enum SchulerDatum {
		EMAIL, KLASSE, NAME, NUM, VORNAME;
	}

	public static final String[] dataFields = { "Vorname", "Name", "Name_norm",
			"Klasse", "Email", "bemerkDeu", "bemerkSpa" };
	public static final String[] keyFields = { "SchuelerNum", " SchulJahr" };

	String email;

	public String name, vorName, klasse;

	int num;
	public String bemerkDeu, bemerkSpa;
	int atlId;

	public Schueler() {
		name = "";
		vorName = "";
		klasse = "";
		num = -1;
		email = "";
		bemerkDeu = "";
		bemerkSpa = "";
	}

	public Schueler(int code, String vorname, String name, String klasse) {
		this.vorName = vorname;
		this.name = name;
		this.klasse = klasse;
		this.num = code;
		email = "";
		bemerkDeu = "";
		bemerkSpa = "";
	}

	public Schueler(ResultSet rs) throws SQLException {
		this.name = rs.getString("name");
		this.vorName = rs.getString("vorName");
		this.klasse = rs.getString("klasse");
		this.num = rs.getInt("schuelernum");
		this.email = rs.getString("email");
		this.bemerkDeu = rs.getString("bemerkDeu");
		this.bemerkSpa = rs.getString("bemerkSpa");
		try {
			this.atlId = rs.getInt("atlId");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	@Override
	public String toString() {
		return vorName + " " + name + " (" + klasse + ")";
	}

	public String[] toStringArray() {
		return new String[] { "#" + num + "/" + atlId, name, vorName, klasse,
				bemerkDeu };
	}
}
