package dsv.buecher;

import java.sql.Date;

public class KasseDetail {
	Date date;
	int n, n2;
	double amount;
	public double ventas, devs;

	public KasseDetail(Date date, int n, double amount, int n2) {
		super();
		this.date = date;
		this.n = n;
		this.n2 = n2;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return date + " " + n + " " + amount + " " + n2;
	}

	public String[] toStringArray() {
		return new String[] { date.toString(), "" + n, "" + n2,
				DsvBuecher.currency(amount) };
	}
}
