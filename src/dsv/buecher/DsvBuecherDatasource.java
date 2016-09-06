package dsv.buecher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.sqlite.SQLiteConfig;

public class DsvBuecherDatasource {

	public class BestellungDetail {
		public String detail;
		public int n;

		public BestellungDetail(int n, String detail) {
			super();
			this.n = n;
			this.detail = detail;
		}

	}

	public class BuchVw {
		public int getStock(int buch) throws SQLException {
			final String sql = "select stock from buecher where buchcode = ? and schuljahr = ?";
			c.rollback();
			ResultSet rs = null;
			PreparedStatement ps = c.prepareStatement(sql);
			try {
				ps.setInt(1, buch);
				ps.setInt(2, getRunningYear());
				rs = ps.executeQuery();
				if (rs.next())
					return rs.getInt("stock");
				else
					throw new SQLException("Missing book: " + buch);

			} finally {
				if (rs != null)
					rs.close();
				ps.close();
				c.rollback();
			}
		}

		@Sql
		private final static String sqlUpdateRsvCount = "update buecher set rsv = "
				+ "(select count(*) from bestellungen b "
				+ "where b.schuljahr = buecher.schuljahr and buecher.schuljahr = ? and buch = buecher.buchcode) "
				+ "where buchcode = ?";

		/**
		 * @param c
		 * @param p
		 * @param facturaid
		 * @param i
		 * @param type
		 * @throws SQLException
		 */
		private void addLine(Connection c, int p, int facturaid,
				ChoosableBuch i, int type) throws SQLException {
			@Sql
			final String sql2 = "insert into RechnungLin(type, id, buchcode, preis, schuljahr) values (?, ?, ?, ?, ?)";
			PreparedStatement ps2 = c.prepareStatement(sql2);
			p = 1;
			ps2.setInt(p++, type);
			ps2.setInt(p++, facturaid);
			ps2.setInt(p++, i.buch.code);
			ps2.setDouble(p++, type == 0 ? i.buch.preis : i.precioVenta);
			ps2.setInt(p++, getRunningYear());
			if (ps2.executeUpdate() != 1)
				throw new SQLException("didn't update 1");
			ps2.close();
		}

		public void alsNichtBestelltMarkieren(Schueler s) throws SQLException {
			deleteCurrentBestellungen(s);
			@Sql
			String alsNichtBestelletMarkierenSql = "delete from bestellung where schuljahr = ? and schueler = ?";
			PreparedStatement ps3 = c
					.prepareStatement(alsNichtBestelletMarkierenSql);
			int p = 1;
			ps3.setInt(p++, getRunningYear());
			ps3.setInt(p++, s.num);
			ps3.executeUpdate();
			ps3.close();
			c.commit();
		}

		/**
		 * @param s
		 * @throws SQLException
		 */
		private void deleteCurrentBestellungen(Schueler s) throws SQLException {
			c.rollback();
			try {
				// ok schuljahr
				@Sql
				String deleteCurrentBestellungenSql = "select * from Bestellungen where Schuljahr = ? and Schueler = ?";
				PreparedStatement ps0 = c
						.prepareStatement(deleteCurrentBestellungenSql);
				ps0.setInt(1, getRunningYear());
				ps0.setInt(2, s.num);

				ResultSet rs = ps0.executeQuery();
				ArrayList<Integer> l = new ArrayList<Integer>();
				while (rs.next())
					l.add(rs.getInt("buch"));
				rs.close();
				ps0.close();

				// ok schuljahr
				@Sql
				String ps1Sql = "delete from Bestellungen where Schuljahr = ? and Schueler = ?";
				PreparedStatement ps1 = c.prepareStatement(ps1Sql);
				int p = 1;
				ps1.setInt(p++, getRunningYear());
				ps1.setInt(p++, s.num);
				ps1.executeUpdate();
				ps1.close();

				for (Integer bcode : l)
					updateRsvCount(bcode);
			} catch (SQLException e) {
				c.rollback();
				c2.rollback();
				throw e;
			}
		}

		private int getLastId(Connection c) throws SQLException {
			Statement stat2 = c.createStatement();
			ResultSet rs = stat2.executeQuery("select last_insert_rowid()");
			int res = rs.getInt(1);
			rs.close();
			stat2.close();
			return res;
		}

		public ArrayList<Buch> getMarcaje(String klasse) throws SQLException {
			ArrayList<Buch> l = new ArrayList<Buch>();
			c.rollback();
			try {
				// XXX FIXME check this
				@Sql
				final String sql = "select *, v.name as VerlagName, v.verlagcode as Verlag_Code, v.land as verlagland "
						+ "from Buecher b, BuecherKlassen bk, Verlage v  "
						+ "where b.VerlagCode = v.VerlagCode and bk.buch = b.buchcode and bk.Klasse = ? and b.schuljahr = ? and bk.schuljahr = ? and v.schuljahr = ? "
						+ "order by Land, fach, titel";
				System.err.println("sql: " + sql);
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				ps.setString(p++, klasse);
				ps.setInt(p++, getRunningYear());
				ps.setInt(p++, getRunningYear());
				ps.setInt(p++, getRunningYear());
				ResultSet rs = ps.executeQuery();

				HashMap<Integer, Buch> hm = new HashMap<Integer, Buch>();
				while (rs.next()) {
					Buch b = new Buch(rs);
					hm.put(b.code, b);
					l.add(b);
				}
				rs.close();
				if (true) {
					// ok schuljahr
					@Sql
					String sql2 = "select bm.buch, bmn.* from BuchMark bm, BuchMarken  bmn where bm.marke = bmn.id and bm.schuljahr = ? and bm.schuljahr = ?";
					PreparedStatement ps2 = c.prepareStatement(sql2);
					p = 1;
					ps2.setInt(p++, getRunningYear());
					ps2.setInt(p++, getRunningYear());
					ResultSet rs2 = ps2.executeQuery();
					while (rs2.next()) {
						int buchCode = rs2.getInt("buch");
						Buch buch = hm.get(buchCode);
						BuchBemerkung marke = new BuchBemerkung(rs2);
						if (buch != null) {
							buch.bemerkungen.add(marke.id);
						}
						// else
						// System.err.println("buch is null");
						// System.err.println("leyendo marca: " + buchCode + " "
						// + marke);
						// else
						// System.err
						// .println("tenemos que ignorar libro: " + buchCode);
					}
					rs2.close();
				}
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
			return l;
		}

		public void removeBookMark(BuchBemerkung data) throws SQLException {
			c.rollback();
			try {
				@Sql
				String sqlCheck = "select count(*) from buchmark where marke = ? and schuljahr = ? ";
				PreparedStatement psCheck = c.prepareStatement(sqlCheck);
				psCheck.setInt(1, data.id);
				psCheck.setInt(2, getRunningYear());
				ResultSet rs = psCheck.executeQuery();
				final int int1 = rs.getInt(1);
				rs.close();
				if (int1 != 0) {
					throw new SQLException(
							"No se puede borrar un comentario en uso en algún libro: "
									+ int1);
				}
				@Sql
				String sqlDelete = "delete from BuchMarken where id = ? and schuljahr = ? ";
				PreparedStatement ps = c.prepareStatement(sqlDelete);
				ps.setInt(1, data.id);
				ps.setInt(2, getRunningYear());
				if (ps.executeUpdate() != 1)
					throw new SQLException("delete no borró una fila, extraño");
				ps.close();
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}

		}

		public void removeBuch(Buch data) throws SQLException {
			if (data.isNew())
				throw new SQLException("cannot delete a not-yet-born book");
			try {
				// @Sql String sqlSelect = "select * from buecherklasse"

				@Sql
				String sql = "delete from buecher where buchcode=? and schuljahr = ? ";
				c.rollback();
				PreparedStatement ps = c.prepareStatement(sql);
				ps.setInt(1, data.code);
				ps.setInt(2, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("delete did not touch 1 row");
				c.createStatement().executeUpdate(
						"delete from buecherklassen where buch = " + data.code
								+ " and schuljahr =  " + getRunningYear());
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}

		}

		public void saveBestellung(Schueler s, ArrayList<Buch> wantedBooks)
				throws SQLException {
			c.rollback();
			c2.rollback();
			try {
				deleteCurrentBestellungen(s);
				double summe = 0;
				int nItems = 0;
				// ok schuljahr
				@Sql
				String sqlIns = "insert into Bestellungen (Schuljahr, Schueler, Buch, SchulJahr) values (?, ?, ?, ? )";
				PreparedStatement ps2 = c.prepareStatement(sqlIns);
				for (Buch b : wantedBooks) {
					summe += b.preis;
					nItems++;
					int p = 1;
					ps2.setInt(p++, getRunningYear());
					ps2.setInt(p++, s.num);
					ps2.setInt(p++, b.code);
					ps2.setInt(p++, getRunningYear());
					if (ps2.executeUpdate() != 1)
						throw new SQLException("INSERT didn't insert 1");
					updateRsvCount(b.code);
				}
				ps2.close();
				updateBestellungSummaryTable(s, summe, nItems);
				c.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				c.rollback();
				c2.rollback();
				throw e;
			}
		}

		public void saveBuch(Buch current, ArrayList<BooleanDifference> kdl,
				ArrayList<Integer> marke) throws SQLException {
			@Sql
			String sqlMainUpd = "update Buecher set Titel = ?, Verlagcode = ?, Isbn = ?, preis =?, Stock=?, sprache = ?, Fach = ?, bemerkDeu=?, bemerkSpa=?    where buchcode = ? and schuljahr = ?";
			@Sql
			String sqlMainIns = "insert into Buecher (Titel, VerlagCode, Isbn,  Preis, Stock, Sprache, Fach, bemerkDeu, bemerkSpa , BuchCode, Schuljahr) values (?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?)";
			@Sql
			String sqlBkIns = "insert into BuecherKlassen (buch, klasse, schuljahr) values (?, ?, ?)";
			@Sql
			String sqlBkDel = "delete from BuecherKlassen where buch = ? and  klasse =? and  schuljahr =?";
			c.rollback();

			try {
				final boolean isNew = current.isNew();
				c.prepareStatement("select 4").close();
				System.err.println("preparing: "
						+ (isNew ? sqlMainIns : sqlMainUpd));
				PreparedStatement s = c.prepareStatement(isNew ? sqlMainIns
						: sqlMainUpd);
				System.err.println("preparing ins");
				PreparedStatement sIns = c.prepareStatement(sqlBkIns);
				System.err.println("preparing del");
				PreparedStatement sDel = c.prepareStatement(sqlBkDel);

				int p = 1;
				s.setString(p++, current.titel);
				if (current.verlag != null)
					s.setInt(p++, current.verlag.code);
				else
					s.setNull(p++, Types.INTEGER);
				s.setString(p++, current.isbn);
				s.setDouble(p++, current.preis);
				s.setInt(p++, current.stock);

				s.setString(p++, current.getSpracheId());
				s.setString(p++, current.fach);
				s.setString(p++, current.bemerkDeu);
				s.setString(p++, current.bemerkSpa);

				if (isNew)
					s.setNull(p++, Types.INTEGER);
				else
					s.setInt(p++, current.code);
				s.setInt(p++, getRunningYear());
				int n = s.executeUpdate();
				if (n != 1)
					throw new SQLException("update did not update 1: " + n);
				Statement stat = c.createStatement();
				if (isNew)
					current.code = getLastId(c);

				stat.executeUpdate("delete from BuchMark where buch = "
						+ current.code + " and schuljahr= " + getRunningYear());
				PreparedStatement markInsPs = c
						.prepareStatement("insert into BuchMark (buch, marke, schuljahr) values (?, ?, ? )");
				if (isNew) {
					stat.executeUpdate("delete from BuecherKlassen where buch = "
							+ current.code);
				}

				for (BooleanDifference ch : kdl) {
					PreparedStatement ps = ch.value ? sIns : sDel;
					p = 1;
					ps.setInt(p++, current.code);
					ps.setString(p++, ch.key);
					ps.setInt(p++, getRunningYear());
					if (ps.executeUpdate() != 1)
						throw new SQLException("update did not update 1");
				}
				for (Integer m : marke) {
					System.err.println("INSERT MARKE: " + current.code + " "
							+ m);
					p = 1;
					markInsPs.setInt(p++, current.code);
					markInsPs.setInt(p++, m);
					markInsPs.setInt(p++, getRunningYear());
					// ps.setInt(p++, getRunningYear());
					if (markInsPs.executeUpdate() != 1)
						throw new SQLException("update did not update 1");
				}
				c.commit();
			} finally {
				c.rollback();
			}
		}

		public void saveBuchPrice(Buch b) throws SQLException {
			// ok schuljahr
			@Sql
			String sql = "update Buecher set preis = ? where buchcode = ? and schuljahr = ?";
			c.rollback();
			PreparedStatement ps = c.prepareStatement(sql);
			int p = 1;
			ps.setDouble(p++, b.preis);
			ps.setInt(p++, b.code);
			ps.setInt(p++, getRunningYear());
			if (ps.executeUpdate() != 1)
				throw new SQLException("price update did not update 1 row");
			c.commit();
		}

		public Connection saveVerkauf(Connection conn, DatosFactura df)
				throws SQLException {
			ArrayList<Buch> librosTocados = new ArrayList<Buch>();
			ResultSet rsss = null;
			try {
				// ok schuljahr
				PreparedStatement psUpdateStock = conn
						.prepareStatement("update buecher set stock = stock + ? where schuljahr = ? and buchcode=?");
				psUpdateStock.setInt(2, getRunningYear());

				PreparedStatement psCheckAvailability = conn
						.prepareStatement("select stock-rsv av from buecher where schuljahr = ? and buchcode=?");
				psCheckAvailability.setInt(1, getRunningYear());

				// ok schuljahr
				PreparedStatement psRemoveBestellungen = conn
						.prepareStatement("delete from bestellungen where schueler = ? and schuljahr = ? and buch=?");
				psRemoveBestellungen.setInt(1, df.schueler.num);
				psRemoveBestellungen.setInt(2, getRunningYear());
				for (ChoosableBuch i : df.rsvCanc) {
					psRemoveBestellungen.setInt(3, i.buch.code);
					if (psRemoveBestellungen.executeUpdate() != 1)
						throw new SQLException("no dio 1");
					librosTocados.add(i.buch);
				}

				if (!df.comprar.isEmpty() || !df.devolver.isEmpty()) {
					// ok schuljahr
					String sql1 = "insert into rechnungen (schuljahr, schueler, zahlungsdatum) values (?, ?, ?)";
					PreparedStatement ps1 = conn.prepareStatement(sql1);
					int p = 1;
					ps1.setInt(p++, getRunningYear());
					ps1.setInt(p++, df.schueler.num);
					ps1.setDate(p++, new Date(System.currentTimeMillis()));
					int n = ps1.executeUpdate();
					if (n != 1)
						throw new SQLException("didn't update 1");
					ps1.close();
					df.id = getLastId(conn);
					for (ChoosableBuch cbuch : df.comprar) {
						if (!cbuch.initiallyBestellt) {
							psCheckAvailability.setInt(2, cbuch.buch.code);
							rsss = psCheckAvailability.executeQuery();
							if (!rsss.next())
								throw new SQLException(
										"availability query didn't give 1 row");
							int av = rsss.getInt("av");
							System.err.println("available for "
									+ cbuch.buch.titel + ": " + av);
							if (av <= 0)
								throw new SQLException(
										"No hay ejemplares disponibles para "
												+ cbuch.buch.titel);
							rsss.close();
						}

						if (cbuch.buch.preis <= 0)
							throw new SQLException(
									"Ein Buch ohne Preis kann nicht verkauft werden: '"
											+ cbuch.buch.titel + "'.");
						addLine(conn, p, df.id, cbuch, 0);
						psUpdateStock.setInt(1, -1);
						psUpdateStock.setInt(3, cbuch.buch.code);
						if (psUpdateStock.executeUpdate() != 1)
							throw new SQLException("stock+1 no 1");
						psRemoveBestellungen.setInt(3, cbuch.buch.code);
						System.err.println("ps6: "
								+ psRemoveBestellungen.executeUpdate());
						librosTocados.add(cbuch.buch);
					}
					for (ChoosableBuch cbuch : df.devolver) {
						addLine(conn, p, df.id, cbuch, 1);
						psUpdateStock.setInt(1, 1);
						psUpdateStock.setInt(3, cbuch.buch.code);
						if (psUpdateStock.executeUpdate() != 1)
							throw new SQLException("stock-1 no 1");
						psRemoveBestellungen.setInt(3, cbuch.buch.code);
						System.err.println("ps6: "
								+ psRemoveBestellungen.executeUpdate());
						librosTocados.add(cbuch.buch);
					}
				}
				for (Buch b : librosTocados)
					updateRsvCount(conn, b.code);
			} catch (SQLException e) {
				if (rsss != null)
					rsss.close();
				e.printStackTrace();
				conn.rollback();
				conn.close();
				throw e;
			}
			return conn;
		}

		/**
		 * @param s
		 * @param summe
		 * @param nItems
		 * @throws SQLException
		 */
		private void updateBestellungSummaryTable(Schueler s, double summe,
				int nItems) throws SQLException {
			// ok schuljahr
			@Sql
			String sqlDel = "delete from bestellung where schuljahr = ? and schueler = ?";
			PreparedStatement ps3 = c.prepareStatement(sqlDel);
			int p = 1;
			ps3.setInt(p++, getRunningYear());
			ps3.setInt(p++, s.num);
			ps3.executeUpdate();
			ps3.close();
			// ok schuljahr
			@Sql
			String sqlInsert = "insert into bestellung (schuljahr, schueler, summe, nItems, finished) values (? ,  ?, ? ,?, ?)";
			PreparedStatement ps4 = c.prepareStatement(sqlInsert);
			p = 1;
			ps4.setInt(p++, getRunningYear());
			ps4.setInt(p++, s.num);
			ps4.setDouble(p++, summe);
			ps4.setInt(p++, nItems);
			ps4.setBoolean(p++, true);
			ps4.executeUpdate();
			ps4.close();
			System.err.println("summe: " + summe);
		}

		private void updateRsvCount(Connection c, int bcode)
				throws SQLException {
			System.err.println("updateRsvCount " + bcode);
			System.err.println(sqlUpdateRsvCount);
			PreparedStatement ps5 = c.prepareStatement(sqlUpdateRsvCount);
			ps5.setInt(1, getRunningYear());
			ps5.setInt(2, bcode);
			if (ps5.executeUpdate() != 1)
				throw new SQLException("rsv update no actualizó 1");
			ps5.close();
		}

		private void updateRsvCount(int bcode) throws SQLException {
			updateRsvCount(c, bcode);
		}

	}

	public class BuchungstypVw {
		static final String tablename = "Buchungstyp";

		public int getNextId() throws SQLException {
			@Sql
			String sql = MessageFormat.format(
					"select MAX(id)+1 from {0} where schuljahr = ?", tablename);
			System.err.println(sql);
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				ResultSet rs = s.executeQuery();
				if (!rs.next())
					throw new SQLException("no next");
				int ret = rs.getInt(1);
				rs.close();
				s.close();
				return ret;
			} catch (SQLException e) {
				c.rollback();
				throw e;
			} finally {
				c.rollback();
			}

		}

		public ArrayList<Buchungstyp> getBuchungTypen() throws SQLException {
			@Sql
			String sql = MessageFormat.format(
					"select * from {0} where schuljahr = ? order by id",
					tablename);
			System.err.println(sql);
			try {
				ArrayList<Buchungstyp> l = new ArrayList<Buchungstyp>();
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					l.add(new Buchungstyp(rs));
				}
				rs.close();
				s.close();
				return l;
			} catch (SQLException e) {
				c.rollback();
				throw e;
			} finally {
				c.rollback();
			}
		}

		public void insertBuchungstyp(Buchungstyp data) throws SQLException {
			@Sql
			String sql = "INSERT INTO " + tablename
					+ " VALUES (?, ?, ?, ?, ?);";
			System.err.println(sql);
			try {
				PreparedStatement s = c.prepareStatement(sql);
				int p = 1;
				s.setInt(p++, getRunningYear());
				s.setInt(p++, data.id);
				s.setString(p++, data.description);
				s.setDouble(p++, data.iva);
				s.setString(p++, data.account);
				int rs = s.executeUpdate();
				if (rs != 1)
					throw new SQLException("insertBuchungstyp failed");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			} finally {
				c.rollback();
			}
		}

		public void remove(Buchungstyp data) throws SQLException {
			@Sql
			String sql = MessageFormat.format(
					"DELETE FROM {0} where schuljahr = ? AND id = ? ",
					tablename);
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				s.setInt(2, data.id);
				int rs = s.executeUpdate();
				if (rs != 1)
					throw new SQLException("Buchunstyp inexistente");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}
	}

	public class RemesaVw {
		public ArrayList<Remesa> getRemesas() throws SQLException {
			ArrayList<Remesa> l = new ArrayList<Remesa>();
			@Sql
			String sql = "select * from Buchungsset where schuljahr = ? order by kreationdatum";
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					l.add(new Remesa(rs));
				}
				rs.close();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
			return l;
		}

		public ArrayList<RechnungInfo> getRemesaById(int remesaId) {
			ArrayList<RechnungInfo> ret = new ArrayList<RechnungInfo>();
			// FIXME
			// select * from rechnungen where id >= principio and id <= fin
			return ret;
		}

		public void createNewRemesa() throws SQLException {
			// TODO Auto-generated method stub
			throw new SQLException("fixme");
		}

		public boolean newPendingRechnungen() {
			return false;
		}
	}

	class FaecherVw {
		public ArrayList<Fach> getFaecher() throws SQLException {
			ArrayList<Fach> l = new ArrayList<Fach>();
			@Sql
			String sql = "select * from Faecher where schuljahr = ? order by name";
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					l.add(new Fach(rs));
				}
				rs.close();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
			return l;
		}

		public void removeFach(Fach data) throws SQLException {
			@Sql
			String sql = "delete from faecher where schuljahr = ? and name = ?";
			c.rollback();
			try {
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				ps.setInt(p++, getRunningYear());
				ps.setString(p++, data.name);
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("we didn't touch 1 rows but " + n);
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}

		}

		public void insertFach(Fach data) throws SQLException {
			@Sql
			String sql = "insert into faecher (name, schuljahr) values (?,?)";
			c.rollback();
			try {
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				System.err.println("añadimos asignatura: " + data.name);
				ps.setString(p++, data.name);
				ps.setInt(p++, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("we didn't touch 1 rows but " + n);
				System.err.println("commit!");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}
	}

	class AbteilungVw {
		public void deleteAbteilung(Abteilung a) throws SQLException {
			c.rollback();

			PreparedStatement ps = null;
			try {
				ps = c.prepareStatement("delete from abteilungen where name=? and schuljahr = ?");
				int p = 1;
				ps.setString(p++, a.name);
				ps.setInt(p++, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("Didn't touch 1 row");
				c.commit();
			} catch (SQLException e) {
				if (ps != null)
					ps.close();
				c.rollback();

				throw e;
			}

		}

		public void updateAbteilung(Abteilung a) throws SQLException {
			c.rollback();

			PreparedStatement ps = null;
			try {
				ps = c.prepareStatement("update abteilungen set name = ? , ordIndex = (select max(ordIndex) +1 from abteilungen), schuljahr = ? ");
				int p = 1;
				ps.setString(p++, a.name);
				ps.setInt(p++, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("Didn't insert 1 row");
				c.commit();
			} catch (SQLException e) {
				if (ps != null)
					ps.close();
				c.rollback();

				throw e;
			}

		}

		public void insertAbteilung(Abteilung a) throws SQLException {
			c.rollback();

			PreparedStatement ps = null;
			try {
				ps = c.prepareStatement("insert into abteilungen (name, ordIndex, schuljahr) valueS ( ? , (select max(ordIndex) +1 from abteilungen), ?) ");
				int p = 1;
				ps.setString(p++, a.name);
				ps.setInt(p++, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("Didn't insert 1 row");
				c.commit();
			} catch (SQLException e) {
				if (ps != null)
					ps.close();
				c.rollback();

				throw e;
			}

		}

		public void swap(Abteilung data, Abteilung data2) throws SQLException {
			try {
				c.rollback();
				PreparedStatement ps0 = c
						.prepareStatement("select ordindex from abteilungen where schuljahr = ? and name =?");
				ps0.setInt(1, getRunningYear());
				ps0.setString(2, data.name);
				ResultSet rs = ps0.executeQuery();
				int idx0 = rs.getInt(1);
				ps0.setString(2, data2.name);
				rs.close();

				rs = ps0.executeQuery();
				int idx1 = rs.getInt(1);

				ps0.close();
				PreparedStatement ps1 = c
						.prepareStatement("update abteilungen set ordindex = ? where schuljahr = ? and name =?");
				ps1.setInt(2, getRunningYear());
				ps1.setNull(1, Types.INTEGER);
				ps1.setString(3, data2.name);
				if (ps1.executeUpdate() != 1)
					throw new SQLException("didn't touch 1 row (nullifying)");

				ps1.setInt(2, getRunningYear());
				ps1.setInt(1, idx1);
				ps1.setString(3, data.name);
				if (ps1.executeUpdate() != 1)
					throw new SQLException("didn't touch 1 row");

				ps1.setInt(2, getRunningYear());
				ps1.setInt(1, idx0);
				ps1.setString(3, data2.name);
				if (ps1.executeUpdate() != 1)
					throw new SQLException("didn't touch 1 row");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}

		}
	}

	public class KlassenVw {
		public ArrayList<Abteilung> getAbteilungen() throws SQLException {
			ArrayList<Abteilung> l = new ArrayList<Abteilung>();
			String sql = "select name from Abteilungen where schuljahr = ? order by ordindex";

			// Connection c = getConnection();
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					l.add(new Abteilung(rs.getString("name"),
							getKlassenInAbteilung(rs.getString("name"))));
				}
				rs.close();
			} finally {
				// close(c);
			}
			return l;
		}

		public ArrayList<Klasse> getKlassen(String orderField)
				throws SQLException {
			ArrayList<Klasse> l = new ArrayList<Klasse>();
			// ok schuljahr
			String sql = "select *, (select  count(*) from schueler s where  s.klasse = k.klasse and s.schuljahr = k.schuljahr) as snum from Klassen k where schuljahr = ? ";
			if (orderField != null)
				sql += "  order by " + orderField;
			Connection c = getConnection();
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setInt(1, getRunningYear());
				ResultSet rs = s.executeQuery();
				while (rs.next())
					l.add(new Klasse(rs));
				rs.close();
			} finally {
				close(c);
			}
			return l;
		}

		private ArrayList<Klasse> getKlassenInAbteilung(String string)
				throws SQLException {
			ArrayList<Klasse> l = new ArrayList<Klasse>();
			String sql = "select * from Klassen where abteilung = ? and schuljahr = ? order by jahr,Klasse";

			Connection c = getConnection();
			try {
				PreparedStatement s = c.prepareStatement(sql);
				s.setString(1, string);
				s.setInt(2, getRunningYear());
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					l.add(new Klasse(rs));
				}
			} finally {
				close(c);
			}
			return l;

		}

		public void removeKlasse(Klasse klasse) throws SQLException {
			ArrayList<Schueler> l = getSchueler(klasse);
			if (!l.isEmpty())
				throw new SQLException(
						"No se puede borrar un grupo con alumnos");

			String sql = "delete from Klassen where SchulJahr = ? and Klasse = ? ";
			c.rollback();
			try {
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				ps.setInt(p++, getRunningYear());
				ps.setString(p++, klasse.name);
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("DELETE didn't delete 1 row: " + n);
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}

		public Klasse getKlasse(String name) throws SQLException {
			String sql = "select * from  Klassen where schuljahr = ? and Klasse = ? ";
			c.rollback();
			ResultSet rs = null;
			try {
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				ps.setInt(p++, getRunningYear());
				ps.setString(p++, name);
				rs = ps.executeQuery();
				if (!rs.next())
					return null;
				return new Klasse(rs.getString("klasse"),
						rs.getString("abteilung"), rs.getInt("jahr"),
						getRunningYear());
			} finally {
				if (rs != null)
					rs.close();
				c.rollback();
			}

		}

		public void saveKlasse(Klasse data) throws SQLException {
			PreparedStatement ps;
			int p = 1;
			c.rollback();
			if (getKlasse(data.name) != null) {
				ps = c.prepareStatement("update Klassen set Abteilung = ?, Jahr = ? where schuljahr = ? and Klasse = ? ");
			} else {
				ps = c.prepareStatement("insert into Klassen  (abteilung, jahr, schuljahr, klasse) values (?, ?, ?, ?)");

			}
			c.rollback();
			try {
				ps.setString(p++, data.abteilung);
				ps.setInt(p++, data.jahr);
				ps.setInt(p++, getRunningYear());
				ps.setString(p++, data.name);
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("update  didn't touch 1 row: " + n);
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}

		}
	}

	class RechnungVw {
		/**
		 * broken
		 * 
		 * @param schuelerNum
		 * @return
		 * @throws SQLException
		 */
		public ArrayList<LineaFactura> getBestellung(int schuelerNum)
				throws SQLException {
			// ok schuljahr
			String sql = "select Titel, v.Name as Verlag, Preis, Isbn, BuchCode  "
					+ " from Bestellungen be,  Buecher bue, BuecherKlassen bk, Schueler s , Verlage v"
					+ " where  be.schuljahr = ? and bue.schuljahr = ? and bk.schuljahr = ? and s.schuljahr = ? and v.schuljahr = ? and bue.BuchCode = bk.Buch and bue.BuchCode = be.Buch and bue.VerlagCode = v.VerlagCode and be.Schueler =? and s.Schuelernum = be.Schueler and bk.Klasse = s.Klasse";
			ArrayList<LineaFactura> l = new ArrayList<LineaFactura>();
			Connection c = getConnection();
			try {
				PreparedStatement s = c.prepareStatement(sql);
				int p = 1;
				for (int i = 0; i < 5; i++)
					s.setInt(p++, getRunningYear());

				s.setInt(p++, schuelerNum);
				ResultSet rs = s.executeQuery();
				while (rs.next())
					l.add(new LineaFactura(rs));
			} finally {
				close(c);
			}
			return l;
		}

		public List<RechnungLine> getRechnung(Schueler schueler)
				throws SQLException {
			ArrayList<RechnungLine> rll = new ArrayList<RechnungLine>();
			for (ChoosableBuch i : getBooksForUser(schueler))
				rll.add(new RechnungLine(i.buch));
			// FIXME: falta schuljahr

			return rll;
		}

		public List<DatosFactura> getRechnungen(Schueler schueler)
				throws SQLException {
			ArrayList<DatosFactura> rll = new ArrayList<DatosFactura>();
			HashMap<Integer, DatosFactura> hm = new HashMap<Integer, DatosFactura>();
			c.rollback();
			try {
				// ok schuljahr. hay que comprobar esta sentencia.
				@Sql
				String sql = "select id,zahlungsdatum, (select sum((1-rl.type*2) * rl.preis) from rechnunglin rl where rl.id = rechnungen.id and rl.schuljahr = rechnungen.schuljahr) as summe from Rechnungen where schuljahr = ? and schueler = ? ";
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				ps.setInt(p++, getRunningYear());
				ps.setInt(p++, schueler.num);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					DatosFactura df = new DatosFactura(rs.getInt("id"),
							schueler, rs.getDate("zahlungsdatum"));
					df.summe = rs.getDouble("summe");
					rll.add(df);
					hm.put(df.id, df);
				}
				rs.close();
				ps.close();
			} finally {
				c.rollback();
			}
			return rll;
		}

		public ArrayList<BestellungInfo> getRechnungenStartingWith(
				String prefix, BestellungState filter, String klasse)
				throws SQLException {
			c.rollback();
			ArrayList<BestellungInfo> l;
			ResultSet rs;
			try {
				l = new ArrayList<BestellungInfo>();
				// ok schuljahr
				@Sql
				String sql = "select *  from Schueler where SchulJahr = ?  and (? is null or klasse = ?)  and name_norm like ? order by name, vorname ";
				// ok schuljahr
				@Sql
				String sql2 = "select count(Preis) as Buecher, sum(Preis) as Summe , Name, Vorname, Klasse, s.Schuelernum  "
						+ " from Bestellungen be, Buecher bu, Schueler s "
						+ " where be.schuljahr = bu.schuljahr and be.schuljahr = s.schuljahr  and s.schuljahr = ? and be.Buch = bu.BuchCode and s.SchuelerNum = be.Schueler  and be.schueler=?"
						+ " group by be.Schueler" + " order by name, vorname";

				PreparedStatement s = c.prepareStatement(sql);
				PreparedStatement s2 = c2.prepareStatement(sql2);
				int p = 1;
				s.setInt(p++, getRunningYear());
				s.setString(p++, klasse);
				s.setString(p++, klasse);
				// s.setInt(p++, getRunningYear());
				s.setString(p++, normalizeName(prefix) + "%");
				rs = s.executeQuery();
				while (rs.next()) {
					p = 1;
					s2.setInt(p++, getRunningYear());
					s2.setInt(p++, rs.getInt("schuelernum"));
					ResultSet rs2 = s2.executeQuery();
					double summe = 0;// FIXME
					if (rs2.next()) {
						summe = rs2.getDouble("summe");
					}
					rs2.close();
					if (filter == BestellungState.BESTELLTE && summe != 0
							|| filter == null
							|| filter == BestellungState.NICHT_BESTELLTE
							&& summe == 0)
						l.add(new BestellungInfo(new Schueler(rs), summe));
				}
				rs.close();
			} finally {
				c.rollback();
				c2.rollback();
			}
			return l;
		}

		public ArrayList<LineaFactura> getRechnungLines(int rn)
				throws SQLException {
			// ok schuljahr
			@Sql
			String sql = "select rl.type as type, Titel, v.Name as Verlag, rl.Preis, Isbn, bue.BuchCode "
					+ " from RechnungLin rl inner join  Buecher bue on bue.buchcode = rl.buchcode left join Verlage v on bue.verlagcode = v.verlagcode "
					+ " where rl.id =? ORDER by Land,Verlag,Titel ";
			ArrayList<LineaFactura> l = new ArrayList<LineaFactura>();
			Connection c = getConnection();
			try {
				PreparedStatement s = c.prepareStatement(sql);
				int p = 1;
				s.setInt(p++, rn);
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					l.add(new LineaFactura(rs));
				}
			} finally {
				close(c);
			}
			return l;
		}

		ResultSet simpleQuery(String sql) throws SQLException {
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(sql);
			rs.close();
			s.close();
			return rs;
		}

		public Rechnung getRechnung(int id) {
			return null; // FIXME
			// Rechnung r = new Rechnung(
			// simpleQuery("select * from rechnungen where id = " + id));
			// r.setSchueler(new Schueler(
			// simpleQuery("select * from schueler where schuelernum = " + 1)));
			// ResultSet rsz =
			// simpleQuery("select * from rechnung where rechnungid = "
			// + r.id);
			//
			// Collection<LineaFactura> z;
			// r.setRechnungZeilen(z);
			// return r;
		}

		public ArrayList<RechnungInfo> getRechungenInDatum(Date data,
				String sortBy) throws SQLException {
			System.err.println("getRechungenInDatum(" + data + ", " + sortBy
					+ ")");
			// ok schuljahr
			String sql = "select"
					+ " re.id as rid , Zahlungsdatum, count(rl.Preis) as Buecher, "
					+ " sum((1-rl.type*2) * rl.preis)  as Summe ,sum((1-rl.type) * rl.preis)  as Summe0,"
					+ "sum(rl.type * rl.preis)  as Summe1 , Name, Vorname, Klasse, bu.bemerkDeu, bu.bemerkSpa, "
					+ " s.SchuelerNum schuelernum, email  "
					+ " from Rechnungen re, RechnungLin rl, Buecher bu, Schueler s"
					+ " where   re.schuljahr = ? and rl.schuljahr = ? and bu.schuljahr = ? and s.schuljahr = ? and 	 re.id = rl.id and rl.BuchCode = bu.Buchcode and s.SchuelerNum = re.Schueler and Zahlungsdatum is  not null  "
					+ (data != null ? "and date(zahlungsdatum/1000, 'unixepoch', 'localtime')= date(?/1000, 'unixepoch', 'localtime')"
							: "") + "   group by rl.id";
			if (sortBy != null)
				sql += " order by " + sortBy;
			System.err.println("sql: " + sql);
			ArrayList<RechnungInfo> l = new ArrayList<RechnungInfo>();
			Connection c = getConnection();
			try {
				PreparedStatement s = c.prepareStatement(sql);
				// s.setInt(1, getRunningYear());
				int p = 1;
				for (int i = 0; i < 4; i++) {
					s.setInt(p++, getRunningYear());
				}
				if (data != null)
					s.setDate(p++, data);
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					final RechnungInfo ri = new RechnungInfo(rs.getInt("rid"),
							new Schueler(rs), rs.getDate("Zahlungsdatum"),
							rs.getDouble("Summe"));
					ri.totalDev = rs.getDouble("summe1");
					ri.totalVentas = rs.getDouble("summe0");
					l.add(ri);
				}
			} finally {
				close(c);
			}
			System.err.println("l has " + l.size() + " elements");
			return l;
		}
	}

	class SchuelerVw {
		public void removeSchueler(int num) throws SQLException {
			if (num == -1)
				throw new SQLException("cannot delete -1, not implemented");
			c.rollback();

			try {
				removePedidosForUser(num);
				{
					@Sql
					String sql = "delete from Schueler where SchuelerNum =? and SchulJahr = ? ";
					PreparedStatement s = c.prepareStatement(sql);
					int p = 1;
					s.setInt(p++, num);
					s.setInt(p++, getRunningYear());
					int n = s.executeUpdate();
					if (n != 1)
						throw new SQLException("update did not update 1: " + n);
				}
				c.commit();
			} finally {
				c.rollback();
			}
		}

		private int removePedidosForUser(int schuelerNum) throws SQLException {
			{
				@Sql
				String sql2 = "delete from bestellung where schuljahr = ? and schueler = ? ";
				PreparedStatement s2 = c.prepareStatement(sql2);
				s2.setInt(1, getRunningYear());
				s2.setInt(2, schuelerNum);
				int deletedBestellungen = s2.executeUpdate();
				System.err.println("se han borrado " + deletedBestellungen
						+ " pedidos");
			}
			{
				@Sql
				String sql4 = "select * from bestellungen where schuljahr = ? and schueler = ?";
				@Sql
				String sql3 = "delete from bestellungen where schuljahr = ? and schueler = ? ";
				PreparedStatement s2 = c.prepareStatement(sql3);
				PreparedStatement s4 = c.prepareStatement(sql4);
				s2.setInt(1, getRunningYear());
				s2.setInt(2, schuelerNum);
				s4.setInt(1, getRunningYear());
				s4.setInt(2, schuelerNum);
				ResultSet rs = s4.executeQuery();
				ArrayList<Integer> l = new ArrayList<Integer>();
				while (rs.next())
					l.add(rs.getInt("buch"));
				s4.close();

				int deletedBestellungen = s2.executeUpdate();
				System.err.println("se han borrado " + deletedBestellungen
						+ " líneas de pedido");
				s2.close();
				for (int i : l)
					buchVw.updateRsvCount(c, i);
				return l.size();
			}
		};

		public void deleteStudents() throws SQLException {
			try {
				c.rollback();
				// ok schuljahr
				String sql1 = "delete from schueler where schuljahr = ?";
				System.err.println("sql: " + sql1);
				PreparedStatement s1 = c.prepareStatement(sql1);
				s1.setInt(1, getRunningYear());
				int deleted = s1.executeUpdate();
				System.err.println("hemos borrado alumnos: " + deleted);
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}

		public void importStudents(ArrayList<Schueler> sl) throws SQLException {
			try {
				c.rollback();
				for (Schueler i : sl) {
					i.num = -1; // FIXME
					saveSchueler(i);
				}
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}

		public String saveSchueler(Schueler current) throws SQLException {

			c.rollback();

			String msg = null;
			String key[] = RauUtil.postfix(Schueler.keyFields, "=?");
			String[] dataFields = RauUtil.postfix(Schueler.dataFields, "=?");

			Schueler oldSchueler = null;

			if (current.num != -1) {
				oldSchueler = getSchueler(current.num);
				if (!oldSchueler.klasse.equals(current.klasse)
						&& rechnungVw.getRechnungen(current).size() != 0) {
					throw new SQLException(
							"Die Klasse kann nicht gewechselt werden: es wurden Rechnungen erstellt.");
				}
			}
			// ok schuljahr
			String sql = "update Schueler set "
					+ RauUtil.separate(dataFields, ", ") + " where "
					+ RauUtil.separate(key, " and ");
			// ok schuljahr
			String insSql = "insert into Schueler ("
					+ RauUtil.separate(Schueler.dataFields, ", ")
					+ ", "
					+ RauUtil.separate(Schueler.keyFields, ",")
					+ ")  values ("
					+ RauUtil.separate(
							RauUtil.getRepetition(dataFields.length
									+ key.length, "?"), ", ") + ")";
			System.err.println((current.num == -1 ? insSql : sql) + " "
					+ current);

			try {
				PreparedStatement s = c
						.prepareStatement(current.num == -1 ? insSql : sql);
				int p = 1;
				s.setString(p++, current.vorName);
				s.setString(p++, current.name);
				s.setString(p++, normalizeName(current.name));
				s.setString(p++, current.klasse);
				s.setString(p++, current.email);
				s.setString(p++, current.bemerkDeu);
				s.setString(p++, current.bemerkSpa);
				if (current.num == -1)
					s.setNull(p++, Types.INTEGER);
				else
					s.setInt(p++, current.num);
				s.setInt(p++, getRunningYear());
				int n = s.executeUpdate();
				if (n != 1)
					throw new SQLException("update did not update 1: " + n);

				if (oldSchueler != null
						&& !current.klasse.equals(oldSchueler.klasse)) {
					if (removePedidosForUser(current.num) != 0)
						msg = " Die Bestellung wurde gelöscht.";
				}

				c.commit();
				return msg;
			} catch (SQLException e) {
				dump();
				throw e;
			} finally {
				c.rollback();
			}
		}

		public void setBemerkung(Schueler s) throws SQLException {
			System.err.println("setBemerk " + s);
			saveSchueler(s);

		}
	}

	class SprachVw {
		public final HashMap<String, Language> langById = new HashMap<String, Language>();
		public final HashMap<String, Language> langByName = new HashMap<String, Language>();
		public final static String tname = "sprachen";

		public void removeLanguage(Language data) throws SQLException {
			System.err.println("borrando: " + data);
			@Sql
			String sql = "delete from " + tname
					+ " where id = ? and schuljahr = ?";
			c.rollback();
			try {
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				ps.setString(p++, data.id);
				ps.setInt(p++, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("we didn't touch 1 rows but " + n);
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}

		}

		public void insertSprache(Language data) throws SQLException {
			@Sql
			String sql = "insert into " + tname
					+ " (id, name, schuljahr) values (?, ?,?)";
			c.rollback();
			try {
				PreparedStatement ps = c.prepareStatement(sql);
				int p = 1;
				System.err.println("añadimos lengua: " + data.name);
				ps.setString(p++, data.id);
				ps.setString(p++, data.name);
				ps.setInt(p++, getRunningYear());
				int n = ps.executeUpdate();
				if (n != 1)
					throw new SQLException("we didn't touch 1 rows but " + n);
				System.err.println("commit!");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}
	}

	class UserVw {
		public GenericRequestInterface<User> getUsers() throws SQLException {
			// ok schuljahr: esta tabla es igual para todos los años.
			@Sql
			String sql = "select * from dsvUsers";
			Connection c = getConnection();
			PreparedStatement s = c.prepareStatement(sql);
			return new UserIncRequest<User>(c, s.executeQuery(), User.class);
		}
	}

	class VerlagVw {
		public GenericRequestInterface<Verlag> getVerlage() throws SQLException {
			// ok schuljahr
			@Sql
			String sql = "select * from Verlage where schuljahr = ? order by name ";
			Connection c = getConnection();
			PreparedStatement s = c.prepareStatement(sql);
			s.setInt(1, getRunningYear());
			return new VerlageInMemRequest(c, s.executeQuery());
		}

		public void saveVerlag(Verlag data) throws SQLException {
			c.rollback();
			// ok schuljahr
			String iSql = "insert into verlage (name, land,verlagcode, schuljahr) values (?, ?, ?, ?)";
			// ok schuljahr
			String uSql = "update verlage set name =? , land =? where verlagcode= ? and schuljahr=?";
			PreparedStatement ps = c.prepareStatement(data.code == -1 ? iSql
					: uSql);
			int p = 1;
			ps.setString(p++, data.name);
			ps.setString(p++, data.land);
			if (data.code == -1)
				ps.setNull(p++, Types.INTEGER);
			else
				ps.setInt(p++, data.code);
			ps.setInt(p++, getRunningYear());
			int n = ps.executeUpdate();
			if (n != 1)
				throw new SQLException("did not update 1 row");
			c.commit();
		}
	}

	private static final boolean keepStock = false;

	private final PreparedStatement bestellungDetailPs;

	public final BuchVw buchVw = new BuchVw();
	public final BuchungstypVw buchungstypVw = new BuchungstypVw();
	public final RemesaVw remesaVw = new RemesaVw();

	public final HashMap<Integer, BuchBemerkung> buecherBemerkungen = new HashMap<Integer, BuchBemerkung>();

	private final Connection c;

	private final Connection c2;

	public final FaecherVw faecherVw = new FaecherVw();

	final public HashMap<String, Klasse> klassen = new HashMap<String, Klasse>();

	public final KlassenVw klassenVw = new KlassenVw();
	public final AbteilungVw abteilungenVw = new AbteilungVw();

	private final HashMap<Connection, Exception> openConns = new HashMap<Connection, Exception>();

	public final RechnungVw rechnungVw = new RechnungVw();

	private int runningYear;

	public final SchuelerVw schuelerVw = new SchuelerVw();

	public final SprachVw sprachVw = new SprachVw();

	private final String url;

	public final UserVw userVw = new UserVw();

	public final VerlagVw verlagVw = new VerlagVw();

	// final private PreparedStatement bestellungDetailPs2;
	public DsvBuecherDatasource(String url) throws SQLException {
		super();
		this.url = url;
		c = getConnection();
		c2 = getConnection();
		// ok schuljahr. complejo, comprobar
		@Sql
		String sql = "select count(*) as c,k.klasse  as klasse"
				+ " from bestellungen b"
				+ " inner join schueler s on b.schueler = s.schuelernum and b.schuljahr = s.schuljahr   "
				+ " inner join klassen k on k.klasse = s.klasse and k.schuljahr = b.schuljahr "
				+ " where b.schuljahr = ? and buch = ? group by s.klasse"
				+ " order by jahr, k.klasse";
		System.err.println(sql);
		bestellungDetailPs = c.prepareStatement(sql);
		// String sql2 =
		// "select count(*) as c from bestellungen b inner join schueler s on b.schueler = s.schuelernum and s.schuljahr = ?  where b.schuljahr=s.schuljahr and buch = ? and klasse = ?";
		// bestellungDetailPs2 = c.prepareStatement(sql2);

		// if (true)
		// try {

		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// FIXME falta schuljahr
		// if (false)
		System.err
				.println(c
						.createStatement()
						.executeUpdate(
								"update buecher set rsv = (select count(*) from bestellungen b inner join schueler on b.schueler = schueler.schuelernum  where b.schuljahr = buecher.schuljahr  and buch = buecher.buchcode)"));
		// System.err.println(c.createStatement().executeUpdate(
		// "update buecher set stock=rsv"));

		// FIXME falta schuljahr
		// if (false)
		System.err
				.println(c
						.createStatement()
						.executeUpdate(
								"update bestellung set summe = (select sum(preis) from bestellungen bl, buecher bu  where bu.buchcode = bl.buch and bl.schueler = bestellung.schueler);"));

		// System.err
		// .println(c
		// .createStatement()
		// .executeUpdate(
		// "update buecher set stock= (select count(*) from bestellungen b where b.schuljahr = buecher.schuljahr  and buch = buecher.buchcode)"));
		c.commit();

		// if (false)
		// normalizeAllSchueler();
		// System.exit(1); //FIXME

	}

	public void close() {
		close(c);
		close(c2);

	}

	/**
	 * @param c
	 */
	void close(Connection c) {
		try {
			if (c != null) {
				openConns.remove(c);
				c.close();
			}
		} catch (SQLException e) {
		}
		// System.err.println("closed: " + openConns.size());
	}

	public void dump() {
		System.err.println("connections left open: ");
		for (Entry<Connection, Exception> i : openConns.entrySet())
			try {
				if (!i.getKey().isClosed())
					i.getValue().printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public BestellungDetail getBestellungTotalForBook(int code)
			throws SQLException {
		ArrayList<String> detailList = new ArrayList<String>();
		bestellungDetailPs.setInt(1, getRunningYear());
		bestellungDetailPs.setInt(2, code);
		ResultSet rs = bestellungDetailPs.executeQuery();
		int total = 0;
		while (rs.next()) {
			total += rs.getInt("c");
			detailList.add(rs.getString("klasse") + "(" + rs.getInt("c") + ")");
		}
		rs.close();
		c.rollback();
		return new BestellungDetail(total, RauUtil.separate(
				detailList.toArray(), ","));
	}

	public HashMap<String, Integer> getBestellungTotalForBookWOG(int code,
			String klasse) throws SQLException {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		bestellungDetailPs.setInt(1, getRunningYear());
		bestellungDetailPs.setInt(2, code);
		ResultSet rs = bestellungDetailPs.executeQuery();
		while (rs.next())
			hm.put(rs.getString("klasse"), rs.getInt("c"));
		rs.close();
		return hm;
	}

	public ArrayList<ChoosableBuch> getBooksForUser(Schueler sch)
			throws SQLException {
		ArrayList<ChoosableBuch> l = new ArrayList<ChoosableBuch>();
		@Sql
		String sql = "select b.*,  v.Name  as VerlagName,b.verlagcode as Verlag_Code,  v.land as verlagland, (select count(*)"
				+ " from Bestellungen be where be.Schuljahr = b.schuljahr and Schueler = ? and be.Buch = b.BuchCode) as Bestellt "
				+ " from Buecher b, BuecherKlassen bk, Verlage v"
				+ " where b.schuljahr = bk.schuljahr and v.schuljahr = b.schuljahr and b.BuchCode = bk.Buch and b.schuljahr = ?  and Klasse = ? and b.VerlagCode = v.VerlagCode "
				+ " order by fach, titel";
		System.err.println("sql: " + sql);
		Connection c = getConnection();
		HashMap<Integer, Buch> hm = new HashMap<Integer, Buch>();
		try {
			PreparedStatement s = c.prepareStatement(sql);
			int p = 1;
			s.setInt(p++, sch.num);
			s.setInt(p++, getRunningYear());
			s.setString(p++, sch.klasse);
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				final Buch buch = new Buch(rs);
				hm.put(buch.code, buch);
				l.add(new ChoosableBuch(buch, rs.getInt("bestellt") != 0));
			}

			if (true) {
				// ok schuljahr
				@Sql
				String sql2 = "select bm.buch, bmn.* from BuchMark bm, BuchMarken  bmn where bm.schuljahr = ? and bmn.schuljahr = ? and bm.marke = bmn.id";

				p = 1;
				PreparedStatement ps2 = c.prepareStatement(sql2);
				ps2.setInt(p++, getRunningYear());
				ps2.setInt(p++, getRunningYear());
				ResultSet rs2 = ps2.executeQuery();
				while (rs2.next()) {
					int buchCode = rs2.getInt("buch");
					Buch buch = hm.get(buchCode);
					BuchBemerkung marke = new BuchBemerkung(rs2);
					if (buch != null) {
						buch.bemerkungen.add(marke.id);
					}
					// else
					// System.err.println("buch is null");
					// System.err.println("leyendo marca: " + buchCode + " " +
					// marke);
					// else
					// System.err
					// .println("tenemos que ignorar libro: " + buchCode);
				}
				rs2.close();
			}

		} finally {
			close(c);
		}
		System.err.println("l: " + l);
		return l;

	}

	public ArrayList<ChoosableBuch> getBooksForUser2(Schueler sch)
			throws SQLException {
		ArrayList<ChoosableBuch> l = new ArrayList<ChoosableBuch>();
		HashMap<Integer, ChoosableBuch> hm2 = new HashMap<Integer, ChoosableBuch>();
		String sql = "select b.*,  v.Name  as VerlagName,b.verlagcode as Verlag_Code,  v.land as verlagland, (select count(*) from Bestellungen be where Schuljahr = ? and Schueler = ? and be.Buch = b.BuchCode) as Bestellt "
				+ " from Buecher b, BuecherKlassen bk, Verlage v where b.BuchCode = bk.Buch and  Klasse = ?   and b.VerlagCode = v.VerlagCode and b.schuljahr = ? "
				+ " order by Land, fach, titel";
		Connection c = getConnection();
		HashMap<Integer, Buch> hm = new HashMap<Integer, Buch>();
		try {
			PreparedStatement s = c.prepareStatement(sql);
			int p = 1;
			s.setInt(p++, getRunningYear());
			s.setInt(p++, sch.num);
			s.setString(p++, sch.klasse);
			s.setInt(p++, getRunningYear());
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				final Buch buch = new Buch(rs);
				hm.put(buch.code, buch);
				final ChoosableBuch cb = new ChoosableBuch(buch,
						rs.getInt("bestellt") != 0);
				cb.zustand = cb.bestellt ? XKaufZustand.BESTELLT
						: XKaufZustand.NIX;
				hm2.put(buch.code, cb);
				l.add(cb);
			}

			{
				// ok schuljahr
				String sqlBuchMarken = "select bm.buch, bmn.* from BuchMark bm, BuchMarken  bmn where bm.schuljahr = ? and bmn.schuljahr = ? and bm.marke = bmn.id";
				PreparedStatement psBuchMarken = c
						.prepareStatement(sqlBuchMarken);
				psBuchMarken.setInt(1, getRunningYear());
				psBuchMarken.setInt(2, getRunningYear());
				ResultSet rs2 = psBuchMarken.executeQuery();
				while (rs2.next()) {
					int buchCode = rs2.getInt("buch");
					Buch buch = hm.get(buchCode);
					BuchBemerkung marke = new BuchBemerkung(rs2);
					if (buch != null) {
						buch.bemerkungen.add(marke.id);
					}
					// else
					// System.err.println("buch is null");
				}
				rs2.close();
			}

			{
				// ok schuljahr
				String sql2 = "select *,rl.preis as preis2"
						+ " from rechnungen r inner join rechnunglin rl on r.id = rl.id and schueler = ? and rl.schuljahr = r.schuljahr "
						+ " where r.schuljahr = ? " + " order by r.id";
				PreparedStatement ps2 = c.prepareStatement(sql2);
				ps2.setInt(1, sch.num);
				ps2.setInt(2, getRunningYear());
				ResultSet rs3 = ps2.executeQuery();
				while (rs3.next()) {
					int type = rs3.getInt("type");
					double preis = rs3.getDouble("preis2");
					final ChoosableBuch cb = hm2.get(rs3.getInt("buchcode"));
					cb.precioVenta = preis;
					if (cb != null) {
						// RechnungLine rl = new RechnungLine(type, cb.buch,
						// preis);
						if (type == 0)
							cb.zustand = XKaufZustand.VERKAUFT;
						else
							cb.zustand = XKaufZustand.DEVUELTO;
					}

				}
			}
		} finally {
			close(c);
		}

		return l;

	}

	public synchronized ArrayList<BuchBemerkung> getBuchMarken()
			throws SQLException {
		ArrayList<BuchBemerkung> l = new ArrayList<BuchBemerkung>();
		// ok schuljahr
		String sql = "select * from buchmarken where schuljahr = ? order by dsc";
		try {
			PreparedStatement s = c.prepareStatement(sql);
			System.err.println("running year: " + getRunningYear());
			s.setInt(1, getRunningYear());
			System.err.println("running query: " + sql);
			System.err.println("running query: " + s);
			ResultSet rs = s.executeQuery();
			while (rs.next())
				l.add(new BuchBemerkung(rs));
			rs.close();
		} finally {

			c.rollback();
		}
		System.err.println("leídas marcas: " + l);
		return l;

	}

	public synchronized ArrayList<Buch> getBuecher(String orderField)
			throws SQLException {
		c.rollback();
		ArrayList<Buch> l = new ArrayList<Buch>();
		// ok schuljahr
		String sql = " select bbb.n as bhbest, *, v.name as VerlagName, v.verlagcode as Verlag_Code, v.land as verlagland "
				+ " , (select  min(klassen.jahr ) from buecherklassen bk inner join klassen on bk.klasse = klassen.klasse and bk.buch = b.buchcode ) as minjahr "
				+ " from Buecher b"
				+ " inner join Verlage v on b.VerlagCode = v.VerlagCode"
				+ " left join bhbuchbest bbb on b.buchcode = bbb.buch"
				+ " where b.schuljahr = ? and v.schuljahr = ? ";
		if (orderField != null)
			sql += "  order by " + orderField;
		PreparedStatement s = c.prepareStatement(sql);
		s.setInt(1, getRunningYear());
		s.setInt(2, getRunningYear());
		ResultSet rs = s.executeQuery();
		HashMap<Integer, Buch> hm = new HashMap<Integer, Buch>();
		while (rs.next()) {
			Buch b = new Buch(rs);
			b.bhbest = rs.getInt("bhbest");
			b.vv = rs.getInt("vv");
			b.sbst = rs.getInt("sbst");
			b.minjahr = rs.getInt("minjahr");
			hm.put(b.code, b);
			l.add(b);
		}
		rs.close();
		c.rollback();
		getKlassenForBuch(hm);
		getBuchMarken(hm);

		c.commit();
		return l;
	}

	Buch getBuch(int buchcode) throws SQLException {
		c.rollback();
		ResultSet rs = null;
		try {
			String sql = " select bbb.n as bhbest, *, v.name as VerlagName, v.verlagcode as Verlag_Code, v.land as verlagland "
					+ " , (select  min(klassen.jahr ) from buecherklassen bk inner join klassen on bk.klasse = klassen.klasse and bk.buch = b.buchcode ) as minjahr "
					+ " from Buecher b"
					+ " inner join Verlage v on b.VerlagCode = v.VerlagCode"
					+ " left join bhbuchbest bbb on b.buchcode = bbb.buch"
					+ " where b.schuljahr = ? and v.schuljahr = ? and b.buchcode = ?";
			PreparedStatement s = c.prepareStatement(sql);
			int p = 1;
			s.setInt(p++, getRunningYear());
			s.setInt(p++, getRunningYear());
			s.setInt(p++, buchcode);
			rs = s.executeQuery();
			HashMap<Integer, Buch> hm = new HashMap<Integer, Buch>();
			if (rs.next()) {
				Buch b = new Buch(rs);
				b.bhbest = rs.getInt("bhbest");
				b.vv = rs.getInt("vv");
				b.sbst = rs.getInt("sbst");
				b.minjahr = rs.getInt("minjahr");
				hm.put(b.code, b);
				getKlassenForBuch(hm);
				getBuchMarken(hm);
				c.commit();
				return b;
			} else
				throw new SQLException("buch not found");

		} finally {
			if (rs != null)
				rs.close();
			c.rollback();
		}
	}

	private void getKlassenForBuch(HashMap<Integer, Buch> hm)
			throws SQLException {
		// ok schuljahr
		String sql2 = "select bk.buch buch, k.*"
				+ " from BuecherKlassen bk, Klassen k"
				+ " where bk.klasse = k.klasse and bk.schuljahr = ? and k.schuljahr = ?"
				+ " order by k.jahr, k.klasse";
		final PreparedStatement ps2 = c.prepareStatement(sql2);
		int p = 1;
		ps2.setInt(p++, getRunningYear());
		ps2.setInt(p++, getRunningYear());
		ResultSet rs2 = ps2.executeQuery();
		while (rs2.next()) {
			int buchCode = rs2.getInt("buch");
			Buch buch = hm.get(buchCode);
			if (buch != null)
				buch.klassen.add(new Klasse(rs2));
		}
		rs2.close();
	}

	private void getBuchMarken(HashMap<Integer, Buch> hm) throws SQLException {
		@Sql
		String sql2 = "select bm.buch, bmn.* from BuchMark bm, BuchMarken  bmn where bm.marke = bmn.id and bm.schuljahr = ? and bmn.schuljahr = ? ";
		PreparedStatement st2 = c.prepareStatement(sql2);
		st2.setInt(1, getRunningYear());
		st2.setInt(2, getRunningYear());
		ResultSet rs2 = st2.executeQuery();
		while (rs2.next()) {
			Buch buch = hm.get(rs2.getInt("buch"));
			BuchBemerkung marke = new BuchBemerkung(rs2);
			if (buch != null)
				buch.bemerkungen.add(marke.id);
		}
		rs2.close();
	}

	public Connection getConnection() throws SQLException {
		// final Properties props = new Properties();
		// props.setProperty("foreign_keys", "1");
		SQLiteConfig conf = new SQLiteConfig();
		// conf.enforceForeignKeys(true);
		final Connection c = DriverManager.getConnection(url,
				conf.toProperties());
		// c.createStatement().execute("pragma foreign_keys=1");
		// int rs = c.createStatement().executeUpdate(
		// " insert into test values ('cosa');");
		// System.err.println("foreign_keys: " + rs);
		c.setAutoCommit(false);
		c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		openConns.put(c, new Exception());
		return c;
	}

	public ArrayList<KasseDetail> getKasse() throws SQLException {
		ArrayList<KasseDetail> l = new ArrayList<KasseDetail>();
		try {
			// ok schuljahr
			String sql = "select Zahlungsdatum as datum, count(rl.Preis)as buecher, sum((1-type*2)*rl.Preis) as summe,"
					+ " ( select count(*)  from Rechnungen re2 where date(re.zahlungsdatum/1000, 'unixepoch', 'localtime')= date(re2.zahlungsdatum/1000, 'unixepoch', 'localtime')) as rechn, sum((1-type)*rl.Preis) as ventas , sum(type*rl.Preis) as devs"
					+ " from Rechnungen re , RechnungLin rl, Buecher bu"
					+ " where re.schuljahr = ? and rl.schuljahr = ? and re.id = rl.id and bu.buchcode = rl.buchcode and bu.schuljahr = ? and Zahlungsdatum is  not null group by date(zahlungsdatum/1000, 'unixepoch', 'localtime')";
			// FIXME: and rl.schuljahr = bu.schuljahr
			System.err.println("sql: " + sql);
			PreparedStatement s = c.prepareStatement(sql);
			int p = 1;
			s.setInt(p++, getRunningYear());
			s.setInt(p++, getRunningYear());
			s.setInt(p++, getRunningYear());
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				final KasseDetail kd = new KasseDetail(rs.getDate("datum"),
						rs.getInt("buecher"), rs.getDouble("summe"),
						rs.getInt("rechn"));
				kd.ventas = rs.getDouble("ventas");
				kd.devs = rs.getDouble("devs");
				l.add(kd);
			}
			rs.close();
			s.close();
		} finally {
			c.rollback();
		}

		return l;
	}

	public HashSet<String> getKlassenVonBuch(int code) throws SQLException {
		// ok schuljahr
		String sql = "select Klasse" + " from BuecherKlassen"
				+ " where Schuljahr= ?  and Buch = ?";
		HashSet<String> l = new HashSet<String>();
		PreparedStatement s = c.prepareStatement(sql);
		s.setInt(1, getRunningYear());
		s.setInt(2, code);
		ResultSet rs = s.executeQuery();
		while (rs.next())
			l.add(rs.getString("klasse"));
		rs.close();
		return l;
	}

	public ArrayList<Language> getLanguages() throws SQLException {
		c.rollback();
		ArrayList<Language> l = new ArrayList<Language>();
		// ok schuljahr
		String sql = "select * from sprachen where schuljahr = ? ";
		PreparedStatement s = null;

		try {
			s = c.prepareStatement(sql);
			s.setInt(1, getRunningYear());
			ResultSet rs = s.executeQuery();
			while (rs.next())
				l.add(new Language(rs));
			rs.close();
			s.close();
			return l;
		} finally {
			c.rollback();
		}

	}

	public String getMarcajeText() throws SQLException {
		c.rollback();
		ResultSet rs = null;
		try {
			// ok schuljahr
			PreparedStatement ps = c
					.prepareStatement("select data from formtext where schuljahr = ? and elem = ? ");
			int p = 1;
			ps.setInt(p++, getRunningYear());
			ps.setString(p++, "MARCAJE");
			rs = ps.executeQuery();
			if (!rs.next())
				// throw new SQLException("0 elements!");
				return getMarcajeText2();
			else
				return rs.getString("data");
		} finally {
			if (rs != null)
				rs.close();
			c.rollback();
		}

	}

	public String getMarcajeText2() {
		return "Bitte kreuzen Sie die gewünschten Bücher in der ersten Spalte an|Por favor, marque con una X en la primera columna los libros que desee encargar\n"
				+ "Rückgabetermin|Fecha límite de entrega|25.6.2009\n"
				+ "Die Preise dienen zur Orientierung|Los precios son orientativos\n";
	}

	public int getRunningYear() {
		if (runningYear == 0)
			System.err.println("WARNING: NULL schuljahr");
		return runningYear;
	}

	public String getRunningYearName() {
		return getYearName(getRunningYear());
	}

	public static String getYearName(int y) {
		return y + "/" + new DecimalFormat("00").format((y + 1) % 100);
	}

	public ArrayList<Schueler> getSchueler() throws SQLException {
		ArrayList<Schueler> l = new ArrayList<Schueler>();
		@Sql
		String sql = "select * from Schueler where SchulJahr =? ";
		try {
			PreparedStatement s = c.prepareStatement(sql);
			s.setInt(1, getRunningYear());
			ResultSet rs = s.executeQuery();
			while (rs.next())
				l.add(new Schueler(rs));
			rs.close();
		} finally {
			c.rollback();
		}
		return l;
	}

	public Schueler getSchueler(int sch) throws SQLException {
		// @Sql
		// String sql =
		// "select SchuelerNum, Vorname,  Name, Klasse, Email, bemerkDeu, bemerkSpa from Schueler where SchulJahr =? and schuelernum = ?  ";
		@Sql
		String sql = "select * from Schueler where SchulJahr =? and schuelernum = ?  ";

		ResultSet rs = null;
		try {
			PreparedStatement s = c.prepareStatement(sql);
			int p = 1;
			s.setInt(p++, getRunningYear());
			s.setInt(p++, sch);
			rs = s.executeQuery();
			if (rs.next())
				return new Schueler(rs);
			else
				throw new SQLException("no existe ese usuario");

		} finally {
			if (rs != null)
				rs.close();
			c.rollback();
		}

	}

	public ArrayList<Schueler> getSchueler(Klasse klasse) throws SQLException {
		ArrayList<Schueler> l = new ArrayList<Schueler>();
		// ok schuljahr
		String sql = "select * "
				+ " from Schueler where SchulJahr =? and klasse = ?"
				+ " order by name, vorname";
		// Connection c = getConnection();
		try {
			PreparedStatement s = c.prepareStatement(sql);
			s.setInt(1, getRunningYear());
			s.setString(2, klasse.name);
			ResultSet rs = s.executeQuery();
			while (rs.next())
				l.add(new Schueler(rs));
			rs.close();
		} finally {
			c.rollback();
		}
		return l;
	}

	// public ArrayList<Map.Entry<VerlagNationalitat, > getBestellungen() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	public ArrayList<SchuelerBestellung> getSchuelerBestellungenInKlasse(
			String klasseName, BestellungState state, String orderBy)
			throws SQLException {
		ArrayList<SchuelerBestellung> l = new ArrayList<SchuelerBestellung>();
		try {
			String cond2 = "";
			if (state != null)
				switch (state) {
				case BESTELLTE:
					cond2 = " and finished";
					break;
				case NICHT_BESTELLTE:
					cond2 = " and ( not finished or finished is null) ";
					break;
				}
			String cond1 = "";
			if (klasseName != null)
				cond1 = " and Klasse = ? ";
			// ok schuljahr
			@Sql
			String sql = "select email, SchuelerNum, Vorname,  Name, Klasse, finished, nItems, summe, bemerkDeu, bemerkSpa from"
					+ " Schueler left outer join bestellung on schueler.schuelernum = bestellung.schueler  where schueler.schuljahr = ? "
					+ cond1 + cond2;
			if (orderBy != null)
				sql += " order by " + orderBy;
			System.err.println("sql getSchuelerBestellungenInKlasse: " + sql);
			// new Exception().printStackTrace();
			PreparedStatement s = c.prepareStatement(sql);
			int p = 1;
			s.setInt(p++, getRunningYear());
			if (!cond1.isEmpty())
				s.setString(p++, klasseName);
			ResultSet rs = s.executeQuery();
			while (rs.next())
				l.add(new SchuelerBestellung(new Schueler(rs), rs
						.getBoolean("finished"), rs.getDouble("summe"), rs
						.getInt("nItems")));
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Ex: " + e);
			throw new SQLException(e);
		} finally {
			c.rollback();
		}
		return l;
	}

	public SchulerIncRequest getSchuelerInc(String klasse, String orderBy)
			throws SQLException {
		if (orderBy == null)
			orderBy = "name,vorname";
		// ok schuljahr
		//SchuelerNum, Vorname,  Name, Klasse, Email, bemerkDeu, bemerkSpa
		String sql = "select * "
				+ " from Schueler where SchulJahr = ?  "
				+ (klasse != null ? " and klasse = ? " : "")
				+ "  order by "
				+ orderBy;
		Connection c = getConnection();
		PreparedStatement s = c.prepareStatement(sql);
		s.setInt(1, getRunningYear());
		if (klasse != null)
			s.setString(2, klasse);
		return new SchulerIncRequest(c, s.executeQuery());
	}

	public ArrayList<SchuelerBalance> getSchuelerBalanceInKlasse(
			final String klasse, final String orderBy) throws SQLException {
		ArrayList<SchuelerBalance> ret = new ArrayList<SchuelerBalance>();
		c.rollback();
		@Sql
		String sql = "select *, (select  sum(preis  ) from rechnunglin rl "
				+ " inner join  rechnungen r on rl.id = r.id where rl.schuljahr=s.schuljahr and r.schueler = s.schuelernum  and type = 0) as pagado, "
				+ "(select sum( preis  ) from rechnunglin rl inner join  rechnungen r on rl.id = r.id where rl.schuljahr=s.schuljahr and r.schueler = s.schuelernum   and type = 1) as devuelto from schueler s"
				+ " where schuljahr = ? and klasse = ? order by ?";
		System.err.println(sql);
		try {
			PreparedStatement ps = c.prepareStatement(sql);
			int p = 1;
			ps.setInt(p++, getRunningYear());
			ps.setString(p++, klasse);
			ps.setString(p++, orderBy);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				ret.add(new SchuelerBalance(rs));
			rs.close();
			ps.close();
			return ret;
		} finally {
			c.rollback();
		}
	}

	public ArrayList<SchuelerBestellung> getSchuelerOhneRechnungenInKlasse(
			final String klasse, final String orderBy) throws SQLException {
		c.rollback();
		@Sql
		String sql = "select * from schueler s"
				+ " where schuljahr = ? and klasse = ? and not exists (select * from rechnungen re  where re.schueler = s.schuelernum and re.schuljahr = s.schuljahr) and exists (select * from bestellungen be  where be.schuljahr = s.schuljahr and be.schueler = s.schuelernum) order by ?";
		try {
			ArrayList<SchuelerBestellung> l = new ArrayList<SchuelerBestellung>();
			PreparedStatement ps = c.prepareStatement(sql);
			int p = 1;
			ps.setInt(p++, getRunningYear());
			ps.setString(p++, klasse);
			ps.setString(p++, orderBy);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				l.add(new SchuelerBestellung(new Schueler(rs), false, 0, 0));
			rs.close();
			ps.close();
			return l;
		} finally {
			c.rollback();
		}
	}

	public ArrayList<Integer> getSchuljahre() throws SQLException {
		ArrayList<Integer> l = new ArrayList<Integer>();
		// esta tabla es para todos los schuljahre (de hecho contiene los
		// schuljahre!)
		String sql = "select * from Schuljahre order by jahr desc ";
		PreparedStatement ps = c.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
			l.add(rs.getInt(1));
		c.commit();
		return l;
	}

	public void removeVerlag(Verlag data) throws SQLException {
		try {
			c.rollback();
			// ok schuljahr
			PreparedStatement ps1 = c
					.prepareStatement("select count(*) from Buecher where VerlagCode = ? and schuljahr = ?");
			ps1.setInt(1, data.code);
			ps1.setInt(2, getRunningYear());
			ResultSet rs1 = ps1.executeQuery();
			if (!rs1.next())
				throw new SQLException("error reading count");
			int n1 = rs1.getInt(1);
			rs1.close();
			if (n1 != 0)
				throw new SQLException("No se puede borrar la editorial "
						+ data.name + " porque " + n1
						+ " libros la tienen asignada");

			// ok schuljahr
			PreparedStatement ps = c
					.prepareStatement("delete from verlage where verlagcode = ? and schuljahr = ? ");
			ps.setInt(1, data.code);
			ps.setInt(2, getRunningYear());
			int n = ps.executeUpdate();
			if (n != 1)
				throw new SQLException("error, didn't delete 1");
			c.commit();
		} catch (SQLException e) {
			c.rollback();
			throw e;
		}
	}

	public void saveBemerkung(BuchBemerkung bemerkung) throws SQLException {
		// ok schuljahr
		@Sql
		String iSql = "insert into BuchMarken (shortname, dsc, id, schuljahr) values (?, ?, ?, ?)";
		// ok schuljahr
		@Sql
		String uSql = "update BuchMarken  set  shortname = ? , dsc = ?  where id = ? and schuljahr = ?";
		PreparedStatement ps = c.prepareStatement(bemerkung.isNew() ? iSql
				: uSql);
		int p = 1;
		ps.setString(p++, bemerkung.shortname);
		ps.setString(p++, bemerkung.dsc);
		if (bemerkung.isNew())
			ps.setNull(p++, Types.INTEGER);
		else
			ps.setInt(p++, bemerkung.id);
		ps.setInt(p++, getRunningYear());
		int n = ps.executeUpdate();

		if (n != 1)
			throw new SQLException("update didn't update 1 row: " + n);
		c.commit();
	}

	public boolean schuelerHasBill(int num) throws SQLException {
		c.rollback();
		ResultSet rs = null;
		@Sql
		String sql = "select count(*) from rechnungen where schuljahr=? and schueler = ? ";
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			ps.setInt(1, getRunningYear());
			ps.setInt(2, num);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1) != 0;
			else
				throw new SQLException("didn't return 1 row");

		} finally {
			if (rs != null)
				rs.close();
			ps.close();
			c.rollback();
		}
	}

	public boolean schuelerHasRequests(Schueler sch) throws SQLException {
		ArrayList<ChoosableBuch> booksForUser = getBooksForUser(sch);
		for (ChoosableBuch i : booksForUser)
			if (i.bestellt)
				return true;
		return false;
	}

	public void setMarcajeText(String s) throws SQLException {
		c.rollback();
		// ok schuljahr
		PreparedStatement ps = c
				.prepareStatement("update formtext set data=? where schuljahr = ? and elem = ? ");
		int p = 1;
		ps.setString(p++, s);
		ps.setInt(p++, getRunningYear());
		ps.setString(p++, "MARCAJE");
		if (ps.executeUpdate() != 1)
			throw new SQLException("didn't update 1");
		ps.close();
		c.commit();

	}

	public void setRunningYear(int newYear) throws SQLException {
		runningYear = newYear;
		for (Klasse i : klassenVw.getKlassen("klasse"))
			klassen.put(i.name, i);

		loadLanguages();

		for (BuchBemerkung i : getBuchMarken())
			buecherBemerkungen.put(i.id, i);
		c.commit();
	}

	public void loadLanguages() throws SQLException {
		sprachVw.langById.clear();
		sprachVw.langByName.clear();
		for (Language lang : getLanguages()) {
			sprachVw.langById.put(lang.id, lang);
			sprachVw.langByName.put(lang.name, lang);
		}
	}

	@SuppressWarnings("unused")
	private void normalizeAllSchueler() throws SQLException {
		c.rollback();
		ResultSet rs = c.createStatement().executeQuery(
				"select * from schueler");
		PreparedStatement ps = c
				.prepareStatement("update schueler set name_norm = ? where schuelernum = ? and name_norm not like ? ");
		int updated = 0;
		while (rs.next()) {
			String from = rs.getString("name");
			String to = normalizeName(from);
			ps.setString(1, to);
			ps.setInt(2, rs.getInt("schuelernum"));
			ps.setString(3, to);
			int partial = ps.executeUpdate();
			if (partial != 0)
				System.err.println(from + " -> " + to);
			updated += partial;

		}
		System.err.println("total normalized : " + updated);
		if (updated > 0)
			c.commit();
		else
			c.rollback();
	}

	private static String normalizeName(String n) {
		n = n.replaceAll("Á", "A");
		n = n.replaceAll("Í", "I");
		n = n.replaceAll("É", "E");
		n = n.replaceAll("Ó", "O");
		n = n.replaceAll("Ú", "U");
		n = n.replaceAll("Ñ", "N");

		n = n.replaceAll("Ä", "A");
		n = n.replaceAll("Ö", "O");
		n = n.replaceAll("Ü", "U");
		return n;
	}

	public String getAvisoProfeText() throws SQLException {
		c.rollback();
		ResultSet rs = null;
		try {
			// ok schuljahr
			PreparedStatement ps = c
					.prepareStatement("select data from formtext where schuljahr = ? and elem = ? ");
			int p = 1;
			ps.setInt(p++, getRunningYear());
			ps.setString(p++, "AVISOPROFE");
			rs = ps.executeQuery();
			if (!rs.next())
				// throw new SQLException("0 elements!");
				return getMarcajeText2();
			else
				return rs.getString("data");
		} finally {
			if (rs != null)
				rs.close();
			c.rollback();
		}

	}

	public void setAvisoProfeText(String s) throws SQLException {
		c.rollback();
		// ok schuljahr
		PreparedStatement ps = c
				.prepareStatement("update formtext set data=? where schuljahr = ? and elem = ? ");
		int p = 1;
		ps.setString(p++, s);
		ps.setInt(p++, getRunningYear());
		ps.setString(p++, "AVISOPROFE");
		if (ps.executeUpdate() != 1)
			throw new SQLException("didn't update 1");
		ps.close();
		c.commit();

	}

	public void jump(int from, int to) throws SQLException {

		String[] tables = { "faecher", "abteilungen", "sprachen", "buecher",
				"buecherklassen", "verlage", "klassen", "bestellungen",
				"rechnungen", "rechnunglin", "buchmark", "schueler",
				"buchmarken", "bestellung", "formtext" };
		c.rollback();
		for (String t : tables) {
			int res = c.createStatement().executeUpdate(
					"delete from " + t + " where schuljahr = " + to);
			System.err.println("Hemos borrado " + res + " filas de " + t);
		}
		c.createStatement().executeUpdate(
				"delete from schuljahre where jahr = " + to);
		/* dsvusers - do nothing */
		runQuery("insert into faecher(name, schuljahr) select name," + to
				+ " from faecher where schuljahr=" + from);

		runQuery(" insert into abteilungen (name, ordindex, schuljahr) select name, ordindex, "
				+ to + " from abteilungen where schuljahr=" + from);
		runQuery(MessageFormat
				.format(" insert into sprachen (id, name, schuljahr) select id, name, {0} from sprachen where schuljahr={1}",
						"" + to, "" + from));

		// BUCHER

		//
		runQuery("	insert into verlage (verlagcode, name, land, schuljahr ) select null,name, land, "
				+ to + "  from verlage where schuljahr=" + from);
		final String stockSpec;
		if (keepStock)
			stockSpec = "stock";
		else
			stockSpec = "0";
		runQuery(MessageFormat
				.format("insert into buecher (buchcode, titel, verlagcode, sprache, preis, fach, isbn, bemerkung, schuljahr, stock, rsv) select null, titel, (select v2.verlagcode from verlage v2 where v2.name = v.name and v2.schuljahr={1}), sprache, preis, fach, isbn, bemerkung, {1}, "
						+ stockSpec
						+ ", rsv  from buecher b inner join verlage v on b.verlagcode = v.verlagcode where b.schuljahr= {0}",
						"" + from, "" + to));
		runQuery(MessageFormat
				.format("insert into buecherklassen ( buch, klasse, schuljahr, vomvorjahr)"
						+ " select (select buchcode from buecher b"
						+ " where b.titel = (select titel from buecher b2 where b2.buchcode = bk.buch)"
						+ " and b.isbn = (select isbn from buecher b2 where b2.buchcode = bk.buch)"
						+ "  and schuljahr = {1}), klasse, {1}, vomvorjahr from buecherklassen bk where bk.schuljahr = {0}",
						"" + from, "" + to));
		runQuery("	insert into buchmarken(id, shortname, dsc, schuljahr) select null, shortname, dsc, "
				+ to + "  from buchmarken where schuljahr=" + from);
		@Sql
		String sqlBuchMark = "insert into buchmark ( buch, marke, schuljahr) select "
				+ "(select buchcode from buecher b "
				+ "	where schuljahr = {1} and b.titel = (select titel from buecher where buchcode = sbm.buch and schuljahr={0}) and b.isbn = (select isbn  from buecher where buchcode = sbm.buch and schuljahr={0}) ), "
				+ "(select id from buchmarken bm where schuljahr = {1} and  bm.dsc  = (select dsc from buchmarken where id = sbm.marke and schuljahr = {0} ) ), "
				+ " {1} from buchmark sbm where sbm.schuljahr = {0}";
		runQuery(MessageFormat.format(sqlBuchMark, "" + from, "" + to));

		// klassen

		//
		runQuery("	insert into klassen (klasse, abteilung, jahr, schuljahr) select klasse, abteilung, jahr,"
				+ to + "  from klassen where schuljahr=" + from);
		runQuery("insert into schuljahre values (" + to + ")");

		/* schueler - do nothing */

		runQuery("	insert into formtext(elem, data, schuljahr) select elem, data, "
				+ to + "  from formtext where schuljahr=" + from);

		c.commit();
		System.err.println("ok");

	}

	private void runQuery(String format) throws SQLException {
		System.err.print("q: " + format);
		int rows = c.createStatement().executeUpdate(format);
		System.err.println(": " + rows);
		// c.commit();
	}

	public int stockNull() throws SQLException {
		c.rollback();
		@Sql
		String sql = "update buecher set stock = 0 where schuljahr = ? and stock <> 0";
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			ps.setInt(1, getRunningYear());
			int n = ps.executeUpdate();
			c.commit();
			return n;
		} finally {
			ps.close();
			c.rollback();
		}
	}

	public int deshacerPedido() throws SQLException {
		@Sql
		String sql = "update buecher  set stock = (select vv from bhbuchbest bhbb where bhbb.buch = buecher.buchcode) where exists ( select * from bhbuchbest bhbb where bhbb.buch = buecher.buchcode )  and schuljahr=?";
		c.rollback();
		try {
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setInt(1, getRunningYear());
			int n = ps.executeUpdate();
			borrarLineasPedido();
			c.commit();
			return n;
		} catch (SQLException e) {
			c.rollback();
			throw e;
		}
	}

	public int hacerPedidoEditoriales() throws SQLException {
		c.rollback();
		try {
			System.err.println("deleted bhbuchbest: " + borrarLineasPedido());

			@Sql
			String sqlInsert = "insert into bhbuchbest(buch, n, schuljahr, vv, sbst) select buchcode, rsv-stock, schuljahr, stock, rsv from buecher where schuljahr=? and rsv <>0 ";
			PreparedStatement psCopy = c.prepareStatement(sqlInsert);

			psCopy.setInt(1, getRunningYear());

			int n = psCopy.executeUpdate();

			@Sql
			String setStockToRsvSql = "update buecher set stock = rsv where schuljahr = ?";
			PreparedStatement ps = c.prepareStatement(setStockToRsvSql);
			ps.setInt(1, getRunningYear());
			/* int n2 = */ps.executeUpdate();
			ps.close();
			c.commit();
			return n;
		} catch (SQLException e) {
			c.rollback();
			throw e;
		}
	}

	private int borrarLineasPedido() throws SQLException {
		PreparedStatement psDelete = null;
		try {
			@Sql
			String sqlDelete = "delete from bhbuchbest where schuljahr = ?";
			psDelete = c.prepareStatement(sqlDelete);
			psDelete.setInt(1, getRunningYear());
			return psDelete.executeUpdate();
		} finally {
			if (psDelete != null)
				psDelete.close();
		}
	}

	public int nullPrices() throws SQLException {
		c.rollback();
		@Sql
		String sql = "update buecher set preis = 0 where schuljahr = ? and preis <> 0";
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			ps.setInt(1, getRunningYear());
			int n = ps.executeUpdate();
			c.commit();
			return n;
		} finally {
			ps.close();
			c.rollback();
		}
	}

	public int getBuchBestellungen() throws SQLException {
		c.rollback();
		ResultSet rs = null;
		@Sql
		String sql = "select count(*) from bhbuchbest where schuljahr=?";
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			ps.setInt(1, getRunningYear());
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			else
				throw new SQLException("didn't return 1 row");

		} finally {
			if (rs != null)
				rs.close();
			ps.close();
			c.rollback();
		}
	}

	public int getBuchBestellungen(Schueler s) throws SQLException {
		c.rollback();
		ResultSet rs = null;
		@Sql
		String sql = "select count(*) from bestellungen where schuljahr=? and schueler = ? ";
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			int p = 1;
			ps.setInt(p++, getRunningYear());
			ps.setInt(p++, s.num);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			else
				throw new SQLException("didn't return 1 row");

		} finally {
			if (rs != null)
				rs.close();
			ps.close();
			c.rollback();
		}
	}

	public int getBuchRechnungen() throws SQLException {
		c.rollback();
		ResultSet rs = null;
		@Sql
		String sql = "select count(*) from rechnungen where schuljahr=?";
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			ps.setInt(1, getRunningYear());
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			else
				throw new SQLException("didn't return 1 row");

		} finally {
			if (rs != null)
				rs.close();
			ps.close();
			c.rollback();
		}
	}

	public List<Schueler> getBestellerOhneRechungForBook(int book)
			throws SQLException {
		ArrayList<Schueler> l = new ArrayList<Schueler>();
		@Sql
		String sql = "select * from bestellungen  bn inner join schueler on schueler.schuelernum = bn. schueler where bn . schuljahr= ? and buch = ?  and not exists (select * from rechnunglin rl inner join rechnungen on rechnungen.id = rl.id  where rl.buchcode=bn.buch and rechnungen.schueler = bn.schueler and not returned)";

		System.err.println(sql);
		System.err.println(book);
		c.rollback();
		ResultSet rs = null;
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			int p = 1;
			ps.setInt(p++, getRunningYear());
			ps.setInt(p++, book);
			rs = ps.executeQuery();
			while (rs.next())
				l.add(new Schueler(rs));

			return l;
		} finally {
			if (rs != null)
				rs.close();
			ps.close();
			c.rollback();
		}

	}

	public List<Schueler> getBuyersForBook(int book) throws SQLException {
		ArrayList<Schueler> l = new ArrayList<Schueler>();
		@Sql
		String sql = "select schueler.* from schueler, "
				+ "rechnunglin inner join rechnungen on rechnunglin.id = rechnungen.id   "
				+ "where rechnungen.schueler = schueler.schuelernum  and not returned and buchcode=? and schueler.schuljahr = ? and rechnungen.schuljahr=?";
		System.err.println(sql);
		System.err.println(book);
		c.rollback();
		ResultSet rs = null;
		PreparedStatement ps = c.prepareStatement(sql);
		try {
			int p = 1;
			ps.setInt(p++, book);
			ps.setInt(p++, getRunningYear());
			ps.setInt(p++, getRunningYear());
			rs = ps.executeQuery();
			while (rs.next())
				l.add(new Schueler(rs));

			return l;
		} finally {
			if (rs != null)
				rs.close();
			ps.close();
			c.rollback();
		}
	}
}
