package dsv.buecher;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RechnungenTab extends TabFolderHolder {
	private static String[] headers = { "#", "Name", "Vorname", "Klasse",
			"Bemerkung (deu)", "Zu zahlende Summe" };
	private final Text inputText;
	private final Table table;
	private final Text sumText;
	private static final String voidText = "<Geben sie den Namen hier ein>";
	final SelectionAdapter classSelector = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			// for (Button b : buttons)
			// if (b != e.widget)
			// b.setSelection(false);
			//
			// if (e.widget.getData() != null)
			// klasseName = ((Klasse) e.widget.getData()).name;
			// else
			// klasseName = null;
			// loadData();
		}
	};
	final Klasse allKlassen = new Klasse("All");
	private String currentKlasse = null;

	public RechnungenTab(final TabFolder tabFolder, final DsvBuecher par)
			throws SQLException {
		super(tabFolder, "Rechnungen", par);

		try {
			Properties props = new Properties();
			props.load(new FileInputStream("printer.cfg"));

			// if (!par.pm.can(DsvbPermission.MENU))
			copias = Integer.parseInt(props.getProperty("copies"));
			if (copias < 0 || copias > 2)
				copias = 0;
		} catch (Exception e) {
			copias = 0;
		}
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData());
		Composite cp2 = new Composite(composite, SWT.PUSH);
		RowLayout layout = new RowLayout();
		// Optionally set layout fields.
		layout.wrap = true;
		// Set the layout into the composite.
		cp2.setLayout(layout);

		new Composite(composite, 0) {
			{
				final ArrayList<Klasse> klasseList = gui.ds.klassenVw
						.getKlassen("jahr, klasse");
				setLayout(new RowLayout());
				new Label(this, 0).setText("Klasse");
				final Combo klassen = new Combo(this, SWT.READ_ONLY);

				klasseList.add(0, allKlassen);
				for (Klasse k : klasseList)
					klassen.add(k.name);
				klassen.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						final Klasse k = klasseList.get(klassen
								.getSelectionIndex());
						currentKlasse = k.name;
						if (k == allKlassen)
							currentKlasse = null;
						try {
							loadData();
						} catch (SQLException e1) {
							gui.showError(e1);
						}
					}
				});
				// schuelerNumLabel = new Label(this, SWT.NONE);
				// schuelerNumLabel.setLayoutData(new RowData());
				//
				// schuelerNumLabel.setText("(no data)");
			}
		};

		new Group(composite, 0) {
			@Override
			protected void checkSubclass() {
			}

			{
				setText("Filtern nach");

				setLayout(new RowLayout());
				final Button b0 = new Button(this, SWT.RADIO);
				b0.setText("&Alle Schüler");

				b0.setData(null);
				final SelectionAdapter selectionAdapter = new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						filter = (BestellungState) ((Button) arg0.widget)
								.getData();

						try {
							loadData();
						} catch (SQLException e) {
							gui.showError(e);
						}
					}
				};
				b0.addSelectionListener(selectionAdapter);
				final Button buttonNB = new Button(this, SWT.RADIO);
				buttonNB.setText("&Ohne Bestellung / Bestellung abgeholt");
				buttonNB.setData(BestellungState.NICHT_BESTELLTE);
				buttonNB.addSelectionListener(selectionAdapter);
				final Button buttonB = new Button(this, SWT.RADIO);
				buttonB.setText("&Bestellung nicht abgeholt");
				buttonB.setData(BestellungState.BESTELLTE);
				buttonB.addSelectionListener(selectionAdapter);

				buttonB.setSelection(true);
				filter = BestellungState.BESTELLTE;

			}
		};

		new Label(cp2, SWT.LEFT).setText("Schülername:");
		inputText = new Text(cp2, SWT.LEFT);

		configurarTextParaMostrarTextoSiNoFocoYVacio(inputText, voidText);
		final RowData rd = new RowData();
		rd.width = 300;
		inputText.setLayoutData(rd);
		inputText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!inputText.getText().equals(voidText)
						&& !inputText.getText().toUpperCase()
								.equals(inputText.getText())) {
					int p = inputText.getCaretPosition();
					inputText.setText(inputText.getText().toUpperCase());
					inputText.setSelection(p);
				}
			}

		});
		Button b = new Button(cp2, SWT.RIGHT);
		b.setText("Suchen");
		inputText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				final String t = inputText.getText();
				par.xs.execute(new Runnable() {
					@Override
					public void run() {
						try {
							showRechnungen(t, currentKlasse);
						} catch (SQLException e) {
							par.showError(e);
						}
					}
				});
			}
		});
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					loadData();
				} catch (SQLException e) {
					par.showError(e);
				}
			}
		});
		table = new Table(composite, SWT.VIRTUAL | SWT.MULTI
				| SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setItemCount(0);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double sum = 0;
				for (TableItem l : table.getSelection())
					if (l.getData() instanceof BestellungInfo)
						sum += ((BestellungInfo) l.getData()).getAmount();
				sumText.setText(DsvBuecher.currency(sum));
			}

		});
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

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
				final BestellungInfo thisObj;
				synchronized (l) {
					if (event.index >= l.size())
						return;
					thisObj = l.get(event.index);
				}
				final String[] a1 = thisObj.schueler.toStringArray();
				final String[] a2 = { DsvBuecher.currency(thisObj.getAmount()) };
				item.setText(DsvBuecher.joinArrays(a1, a2));
				item.setData(thisObj);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				xverkauf();
			}
		});
		final Menu m = new Menu(table);
		table.setMenu(m);
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
					final MenuItem pdfMaker = new MenuItem(m, SWT.PUSH);
					pdfMaker.setText("Venta");
					pdfMaker.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							xverkauf();

						}
					});
				}
				{
					final MenuItem pdfMaker = new MenuItem(m, SWT.PUSH);
					pdfMaker.setText("Mostrar facturas");
					pdfMaker.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								mostrarFacturas();
							} catch (SQLException e1) {
								gui.showError(e1);
							}

						}

					});
				}
				// new MenuItem(m, SWT.PUSH).setText("Encajar/desencajar");
				// final MenuItem editarPedidoMenuItem = new MenuItem(m,
				// SWT.PUSH);
				// editarPedidoMenuItem.setText("Editar pedido");
				// editarPedidoMenuItem
				// .addSelectionListener(new SelectionAdapter() {
				// @Override
				// public void widgetSelected(SelectionEvent e) {
				// if (table.getSelectionCount() != 1) {
				// par.getShell().getDisplay().beep();
				// return;
				// }
				//
				// for (TableItem i : table.getSelection())
				// try {
				// BestellungMarkForm
				// .showMarkingForm(
				// ((RechnungInfo) i
				// .getData()).schueler,
				// par);
				// } catch (SQLException e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// }
				// }
				// });

			}
		});
		sumText = new Text(composite, SWT.NONE);
		composite.pack();
	}

	/**
	 * 
	 */
	public static void configurarTextParaMostrarTextoSiNoFocoYVacio(
			final Text text, final String textIfVoid) {
		if (text.getText().isEmpty())
			text.setText(textIfVoid);
		text.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (text.getText().equals(textIfVoid)) {
					text.setText("");
					text.setEditable(true);
				}

			}

			@Override
			public void focusLost(FocusEvent e) {
				if (text.getText().isEmpty()) {
					text.setText(textIfVoid);
					text.setEditable(false);
				}
			}
		});
	}

	private ArrayList<BestellungInfo> l = new ArrayList<BestellungInfo>();

	protected void showRechnungen(String prefix, String k) throws SQLException {
		synchronized (gui.ds) {
			l = gui.ds.rechnungVw.getRechnungenStartingWith(prefix, filter, k);

		}
		gui.getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				table.setItemCount(l.size());
				table.clearAll();
				sumText.setText("");
			}
		});
	}

	/**
	 * @throws SQLException
	 */
	Table t2;

	@SuppressWarnings("unused")
	private void showPdf() throws SQLException {
		ArrayList<RechnungInfo> rechnungInfos = new ArrayList<RechnungInfo>();
		for (TableItem i : table.getSelection())
			rechnungInfos.add((RechnungInfo) i.getData());
		final ArrayList<Schueler> l = new ArrayList<Schueler>();
		for (TableItem i : table.getSelection())
			l.add(((RechnungInfo) i.getData()).schueler);
		final Shell s3 = new Shell(gui.getShell(), SWT.RESIZE | SWT.CLOSE
				| SWT.APPLICATION_MODAL | SWT.V_SCROLL);
		s3.setLayout(new GridLayout());
		s3.setText("Realizar venta");
		double sum = 0;
		new Group(s3, 0) {
			@Override
			protected void checkSubclass() {
			}

			{
				t2 = new Table(this, SWT.MULTI);
				setText("Schüler");
				setLayout(new GridLayout(2, false));
			}
		};

		for (final HeaderDef header : chosenSchuelerTableHeaders) {
			final TableColumn column = new TableColumn(t2, SWT.NONE);
			column.setWidth(header.width);
			column.setText(header.name);
			if (header.alignment != 0)
				column.setAlignment(header.alignment);
		}
		t2.setHeaderVisible(true);
		for (RechnungInfo ri : rechnungInfos) {
			new TableItem(t2, SWT.NONE).setText(new String[] {
					ri.schueler.name, ri.schueler.vorName,
					DsvBuecher.currency(ri.amount) });
			sum += ri.amount;
		}
		final Label suml = new Label(s3, SWT.NONE);
		suml.setText("Summe: " + DsvBuecher.currency(sum));

		final Button b3 = new Button(s3, SWT.PUSH);
		b3.setText("Imprimir");
		b3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {

					RechnungForm.show(gui.getShell(), l, gui);
				} catch (SQLException ex) {
					gui.showError(ex);
				}

			}
		});
		final Button b = new Button(s3, SWT.PUSH);
		b.setText("Marcar como cobrada");
		final Button b2 = new Button(s3, SWT.PUSH);

		b2.setText("Marcar como NO cobrada");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputText.setText("");
				inputText.setFocus();
				synchronized (l) {
					l.clear();
				}
				table.clearAll();
				s3.close();
			}
		});
		b2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				s3.close();
			}
		});

		Table tablaDevoluciones = new Table(s3, SWT.CHECK);
		for (final HeaderDef header : headers2) {
			final TableColumn column = new TableColumn(tablaDevoluciones,
					SWT.NONE);
			column.setWidth(header.width);
			column.setText(header.name);
			if (header.alignment != 0)
				column.setAlignment(header.alignment);
		}
		for (RechnungInfo ri : rechnungInfos)
			for (RechnungLine i : gui.ds.rechnungVw.getRechnung(ri.schueler)) {
				TableItem ti = new TableItem(tablaDevoluciones, 0);
				ti.setText(i.buch.toStringArray(gui.ds));
			}
		tablaDevoluciones.setHeaderVisible(true);
		tablaDevoluciones.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		s3.pack();
		s3.open();

	}

	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> pasa(Table t, ArrayList<T> l) {
		for (TableItem i : t.getSelection())
			l.add(((T) i.getData()));
		return l;
	}

	@Override
	public void onFocus() {
		inputText.setFocus();
		inputText.selectAll();
	}

	@Override
	public void fill(HashMap<StandardMenus, Menu> im2) {

		MenuItem mi = RauUtil.createMenuItem(im2.get(StandardMenus.SPECIAL),
				SWT.CASCADE, "Druckmodus", new SelectionAdapter() {
				});
		Menu m2 = new Menu(mi);
		mi.setMenu(m2);
		MenuItem r0 = RauUtil.createMenuItem(m2, SWT.RADIO, "Sólo abrir PDF",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						copias = 0;
					}
				});
		MenuItem r1 = RauUtil.createMenuItem(m2, SWT.RADIO,
				"Automatisch 1 Kopie drucken", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						copias = 1;
					}
				});
		MenuItem r2 = RauUtil.createMenuItem(m2, SWT.RADIO,
				"Automatisch 2 Kopien drücken", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						copias = 2;
					}
				});

		new MenuItem[] { r0, r1, r2 }[copias].setSelection(true);
		
		if (gui.pm.can(DsvbPermission.SUPER))
			RauUtil.createMenuItem(im2.get(StandardMenus.SPECIAL), SWT.PUSH,
					"Buchungssetverwaltung", new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							try {
								new RemesaBrowser(gui);
							} catch (SQLException e) {
								gui.showError(e);
							}
							
						}
					});

	}

	int copias = 0;

	private void xverkauf() {
		if (table.getSelectionCount() == 0) {
			composite.getDisplay().beep();
			return;
		}
		// XVerkaufForm f = new XVerkaufForm(gui);
		try {
			ArrayList<Schueler> l = new ArrayList<Schueler>();
			for (TableItem schu : table.getSelection())
				l.add(((BestellungInfo) schu.getData()).schueler);
			new XVerkaufForm(gui).showMarkingForm2(l);
			showRechnungen(inputText.getText(), currentKlasse);
		} catch (SQLException e1) {
			gui.showError(e1);
		}
	}

	private final static HeaderDef[] chosenSchuelerTableHeaders = {
			new HeaderDef("Name", null, 40),
			new HeaderDef("Vorname", null, 20),
			new HeaderDef("Summe", null, 10).setAlignment(SWT.RIGHT) };
	private final static HeaderDef[] headers2 = {
			new HeaderDef("Titel", "titel", 30),
			new HeaderDef("Verlag", "verlagname"),
			new HeaderDef("Fach", "fach"),
			new HeaderDef("Preis", "preis", 10).setAlignment(SWT.RIGHT) };

	private void mostrarFacturas() throws SQLException {
		if (table.getSelectionCount() != 1) {
			gui.getShell().getDisplay().beep();
			return;
		}
		BestellungInfo ri = (BestellungInfo) table.getSelection()[0].getData();
		Shell s2 = new Shell(gui.getShell(), SWT.RESIZE | SWT.CLOSE | SWT.MAX);
		s2.setLayout(new FillLayout());
		s2.setText("Rechnungen für " + ri.schueler);
		new RechnungMgm(s2, gui.ds, ri.schueler);
		s2.open();
	}

	private static String devoidNameSpec(String name) {
		if (name.equals(voidText))
			return "";
		else
			return name;
	}

	private void loadData() throws SQLException {
		if ((inputText.getText().equals(voidText) || inputText.getText()
				.isEmpty()) && currentKlasse != null)
			showRechnungen("", currentKlasse);
		else
			showRechnungen(devoidNameSpec(inputText.getText()), currentKlasse);
	}

	private BestellungState filter;
}
