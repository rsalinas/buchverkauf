package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class FachRequest implements PassableGenericRequestInterface<Fach> {
	ArrayList<Fach> l = new ArrayList<Fach>();

	public FachRequest(Connection c, ResultSet rs) throws SQLException {
		while (rs.next())
			l.add(new Fach(rs));
		c.close();
	}

	public Fach get(int i) {
		return l.get(i);
	}

	public int size() {
		return l.size();
	}

	@Override
	public Iterator<Fach> iterator() {
		return l.iterator();
	}

}
