package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class Buch implements Comparable<Buch> {
	public String bemerkung;
	public int code;
	public String fach, isbn;
	public double preis;
	public String sprache;
	public String titel;
	public Verlag verlag;
	public final ArrayList<Klasse> klassen = new ArrayList<Klasse>();
	public ArrayList<Integer> bemerkungen = new ArrayList<Integer>();
	public int stock = 0;
	public int reservations = 0;
	public int bhbest;
	public int vv;
	public int sbst;
	public int minjahr = 0;

	public String bemerkSpa, bemerkDeu;

	// public final List<Klasse> klassen2 = new AbstractList<Klasse>() {
	//
	// @Override
	// public Klasse get(int index) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public int size() {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	//
	// };

	public Buch(ResultSet rs) throws SQLException {
		fach = rs.getString("fach");
		if (fach == null) {
			System.err.println("fach was null!");
			fach = "";
		}
		isbn = rs.getString("isbn");
		if (isbn == null) {
			System.err.println("ISBN was null!");
			isbn = "";
		}
		sprache = rs.getString("sprache");
		if (sprache.equals("esp"))
			sprache = "es";
		else if (sprache.equals("deu"))
			sprache = "de";
		else if (sprache.equals("eng"))
			sprache = "en";
		else if (sprache.equals("fra"))
			sprache = "fr";
		else if (sprache.equals("val"))
			sprache = "val";
		titel = rs.getString("titel");
		code = rs.getInt("buchcode");
		preis = rs.getDouble("preis");
		verlag = new Verlag(rs.getInt("Verlag_Code"), rs
				.getString("verlagname"), rs.getString("verlagland"));
		stock = rs.getInt("stock");
		reservations = rs.getInt("rsv");
		bemerkSpa = rs.getString("bemerkSpa");
		bemerkDeu = rs.getString("bemerkDeu");

	}

	public Buch() {
		code = -1;
		bemerkung = "";
		fach = "";
		isbn = "";
		preis = 0;
		sprache = "";
		titel = "";
		verlag = new Verlag();
		bhbest = 0;
		bemerkDeu = "";
		bemerkSpa = "";

	}

	public String[] toStringArray(DsvBuecherDatasource ds) {
		try {
			return new String[] { "" + code, titel, verlag.name, isbn, fach,
					DsvBuecher.currency(preis), sprache,
					getCommaSeparatedKlassen(), /* "" + minjahr, */
					getBemerkungen(ds) };
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new String[] { "" + code, "Exception" };
		}
	}

	private String getCommaSeparatedKlassen() {
		if (klassen.isEmpty())
			return "";
		assert !klassen.isEmpty();

		String[] a = new String[klassen.size()];

		for (int i = 0; i < klassen.size(); i++)
			a[i] = klassen.get(i).name;
		return RauUtil.separate(a, ", ");
	}

	@Override
	public String toString() {
		return code + " " + titel + " " + verlag;
	}

	public void setSprache(Language language) {
		System.err.println("setSprache - " + language);
		if (language != null)
			sprache = language.id;
		else
			sprache = null;

	}

	public String getSpracheId() {
		return sprache;
	}

	public String getBemerkungen(DsvBuecherDatasource ds) {
		ArrayList<String> l = new ArrayList<String>();
		System.err.println("bemerkungen: " + bemerkungen);
		for (Integer i : bemerkungen) {

			System.err.println("buscando " + i + "..." + " en "
					+ ds.buecherBemerkungen.size());
			assert ds.buecherBemerkungen != null;
			assert ds.buecherBemerkungen.get(i) != null;
			assert ds.buecherBemerkungen.get(i).shortname != null;
			l.add(ds.buecherBemerkungen.get(i).shortname);
		}
		return l.isEmpty() ? "---" : RauUtil.separate(l.toArray(new String[l
				.size()]), ", ");
	}

	public boolean isNew() {
		return code == -1;
	}

	@Override
	public int compareTo(Buch o) {
		return titel.compareTo(o.titel);
	}

	public static Buch[] sort(ArrayList<Buch> arrayList) {
		Buch[] a = arrayList.toArray(new Buch[arrayList.size()]);
		Arrays.sort(a);
		return a;
	}
}
