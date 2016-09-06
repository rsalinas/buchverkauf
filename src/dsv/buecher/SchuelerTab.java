package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

public class SchuelerTab extends TabFolderHolder {
	protected Button cancelButton;
	protected Schueler current;
	protected Combo klasseCombo;
	private ArrayList<Klasse> ll;
	protected Text nameText;
	protected Button newButton;
	protected Button saveButton;
	private SchulerIncRequest schueler;
	private Table table = null;

	protected Text vorNameText;
	protected Text emailText;
	protected MenuItem delButton;
	protected MenuItem allCommentButton;
	private String currentKlasse = "___";
	private String orderBy = "name,vorname";
	protected String[] orders = { "schuelernum", "name,vorname",
			"vorname,name,klasse", "klasse,name,vorname" };
	protected boolean expert = false;
	protected Label schuelerNumLabel;
	private Combo klassen;
	private final ArrayList<Klasse> klasseList;

	@Override
	public void f5() {
		System.err.println("schuelertab.f5()");
		if (klassen == null)
			return;
		klassen.removeAll();
		klasseCombo.removeAll();
		try {
			klasseCombo.add("");
			klassen.add(allKlassen.name);
			for (Klasse k : gui.ds.klassenVw.getKlassen("jahr,Klasse")) {
				klassen.add(k.name);
				System.err.println("añadido: " + k);
				klasseCombo.add(k.name + " (" + k.abteilung + ")");

			}

		} catch (SQLException e) {
			gui.showError(e);
		}
	}

	final Klasse allKlassen = new Klasse("All");
	protected Text bemerkungenDeuText;
	protected Text bemerkungenSpaText;

	public SchuelerTab(final TabFolder tabFolder, final DsvBuecher dsvBuecher)
			throws SQLException {
		super(tabFolder, "&Schüler", dsvBuecher);
		klasseList = dsvBuecher.ds.klassenVw.getKlassen("jahr,Klasse");
		final Composite composite = new Composite(tabFolder, SWT.PUSH);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData());
		new Composite(composite, 0) {
			{
				setLayout(new RowLayout());
				new Label(this, 0).setText("Klasse");
				klassen = new Combo(this, SWT.READ_ONLY);

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
						asyncLoadData();
					}
				});
				schuelerNumLabel = new Label(this, SWT.NONE);
				schuelerNumLabel.setLayoutData(new RowData());

				schuelerNumLabel.setText("(no data)");
			}
		};
		composite.pack();
		// schuelerNumLabel.setSize(100, schuelerNumLabel.getSize().y);

		item.setControl(composite);

		table = new Table(composite, SWT.FULL_SELECTION | SWT.VIRTUAL
				| SWT.MULTI);

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 640, 320);
		// table.setFont(font);
		table.setLinesVisible(true);

		// Table virtualTable = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION
		// | SWT.VIRTUAL | SWT.BORDER);
		table.setHeaderVisible(true);
		// {
		// TableColumn column = new TableColumn(table, SWT.RIGHT);
		// column.setText("My Virtual Table");
		// column.setWidth(480);
		// }
		int width = 0;

		String[] headers = { "#", "Nachname", "Vorname", "Klasse",
				"Bemerkung (Deu)" };
		for (String header : headers) {
			width = 30;
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth((width * 8));
			if (header.equals("#"))
				column.setWidth(7 * 8); // TODO: usar el nuevo sistema de
			// cabeceras
			else if (header.equals("Klasse"))
				column.setWidth(10 * 8); // TODO: usar el nuevo sistema de
			// cabeceras
			// guess
			column.setText(header);
		}

		int pos = 0;
		for (final TableColumn c : table.getColumns()) {
			final int finalPos = pos++;
			c.setMoveable(true);
			c.addSelectionListener(new SelectionAdapter() {
				final int thisPos = finalPos;

				@Override
				public void widgetSelected(SelectionEvent e) {
					orderBy = orders[thisPos];
					table.setSortColumn(c);
					asyncLoadData();
				}

			});
		}
		asyncLoadData();
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() != 1) {
					nameText.setText("");
					vorNameText.setText("");
					if (emailText != null)
						emailText.setText("");
					klasseCombo.setText("");
					bemerkungenDeuText.setText("");
					bemerkungenSpaText.setText("");
					return;
				}
				current = (Schueler) table.getSelection()[0].getData();
				if (current == null)
					return;
				beanToVisual();
			}
		});
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				try {
					Schueler sch = schueler.get(event.index);
					if (sch != null) {
						item.setText(sch.toStringArray());
						item.setData(sch);
					}
				} catch (SQLException e) {
					dsvBuecher.showError(e);
				}
			}

		});
		new Group(composite, SWT.None) {
			{
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
						1));
				setLayout(new GridLayout(2, false));
				setText("Details");
				new Label(this, SWT.NONE).setText("Name");
				nameText = new Text(this, SWT.NONE);
				new Label(this, SWT.NONE).setText("Vorname");
				vorNameText = new Text(this, SWT.NONE);
				vorNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, false, 1, 1));
				nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false, 1, 1));

				new Label(this, SWT.NONE).setText("Klasse");
				klasseCombo = new Combo(this, SWT.READ_ONLY);
				klasseCombo.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						try {
							if (dsvBuecher.ds.getBuchBestellungen(current) != 0)
								dsvBuecher
										.showWarning("Vorsicht: Wenn Sie die Klasse eines Schülers welcheln, wird die Bestellung gelöscht.  Wenn Sie nun den Knopf 'Speichern' drücken, kann der Vorgang nicht rückgängig gemacht werden. ");
						} catch (SQLException e) {
							dsvBuecher.showError(e);
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						// TODO Auto-generated method stub

					}

				});
				klasseCombo.setSize(200, 200);

				klasseCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						false, false, 1, 1));
				ll = dsvBuecher.ds.klassenVw.getKlassen("jahr,Klasse");
				for (Klasse k : ll)
					klasseCombo.add(k.name + " (" + k.abteilung + ")");
				klasseCombo.add("");

				new Label(this, SWT.NONE).setText("Bemerkungen (Deu):");
				bemerkungenDeuText = new Text(this, SWT.NONE);
				bemerkungenDeuText.setLayoutData(new GridData(SWT.FILL,
						SWT.FILL, true, false, 1, 1));

				new Label(this, SWT.NONE).setText("Bemerkungen (Spa):");
				bemerkungenSpaText = new Text(this, SWT.NONE);
				bemerkungenSpaText.setLayoutData(new GridData(SWT.FILL,
						SWT.FILL, true, false, 1, 1));

				new Composite(this, SWT.RIGHT) {
					{
						setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
								false, 1, 2));

						setLayout(new RowLayout());
						saveButton = new Button(this, SWT.PUSH);
						saveButton.setText("&Speichern");
						saveButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									visualToBean();
									if (current.klasse == null)
										throw new SQLException("Klasse fehlt");
									if (current.name == null
											|| current.name.length() == 0)
										throw new SQLException("Nachname fehlt");
									if (current.vorName == null
											|| current.vorName.length() == 0)
										throw new SQLException("Vorname fehlt");

									String msg = dsvBuecher.ds.schuelerVw
											.saveSchueler(current);

									dsvBuecher.modelChanged();
									if (msg != null) {
										dsvBuecher.showWarning(msg);
									}
									if (current.num == -1) {
										nameText.setText("");
										vorNameText.setText("");
									}

								} catch (SQLException e1) {
									dsvBuecher.showError(e1);
								}
								asyncLoadData();
							}

						});
						// cancelButton = new Button(this, SWT.PUSH);
						// cancelButton.setText("Cancel");
						// cancelButton
						// .addSelectionListener(new SelectionAdapter() {
						// @Override
						// public void widgetSelected(SelectionEvent e) {
						// // TODO Auto-generated method stub
						// super.widgetSelected(e);
						// }
						// });
						newButton = new Button(this, SWT.PUSH);
						newButton.setText("Neu");
						newButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								current = new Schueler();
								current.klasse = currentKlasse;
								beanToVisual();

							}
						});
					}
				};

			}

			@Override
			protected void checkSubclass() {
			}
		};

		final Menu m = new Menu(table);
		m.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.err.println(event);
				MenuItem[] menuItems = m.getItems();
				for (MenuItem menuItem : menuItems) {
					menuItem.dispose();
				}
				TableItem[] selection = table.getSelection();
				if (selection.length == 0)
					return;

				if (expert) {
					final MenuItem bestellungBearbeitenOpt = new MenuItem(m,
							SWT.PUSH);
					bestellungBearbeitenOpt.setText("Bestellung bearbeiten");
					bestellungBearbeitenOpt
							.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										BestellungMarkForm.showMarkingForm(
												(Schueler) table.getSelection()[0]
														.getData(), gui);
									} catch (SQLException e1) {
										dsvBuecher.showError(e1);
									}
								}
							});
					final MenuItem zeigeRechnungButton = new MenuItem(m,
							SWT.PUSH);
					zeigeRechnungButton.setText("Rechnung zeigen");
					zeigeRechnungButton
							.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										ArrayList<Schueler> l = new ArrayList<Schueler>();
										for (TableItem i : table.getSelection())
											l.add(((Schueler) i.getData()));
										RechnungForm.show(
												dsvBuecher.getShell(), l,
												dsvBuecher);
									} catch (SQLException e1) {
										dsvBuecher.showError(e1);
									}
								}
							});
					new MenuItem(m, SWT.SEPARATOR);
				}

				RauUtil.createMenuItem(m, SWT.PUSH,
						"Bemerkung markierten Schülern zuordnen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								setCommentSelected();

							}
						});
				RauUtil.createMenuItem(m, SWT.PUSH, "Löschen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								deleteSelected(dsvBuecher, composite);

							}
						});

			}

		});

		table.setMenu(m);

		RauUtil.createMenuItem(m, SWT.PUSH, "Löschen", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				deleteSelected(dsvBuecher, composite);

			}
		});

		table.setMenu(m);

	}

	/**
	 * 
	 */
	private void beanToVisual() {
		nameText.setText(current.name);
		vorNameText.setText(current.vorName);
		if (emailText != null)
			emailText.setText(current.email);
		if (current.klasse == null || current.klasse.isEmpty())
			klasseCombo.setText("");
		else
			klasseCombo.select(Klasse.getPos(current.klasse, ll));
		nameText.setFocus();
		bemerkungenDeuText.setText(current.bemerkDeu);
		bemerkungenSpaText.setText(current.bemerkSpa);
	}

	/**
	 * @param dsvBuecher
	 * @throws SQLException
	 */
	private void loadData() throws SQLException {
		if (schueler != null)
			schueler.close();
		schueler = gui.ds.getSchuelerInc(currentKlasse, orderBy);
		gui.getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				table.setItemCount(schueler.size());
				table.clearAll();
				schuelerNumLabel.setText("(" + schueler.size() + " Schüler)");
				schuelerNumLabel.getParent().pack();
			}
		});

	}

	private void asyncLoadData() {
		final Runnable asyncDataLoader = new Runnable() {
			@Override
			public void run() {
				try {
					loadData();
				} catch (SQLException e) {
					gui.showError(e);
				}

			}

		};
		gui.xs.execute(asyncDataLoader);
	}

	private void visualToBean() {
		current.name = nameText.getText();
		current.vorName = vorNameText.getText();
		if (emailText != null)
			current.email = emailText.getText();
		System.err.println(klasseCombo.getSelectionIndex());
		System.err.println(ll.size());
		if (klasseCombo.getSelectionIndex() >= 0
				&& klasseCombo.getSelectionIndex() < ll.size())
			current.klasse = ll.get(klasseCombo.getSelectionIndex()).name;
		else
			current.klasse = null;

		current.bemerkDeu = bemerkungenDeuText.getText();
		current.bemerkSpa = bemerkungenSpaText.getText();
	}

	public void filterInClass(String name) {
		currentKlasse = name;
		asyncLoadData();
	}

	@Override
	public void fill(HashMap<StandardMenus, Menu> hm) {
		{
			if (gui.pm.can(DsvbPermission.SUPER)) {
				MenuItem schuelerImport = new MenuItem(
						hm.get(StandardMenus.SPECIAL), SWT.PUSH);
				schuelerImport.setText("Schüler importieren");
				schuelerImport.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							new StudentImportForm(gui);
						} catch (SQLException e1) {
							gui.showError(e1);
						}
					}
				});
			}
		}
	}

	@Override
	public void onFocus() {
		table.deselectAll();
		current = new Schueler();
		beanToVisual();

	}

	private void setCommentSelected() {
		final Shell shell2 = new Shell(Display.getCurrent());
		Composite shell = new Composite(shell2, SWT.NONE);
		shell2.setLayout(new FillLayout());
		Label label = new Label(shell, SWT.NONE | SWT.BORDER);
		label.setText("Deutsch:");
		final Text textDeu = new Text(shell, SWT.NONE);
		Label labelSpa = new Label(shell, SWT.NONE | SWT.BORDER);
		labelSpa.setText("Spanisch:");
		final Text textSpa = new Text(shell, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			// layout.marginWidth = layout.marginHeight = 5;
			shell.setLayout(layout);
		}

		textDeu.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		textSpa.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Composite botonera = new Composite(shell, SWT.NONE);
		botonera.setLayout(new RowLayout(SWT.HORIZONTAL));
		RauUtil.createButton(botonera, "Speichern", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					for (TableItem ti : table.getSelection()) {

						Schueler sc = (Schueler) ti.getData();
						sc.bemerkDeu = textDeu.getText();
						sc.bemerkSpa = textSpa.getText();
						gui.ds.schuelerVw.setBemerkung(sc);
					}
					shell2.dispose();
					asyncLoadData();
				} catch (SQLException e) {
					gui.showError(e);
				}
			}
		});
		RauUtil.createButton(botonera, "Abbrechen", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell2.dispose();
			}
		});
		shell2.pack();
		shell2.setSize(600, shell2.getSize().y);
		shell2.open();
		while (!shell.isDisposed()) {
			if (!Display.getCurrent().readAndDispatch())
				Display.getCurrent().sleep();
		}
	}

	private void deleteSelected(final DsvBuecher dsvBuecher,
			final Composite composite) {
		TableItem[] sel = table.getSelection();
		if (table.getSelectionCount() == 0)
			return;
		if (table.getSelectionCount() > 1) {
			MessageBox mb = new MessageBox(composite.getShell(), SWT.OK);
			mb.setText("Fehler");
			mb.setMessage("Es kann nur ein Schüler zum löschen gewählt werden.");
			mb.open();
			return;
		}
		try {
			if (dsvBuecher.ds
					.schuelerHasRequests(((Schueler) sel[0].getData()))) {
				MessageBox mb = new MessageBox(composite.getShell(), SWT.YES
						| SWT.NO);
				mb.setText("Schüler löschen");
				mb.setMessage("Sind Sie sicher, dass Sie den gewählten Schüler löschen wollen?\n\nWARNUNG: der Schüler hat eine Bestellung");

				int res = mb.open();
				if (res != SWT.YES)
					return;
			} else if (dsvBuecher.ds.schuelerHasBill(((Schueler) sel[0]
					.getData()).num)) {
				MessageBox mb = new MessageBox(composite.getShell(), SWT.OK);
				mb.setText("Fehler");
				mb.setMessage("Der Schüler kann nicht gelöscht werden, da Rechnung(en) schon erstellt.");
				mb.open();
				return;

			} else {
				MessageBox mb = new MessageBox(composite.getShell(), SWT.YES
						| SWT.NO);
				mb.setText("Schüler löschen");
				mb.setMessage("Sind Sie sicher, dass Sie den gewählten Schüler löschen wollen?");

				int res = mb.open();
				if (res != SWT.YES)
					return;
			}
			for (TableItem ti : sel)
				dsvBuecher.ds.schuelerVw.removeSchueler(((Schueler) ti
						.getData()).num);
			loadData();
		} catch (SQLException e1) {
			dsvBuecher.showError(e1);
		}
	}
}
