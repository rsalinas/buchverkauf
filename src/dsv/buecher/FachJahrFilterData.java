package dsv.buecher;

public class FachJahrFilterData {
	String fach;
	int jahr;

	public FachJahrFilterData(String klasse, int jahr) {
		super();
		this.fach = klasse;
		this.jahr = jahr;
	}

	@Override
	public String toString() {
		return RauUtil.parentize(fach, jahr);
	}
}
