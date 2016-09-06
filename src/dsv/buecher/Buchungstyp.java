package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

public class Buchungstyp {
	public Buchungstyp(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.description = rs.getString("description");
		this.iva = rs.getDouble("iva");
		this.account = rs.getString("account");
	}

	public Buchungstyp() {
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}: {1} IVA={2} acc={3}", id,
				description, iva, account);
	}

	int id;
	String description="";
	double iva;
	String account="";
	public String[] toStringArray() {
		return new String[] { ""+id, description, ""+iva, account};
	}

}
