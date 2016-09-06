package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BuchBemerkung {
	// id autonumeric, shortname unique , dsc unique

	public int id;
	public String shortname, dsc;

	private static int nid = 0;

	@Override
	public String toString() {
		return dsc + " (" + shortname + ")";
	}

	public BuchBemerkung(int id, String shortname, String dsc) {
		super();
		this.id = id;
		this.shortname = shortname;
		this.dsc = dsc;
	}

	public BuchBemerkung(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		// BIG FIXME.
		if (id == 0)
			id = nid++;
		shortname = rs.getString("shortname");
		dsc = rs.getString("dsc");
	}

	public BuchBemerkung() {
		shortname = "";
		dsc = "|";
		id = -1;
	}

	public String[] toStringArray() {
		String[] ss = dsc.split("\\|");
		if (ss.length != 2)
			ss = new String[] { "", "" };
		return new String[] { "" + id, shortname, ss[0], ss[1] };
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dsc == null ? 0 : dsc.hashCode());
		result = prime * result + id;
		result = prime * result
				+ (shortname == null ? 0 : shortname.hashCode());
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
		BuchBemerkung other = (BuchBemerkung) obj;
		if (dsc == null) {
			if (other.dsc != null)
				return false;
		} else if (!dsc.equals(other.dsc))
			return false;
		if (id != other.id)
			return false;
		if (shortname == null) {
			if (other.shortname != null)
				return false;
		} else if (!shortname.equals(other.shortname))
			return false;
		return true;
	}

	public boolean isNew() {
		return id == -1;
	}

	public String getDeutschDsc() {
		return dsc.split("\\|")[0];
	}

}
