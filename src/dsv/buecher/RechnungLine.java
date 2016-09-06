package dsv.buecher;

public class RechnungLine {
	public RechnungLine(Buch buch2) {
		type = 0;
		buch = buch2;
		preis = buch2.preis;
	}

	public RechnungLine(int type, Buch buch, double preis) {
		super();
		this.type = type;
		this.buch = buch;
		this.preis = preis;
	}

	int type;
	Buch buch;
	double preis;
}
