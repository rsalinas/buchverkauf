package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class BestellungenTab extends TabFolderHolder {
	private final Set<Button> buttons = new HashSet<Button>();
	protected String klasseName;
	final SelectionAdapter classSelector = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			for (Button b : buttons)
				if (b != e.widget)
					b.setSelection(false);

			if (e.widget.getData() != null)
				klasseName = ((Klasse) e.widget.getData()).name;
			else
				klasseName = null;
			loadData();
		}
	};
	// private final Text inputText;
	private final Table table;
	private final static HeaderDef[] headers = {
			new HeaderDef("#", "schuelernum", 5),
			new HeaderDef("Name", "name", 30),
			new HeaderDef("Vorname", "vorname", 20),
			new HeaderDef("Klasse", "klasse", 10),
			new HeaderDef("Bestellt", "finished", 10),
			new HeaderDef("Bücher#", "nitems", 10).setAlignment(SWT.RIGHT),
			new HeaderDef("Summe", "summe", 10).setAlignment(SWT.RIGHT) };

	private ArrayList<SchuelerBestellung> l = new ArrayList<SchuelerBestellung>();
	protected String sortBy = "name";

	public BestellungenTab(final TabFolder tabFolder, final DsvBuecher par)
			throws SQLException {
		super(tabFolder, "Bestellungen", par);
		composite.setLayout(new GridLayout(1, false));
		Composite cp2 = new Composite(composite, SWT.NONE);

		new Group(composite, 0) {
			@Override
			protected void checkSubclass() {
			}

			{
				setText("Filtern");
				setLayout(new RowLayout());
				final Button b0 = new Button(this, SWT.RADIO);
				b0.setText("&Alle");
				b0.setData(null);
				final SelectionAdapter selectionAdapter = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						filter = (BestellungState) ((Button) arg0.widget)
								.getData();
						loadData();
					}
				};
				b0.addSelectionListener(selectionAdapter);
				final Button buttonNB = new Button(this, SWT.RADIO);
				buttonNB.setText("&Nicht bestellte");
				buttonNB.setData(BestellungState.NICHT_BESTELLTE);
				buttonNB.addSelectionListener(selectionAdapter);
				final Button buttonB = new Button(this, SWT.RADIO);
				buttonB.setText("&Bestellte");
				buttonB.setData(BestellungState.BESTELLTE);
				buttonB.addSelectionListener(selectionAdapter);

			}
		};

		final ArrayList<Abteilung> abteilungen = par.ds.klassenVw
				.getAbteilungen();
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		cp2.setLayout(layout);

		// inputText = new Text(composite, SWT.LEFT);
		// final Button b = new Button(composite, SWT.RIGHT);
		// b.setText("Suchen");

		new Group(composite, SWT.NONE) {
			@Override
			protected void checkSubclass() {
			}

			{
				setLayout(new RowLayout(SWT.VERTICAL));
				setText("Klassen");
				Button all = new Button(this, SWT.RADIO);
				all.setText("Alle Klassen");
				all.setData(null);
				all.addSelectionListener(classSelector);
				for (final Abteilung a : abteilungen) {
					setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
							1, 1));
					new Composite(this, 0) {
						{
							setLayout(new RowLayout());
							new Label(this, 0).setText(a.name);
							final ArrayList<Button> buttons = new ArrayList<Button>();
							for (final Klasse k : a.klassen) {
								final Button bb = new Button(this, SWT.RADIO
								/* | SWT.NO_RADIO_GROUP */);
								buttons.add(bb);
								bb.setText(k.name);
								bb.setData(k);
								bb.addSelectionListener(classSelector);
								buttons.add(bb);
							}
						}
					};
				}
			}
		};
		table = new Table(composite, SWT.VIRTUAL | SWT.MULTI
				| SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setItemCount(0);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		Menu m = new Menu(composite);
		table.setMenu(m);
		if (gui.ds.getBuchBestellungen() == 0) {
			final MenuItem alsNichtBestelltMarkButton = new MenuItem(m,
					SWT.PUSH);
			alsNichtBestelltMarkButton.setText("Als nicht bestellt markieren");
			alsNichtBestelltMarkButton
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							for (TableItem i : table.getSelection())
								try {
									alsNichtBestelltMarkieren((SchuelerBestellung) i
											.getData());
									loadData();
								} catch (SQLException e) {
									gui.showError(e);
								}

						}
					});
		}

		for (final HeaderDef header : headers) {
			final TableColumn column = new TableColumn(table, SWT.NONE);

			column.setWidth(header.width);
			column.setText(header.name);
			if (header.alignment != 0)
				column.setAlignment(header.alignment);
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (header.field != null) {
						sortBy = header.field;
						loadData();
					} else
						gui.display.beep();
				}

			});

		}
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				item.setText(toStringArray(l.get(event.index)));
				item.setData(l.get(event.index));
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				try {
					if (table.getSelectionCount() == 0) {
						Display.getCurrent().beep();
						return;
					}
					BestellungMarkForm.showMarkingForm(
							((SchuelerBestellung) table.getSelection()[0]
									.getData()).s, par);
					loadData();
				} catch (SQLException e1) {
					par.showError(e1);
				}
			}
		});
		composite.pack();

	}

	protected void alsNichtBestelltMarkieren(SchuelerBestellung data)
			throws SQLException {
		gui.ds.buchVw.alsNichtBestelltMarkieren(data.s);

	}

	private String[] toStringArray(SchuelerBestellung sb) {
		return new String[] { "#" + sb.s.num, sb.s.name, sb.s.vorName,
				sb.s.klasse,
				sb.state == BestellungState.BESTELLTE ? "Ja" : "Nein",
				"" + sb.exemplaere, DsvBuecher.currency(sb.summe) };
	}

	private BestellungState filter;

	/**
	 * @param e
	 */
	private synchronized void loadData() {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			@Override
			public void run() {
				try {
					l = gui.ds.getSchuelerBestellungenInKlasse(klasseName,
							filter, sortBy);
				} catch (SQLException e) {
					gui.showError(e);
				}
			}

		});
		table.setItemCount(l.size());
		table.clearAll();
		String stateText = "";
		if (filter == null)
			stateText = l.size() + " Schüler in Klasse insgesamt";
		else

			switch (filter) {
			case BESTELLTE:
				stateText = l.size() + " Schüler MIT Bestellung in Klasse "
						+ klasseName;
				break;
			case NICHT_BESTELLTE:
				stateText = l.size() + " Schüler OHNE Bestellung in Klasse "
						+ klasseName;
				break;
			}
		table.setToolTipText(stateText);
		setStatusLine(stateText);

	}

	@Override
	public void f5() {
		System.err.println("bestellungentab.f5()");
		loadData();
	}

	@Override
	public void modelChanged() {
		System.err.println("bestellungentab.modelchanged()");
		loadData();
	}

	@Override
	public void fill(HashMap<StandardMenus, Menu> hm) {
		super.fill(hm);
		Menu sm = hm.get(StandardMenus.SPECIAL);
		if (gui.pm.can(DsvbPermission.NEWYEARCHANGE)) {
			RauUtil.createMenuItem(sm, SWT.PUSH, "&Bestellprozess abschließen",
					new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								if (gui.ds.getBuchBestellungen() != 0) {
									throw new SQLException(
											"Ya hay pedido hecho! Deshágalo si eso.");
								}
								MessageBox messageBox = new MessageBox(gui
										.getShell(), SWT.ICON_QUESTION
										| SWT.YES | SWT.NO);
								messageBox
										.setMessage("¿Está seguro de que desea cerrar la introducción de pedidos para el curso académico actual? Podrá deshacerlo mediante la opción disponible en el mismo menú (Bestellung/Special).");
								messageBox
										.setText("Cierre de fase de introducción de pedidos");
								int response = messageBox.open();
								if (response == SWT.YES) {
									int n = gui.ds.hacerPedidoEditoriales();
									MessageBox messageBox2 = new MessageBox(gui
											.getShell(), SWT.OK);
									messageBox2
											.setMessage("Se ha cerrado correctamente la fase de pedidos: "
													+ n + " libros con pedidos");
									messageBox2
											.setText("Cierre de fase de pedidos realizado");
									messageBox2.open();
									gui.forceShutdown();
								}
							} catch (SQLException e1) {
								gui.showError(e1);
							}

						}
					});

			RauUtil.createMenuItem(sm, SWT.PUSH,
					"&Bestellprozess rückgängig machen",
					new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							// FIXME Debe desactivar la pestaña bestellungen y
							// avisar de que se va a hacer
							try {
								if (gui.ds.getBuchBestellungen() == 0)
									throw new SQLException(
											"Kein Bestellprozess ausgeführt.");

								if (gui.ds.getBuchRechnungen() != 0)
									throw new SQLException(
											"Die Bestellung beim Händler kann nicht rückgängig gemacht werden, da Rechnungen schon erstellt.  ("
													+ gui.ds
															.getBuchRechnungen()
													+ " Rechnung(en) erstellt)");
								MessageBox messageBox = new MessageBox(gui
										.getShell(), SWT.ICON_QUESTION
										| SWT.YES | SWT.NO);
								messageBox
										.setMessage("Möchten Sie den Bestellprozess rückgängig machen?");
								messageBox
										.setText("Bestätigung: Bestellprozess rückgängig machen");
								int response = messageBox.open();
								if (response == SWT.YES) {
									gui.ds.deshacerPedido();
									MessageBox messageBox2 = new MessageBox(gui
											.getShell(), SWT.OK);
									messageBox2
											.setMessage("Der Bestellprozess wurde erfolgreich rückgängig gemacht.");
									messageBox2
											.setText("Bestellprozess rückgängig gemacht");
									messageBox2.open();
									gui.forceShutdown();
								}
							} catch (SQLException e1) {
								gui.showError(e1);
							}

						}
					});
		}
	}
}
