package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

// "create table Verlage(VerlagCode, Name, Land)
public class Verlag {
	public Verlag(ResultSet rs) throws SQLException {
		code = rs.getInt("VerlagCode");
		name = rs.getString("Name");
		land = rs.getString("Land");
	}

	public Verlag(int code, String name, String land) {
		this.code = code;
		this.name = name;
		this.land = land;
	}

	public Verlag() {
		code = -1;
		name = "";
		land = "";
	}

	int code;
	public String name, land;

	public String[] toStringArray() {
		return new String[] { "" + code, name, hm.get(land) };
	}

	@Override
	public String toString() {
		return name;
	}

	public static final HashMap<String, String> hm = new HashMap<String, String>();
	public static final HashMap<String, String> hm2 = new HashMap<String, String>();
	static {
		hm.put("d", "Deutschland");
		hm.put("e", "Spanien");
		hm.put("l", "Lokal");
		for (Entry<String, String> i : hm.entrySet())
			hm2.put(i.getValue(), i.getKey());
	}

}
