package dsv.buecher;

public class SchuelerBestellung {
	public SchuelerBestellung(Schueler schueler, boolean finished,
			double summe, int examplaere) {
		this.s = schueler;
		this.state = finished ? BestellungState.BESTELLTE
				: BestellungState.NICHT_BESTELLTE;
		this.summe = summe;
		this.exemplaere = examplaere;
	}

	public final Schueler s;
	public final BestellungState state;
	public final double summe;
	public int exemplaere;
}
