package dsv.buecher;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Remesa {
	public int firstRechnung, lastRechnung;
	public int kreationsDatum;

	public Remesa(ResultSet rs) throws SQLException {
		this.firstRechnung = rs.getInt("firstRechnung");
		this.lastRechnung = rs.getInt("lastRechnung");
		this.kreationsDatum = rs.getInt("kreationDatum");
	}

	public String[] toStringArray() {
		return new String[] { "" + firstRechnung, "" + lastRechnung,
				"" + kreationsDatum };
	}
}
