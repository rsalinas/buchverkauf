package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;

public abstract class IncRequest<T> extends AbstractList<T> {
	final protected ResultSet rs;
	final public Connection c;
	final private int size;

	public IncRequest(Connection c, ResultSet rs) throws SQLException {
		this.c = c;
		this.rs = rs;
		this.size = getSize();
	}

	public int size() {
		return size;
	}

	private int getSize() throws SQLException {
		rs.last();
		int n = rs.getRow();
		rs.beforeFirst();
		return n;
	}

	public void close() {
		DsvBuecher.close(c);
		DsvBuecher.close(rs);
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

}