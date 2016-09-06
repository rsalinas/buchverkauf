package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KarinItem {
	// s.Klasse, v.Name as Verlag, Titel, count(*) as n
	String klasse, verlag, titel;
	int n;
	public String isbn;

//	public KarinItem(String klasse, String verlag, String titel, int n) {
//		super();
//		this.klasse = klasse;
//		this.verlag = verlag;
//		this.titel = titel;
//		this.n = n;
//		this.isbn=isbn;
//	}

	public KarinItem(ResultSet rs) throws SQLException {
		klasse = rs.getString("klasse");
		verlag = rs.getString("verlag");
		titel = rs.getString("titel");
		n = rs.getInt("n");
		isbn = rs.getString("isbn");
	}

}
