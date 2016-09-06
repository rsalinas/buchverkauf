package dsv.buecher;

import java.sql.SQLException;

public interface ToArrayPassableReq {

	ToArrayPassable get(int index) throws SQLException;

}
