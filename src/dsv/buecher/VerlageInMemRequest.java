package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class VerlageInMemRequest implements GenericRequestInterface<Verlag> {

	private final ArrayList<Verlag> l = new ArrayList<Verlag>();

	public VerlageInMemRequest(Connection c, ResultSet rs) throws SQLException {
		while (rs.next())
			l.add(new Verlag(rs));
		rs.close();
		c.close();
	}

	public Verlag get(int i) {
		return l.get(i);
	}

	public int size() {
		return l.size();
	}

	@Override
	public Iterator<Verlag> iterator() {
		return l.iterator();
	}

}
