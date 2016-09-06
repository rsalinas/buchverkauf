package dsv.buecher;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
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
import org.eclipse.swt.widgets.Widget;

import dsv.buecher.report.ReportBuchKaeufer;

public class BuecherTab extends TabFolderHolder {
	private final class BuchInfoGroup extends Composite {
		private final ArrayList<WidgetLink> links = new ArrayList<WidgetLink>();
		public final HashMap<String, Button> buttons = new HashMap<String, Button>();
		private final Text codeText;
		private Buch curBuch;
		private final Combo fachCombo;
		private final DsvBuecher gui;
		private final Text isbnText;
		private HashSet<String> klasseNamen;
		private final HashMap<Integer, TableItem> markControls = new HashMap<Integer, TableItem>();
		protected Text preisText;
		protected StyledText bemerkDeu, bemerkSpa;

		private final Combo spracheCombo;
		private final Text stockText;
		private final Text titelText;
		private final Combo verlagCombo;

		public void link(Widget widget, Buch object, String varname) {
			links.add(new WidgetLink(widget, object, varname));
		}

		private BuchInfoGroup(Composite parent, int style,
				final DsvBuecher dsvBuecher) throws SQLException {
			super(parent, style);

			this.gui = dsvBuecher;
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			setLayout(new GridLayout(6, true));
			final Label l2 = new Label(this, SWT.RIGHT);
			l2.setText("Titel:");
			l2.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1,
					1));
			titelText = new Text(this, SWT.NONE);
			titelText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false, 1, 1));
			link(titelText, curBuch, "titel");

			final Label l3 = new Label(this, SWT.NONE);
			l3.setText("Verlag:");
			l3.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1,
					1));
			verlagCombo = new Combo(this, SWT.READ_ONLY);
			// link(verlagCombo, curBuch, "verlag");
			verlagCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false, 1, 1));
			final Label l1 = new Label(this, SWT.NONE);
			l1.setText("Code:");
			l1.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1,
					1));
			codeText = new Text(this, SWT.NONE);
			codeText.setEnabled(false);
			codeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
					false, 1, 1));
			final Label stockLabel = new Label(this, SWT.NONE);
			stockLabel.setText(ds.getBuchBestellungen() != 0 ? "Stock:"
					: "Vom Vorjahr:");
			stockLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false,
					false, 1, 1));
			stockText = new Text(this, SWT.NONE);

			stockText.setEnabled(true);
			stockText.setEnabled(ds.getBuchBestellungen() == 0);
			stockText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
					false, 1, 1));
			link(stockText, curBuch, "stock");

			final Label fachLabel = new Label(this, SWT.NONE);
			fachLabel.setText("Fach:");
			fachLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false,
					false, 1, 1));
			fachCombo = new Combo(this, SWT.READ_ONLY);
			populateFachCombo(fachCombo);
			fachCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
					false, 1, 1));

			final Label lSprache = new Label(this, SWT.NONE);
			lSprache.setText("Sprache:");
			lSprache.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false,
					false, 1, 1));
			spracheCombo = new Combo(this, SWT.READ_ONLY);
			spracheCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false, 1, 1));

			populateSpracheCombo(spracheCombo);

			populateVerlagCombo(verlagCombo);

			Label isbnLabel = new Label(this, 0);
			isbnLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true,
					false, 1, 1));
			isbnLabel.setText("ISBN:");
			isbnText = new Text(this, 0);
			isbnText.setText("isbn");
			isbnText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false, 1, 1));
			final Label lPreis = new Label(this, SWT.NONE);
			lPreis.setText("Preis:");
			lPreis.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false,
					false, 1, 1));
			new Composite(this, SWT.NONE) {
				{
					setLayout(new RowLayout(SWT.HORIZONTAL));
					preisText = new Text(this, SWT.NONE);
					this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false, 1, 1));
					new Label(this, SWT.NONE).setText("€");
					link(preisText, curBuch, "preis");
				}
			};

			// new Composite(this, 0).setLayoutData(new GridData(4,1));

			new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL)
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false, 6, 1));

			new Composite(this, 0) {
				{
					setLayout(new RowLayout(SWT.HORIZONTAL));
					setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false,
							6, 1));
					makeAbteilungBlock(dsvBuecher, this, BuchInfoGroup.this);
					new Group(this, SWT.FILL) {
						{
							setText("Bemerkungen");
							RowLayout rl = new RowLayout(SWT.VERTICAL);
							rl.fill = false;
							setLayout(rl);

							markeTable = new Table(this, SWT.CHECK);
							RowData rd = new RowData();
							rd.height = getShell().getBounds().height / 3;
							markeTable.setLayoutData(rd);
							populateMarkeTable();
						}

						@Override
						protected void checkSubclass() {
						}
					};

				}
			};
			// new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL)
			// .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
			// false, 6, 1));FILL
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtonState();
					// notizenButton.setEnabled(table.getSelectionCount() == 1);
					// System.err.println("mirando");

				}
			});
			new Group(this, SWT.FILL) {
				{
					Layout rl = new FillLayout();
					setLayout(rl);
					setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
							true, 6, 1));
					setText("Individuelle Buchbemerkung (Deutsch):");
					bemerkDeu = new StyledText(this, SWT.FILL);
					link(bemerkDeu, curBuch, "bemerkDeu");
				}

				@Override
				protected void checkSubclass() {
				}
			};
			new Group(this, SWT.FILL) {
				{
					setText("Individuelle Buchbemerkung (Spanisch):");
					Layout rl = new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL);
					setLayout(rl);
					setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
							true, 6, 1));
					bemerkSpa = new StyledText(this, SWT.NONE);
					link(bemerkSpa, curBuch, "bemerkSpa");

				}

				@Override
				protected void checkSubclass() {
				}
			};

			new Composite(this, SWT.NONE) {
				{
					setLayout(new RowLayout(SWT.HORIZONTAL));
					setLayoutData(new GridData(SWT.NONE, SWT.NONE, false,
							false, 6, 1));
					final Button newButton;
					newButton = new Button(this, SWT.PUSH);
					newButton.setBounds(300, 325, 50, 30);
					newButton.setText("N&eues Buch");
					newButton.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								g.newBook();
							} catch (SQLException e1) {
								gui.showError(e1);
							}
						}
					});
					{
						Button saveButton;
						saveButton = new Button(this, SWT.PUSH);
						saveButton.setText("Speicher&n");
						saveButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								g.save();
							}
						});
					}

				}
			};
			
		}

		@Override
		protected void checkSubclass() {
		}

		@SuppressWarnings("unused")
		public int hasChanged() {
			int changes = 0;
			if (!titelText.getText().equals(curBuch.titel))
				changes++;
			// TODO: controlar todos los campos
			for (Entry<String, Button> e : buttons.entrySet()) {
				if (e.getValue().getSelection() != klasseNamen.contains(e
						.getKey()))
					changes++;
			}
			return changes;
		}

		public void modelChanged() {
			try {
				populateFachCombo(fachCombo);
				populateVerlagCombo(verlagCombo);
				populateSpracheCombo(spracheCombo);
				populateMarkeTable();
			} catch (SQLException e) {
				gui.showError(e);
			}
		}

		private void newBook() throws SQLException {
			table.deselectAll();
			verlagCombo.setText("");
			spracheCombo.setText("");
			fachCombo.setText("");
			isbnText.setText("");
			preisText.setText("");
			titelText.setText("");
			titelText.setFocus();
			bemerkDeu.setText("");
			bemerkSpa.setText("");
			showProperties(new Buch());
		}

		private void populateMarkeTable() throws SQLException {
			markeTable.removeAll();
			for (BuchBemerkung i : ds.getBuchMarken()) {
				if (i.dsc.split("\\|").length != 2)
					continue;
				TableItem ti = new TableItem(markeTable, SWT.CHECK);
				ti.setData(i);
				markControls.put(i.id, ti);
				ti.setText(i.dsc.split("\\|")[0]);
			}
		}

		private void save() {
			if (curBuch == null) {
				gui.display.beep();
				return;
			}
			try {

				assert curBuch != null;
				assert ds.sprachVw.langByName != null;
				curBuch.setSprache(ds.sprachVw.langByName.get(spracheCombo
						.getText()));
				if (titelText.getText().isEmpty())
					throw new DataConsistencyException(
							"Titel muss noch geschrieben werden");
				if (fachCombo.getText().isEmpty())
					throw new DataConsistencyException(
							"Fach muss noch gewählt werden");
				if (verlagCombo.getText().isEmpty())
					throw new DataConsistencyException(
							"Verlag muss noch gewählt werden");
				if (preisText.getText().isEmpty())
					throw new DataConsistencyException(
							"Preis muss noch fixiert werden");
				if (spracheCombo.getText().isEmpty())
					throw new DataConsistencyException(
							"Sprache muss noch gewählt werden");
				if (preisText.getText().isEmpty()
						|| NumberFormat.getInstance()
								.parse(preisText.getText()).doubleValue() == 0) {
					MessageBox mb = new MessageBox(getShell());
					mb.setText("Vorsicht");
					mb.setMessage("Preis ist nicht gesetzt worden");
					mb.open();

				}

				curBuch.preis = NumberFormat.getInstance()
						.parse(preisText.getText()).doubleValue();

				curBuch.stock = NumberFormat.getInstance()
						.parse(stockText.getText()).intValue();

				curBuch.titel = titelText.getText();
				curBuch.isbn = isbnText.getText();
				curBuch.fach = fachCombo.getText();
				curBuch.bemerkSpa = bemerkSpa.getText();
				curBuch.bemerkDeu = bemerkDeu.getText();

				// -1 porque [0] == null
				if (verlagCombo.getSelectionIndex() >= 1)
					curBuch.verlag = verlage.get(verlagCombo
							.getSelectionIndex() - 1);
				else
					curBuch.verlag = null;
				// curBuch.sprache = ((HashMap<String,
				// Language>) spracheText
				// .getData()).get(spracheText.getText()).name;
				final ArrayList<BooleanDifference> kdl = new ArrayList<BooleanDifference>();
				for (Entry<String, Button> i : buttons.entrySet()) {
					if (i.getValue().getSelection() != klasseNamen.contains(i
							.getKey())) {
						kdl.add(new BooleanDifference(i.getKey(), i.getValue()
								.getSelection()));
						if (i.getValue().getSelection())
							klasseNamen.add(i.getKey());
						else
							klasseNamen.remove(i.getKey());
						curBuch.klassen.clear();
						for (String klasseName : klasseNamen)
							curBuch.klassen.add(ds.klassen.get(klasseName));
					}

				}
				curBuch.bemerkungen = new ArrayList<Integer>();
				for (Entry<Integer, TableItem> i : markControls.entrySet())
					if (i.getValue().getChecked())
						curBuch.bemerkungen.add(i.getKey());

				ds.buchVw.saveBuch(curBuch, kdl, curBuch.bemerkungen);
				syncLoadData();
				/* Needed to update internal values */
				showProperties(curBuch);
				if (mostrarAvisoGrabacionCorrecta) {
					MessageBox mb = new MessageBox(gui.getShell(), SWT.OK);
					mb.setText("Grabación correcta");
					mb.setMessage("Los datos del libro han sido actualizados correctamente");
					mb.open();
				}
			} catch (NumberFormatException e1) {
				gui.showError(e1);
			} catch (SQLException e1) {
				gui.showError(e1);
			} catch (DataConsistencyException e3) {
				gui.showError(e3);
			} catch (ParseException e) {
				gui.showError(e);
			}
		}

		/**
		 * @param b
		 * @throws SQLException
		 */
		protected void showProperties(final Buch b) throws SQLException {
			if (b == null)
				return;
			try {
				System.err.println("show: " + b.toString());
				if (isDisposed())
					return;
				this.curBuch = b;
				for (WidgetLink lnk : links) {
					if (lnk.widget.isDisposed()) {
						System.err.println("extraño, disposed, saliendo");
						return;
					}
					System.err.println("lnk type: " + lnk.widget.getClass());
					if (lnk.widget instanceof Text) {
						if (b.getClass().getField(lnk.varname).getType() == String.class)
							((Text) lnk.widget).setText((String) b.getClass()
									.getField(lnk.varname).get(b));
						else if (b.getClass().getField(lnk.varname).getType() == double.class) {
							if (lnk.varname.equals("preis"))
								((Text) lnk.widget).setText(new DecimalFormat(
										"0.00").format(b.getClass()
										.getField(lnk.varname).getDouble(b)));
							else
								((Text) lnk.widget).setText(""
										+ b.getClass().getField(lnk.varname)
												.getDouble(b));

						} else if (b.getClass().getField(lnk.varname).getType() == int.class) {

							((Text) lnk.widget).setText(""
									+ b.getClass().getField(lnk.varname)
											.getInt(b));

						}
					} else if (lnk.widget instanceof Combo) {
						if (b.getClass().getField(lnk.varname).getType() == String.class)
							((Combo) lnk.widget).setText((String) b.getClass()
									.getField(lnk.varname).get(b));
					}

				}
				codeText.setText(b.isNew() ? "NEU" : "" + b.code);
				if (b.verlag != null)
					verlagCombo.setText(b.verlag.name);
				else
					verlagCombo.setText("");
				fachCombo.setText(b.fach);
				bemerkSpa.setText(b.bemerkSpa);
				bemerkDeu.setText(b.bemerkDeu);
				isbnText.setText(b.isbn != null ? b.isbn : "?");
				klasseNamen = new HashSet<String>();
				if (!b.isNew()) {
					if (ds.sprachVw.langById.containsKey(b.getSpracheId()))
						spracheCombo.setText(ds.sprachVw.langById.get(b
								.getSpracheId()).name);
					else {
						throw new SQLException("Cannot find lang: "
								+ b.getSpracheId());
					}
					if (b.klassen != null)
						for (Klasse k : b.klassen) {
							System.err.println("klasse: " + k);
							if (k != null) {
								klasseNamen.add(k.name);
							}
						}
				}
				for (Entry<String, Button> e : buttons.entrySet()) {
					if (e.getValue().isDisposed())
						return;
					e.getValue().setSelection(klasseNamen.contains(e.getKey()));
				}
				for (Entry<Integer, TableItem> e : markControls.entrySet()) {
					e.getValue().setChecked(b.bemerkungen.contains(e.getKey()));

				}
			} catch (Exception e1) {
				gui.showError(e1);
				return;
			}
		}
	}

	private final static HeaderDef[] headers = {
			new HeaderDef("#", "buchcode", 6),
			new HeaderDef("Titel", "titel", 30),
			new HeaderDef("Verlag", "verlagname"),
			new HeaderDef("ISBN", "isbn"), new HeaderDef("Fach", "fach", 10),
			new HeaderDef("Preis", "preis", 10).setAlignment(SWT.RIGHT),
			new HeaderDef("Sprache", "sprache", 5),
			new HeaderDef("Klassen", "minjahr", 20),
			new HeaderDef("Bemerkungen", null, 15) };

	public static final boolean mostrarAvisoGrabacionCorrecta = false;

	public final ArrayList<Abteilung> abteilungen;

	private final ArrayList<Buch> buecher = new ArrayList<Buch>();
	private Button delButton;

	@SuppressWarnings("unused")
	private final Button detailButton = null;

	private final DsvBuecherDatasource ds;

	private Object filter = null;

	private final SelectionAdapter filterSelectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			setFilter(e.widget.getData());
		}

	};
	public BuchInfoGroup g;

	private final DsvBuecher gui;

	private Table markeTable;

	protected String sortBy = "titel";

	// private final GenericRequestInterface<Language> langreq;
	private Table table = null;

	protected final ArrayList<Verlag> verlage = new ArrayList<Verlag>();

	public BuecherTab(final TabFolder tabFolder, final DsvBuecher dsvBuecher)
			throws SQLException {
		super(tabFolder, "&Bücher", dsvBuecher);
		this.ds = dsvBuecher.ds;
		this.gui = dsvBuecher;
		abteilungen = ds.klassenVw.getAbteilungen();

		final Composite composite = new Composite(tabFolder, SWT.PUSH);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData());
		composite.setSize(640, 480);
		// composite.pack();
		item.setControl(composite);

		table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.VIRTUAL);

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		table.setBounds(0, 0, 640, 320);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		for (final HeaderDef header : headers) {
			// if (!gui.pm.can(DsvbPermission.VIEWID) &&
			// "#".equals(header.name))
			// continue;
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
		syncLoadData();
		// TableItem[] items = new TableItem[data.length]; // An item for
		// each field
		// for (int i = 0; i < data.length; i++) {
		// items[i] = new TableItem(table, SWT.NONE);
		// items[i].setText(data[i]);
		// }
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				final Buch buch;
				synchronized (buecher) {
					buch = buecher.get(event.index);
				}
				item.setData(buch);
				item.setText(buch.toStringArray(gui.ds));
			}
		});
		g = new BuchInfoGroup(composite, SWT.None, dsvBuecher);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() != 1)
					return;
				final Buch b = (Buch) table.getSelection()[0].getData();
				try {
					if (g.isDisposed())
						return;
					g.showProperties(b);
				} catch (SQLException ex) {
					dsvBuecher.showError(ex);
				}
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

				new MenuItem(m, SWT.SEPARATOR);
				final MenuItem loeschen = new MenuItem(m, SWT.PUSH);
				loeschen.setText("Löschen");
				loeschen.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						MessageBox mb = new MessageBox(composite.getShell(),
								SWT.YES | SWT.NO);

						mb.setText("Bestätigung ");
						mb.setMessage("Bestätigen Sie, dass Sie die gewählten Bücher löschen wollen?\n\nWarnung: Sämtliche Reservierungen/Bestellungen, falls vorhanden, werden gelöscht.");
						if (mb.open() == SWT.YES)
							try {
								for (TableItem i : table.getSelection())
									ds.buchVw.removeBuch((Buch) i.getData());
								loadData();
							} catch (SQLException e1) {
								gui.showError(e1);
							}
					}
				});
				RauUtil.createMenuItem(m, SWT.PUSH, "Käufer für dieses Buch",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								final List<Buch> buecherl = new ArrayList<Buch>();
								for (int i : table.getSelectionIndices()) {
									buecherl.add(buecher.get(i));
								}
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportBuchKaeufer(gui,
															buecherl, true,
															"Käufer für folgendes Buch");
												} catch (Exception e) {
													gui.showError(e);
												}
											}
										});

							}
						});
				RauUtil.createMenuItem(m, SWT.PUSH,
						"Besteller ohne Rechnung für dieses Buch",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								final List<Buch> buecherl = new ArrayList<Buch>();
								for (int i : table.getSelectionIndices()) {
									buecherl.add(buecher.get(i));
								}
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportBuchKaeufer(gui,
															buecherl, false,
															"Besteller ohne Rechnung für folgendes Buch");
												} catch (Exception e) {
													gui.showError(e);
												}
											}
										});

							}
						});

			}

		});
		table.setMenu(m);
		table.setToolTipText("Los libros seleccionados");

	}

	/**
	 * @param dsvBuecher
	 */
	@SuppressWarnings("unused")
	private void asyncLoadData() {
		gui.xs.execute(new Runnable() {
			@Override
			public void run() {
				loadData();
			}

		});
	}

	@Override
	public void f5() {
		syncLoadData();
		modelChanged();

	}

	@Override
	public void fill(HashMap<StandardMenus, Menu> hm) {
		Menu viewMenu = hm.get(StandardMenus.VIEW);
		final Menu specialMenu = hm.get(StandardMenus.SPECIAL);

		try {
			final MenuItem editAll = new MenuItem(specialMenu, SWT.PUSH);
			editAll.setText("Alle Preise bearbeiten");
			editAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Shell s2 = new Shell(gui.getShell(), SWT.RESIZE | SWT.CLOSE
							| /* SWT.V_SCROLL | */SWT.APPLICATION_MODAL);
					s2.setLayout(new FillLayout());
					MassivePriceChanger a = new MassivePriceChanger(s2,
							buecher, ds, gui);
					s2.open();
					boolean mustsave = a.show();
					if (mustsave)
						loadData();
				}
			});
			Menu filterMenu = new Menu(viewMenu);
			{
				new MenuItem(filterMenu, SWT.SEPARATOR);
				final MenuItem filterAll = new MenuItem(filterMenu, SWT.CASCADE);
				filterAll.setText("Alle");
				filterAll.setData(null);
				filterAll.addSelectionListener(filterSelectionAdapter);
			}
			RauUtil.createMenuItem(viewMenu, SWT.PUSH,
					"Nach Verlag + Titel sortieren", new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							sortBy = "verlagname, titel";
							syncLoadData();
						}
					});
			RauUtil.createMenuItem(viewMenu, SWT.PUSH,
					"Nach niedrigster Klasse + Fach sortieren",
					new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							sortBy = "minjahr, fach ";
							syncLoadData();
						}
					});

			final MenuItem m4 = new MenuItem(viewMenu, SWT.CASCADE);
			m4.setText("Filtern");
			m4.setMenu(filterMenu);
			{
				final MenuItem filterByVerlag = new MenuItem(filterMenu,
						SWT.CASCADE);
				Menu verlagFilterMenu = new Menu(filterByVerlag);
				filterByVerlag.setMenu(verlagFilterMenu);
				filterByVerlag.setText("Nach Verlag");
				for (Verlag i : ds.verlagVw.getVerlage()) {
					final MenuItem menuItem = new MenuItem(verlagFilterMenu,
							SWT.PUSH);
					menuItem.setText(i.toString());
					menuItem.setData(i);
					menuItem.addSelectionListener(filterSelectionAdapter);
				}
			}
			{
				final MenuItem filterByVerlag = new MenuItem(filterMenu,
						SWT.CASCADE);
				Menu verlagFilterMenu = new Menu(filterByVerlag);
				filterByVerlag.setMenu(verlagFilterMenu);
				filterByVerlag.setText("Nach Verlagsland");
				for (Entry<String, String> i : Verlag.hm.entrySet()) {
					final MenuItem menuItem = new MenuItem(verlagFilterMenu,
							SWT.PUSH);
					menuItem.setText(i.getValue());
					menuItem.setData(new VerlagNationalitat(i.getKey(), i
							.getValue()));
					menuItem.addSelectionListener(filterSelectionAdapter);
				}
			}

			{
				final MenuItem mi = new MenuItem(filterMenu, SWT.CASCADE);
				Menu m5 = new Menu(mi);
				mi.setMenu(m5);
				mi.setText("Nach Fach");
				for (Fach i : ds.faecherVw.getFaecher()) {
					MenuItem menuItem = new MenuItem(m5, SWT.PUSH);
					menuItem.setData(i);
					menuItem.setText(i.name);
					menuItem.addSelectionListener(filterSelectionAdapter);
				}
			}
			{
				final MenuItem mi = new MenuItem(filterMenu, SWT.CASCADE);
				Menu m5 = new Menu(mi);
				mi.setMenu(m5);
				mi.setText("Nach Fach und Schulstufe");

				for (Fach i : ds.faecherVw.getFaecher()) {
					HashSet<Integer> hm2 = new HashSet<Integer>();
					MenuItem menuItem = new MenuItem(m5, SWT.CASCADE);
					Menu mm = new Menu(menuItem);
					menuItem.setMenu(mm);
					menuItem.setText(i.name);
					for (Klasse j : ds.klassenVw.getKlassen("jahr")) {
						if (!hm2.contains(j.jahr)) {
							hm2.add(j.jahr);
							MenuItem menuItem2 = new MenuItem(mm, SWT.PUSH);
							menuItem2.setData(new FachJahrFilterData(i.name,
									j.jahr));
							menuItem2.setText("" + j.jahr);
							menuItem2
									.addSelectionListener(filterSelectionAdapter);
						}
					}
				}
			}

			{
				final MenuItem mi = new MenuItem(filterMenu, SWT.CASCADE);
				Menu m5 = new Menu(mi);
				mi.setMenu(m5);
				mi.setText("Nach Schulstufe");
				HashSet<Integer> set = new HashSet<Integer>();
				for (Klasse i : ds.klassenVw.getKlassen("jahr")) {
					if (!set.contains(i.jahr)) {
						set.add(i.jahr);
						MenuItem menuItem = new MenuItem(m5, SWT.PUSH);
						menuItem.setData(new Year(i.jahr));
						menuItem.setText("" + i.jahr);
						menuItem.addSelectionListener(filterSelectionAdapter);
					}
				}
			}
			{
				final MenuItem mi = new MenuItem(filterMenu, SWT.CASCADE);
				Menu m5 = new Menu(mi);
				mi.setMenu(m5);
				mi.setText("Nach Klasse");
				for (Klasse i : ds.klassenVw.getKlassen("jahr")) {
					MenuItem menuItem = new MenuItem(m5, SWT.PUSH);
					menuItem.setData(i);
					menuItem.setText(i.name);
					menuItem.addSelectionListener(filterSelectionAdapter);
				}
			}
			{
				final MenuItem mi = new MenuItem(filterMenu, SWT.CASCADE);
				Menu m5 = new Menu(mi);
				mi.setMenu(m5);
				mi.setText("Nach Bemerkung");
				for (BuchBemerkung i : ds.getBuchMarken()) {
					MenuItem menuItem = new MenuItem(m5, SWT.PUSH);
					menuItem.setData(i);
					menuItem.setText(i.getDeutschDsc());
					menuItem.addSelectionListener(filterSelectionAdapter);
				}
			}

			RauUtil.createMenuItem(specialMenu, SWT.PUSH,
					"&Poner a cero los precios", new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							try {

								MessageBox messageBox = new MessageBox(
										specialMenu.getShell(),
										SWT.ICON_QUESTION | SWT.YES | SWT.NO);
								messageBox
										.setMessage("Möchten Sie alle Preise auf Null setzen? (Dieser Vorgang kann nicht rückgängig gemacht werden)");
								messageBox
										.setText("Poner los precios a cero (!)");
								int response = messageBox.open();
								if (response == SWT.YES) {
									int n = ds.nullPrices();
									MessageBox messageBox2 = new MessageBox(
											specialMenu.getShell(), SWT.OK);
									messageBox2
											.setMessage("Die Preise wurden auf Null in "
													+ n + " Büchern gesetzt");
									messageBox2
											.setText("Preiseänderung auf Null");
									messageBox2.open();
								}
							} catch (SQLException e1) {
								gui.showError(e1);
							}

						}
					});

			// if (false)
			// new Adapter(specialMenu, "&Puesta a cero del stock", SWT.PUSH) {
			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// try {
			//
			// MessageBox messageBox = new MessageBox(specialMenu
			// .getShell(), SWT.ICON_QUESTION | SWT.YES
			// | SWT.NO);
			// messageBox
			// .setMessage("¿Está seguro de querer poner el stock actual a cero? Se perderá la información previa. Podrá volver a editarlo entrando libro a libro en la pestaña Libros.");
			// messageBox.setText("Poner el stock a cero");
			// int response = messageBox.open();
			// if (response == SWT.YES) {
			// int n = ds.stockNull();
			// MessageBox messageBox2 = new MessageBox(
			// specialMenu.getShell(), SWT.OK);
			// messageBox2
			// .setMessage("Se ha puesto correctamente el stock a cero en "
			// + n + " libros.");
			// messageBox2.setText("Stock puesto a cero");
			// messageBox2.open();
			// }
			// } catch (SQLException e1) {
			// gui.showError(e1);
			// }
			//
			// }
			// };

		} catch (SQLException e) {
			gui.showError(e);
		}

	}

	/**
	 * @param b
	 * @return
	 */
	private boolean filterAccepts(Buch b) {
		// System.err.println("land: " + b.verlag.land);
		return filter == null || filter instanceof Verlag
				&& ((Verlag) filter).name.equals(b.verlag.name)
				|| filter instanceof Fach
				&& ((Fach) filter).name.equals(b.fach)
				|| filter instanceof Klasse && b.klassen.contains(filter)
				|| filter instanceof Year
				&& in(((Year) filter).jahr, b.klassen)
				|| filter instanceof FachJahrFilterData
				&& in(((FachJahrFilterData) filter).jahr, b.klassen)
				&& b.fach.equals(((FachJahrFilterData) filter).fach)
				|| filter instanceof BuchBemerkung
				&& b.bemerkungen.contains(((BuchBemerkung) filter).id)
				|| filter instanceof VerlagNationalitat
				&& b.verlag.land.equals(((VerlagNationalitat) filter).name);
	}

	private boolean in(int jahr, ArrayList<Klasse> klassen) {
		for (Klasse k : klassen)
			if (k.jahr == jahr)
				return true;
		return false;
	}

	/**
	 */
	private void loadData() {
		try {
			synchronized (buecher) {
				buecher.clear();
				for (Buch b : gui.ds.getBuecher(sortBy))
					if (filterAccepts(b))
						buecher.add(b);
			}
			gui.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					final String filterDesc = filter == null ? "-" : filter
							.getClass().getName().replaceAll("^.*\\.", "")
							+ "=" + filter;
					table.setToolTipText("Bücheranzahl: " + buecher.size()
							+ "\n" + "Filter: " + filterDesc);
					table.setItemCount(buecher.size());
					table.clearAll();
				}
			});
		} catch (SQLException e) {
			gui.showError(e);
		}
	}

	/**
	 * @param dsvBuecher
	 * @param composite
	 * @param g
	 *            TODO
	 * @throws SQLException
	 */
	private void makeAbteilungBlock(final DsvBuecher dsvBuecher,
			Composite composite, final BuchInfoGroup g) {
		new Group(composite, 0) {
			{
				setText("Klassen");
				setLayout(new GridLayout(2, false));
				for (final Abteilung a : abteilungen) {
					final Label l1 = new Label(this, 0);
					l1.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false,
							false, 1, 1));
					l1.setText(a.name);

					new Composite(this, SWT.PUSH) {
						{
							setLayoutData(new GridData(SWT.NONE, SWT.NONE,
									false, false, 1, 1));
							setLayout(new RowLayout());

							for (final Klasse k : a.klassen) {
								Button bb = new Button(this, SWT.CHECK);
								g.buttons.put(k.name, bb);
								bb.setText(k.name);
								bb.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) {

									}
								});
							}
						}

						@Override
						protected void checkSubclass() {
						}
					};
				}
			}

			@Override
			protected void checkSubclass() {
			}
		};
	}

	@Override
	public void modelChanged() {
		g.modelChanged();
	}

	@Override
	public void onFocus() {
		System.err.println("miran libros");
		table.deselectAll();
		try {
			g.showProperties(new Buch());
		} catch (SQLException e) {
			gui.showError(e);
		}

	}

	/**
	 * @throws SQLException
	 */
	private void populateFachCombo(Combo fachCombo) throws SQLException {
		String current = fachCombo.getText();
		fachCombo.removeAll();
		fachCombo.add("");
		for (Fach i : ds.faecherVw.getFaecher())
			fachCombo.add(i.name);
		fachCombo.setText(current);
	}

	/**
	 * 
	 */
	private void populateSpracheCombo(Combo spracheCombo) {
		String current = spracheCombo.getText();
		spracheCombo.removeAll();
		spracheCombo.add("");
		spracheCombo.setData(ds.sprachVw.langByName);
		for (Language lang : ds.sprachVw.langByName.values())
			spracheCombo.add(lang.name);

		spracheCombo.setText(current);
	}

	/**
	 * @param verlagCombo
	 * @throws SQLException
	 * 
	 */
	private void populateVerlagCombo(Combo verlagCombo) throws SQLException {
		String current = verlagCombo.getText();
		verlagCombo.removeAll();
		verlagCombo.add("");
		verlage.clear();

		for (Verlag v : ds.verlagVw.getVerlage()) {
			verlagCombo.add(v.name);
			verlage.add(v);
		}
		verlagCombo.setText(current);

	}

	protected void setButtonState() {
		if (delButton != null)
			delButton.setEnabled(table.getSelectionCount() != 0);

	}

	/**
	 * @param filter1
	 */
	private void setFilter(Object filter1) {
		filter = filter1;
		syncLoadData();
	}

	private void syncLoadData() {
		BusyIndicator.showWhile(gui.display, new Runnable() {
			@Override
			public void run() {
				loadData();
			}

		});
	}

}
