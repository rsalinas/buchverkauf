package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dsv.buecher.report.ReportMarcaje;

public class KlassenTab extends TabFolderHolder {
	private final static String[] headers = { "Klasse", "Jahr", "Abteilung",
			"Schüleranzahl" };
	private Table table = null;
	@SuppressWarnings("unused")
	private final Button button = null;
	private ArrayList<Klasse> klassenList;

	@Override
	public void f5() {

		try {
			loadData();
			System.err.println("klassentab reloaded data!");
		} catch (SQLException e) {
			gui.showError(e);
		}
	}

	public KlassenTab(final TabFolder tabFolder, final DsvBuecher dsvBuecher)
			throws SQLException {
		super(tabFolder, "&Klassen", dsvBuecher);
		composite.setLayout(new GridLayout());

		item.setControl(composite);

		table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.VIRTUAL);

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 640, 320);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		loadData();
		for (String header : headers) {
			int width = 20;
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth((width * 8)); // Characters by pixels... rough
			// guess
			column.setText(header);
		}

		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				item.setText(klassenList.get(event.index).toStringArray());
				item.setData(klassenList.get(event.index));
			}

		});

		final Menu m = new Menu(table);
		m.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event event) {
				MenuItem[] menuItems = m.getItems();
				for (MenuItem menuItem : menuItems) {
					menuItem.dispose();
				}
				TableItem[] selection = table.getSelection();
				if (selection.length == 0)
					return;
				{
					final MenuItem allSelect = new MenuItem(m, SWT.PUSH);
					allSelect.setText("Alle Klassen auswählen");
					allSelect.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							table.selectAll();
						}
					});

				}
				MenuItem klasseBearbeitenButton = new MenuItem(m, SWT.PUSH);
				klasseBearbeitenButton.setText("Klasse bearbeiten");
				klasseBearbeitenButton
						.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								bearb();
							}
						});
				final MenuItem zeigeSchuelerInThisClass = new MenuItem(m,
						SWT.PUSH);
				zeigeSchuelerInThisClass
						.setText("Schüler in dieser Klasse zeigen");
				zeigeSchuelerInThisClass
						.addSelectionListener(new SelectionAdapter() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								gui.schuelerTab.filterInClass(((Klasse) table
										.getSelection()[0].getData()).name);
								gui.tabFolder.setSelection(1);
							}

						});
				RauUtil.createMenuItem(m, SWT.PUSH, "Bestellliste drucken",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								imprimirHojasMarcaje(false);

							}
						});
				RauUtil.createMenuItem(m, SWT.PUSH, "Preisliste drucken",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								imprimirHojasMarcaje(true);

							}
						});
				new MenuItem(m, SWT.SEPARATOR);

				final MenuItem loeschen = new MenuItem(m, SWT.PUSH);
				loeschen.setText("Löschen");
				loeschen.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						try {
							for (TableItem i : table.getSelection())
								if (confirmanBorrar(((Klasse) i.getData()).name))
									gui.ds.klassenVw.removeKlasse((Klasse) i
											.getData());
							loadData();
							MessageBox mb = new MessageBox(gui.getShell(),
									SWT.OK);
							mb.setText("Erfolgreich gelöscht");
							mb.setMessage("Die Daten wurden gesichert.\n\nDas Programm muss neu gestartet werden um Änderungen im Bücherregister sichtbar zu machen.");
							mb.open();
						} catch (SQLException e) {
							gui.showError(e);
						}
					}

					private boolean confirmanBorrar(String klasse) {
						int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO
								| SWT.SAVE;
						MessageBox messageBox = new MessageBox(gui.getShell(),
								style);
						messageBox
								.setText("Bestätigung 'Löschen einer Klasse'");
						messageBox.setMessage("Möchten Sie die Klasse '"
								+ klasse + "' löschen?");
						return messageBox.open() == SWT.YES;
					}
				});
			}

		});
		table.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub
				bearb();

			}
		});
		table.setMenu(m);
		RauUtil.createButton(composite, "Crear", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Klasse klasse = new Klasse(dsvBuecher.ds.getRunningYear());
				new KlassenFrame(composite.getShell(), klasse);
			}
		});
	}

	private void loadData() throws SQLException {
		klassenList = gui.ds.klassenVw.getKlassen("jahr,klasse");
		table.setItemCount(klassenList.size());
		table.clearAll();
	}

	private void imprimirHojasMarcaje(final boolean sinAclaraciones) {
		final ArrayList<String> l = new ArrayList<String>();
		for (TableItem i : table.getSelection()) {
			if (i.getData() != null)
				l.add(((Klasse) i.getData()).name);
			else
				System.err.println("Es nulo! " + i.getText());
		}
		BusyIndicator.showWhile(gui.display, new Runnable() {
			@Override
			public void run() {
				try {
					ReportMarcaje.dale(l, gui, sinAclaraciones);
				} catch (Exception e) {
					gui.showError(e);
				}
			}

		});
	}

	private void bearb() {
		try {
			for (TableItem i : table.getSelection()) {
				new KlassenFrame(gui.getShell(), (Klasse) i.getData());
				break;
			}
			loadData();
		} catch (SQLException e) {
			gui.showError(e);
		}
	}

	public class KlassenFrame {

		private final Shell sh;
		@SuppressWarnings("unused")
		private final Klasse data;

		public KlassenFrame(Shell pshell, final Klasse data) {
			sh = new Shell(pshell, SWT.RESIZE | SWT.CLOSE
					| SWT.APPLICATION_MODAL);
			sh.setText("Klassenverwaltung");
			this.data = data;

			sh.setLayout(new GridLayout(2, false));
			// FIXME
			// if (data.code != -1) {
			// new Label(sh, 0).setText("Id:");
			// final Text codeText = new Text(sh, 0);
			// codeText.setText("" + data.code);
			// codeText.setEnabled(false);
			// codeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
			// false, 1, 1));
			// }
			new Label(sh, 0).setText("Klasse:");
			final Text nameText = new Text(sh, 0);
			nameText.setText(data.name);
			new Label(sh, 0).setText("Abteilung:");
			final Combo abteilungText = new Combo(sh, SWT.READ_ONLY);
			try {
				for (Abteilung i : gui.ds.klassenVw.getAbteilungen())
					abteilungText.add(i.name);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			abteilungText.setText(data.abteilung);
			// if (Verlag.hm.containsKey(data.land))
			// landText.setText(Verlag.hm.get(data.land));
			// else
			// landText.setText("");

			for (Control w : new Control[] { nameText, abteilungText })
				w.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
						1, 1));
			// new Label(sh, 0).setText("arg0");
			final Button bSave = new Button(sh, SWT.PUSH);
			bSave.setText("&Save");
			bSave.setLayoutData(new GridData(1, 1, false, false));
			bSave.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					BusyIndicator.showWhile(sh.getDisplay(), new Runnable() {
						@Override
						public void run() {
							try {
								data.name = nameText.getText();
								data.jahr = Integer.parseInt(data.name
										.replaceAll("^(\\d*)\\D?.*$", "$1"));
								System.err.println("jahr(" + data.name
										+ ") == " + data.jahr);
								data.abteilung = abteilungText.getText();
								gui.ds.klassenVw.saveKlasse(data);
							} catch (SQLException e) {
								gui.showError(e);
							}
						}
					});
					MessageBox mb = new MessageBox(sh, SWT.OK);
					mb.setText("Erfolgreich gespeichert");
					mb.setMessage("Die Daten wurden gesichert.\n\nDas Programm muss neu gestartet werden um Änderungen im Bücherregister sichtbar zu machen.");
					mb.open();
					sh.close();
					gui.klassenTab.f5();
					gui.buecherTab.f5();
					gui.schuelerTab.f5();
				}
			});

			final Button b2 = new Button(sh, SWT.PUSH);
			b2.setText("&Cancel");
			b2.setLayoutData(new GridData(1, 1, false, false));
			b2.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					sh.close();
				}

			});
			// sh.setSize(300, 200);
			sh.pack();
			sh.setSize(4 * 100, sh.getSize().y);
			sh.open();
			Display display = sh.getDisplay();
			while (!sh.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}

	}
}
