package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class User implements ToArrayPassable {
	String login, name;
	final HashSet<UserPerm> permissions = new HashSet<UserPerm>();

	public User(ResultSet rs) throws SQLException {
		login = rs.getString("login");
		name = rs.getString("name");
		for (String s : rs.getString("perms").split(","))
			permissions.add(UserPerm.valueOf(s));
	}

	@Override
	public String[] toStringArray() {
		return new String[] { login, name, permissions.toString() };
	}
}
