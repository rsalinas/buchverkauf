package dsv.buecher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dsv.buecher.Schueler.SchulerDatum;

public class StudentImportForm {
	private String filename = "";
	private final Text fnt;

	private ArrayList<String[]> newList;
	final private HashSet<String> namen = new HashSet<String>();

	private final Shell shell;

	Table table;
	private final DsvBuecher gui;
	private final HashSet<String> availableKlasseNamen;
	protected boolean valid = false;
	private final Button ejecutar;

	public StudentImportForm(DsvBuecher parent) throws SQLException {
		this.gui = parent;
		availableKlasseNamen = getAvailableKlasseNamen(gui.ds.klassenVw
				.getKlassen("klasse"));
		shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.CLOSE
				| SWT.APPLICATION_MODAL);
		shell.setText("Schülerimport");
		shell.setLayout(new GridLayout(1, false));
		Button borrarButton = RauUtil.createButton(shell,
				"Aktuelle Schüler löschen", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO);
						mb.setText("Löschen bestätigen");
						mb.setMessage("Möchten Sie wirklich ALLE SCHÜLER im gewählten Schuljahr LÖSCHEN?  (Achtung: Dieser Vorgang kann nicht rückgängig gemacht werden.)");

						if (mb.open() == SWT.YES)
							try {
								gui.ds.schuelerVw.deleteStudents();
							} catch (SQLException e1) {
								gui.showError(e1);
							}
						gui.klassenTab.f5();
					}
				});
		new Label(shell, 0)
				.setText("Datei in CSV-Format (Tab-getrennte Spalten: Name, Vorname, Klasse im nächsten Schuljahr)");

		Button chooseFileButton = new Button(shell, SWT.PUSH);
		fnt = new Text(shell, 0);
		fnt.setText(filename);
		fnt.setEnabled(false);
		fnt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		chooseFileButton.setText("Datei auswählen...");
		chooseFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.NONE);
				filename = fd.open();
				if (filename != null)
					fnt.setText(filename);
				else
					fnt.setText("(sin selección)");
			}
		});
		Button calc = new Button(shell, SWT.PUSH);
		calc.setText("Datei einlesen");
		calc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					valid = process();
					ejecutar.setEnabled(valid);
				} catch (Exception e1) {
					gui.showError(e1);

				}
			}
		});
		new Label(shell, SWT.NONE)
				.setText("Vorsicht: Ungültige Zeilen werden nicht importiert (d.h. nicht getrennte Zeilen durch Tab; nur drei Spalten erlaubt)");
		new Label(shell, SWT.NONE).setText("Gültige Zeilen der CSV-Datei:");
		table = new Table(shell, SWT.BORDER | SWT.MULTI);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		int width = 0;

		String[] headers = { "#", "Name", "Vorname", "Klasse" };
		for (String header : headers) {
			width = 20;
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth((width * 8));
			column.setText(header);
		}

		ejecutar = new Button(shell, SWT.PUSH);
		ejecutar.setEnabled(false);
		ejecutar.setText("Obige neue Schüler-Tabelle importieren");
		ejecutar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (sl.isEmpty()) {
					MessageBox mb = new MessageBox(shell, SWT.ERROR | SWT.OK);
					mb.setText("Error");
					mb.setMessage("Datei ungültig.  Kein Import möglich.");
					mb.open();
					return;
				}
				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO);
				mb.setText("Confirmación importación");
				mb.setMessage(MessageFormat
						.format("Möchten Sie wirklich die {0} neuen Schüler importieren?",
								sl.size()));

				int ans = mb.open();
				if (ans == SWT.YES)
					try {
						if (proceed()) {
							gui.klassenTab.f5();
							shell.close();
						}
					} catch (SQLException e1) {
						gui.showError(e1);
					}
			}
		});
		// shell.pack();
		shell.open();
	}

	private HashSet<String> getAvailableKlasseNamen(Collection<Klasse> klassen) {
		HashSet<String> l = new HashSet<String>();
		for (Klasse i : klassen)
			l.add(i.name);
		return l;
	}

	protected boolean process() throws IOException, SQLException {
		HashSet<String> badKlassen = new HashSet<String>();
		namen.clear();
		if (filename == null || filename.length() == 0) {
			MessageBox mb = new MessageBox(shell, SWT.ERROR);
			mb.setText("Fehler");
			mb.setMessage("Wählen Sie eine gültige Datei aus.");
			mb.open();
			return false;
		}

		newList = new ArrayList<String[]>();
		FileInputStream fis = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String l;
		table.clearAll();
		table.removeAll();

		HashMap<Schueler.SchulerDatum, Integer> hm1 = new HashMap<SchulerDatum, Integer>();
		int pos = 0;
		hm1.put(Schueler.SchulerDatum.NAME, pos++);
		hm1.put(Schueler.SchulerDatum.VORNAME, pos++);
		hm1.put(Schueler.SchulerDatum.KLASSE, pos++);
		int ncode = 1;
		sl.clear();
		while ((l = br.readLine()) != null) {
			String[] ss = l.split("\t");

			if (ss.length == 3) {
				int code;
				if (hm1.containsKey(Schueler.SchulerDatum.NUM))
					code = Integer.parseInt(ss[hm1
							.get(Schueler.SchulerDatum.NUM)]);
				else
					code = ncode++;
				final Schueler schueler = new Schueler(code,
						ss[hm1.get(Schueler.SchulerDatum.VORNAME)],
						ss[hm1.get(Schueler.SchulerDatum.NAME)],
						ss[hm1.get(Schueler.SchulerDatum.KLASSE)]);
				if (!availableKlasseNamen.contains(schueler.klasse))
					badKlassen.add(schueler.klasse);
				String sname = schueler.vorName + ", " + schueler.name;
				if (namen.contains(sname)) {
					throw new SQLException(
							"Mindestens ein Schulerpaar mit gleichen Vor- und Nachnamen: "
									+ sname);
				} else
					namen.add(sname);

				sl.add(schueler);
				newList.add(ss);
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(schueler.toStringArray());

			}
		}
		if (sl.isEmpty()) {
			MessageBox mb = new MessageBox(shell, SWT.ERROR | SWT.OK);
			mb.setText("Error");
			mb.setMessage("Keine gültige Zeile eingelesen.  Kein Import möglich.");
			mb.open();
			return false;
		}
		if (!badKlassen.isEmpty()) {
			MessageBox mb = new MessageBox(shell, SWT.ERROR | SWT.OK);
			mb.setText("Error: ungültige Klassen");
			mb.setMessage("Es sind Schüler mit einer falschen Klasse vorhanden.  Kein Import möglich. "
					+ badKlassen);
			mb.open();
			return false;
		}
		return true;
	}

	/**
	 * @return
	 * @throws SQLException
	 * 
	 */
	private boolean proceed() throws SQLException {
		final SQLException[] e2 = new SQLException[1];
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			@Override
			public void run() {
				try {
					gui.ds.schuelerVw.importStudents(sl);
				} catch (SQLException e) {
					e2[0] = e;
				}
			}
		});
		if (e2[0] != null) {
			gui.showError(e2[0]);
			return false;
			// throw e2[0];
		} else {
			MessageBox mb = new MessageBox(shell, SWT.OK);
			mb.setText("Import erfolgreich");
			mb.setMessage("Der Import war erfolgreich. " + sl.size()
					+ " Schüler sind vorhanden.");
			mb.open();
			return true;
		}
	}

	private final ArrayList<Schueler> sl = new ArrayList<Schueler>();
}
