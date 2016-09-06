package dsv.buecher;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;

public class Rechnung {
	final static DateFormat dtf = DateFormat.getDateTimeInstance();
	public int id;
	public Date datum;
	public Schueler schueler;
	public ArrayList<LineaFactura> rechnungsZeilen;

	public Rechnung(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.datum = null;
	}

	public double amount() {
		return totalVentas()-totalDev();
	}

	public double totalVentas() {
		double total = 0;
		for (LineaFactura lf : rechnungsZeilen) {
			if (lf.zustand == XKaufZustand.VERKAUFT)
				total += lf.price;
		}
		return total;
	}

	public double totalDev() {
		double total = 0;
		for (LineaFactura lf : rechnungsZeilen) {
			if (lf.zustand == XKaufZustand.DEVUELTO)
				total += lf.price;
		}
		return total;
	}

	public void setSchueler(Schueler schueler2) {
		this.schueler = schueler2;
	}

	public void setRechnungZeilen(Collection<LineaFactura> z) {
		rechnungsZeilen.addAll(z);
	}

}
