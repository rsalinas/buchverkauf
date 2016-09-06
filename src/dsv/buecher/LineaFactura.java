package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LineaFactura {

	public LineaFactura(ResultSet rs) throws SQLException {
		this.titel = rs.getString("titel");
		this.price = rs.getDouble("preis");
		this.verlag = rs.getString("verlag");
		this.isbn = rs.getString("isbn");
		this.buchcode = rs.getInt("buchcode");
		this.buchungsTyp = tryGetFrom(rs);
		switch (rs.getInt("type")) {
		case 0:
			zustand = XKaufZustand.VERKAUFT;
			break;
		case 1:
			zustand = XKaufZustand.DEVUELTO;
			break;
		default:
			zustand = XKaufZustand.NIX;

		}

		// if (price == 0)
		// throw new SQLException("null price!");
	}

	private int tryGetFrom(ResultSet rs) {
		try {
			return rs.getInt("buchungsTyp");
		} catch (Exception e) {
			System.err.println("ignoring missing buchungsTyp");
			return 0;
		}
	}

	public String titel, isbn, verlag;
	public double price;
	public final XKaufZustand zustand;
	public final int buchcode;
	public final int buchungsTyp;

	public String[] toStringArray() {
		return new String[] { titel, "" + price };
	}
}
