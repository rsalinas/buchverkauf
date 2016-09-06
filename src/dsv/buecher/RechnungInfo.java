package dsv.buecher;

import java.sql.Date;
import java.text.DateFormat;

public class RechnungInfo {
	final static DateFormat dtf = DateFormat.getDateTimeInstance();
	public final Schueler schueler;
	public Date datum;
	public final double amount;
	public int id;
	public double totalVentas;
	public double totalDev;
	
	public RechnungInfo(Schueler schueler2, Date datum, double price) {
		this.schueler = schueler2;
		this.amount = price;
		this.datum = datum;
	}

	public RechnungInfo(int id, Schueler schueler, Date date, double price) {
		this.id = id;
		this.schueler = schueler;
		this.amount = price;
		this.datum = date;
	}

	public double getAmount() {
		return amount;
	}


	public String[] toStringArray() {
		return new String[] { "" + id, schueler.vorName, schueler.name,
				schueler.klasse, dtf.format(datum.getTime()),
				DsvBuecher.currency(amount) };
	}
}
