package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Fach implements ToArrayPassable {
	String name;

	public Fach(ResultSet rs) throws SQLException {
		name = rs.getString("name");
	}

	public Fach(String string) {
		this.name = string;
	}

	@Override
	public String[] toStringArray() {
		return new String[] { name };
	}

	@Override
	public String toString() {
		return name;
	}
}
