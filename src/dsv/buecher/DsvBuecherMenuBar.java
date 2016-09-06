package dsv.buecher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import dsv.buecher.report.ReportAlumnosPendientes;
import dsv.buecher.report.ReportAlumnosPendientesComprar;
import dsv.buecher.report.ReportBestellteBuecher;
import dsv.buecher.report.ReportInventarioLibros;
import dsv.buecher.report.ReportMarcaje;
import dsv.buecher.report.ReportVerlagBestellung2;
import dsv.buecher.report.ReportVerlagBestellungFT;
import dsv.buecher.report.ReportVerlagBestellungKompakt;
import dsv.buecher.report.ReportVerlagBestellungKompaktResidual;
import dsv.buecher.report.ReportVerlagBestellungKompaktReturned;
import dsv.buecher.report.ReportKasseNachKlassen;

public class DsvBuecherMenuBar {
	protected static final boolean showBenutzer = false;
	private final HashMap<StandardMenus, MenuConfigurer> bhm = new HashMap<StandardMenus, MenuConfigurer>();
	private final HashMap<StandardMenus, Menu> hm = new HashMap<StandardMenus, Menu>();
	private final MenuConfigurer makeFileMenu = new MenuConfigurer() {

		@Override
		public void fill(Menu filemenu) {

			// /* final MenuItem separator = */new MenuItem(filemenu,
			// SWT.SEPARATOR);
			// final MenuItem prefItem = new MenuItem(filemenu, SWT.PUSH);
			// prefItem.setText("&Einstellungen\tCTRL+P");
			// prefItem.setAccelerator(SWT.CTRL + 'P');

			/* final MenuItem separator2 = */new MenuItem(filemenu,
					SWT.SEPARATOR);
			final MenuItem exitItem = new MenuItem(filemenu, SWT.PUSH);
			exitItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MessageBox messageBox = new MessageBox(parent.getShell(),
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setMessage("Anwendung verlassen?");
					messageBox.setText("Bestätigung");
					int response = messageBox.open();
					if (response == SWT.YES)
						System.exit(0);
				}
			});
			exitItem.setText("&Quit\tCTRL+Q");
			exitItem.setAccelerator(SWT.CTRL + 'Q');

		}

	};
	/**
	 * @param shell
	 * @param m
	 */

	MenuConfigurer makeHelpMenu = new MenuConfigurer() {
		public void fill(Menu helpmenu) {
			MenuItem about = new MenuItem(helpmenu, SWT.PUSH);
			about.setText("&À propos");
			about.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MessageBox messageBox = new MessageBox(parent.getShell(),
							SWT.ICON_QUESTION | SWT.OK);
					messageBox
							.setMessage("Geschrieben von Raúl Salinas Monteagudo");
					messageBox.setText("About");
					messageBox.open();
				}
			});
		};
	};
	private final MenuConfigurer makeListingMenu = new MenuConfigurer() {
		public void fill(Menu rootmenu) {
			{
				MenuItem marcajeMI = new MenuItem(rootmenu, SWT.CASCADE);
				Menu marcaje = new Menu(rootmenu);
				marcajeMI.setText("&Inventarlisten");
				marcajeMI.setMenu(marcaje);

				{
					MenuItem kompaktButton = new MenuItem(marcaje, SWT.PUSH);
					kompaktButton
							.setText("Extended (nach Fach+Titel sortiert)");
					kompaktButton.setEnabled(false);
					kompaktButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							BusyIndicator.showWhile(Display.getCurrent(),
									new Runnable() {
										public void run() {
											try {
												new ReportVerlagBestellungFT(
														parent);
											} catch (Exception e) {
												parent.showError(e);
											}
										}
									});

						}
					});
				}

				RauUtil.createMenuItem(marcaje, SWT.PUSH,
						"&Extended (nach Verlag+Titel sortiert)",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportVerlagBestellung2(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						}).setEnabled(false);

				MenuItem kompaktButton = new MenuItem(marcaje, SWT.PUSH);
				kompaktButton.setText("K&ompakt");
				kompaktButton.setEnabled(false);
				kompaktButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						BusyIndicator.showWhile(Display.getCurrent(),
								new Runnable() {
									public void run() {
										try {
											new ReportVerlagBestellungKompakt(
													parent);
										} catch (Exception e) {
											parent.showError(e);
										}
									}
								});

					}
				});

				RauUtil.createMenuItem(marcaje, SWT.PUSH,
						"Bücher im Lager (Bestand)", new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportVerlagBestellungKompaktResidual(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});
				RauUtil.createMenuItem(marcaje, SWT.PUSH,
						"Zurückgegebene Bücher (übrig)",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								try {
									if (parent.ds.getBuchBestellungen() == 0) {
										MessageBox mb = new MessageBox(parent
												.getShell(), SWT.OK | SWT.ERROR);
										mb.setText("Bericht in dieser Phase nicht erstellbar");
										mb.setMessage("Dieser Bericht ist in dieser Phase (= 'Bestellprozess noch nicht ausgeführt') nicht erstellbar.");
										mb.open();
										return;
									}
								} catch (SQLException e1) {
									parent.showError(e1);
								}

								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportVerlagBestellungKompaktReturned(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});
				RauUtil.createMenuItem(marcaje, SWT.PUSH,
						"Preisliste (alle Klassen)", new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													ArrayList<String> l = new ArrayList<String>();
													for (Klasse k : parent.ds.klassenVw
															.getKlassen("jahr, klasse"))
														l.add(k.name);
													ReportMarcaje.dale(l,
															parent, true);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});
				RauUtil.createMenuItem(rootmenu, SWT.PUSH,
						"Bestellliste (statisch)", new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								// FIXME check
								// parent.ds.get
								try {
									if (parent.ds.getBuchBestellungen() == 0) {
										MessageBox mb = new MessageBox(parent
												.getShell(), SWT.OK | SWT.ERROR);
										mb.setText("Informe no disponible");
										mb.setMessage("No se puede generar la lista de pedido sin haber cerrado la fase de introducción de pedidos. Puede obtener una lista con los pedidos HASTA EL MOMENTO en Reports/Inventarlisten/Bücherinventar.");
										mb.open();
										return;
									}
								} catch (SQLException e1) {
									parent.showError(e1);
								}
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new BestelllisteStaticized(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});
				RauUtil.createMenuItem(rootmenu, SWT.PUSH,
						"&Schüler ohne Bestellung", new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportAlumnosPendientes(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});
				RauUtil.createMenuItem(rootmenu, SWT.PUSH,
						"&Schüler, nicht gekauft", new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportAlumnosPendientesComprar(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});

				
				
				RauUtil.createMenuItem(marcaje, SWT.PUSH, "&Bücherinventar",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								BusyIndicator.showWhile(Display.getCurrent(),
										new Runnable() {
											public void run() {
												try {
													new ReportInventarioLibros(
															parent);
												} catch (Exception e) {
													parent.showError(e);
												}
											}
										});

							}
						});
				MenuItem kontroleEingabe = new MenuItem(rootmenu, SWT.CASCADE);
				Menu eingabemenu = new Menu(kontroleEingabe);
				kontroleEingabe.setMenu(eingabemenu);
				kontroleEingabe.setText("Kontrolle Dateneingabe PC");
				RauUtil.createMenuItem(eingabemenu, SWT.PUSH,
						"Ohne Leerzeilen", new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								try {
									new KontrolleDateneingabeSpaced(parent,
											false);
								} catch (Exception e) {
									parent.showError(e);
								}
							}
						});
				RauUtil.createMenuItem(eingabemenu, SWT.PUSH, "Mit Leerzeilen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								try {
									new KontrolleDateneingabeSpaced(parent,
											true);
								} catch (Exception e) {
									parent.showError(e);
								}
							}
						});
				RauUtil.createMenuItem(eingabemenu, SWT.PUSH,
						"Mit individuellen Bemerkungen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								try {
									new KontrolleDateneingabeIndivBemerk(
											parent, false);
								} catch (Exception e) {
									parent.showError(e);
								}
							}
						});

			}
			RauUtil.createMenuItem(rootmenu, SWT.PUSH,
					"&Bestellungen von allen Schülern", new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							BusyIndicator.showWhile(Display.getCurrent(),
									new Runnable() {
										public void run() {
											try {

												ReportBestellteBuecher
														.dale(parent);
											} catch (Exception e) {
												parent.showError(e);
											}
										}
									});

						}
					});

		}
	};

	class Adapter extends SelectionAdapter {
		public Adapter(Menu m, String t, int swtType) {
			MenuItem about = new MenuItem(m, swtType);
			about.setText(t);
			about.addSelectionListener(this);
		}
	}

	MenuConfigurer makeSpecialMenu = new MenuConfigurer() {
		public void fill(Menu helpmenu) {
			if (parent.pm.can(DsvbPermission.MENU)) {
				if (parent.pm.can(DsvbPermission.SUPER))
					RauUtil.createMenuItem(helpmenu, SWT.PUSH, "SQL-Konsole",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										new SqlConsole(parent.getShell(),
												parent.ds, parent);
									} catch (SQLException e1) {
										parent.showError(e1);
									}
								}
							});
				Menu auxTables = new Menu(helpmenu);
				MenuItem mi2 = new MenuItem(helpmenu, SWT.CASCADE);
				mi2.setText("Hilfstabellen");
				mi2.setMenu(auxTables);
				{
					MenuItem emailDocs = new MenuItem(auxTables, SWT.PUSH);
					emailDocs.setText("&Verlage");
					emailDocs.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								new VerlagManager(parent);
							} catch (SQLException e1) {
								parent.showError(e1);
							}
						}
					});

				}

				{
					MenuItem langEdit = new MenuItem(auxTables, SWT.PUSH);
					langEdit.setText("Sprachen");
					langEdit.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								new LangEditor(parent);
							} catch (SQLException e1) {
								parent.showError(e1);
							}
						}
					});

				}
				if (showBenutzer)
					RauUtil.createMenuItem(auxTables, SWT.PUSH, "Benutzer",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										new UserEditor(parent);
									} catch (SQLException e1) {
										parent.showError(e1);
									}
								}
							});
				RauUtil.createMenuItem(auxTables, SWT.PUSH, "Buchungstyp",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									new BuchungstypEditor(parent);
								} catch (SQLException e1) {
									parent.showError(e1);
								}
							}
						});
	
				RauUtil.createMenuItem(auxTables, SWT.PUSH, "Fächer",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									new FachEditor(parent);
								} catch (SQLException e1) {
									parent.showError(e1);
								}
							}
						});
				new Adapter(auxTables, "Bücherbemerkungen", SWT.PUSH) {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							new BuchMarkeEditor(parent);
						} catch (SQLException e1) {
							parent.showError(e1);
						}
					}
				};
				new Adapter(auxTables, "Abteilungen", SWT.PUSH) {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							new AbteilungenEditor(parent);
						} catch (SQLException e1) {
							parent.showError(e1);
						}
					}
				};
				RauUtil.createMenuItem(auxTables, SWT.PUSH,
						"&Bemerkungen der Schulbuchlisten",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									new MarcajeTextEditor(parent);
								} catch (SQLException e1) {
									parent.showError(e1);
								}
							}
						});
				RauUtil.createMenuItem(
						auxTables,
						SWT.PUSH,
						"&Bemerkung bei den Listen 'Kontrolle Dateneingabe PC'",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									new AvisoProfesEditor(parent);
								} catch (SQLException e1) {
									parent.showError(e1);
								}
							}
						});
				if (parent.pm.can(DsvbPermission.NEWYEARCHANGE))
					new Adapter(helpmenu, "&Neues Schuljahr erstellen",
							SWT.PUSH) {
						@Override
						public void widgetSelected(SelectionEvent e) {
							try {
								ArrayList<Integer> jahre = parent.ds
										.getSchuljahre();
								if (jahre.isEmpty())
									throw new SQLException(
											"Keine Datenbank kann kopiert werden.");

								MessageBox messageBox = new MessageBox(
										parent.getShell(), SWT.ICON_QUESTION
												| SWT.YES | SWT.NO);
								Integer from = jahre.get(0);
								int to = from + 1;
								messageBox
										.setMessage("Möchten Sie einen Schuljahreswechsel auf "
												+ DsvBuecherDatasource
														.getYearName(to)
												+ " vornehmen?");
								messageBox.setText("Bestätigung");
								int response = messageBox.open();
								if (response == SWT.YES) {
									parent.ds.jump(from, to);
									MessageBox messageBox2 = new MessageBox(
											parent.getShell(), SWT.OK);
									messageBox2
											.setMessage("Neues Schuljahr erstellt, mit Daten vom Vorjahr"
													+ "\n\nVorsicht: "
													+ "\n\n Die Schüler wurden nicht kopiert und müssen im neuen Schuljahr importiert werden. "
													+ "\n\n Der Bestand an Büchern vom Vorjahr wurde nicht übertragen und ist im neuen Schuljahr auf Null gesetzt."
													+ "\n\n Die individuellen Buchbemerkungen (Deutsch und Spanisch) wurden nicht ins neue Schuljahr übertragen.");
									messageBox2
											.setText("Schulwechselvorgang erfolgreich");
									messageBox2.open();
									parent.forceShutdown();
								}
							} catch (SQLException e1) {
								parent.showError(e1);
							}
						}
					};

				if (parent.pm.can(DsvbPermission.SUPER)) {
					final MenuItem about = new MenuItem(helpmenu, SWT.CHECK);
					about.setText("Adm-Konto erlaubt");
					about.addSelectionListener(new SelectionAdapter() {
						boolean en;
						{
							try {
								final Properties props = new Properties();
								props.load(new FileInputStream("printer.cfg"));
								en = "true".equals(props
										.getProperty("allow_adm"));
								about.setSelection(en);
								System.err.println("en==" + en);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

						@Override
						public void widgetSelected(SelectionEvent arg0) {
							Properties props = new Properties();
							try {
								props.load(new FileInputStream("printer.cfg"));
								System.err.println("cambian selección: "
										+ about.getSelection());
								en = !en;
								about.setSelection(en);

								props.setProperty("allow_adm", en ? "true"
										: "false");

								FileOutputStream os = new FileOutputStream(
										"printer.cfg");

								props.save(os, "changed");
								os.close();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});

				}
			}
		}
	};

	/**
	 * @param shell
	 * @param m
	 */
	MenuConfigurer makeViewMenu = new MenuConfigurer() {
		public void fill(Menu viewMenu) {
			final MenuItem f5 = new MenuItem(viewMenu, SWT.PUSH);
			f5.setText("&Update\tF5");
			f5.setAccelerator(SWT.F5);
			f5.setSelection(true);
			f5.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					parent.f5();
				}

			});

			// makeLogOption(viewMenu);
			// makeAutoTailMenu(viewMenu);
			// makeLayerSelectorMenu(viewMenu);
		}
	};

	protected final DsvBuecher parent;

	DsvBuecherMenuBar(final Shell shell, final DsvBuecher parent) {
		this.parent = parent;
		final Menu m = new Menu(shell, SWT.BAR);
		shell.setMenuBar(m);
		register(StandardMenus.FILE, makeFileMenu, "&File", m);
		register(StandardMenus.REPORTS, makeListingMenu, "&Reports", m);
		register(StandardMenus.SPECIAL, makeSpecialMenu, "&Special", m);
		register(StandardMenus.VIEW, makeViewMenu, "&View", m);
		register(StandardMenus.HELP, makeHelpMenu, "&Help", m);
	}

	protected void log(String string) {
		System.err.println("log: " + string);
	}

	/**
	 * @param viewMenu
	 */
	@SuppressWarnings("unused")
	private void makeAutoTailMenu(final Menu viewMenu) {
		final MenuItem menuItem = new MenuItem(viewMenu, SWT.CHECK);
		menuItem.setText("Log &follow");
		menuItem.setSelection(true);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// parent.autoTail = menuItem.getSelection();
			}

		});
	}

	private void register(final StandardMenus type,
			final MenuConfigurer configurer, final String title, Menu parentMenu) {
		final MenuItem menuItem = new MenuItem(parentMenu, SWT.CASCADE);
		final Menu menu = new Menu(parent.getShell(), SWT.DROP_DOWN);
		menuItem.setText(title);
		menuItem.setMenu(menu);
		bhm.put(type, configurer);
		hm.put(type, menu);
		menu.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				for (MenuItem i : menu.getItems())
					i.dispose();
				configurer.fill(menu);
				if (parent.tabFolder == null)
					return;

				final Object th = parent.tabFolder.getSelection()[0].getData();

				if (th instanceof TabFolderHolder)
					((TabFolderHolder) th).fill(hm);

			}

		});

	}

}
