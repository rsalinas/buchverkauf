package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Language implements ToArrayPassable {
	public Language(ResultSet rs) throws SQLException {
		id = rs.getString("id");
		name = rs.getString("name");
	}

	public Language() {
		id = "";
		name = "";
	}

	String id, name;

	public String[] toStringArray() {
		return new String[] { id, name };
	}

	@Override
	public String toString() {
		return id + "(" + name + ")";
	}
}
