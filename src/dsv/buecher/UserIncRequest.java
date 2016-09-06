package dsv.buecher;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class UserIncRequest<T> implements GenericRequestInterface<T> {
	private final ArrayList<T> l = new ArrayList<T>();

	public UserIncRequest(Connection c, ResultSet rs, Class<T> rc)
			throws SQLException {

		while (rs.next())
			try {
				l.add(rc.getConstructor(ResultSet.class).newInstance(rs));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				throw (SQLException) e.getTargetException();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		rs.close();
		c.close();

	}

	@Override
	public T get(int i) {
		return l.get(i);
	}

	@Override
	public int size() {
		return l.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator iterator() {
		return l.iterator();
	}

}
