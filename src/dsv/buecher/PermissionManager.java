package dsv.buecher;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;

public class PermissionManager {
	String badAuthPair = "Benutzer und/oder Passwort falsch.";

	public PermissionManager(String login, String password) throws Exception {
		if (login.equalsIgnoreCase("adm")) {
			if (!password.equals("roig"))
				throw new Exception(badAuthPair);

			try {
				Properties props = new Properties();
				props.load(new FileInputStream("printer.cfg"));
				String allowAdm = props.getProperty("allow_adm");
				if (!"true".equals(allowAdm))
					throw new Exception(
							"Zugang für «adm» im Moment nicht erlaubt.");
			} catch (Exception e) {
				throw new Exception(
						"Zugang für «adm» im Moment nicht erlaubt. Entsprechende Datei fehlt.");
			}
			perms.add(DsvbPermission.MENU);
			perms.add(DsvbPermission.ADVANCED);
			perms.add(DsvbPermission.RECHNUNGTAB);
			perms.add(DsvbPermission.KASSETAB);
			perms.add(DsvbPermission.VIEW_OLD_YEARS);

		} else if (login.equalsIgnoreCase("ventas")) {
			perms.add(DsvbPermission.RECHNUNGTAB);
			perms.add(DsvbPermission.KASSETAB);
		} else if (login.equalsIgnoreCase("super")) {
			if (!password.equals("man"))
				throw new Exception(badAuthPair);
			perms.add(DsvbPermission.MENU);
			perms.add(DsvbPermission.ADVANCED);
			perms.add(DsvbPermission.RECHNUNGTAB);
			perms.add(DsvbPermission.KASSETAB);

			perms.add(DsvbPermission.NEWYEARCHANGE);
			perms.add(DsvbPermission.VIEWID);
			perms.add(DsvbPermission.VIEW_OLD_YEARS);
			perms.add(DsvbPermission.SUPER);
		} else {
			throw new Exception(badAuthPair);
		}
	}

	private final HashSet<DsvbPermission> perms = new HashSet<DsvbPermission>();

	public boolean can(DsvbPermission perm) {
		return perms.contains(perm);
	}

}
