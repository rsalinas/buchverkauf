/**
 * 
 */
package dsv.buecher;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author rsalinas
 * 
 */
public class SchuelerBalance extends Schueler {
	public double pagado, devuelto;

	/**
	 * 
	 */

	/**
	 * @param code
	 * @param vorname
	 * @param name
	 * @param klasse
	 */
	public SchuelerBalance(int code, String vorname, String name,
			String klasse, float pagado, float devuelto) {
		super(code, vorname, name, klasse);
		this.pagado = pagado;
		this.devuelto = devuelto;
	}

	/**
	 * @param rs
	 * @throws SQLException
	 */
	public SchuelerBalance(ResultSet rs) throws SQLException {
		super(rs);
		this.pagado = rs.getFloat("pagado");
		this.devuelto = rs.getFloat("devuelto");

	}

}
