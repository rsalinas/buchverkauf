package dsv.buecher;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BuchungsCsvMaker {

	public BuchungsCsvMaker(DsvBuecher gui, Remesa remesa) {
		File f = new File("remesa.csv");
		ArrayList<RechnungInfo> l = gui.ds.remesaVw.getRemesaById(remesa.firstRechnung);
		try {
			PrintWriter pw = new PrintWriter(f);
			pw.println("#Klasse,Nachname,Vorname,atlId,CP-konto,Netto,MwSt,PVP");
			for (RechnungInfo r : l) {
				CsvRegister cr = new CsvRegister(";");
				cr.add(r.schueler.klasse);
				cr.add(r.schueler.name);
				cr.add(r.schueler.vorName);
				cr.add(r.schueler.atlId);
				pw.println(r.id);
			}
			pw.close();
			Desktop.getDesktop().open(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
