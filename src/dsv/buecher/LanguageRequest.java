package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class LanguageRequest extends IncRequest<Language> implements
		PassableGenericRequestInterface<Language> {

	public LanguageRequest(Connection c, ResultSet rs) throws SQLException {
		super(c, rs);
	}

	public Language get(int i) {
		try {
			rs.absolute(i + 1);
			return new Language(rs);
		} catch (SQLException e) {
			return null;// TODO
		}
	}

	@Override
	public Iterator<Language> iterator() {
		return new Iterator<Language>() {

			@Override
			public boolean hasNext() {
				try {
					return rs.next();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}

			@Override
			public Language next() {
				try {
					return new Language(rs);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public void remove() {
				throw new RuntimeException("bad");
			}

		};
	}
}
