package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class LanguageInMemRequest implements
		PassableGenericRequestInterface<Language> {
	private final ArrayList<Language> l = new ArrayList<Language>();

	public LanguageInMemRequest(Connection c, ResultSet rs, boolean close)
			throws SQLException {
		while (rs.next())
			l.add(new Language(rs));
		rs.close();
		if (close)
			c.close();
	}

	public Language get(int i) {
		return l.get(i);
	}

	public int size() {
		return l.size();
	}

	@Override
	public Iterator<Language> iterator() {
		return l.iterator();
	}
}
