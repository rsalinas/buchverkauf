package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Klasse implements Comparable<Klasse> {
	public Klasse(ResultSet rs) throws SQLException {
		name = rs.getString("Klasse");
		abteilung = rs.getString("Abteilung");
		jahr = rs.getInt("jahr");
		schulJahr = rs.getInt("schulJahr");
		try {
			nSchueler = rs.getInt("snum");
		} catch (SQLException e) {
			// e.printStackTrace();
			// FIXME quitar
		}
	}

	public Klasse(String name) {
		this.name = name;
		jahr = 0;
	}

	public Klasse(String name, String abteilung, int jahr, int schulJahr) {
		this.name = name;
		this.abteilung = abteilung;
		this.jahr = jahr;
		this.schulJahr = schulJahr;
	}

	public Klasse(int runningYear) {
		schulJahr = runningYear;
		name = "";
		abteilung = "";
		jahr = 0;
	}

	public String name;
	String abteilung;
	int jahr;
	int schulJahr;
	int nSchueler = -1;

	public String[] toStringArray() {
		return new String[] { name, "" + jahr, abteilung, "" + nSchueler };
	}

	@Override
	public String toString() {
		return name;
	}

	public static int getPos(String klasse, ArrayList<Klasse> l) {
		int pos = 0;
		for (Klasse k : l) {
			if (k.name.equals(klasse))
				return pos;
			else
				pos++;
		}
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (abteilung == null ? 0 : abteilung.hashCode());
		result = prime * result + jahr;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + schulJahr;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Klasse other = (Klasse) obj;
		if (abteilung == null) {
			if (other.abteilung != null)
				return false;
		} else if (!abteilung.equals(other.abteilung))
			return false;
		if (jahr != other.jahr)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (schulJahr != other.schulJahr)
			return false;
		return true;
	}

	@Override
	public int compareTo(Klasse o) {
		if (jahr != o.jahr)
			return jahr - o.jahr;
		else
			return name.compareTo(o.name);
	}

}
