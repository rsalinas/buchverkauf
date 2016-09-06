package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SchulerIncRequest {
	private final ArrayList<Schueler> l = new ArrayList<Schueler>();

	public SchulerIncRequest(Connection c, ResultSet rs) throws SQLException {
		while (rs.next())
			l.add(new Schueler(rs));
		rs.close();
		c.close();
	}

	public Schueler get(int i) throws SQLException {
		return l.get(i);
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public int size() {
		return l.size();
	}
}
