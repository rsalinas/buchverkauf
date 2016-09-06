package dsv.buecher;

import java.sql.Date;
import java.util.ArrayList;

public class DatosFactura implements Cloneable {
	public int id;
	public final ArrayList<ChoosableBuch> comprar;// = new
	public final ArrayList<ChoosableBuch> devolver; // = new
	public final ArrayList<ChoosableBuch> rsvCanc; // = new
	public final Schueler schueler;
	public final Date datum;
	public Double summe;

	public DatosFactura(Schueler schu, ArrayList<ChoosableBuch> comprar,
			ArrayList<ChoosableBuch> devolver,
			ArrayList<ChoosableBuch> rsvCanc, Date date) {
		super();
		this.schueler = schu;
		this.comprar = comprar;
		this.devolver = devolver;
		this.datum = date;
		this.rsvCanc = rsvCanc;
	}

	public DatosFactura(int id, Schueler schueler, Date date) {
		this.id = id;
		comprar = new ArrayList<ChoosableBuch>();
		devolver = new ArrayList<ChoosableBuch>();
		rsvCanc = new ArrayList<ChoosableBuch>();
		this.schueler = schueler;
		this.datum = date;
	}
}
