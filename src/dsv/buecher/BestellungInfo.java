package dsv.buecher;

public class BestellungInfo {

	public double summe;
	public  Schueler schueler;

	public BestellungInfo(Schueler schueler, double summe) {
		this.schueler = schueler;
		this.summe = summe;
	}

	public double getAmount() {
	return summe;
	}

}
