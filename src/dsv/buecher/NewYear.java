package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class NewYear {
	private final Connection conn;

	public NewYear(DsvBuecherDatasource ds2) throws SQLException {
		conn = ds2.getConnection();
	}

	public void adapta(int oldyear, int newyear) throws SQLException {
		System.err.println("adapta()");
		adapta("faecher", oldyear, newyear);
		adapta("abteilungen", oldyear, newyear);
		adapta("sprachen", oldyear, newyear);
		adapta("buecher", oldyear, newyear);
		adapta("buecherklassen", oldyear, newyear);
		adapta("verlage", oldyear, newyear);
		adapta("klassen", oldyear, newyear);
		adapta("buchmark", oldyear, newyear);
		adapta("buchmarken", oldyear, newyear);
		adapta("formtext", oldyear, newyear);

		// conn.createStatement().execute(sql);
	}

	private void adapta(String table, int oldyear, int newyear)
			throws SQLException {
		System.err.println("adapta " + table);
		ResultSet rs = conn.createStatement().executeQuery(
				"select * from " + table + " limit 0");
		ResultSetMetaData md = rs.getMetaData();
		ArrayList<String> l = new ArrayList<String>();
		for (int i = 1; i <= md.getColumnCount(); i++)
			if (!"schuljahr".equalsIgnoreCase(md.getColumnName(i)))
				l.add(md.getColumnName(i));
		if (l.size() != md.getColumnCount() - 1)
			throw new SQLException("parece que la tabla " + table
					+ " no tiene la columna schuljahr");
		for (String cn : l)
			System.err.println("col: " + cn);
		String sql = MessageFormat
				.format(
						"INSERT INTO {0} select {1}, {2} from {3} where schuljahr == {4}",
						table, RauUtil.separate(
								l.toArray(new String[l.size()]), ", "), ""
								+ newyear, table, "" + oldyear);
		System.err.println("sql: " + sql);
		int deleted = conn.createStatement().executeUpdate(
				MessageFormat.format("delete from {0} where schuljahr = {1}",
						table, "" + newyear));
		System.err.println("deleted rows : " + deleted);
		int res = conn.createStatement().executeUpdate(sql);
		System.err.println("result: " + res);
	}
};
