package dsv.buecher;

public class ChoosableBuch {
	public ChoosableBuch(Buch buch2, boolean b) {
		this.buch = buch2;
		this.bestellt = b;
		this.initiallyBestellt = b;
	}

	public final Buch buch;
	public boolean bestellt;
	public boolean initiallyBestellt;
	public XKaufZustand zustand;
	double precioVenta;

	@Override
	public String toString() {
		return RauUtil.parentize(buch, bestellt, initiallyBestellt, zustand,
				precioVenta);
	}

	public String[] toStringArray(DsvBuecherDatasource ds) {
		return new String[] { buch.titel, buch.verlag.name, buch.fach,
				DsvBuecher.currency(buch.preis), buch.getBemerkungen(ds) };
	}
}
