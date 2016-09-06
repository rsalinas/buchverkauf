package dsv.buecher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DbConvert {
	private final String url1;

	public DbConvert(String string, String dbfile) {
		this.url1 = string;
		this.dbfile = dbfile;
	}

	private final String dbfile;

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url1);
	}

	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {
		DbConvert dc = new DbConvert(args[0], args[1]);
		dc.migrate();
	}

	public void migrate() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection conn2 = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
		Connection conn1 = getConnection();
		conn1.setReadOnly(true);
		conn1.setAutoCommit(false);
		conn2.setAutoCommit(false);
		conn2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		importDsvUsers(conn1, conn2);
		importFaecher(conn1, conn2);
		importAbteilungen(conn1, conn2);
		importSprachen(conn1, conn2);
		importBuecher(conn1, conn2);
		importVerlage(conn1, conn2);
		importKlassen(conn1, conn2);
		importBestellungen(conn1, conn2);
		importRechnungen(conn1, conn2);
		importSchueler(conn1, conn2);

		conn2.commit();
		conn2.close();
	}

	/**
	 * @param conn2
	 * @param conn22
	 * @throws SQLException
	 */
	private void importRechnungen(final Connection conn1, Connection conn2)
			throws SQLException {
		Statement s = conn2.createStatement();
		drop(conn2, "rechnungen");
		drop(conn2, "rechnunglin");

		// s
		// .execute(
		// "create table Rechnungen(schueler, id autonumeric, datum, relatedBill)"
		// );
		s
				.execute("create table Rechnungen(id integer primary key autoincrement, schuljahr, schueler, Zahlungsdatum)");
		s
				.execute("create table RechnungLin(type, id, buchcode, preis, returned boolean default false)");
		s.execute("create index rechnungen_schueler on rechnungen(schueler)");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Rechnungen ");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Rechnungen (id, schuljahr, schueler, Zahlungsdatum) values (?, ?, ?, ?)");
		while (rs.next()) {
			int pos = 1;
			ps1.setInt(pos++, rs.getInt("rechnungid"));
			ps1.setInt(pos++, rs.getInt("schuljahr"));
			ps1.setInt(pos++, rs.getInt("schuelercode"));
			ps1.setDate(pos++, rs.getDate("zahlungsdatum"));
			ps1.addBatch();
		}
		ps1.executeBatch();
		conn2.commit();
	}

	/**
	 * @param conn1
	 * @param conn2
	 * @throws SQLException
	 */
	private void importBestellungen(final Connection conn1, Connection conn2)
			throws SQLException {
		drop(conn2, "bestellungen");
		conn2.createStatement().execute(
				"create table bestellungen(schuljahr, schueler, buch)");
		final Statement s = conn2.createStatement();
		s.execute("		create index bestellungen_code on bestellungen (buch);");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Bestellungen");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Bestellungen (schuljahr, schueler, buch ) values (?, ?, ?)");
		long t0 = System.currentTimeMillis();
		while (rs.next()) {
			int pos = 1;
			ps1.setInt(pos++, rs.getInt("schuljahr"));
			ps1.setInt(pos++, rs.getInt("schuelernum"));
			ps1.setInt(pos++, rs.getInt("buchcode"));
			ps1.addBatch();
		}
		long t1 = System.currentTimeMillis();
		System.err.println(t1 - t0);
		ps1.executeBatch();
		long t2 = System.currentTimeMillis();
		System.err.println(t2 - t1);
		conn2.commit();

	}

	private void importKlassen(Connection conn1, Connection conn2)
			throws SQLException {
		drop(conn2, "klassen");
		conn2.createStatement().execute(
				"create table Klassen(Schuljahr, Klasse, Abteilung, Jahr)");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Gruppen ");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Klassen (Schuljahr, Klasse, Abteilung, Jahr) values (?, ?, ?, ?)");
		long t0 = System.currentTimeMillis();
		while (rs.next()) {
			System.err.println("Inserting: " + rs.getString("gruppename"));
			int pos = 1;
			ps1.setInt(pos++, rs.getInt("schuljahr"));
			ps1.setString(pos++, rs.getString("gruppename"));
			ps1.setString(pos++, rs.getString("abteilung"));
			ps1.setInt(pos++, rs.getInt("jahr"));
			ps1.addBatch();
		}
		long t1 = System.currentTimeMillis();
		System.err.println(t1 - t0);
		ps1.executeBatch();
		long t2 = System.currentTimeMillis();
		System.err.println(t2 - t1);
		conn2.commit();
	}

	private void importVerlage(Connection conn1, Connection conn2)
			throws SQLException {
		drop(conn2, "verlage");
		conn2
				.createStatement()
				.execute(
						"create table Verlage(VerlagCode integer primary key autoincrement, Name, Land)");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Verlage ");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Verlage (verlagcode, name, land) values (?, ?, ?)");
		long t0 = System.currentTimeMillis();
		while (rs.next()) {
			System.err.println("Inserting: " + rs.getString("name"));
			int pos = 1;
			ps1.setInt(pos++, rs.getInt("verlagcode"));
			ps1.setString(pos++, rs.getString("name"));
			ps1.setString(pos++, rs.getString("land"));
			ps1.addBatch();
		}
		long t1 = System.currentTimeMillis();
		System.err.println(t1 - t0);
		ps1.executeBatch();
		long t2 = System.currentTimeMillis();
		System.err.println(t2 - t1);
		conn2.commit();
	}

	/**
	 * @param conn
	 * @throws SQLException
	 */
	private void importDsvUsers(Connection conn1, Connection conn)
			throws SQLException {
		drop(conn, "dsvUsers");
		final Statement s = conn.createStatement();
		s.executeUpdate("create table dsvUsers (login, name, perms)");
		s
				.executeUpdate("insert into dsvUsers values ('rsalinas', 'Raul Salinas', 'PEDIDOS')");
		s
				.executeUpdate("insert into dsvUsers values ('rmetri', 'Roberto Metri', 'USER_EDIT')");
	}

	private void importBuecher(Connection conn1, Connection conn2)
			throws SQLException {
		Statement s2 = conn2.createStatement();
		drop(conn2, "buecher");
		s2
				.execute("create table Buecher(BuchCode integer primary key autoincrement, Titel, VerlagCode, Sprache, Preis,  Fach, Isbn, Bemerkung, schuljahr)");
		drop(conn2, "buecherklassen");
		s2
				.execute("create table BuecherKlassen(buch, klasse, schuljahr int, vomvorjahr boolean)");

		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Buecher");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Buecher (Buchcode, Titel, VerlagCode, Sprache, Preis, Fach, Isbn, Bemerkung, Schuljahr) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		while (rs.next()) {
			System.err.println("Inserting: " + rs.getString("titel"));
			int pos = 1;
			ps1.setInt(pos++, rs.getInt("buchcode"));
			ps1.setString(pos++, rs.getString("Titel"));
			ps1.setInt(pos++, rs.getInt("VerlagCode"));
			ps1.setString(pos++, rs.getString("Sprache"));
			ps1.setDouble(pos++, rs.getDouble("Preis"));
			ps1.setString(pos++, rs.getString("Fach"));
			ps1.setString(pos++, rs.getString("Isbn"));
			ps1.setString(pos++, rs.getString("Bemerkung"));
			ps1.setInt(pos++, 2008);
			ps1.addBatch();
		}
		ResultSet rs2 = conn1.createStatement().executeQuery(
				"select * from BuecherKlassen");
		PreparedStatement ps2 = conn2
				.prepareStatement("insert into BuecherKlassen (buch, klasse, schuljahr , vomvorjahr) values (?, ?, ?, ?)");
		while (rs2.next()) {
			int pos = 1;
			ps2.setInt(pos++, rs2.getInt("buchcode"));
			ps2.setString(pos++, rs2.getString("Klasse"));
			ps2.setInt(pos++, rs2.getInt("Schuljahr"));
			ps2.setBoolean(pos++, rs2.getBoolean("Vomvorjahr"));
			ps2.addBatch();
		}

		ps1.executeBatch();
		ps2.executeBatch();
		conn2.commit();
	}

	private void drop(Connection c, String string) throws SQLException {
		c.createStatement().executeUpdate("drop table if exists " + string);
	}

	/**
	 * @param conn2
	 * @throws SQLException
	 */
	private void importSchueler(Connection conn1, Connection conn2)
			throws SQLException {
		Statement s = conn2.createStatement();
		s.execute("create index buch_code on buecher (buchcode)");

		drop(conn2, "Schueler");
		s
				.execute("create table Schueler(schuelernum integer primary key autoincrement, name, vorname, klasse, schuljahr, email, unique (name, vorname, schuljahr))");

		ArrayList<Schueler> schueler = getSchueler(conn1);
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Schueler (SchuelerNum, Name, Vorname, Klasse, Schuljahr, Email) values (?, ?, ?, ?, ?, ?)");
		long t0 = System.currentTimeMillis();
		for (Schueler si : schueler) {
			System.err.println("Inserting: " + si);
			int pos = 1;
			ps1.setInt(pos++, si.num);
			ps1.setString(pos++, si.name);
			ps1.setString(pos++, si.vorName);
			ps1.setString(pos++, si.klasse);
			ps1.setInt(pos++, 2008); // TODO
			ps1.setString(pos++, si.email);
			ps1.addBatch();
		}
		long t1 = System.currentTimeMillis();
		System.err.println(t1 - t0);
		ps1.executeBatch();
		long t2 = System.currentTimeMillis();
		System.err.println(t2 - t1);
		conn2.commit();
	}

	public ArrayList<Schueler> getSchueler(Connection c) throws SQLException {
		ArrayList<Schueler> l = new ArrayList<Schueler>();
		String sql = "select SchuelerNum, Vorname,  Name, Klasse, Email from Schueler where SchulJahr = " + 2008;
		// Connection c = getConnection();
		try {
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(sql);
			while (rs.next())
				l.add(new Schueler(rs));
			c.commit();
		} finally {
			// close(c);
		}
		return l;
	}

	private void importFaecher(Connection conn1, Connection conn2)
			throws SQLException {
		Statement s = conn2.createStatement();
		drop(conn2, "faecher");
		s.execute("create table Faecher(Name primary key)");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Faecher");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Faecher (name) values (?)");
		long t0 = System.currentTimeMillis();
		while (rs.next()) {
			Fach fach = new Fach(rs.getString("fachname"));
			System.err.println("Inserting: " + fach);
			int pos = 1;
			ps1.setString(pos++, fach.name);
			ps1.addBatch();
		}
		long t1 = System.currentTimeMillis();
		System.err.println(t1 - t0);
		ps1.executeBatch();
		long t2 = System.currentTimeMillis();
		System.err.println(t2 - t1);
	}

	private void importAbteilungen(Connection conn1, Connection toConn)
			throws SQLException {
		final Statement s = toConn.createStatement();
		drop(toConn, "abteilungen");
		s
				.execute("create table Abteilungen(Name unique, ordIndex primary key )");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Abteilungen");
		PreparedStatement ps1 = toConn
				.prepareStatement("insert into Abteilungen (Name, ordIndex) values (?, ?)");
		long t0 = System.currentTimeMillis();
		while (rs.next()) {
			System.err.println("Inserting: " + rs.getString("name"));
			int pos = 1;
			ps1.setString(pos++, rs.getString("name"));
			ps1.setInt(pos++, rs.getInt("ordnungindex"));
			ps1.addBatch();
		}
		long t1 = System.currentTimeMillis();
		System.err.println(t1 - t0);
		ps1.executeBatch();
		long t2 = System.currentTimeMillis();
		System.err.println(t2 - t1);
	}

	private void importSprachen(Connection conn1, Connection conn2)
			throws SQLException {
		final Statement s = conn2.createStatement();
		drop(conn2, "Sprachen");
		s
				.executeUpdate("create table Sprachen (id char(5) primary key , name char(32) unique)");
		ResultSet rs = conn1.createStatement().executeQuery(
				"select * from Sprachen");
		PreparedStatement ps1 = conn2
				.prepareStatement("insert into Sprachen (id, name) values (?, ?)");
		while (rs.next()) {
			System.err.println("Inserting: " + rs.getString("name"));
			int pos = 1;
			ps1.setString(pos++, rs.getString("code"));
			ps1.setString(pos++, rs.getString("Name"));
			ps1.addBatch();
		}
		ps1.executeBatch();
	}

}
