package dsv.buecher;

import java.awt.Desktop;
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class XVerkaufForm {
	private final class CompositeExtension extends Composite {
		boolean algunoVendido;
		private final Table table;
		private final Label aviso;
		final HashMap<Integer, TableItem> buttons = new HashMap<Integer, TableItem>();
		final DatosFactura df;

		private final Color green = new Color(getDisplay(), new RGB(0, 255, 0));

		private ArrayList<ChoosableBuch> l;
		private final int pos;

		private final Color red = new Color(getDisplay(), new RGB(255, 0, 0));
		public boolean viewed = false;

		private CompositeExtension(Composite parentComposite, int arg1,
				Schueler schueler, int pos) throws SQLException {
			super(parentComposite, arg1);
			df = new DatosFactura(schueler, new ArrayList<ChoosableBuch>(),
					new ArrayList<ChoosableBuch>(),
					new ArrayList<ChoosableBuch>(), new Date(
							System.currentTimeMillis()));
			this.pos = pos;
			// final Composite s2 = new Composite(tf, 0);
			setLayout(new GridLayout(1, false));
			aviso = new Label(this, SWT.NONE);
			Font initialFont = aviso.getFont();
			FontData[] fontData = initialFont.getFontData();
			for (int i = 0; i < fontData.length; i++)
				fontData[i].setHeight(16);

			Font newFont = new Font(getDisplay(), fontData);
			aviso.setFont(newFont);
			table = new Table(this, SWT.CHECK | SWT.FULL_SELECTION);
			table.setHeaderVisible(true);
			for (final HeaderDef header : headers) {
				final TableColumn column = new TableColumn(table, SWT.NONE);
				column.setWidth(header.width);
				column.setText(header.name);
				if (header.alignment != 0)
					column.setAlignment(header.alignment);
			}

			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
					1));

			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
					if (table.getSelectionCount() == 0)
						return;

					TableItem it = table.getSelection()[0];
					ChoosableBuch cbuch = (ChoosableBuch) it.getData();
					it.setChecked(!it.getChecked());
					if (!checkSelectable(cbuch, it))
						return;
					it.setText(0, cbuch.zustand.getDisplayText(it.getChecked()));
					try {
						procesa(l);
					} catch (Exception e) {
						gui.showError(e);
					}
				}
			});
			Menu m = new Menu(this);
			table.setMenu(m);
			table.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail == SWT.CHECK) {
						ChoosableBuch cbuch = (ChoosableBuch) event.item
								.getData();

						TableItem titem = (TableItem) event.item;
						if (!checkSelectable(cbuch, titem))
							return;
					}

				}
			});
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.item instanceof TableItem) {
						TableItem it = (TableItem) e.item;
						it.setText(0,
								((ChoosableBuch) it.getData()).zustand.s[it
										.getChecked() ? 1 : 0]);
						try {
							procesa(l);
						} catch (Exception e1) {
							gui.showError(e1);
						}
					} else {
						System.err.println("Unexpected type!");
					}
				}
			});
			loadData(gui);
			try {
				procesa(l);
			} catch (Exception e1) {
				gui.showError(e1);
			}

		}

		private boolean checkSelectable(ChoosableBuch cbuch, TableItem titem) {
			if (titem.getChecked() && !cbuch.bestellt
					&& !cbuch.initiallyBestellt
					&& cbuch.zustand != XKaufZustand.VERKAUFT
					&& cbuch.buch.stock - cbuch.buch.reservations <= 0) {
				MessageBox mb = new MessageBox(getShell(), SWT.OK
						| SWT.APPLICATION_MODAL);
				mb.setMessage("Keine Bücherexemplare momentan zur Verfügung");
				mb.setText("Bestellung nicht erlaubt");
				mb.open();
				titem.setChecked(false);
				return false;
			}
			return true;
		}

		/**
		 * @param par
		 * @param schu
		 * @throws SQLException
		 */
		private void loadData(DsvBuecher par) throws SQLException {
			l = par.ds.getBooksForUser2(df.schueler);
			for (TableItem i : table.getItems())
				i.dispose();
			buttons.clear(); // XXX

			algunoVendido = false;
			for (ChoosableBuch d : l) {
				TableItem item = buttons.get(d.buch.code);
				assert item == null;
				item = new TableItem(table, 0);
				buttons.put(d.buch.code, item);

				item.setData(d);
				item.setChecked(d.zustand.active);
				item.setText(DsvBuecher.joinArrays(
						new String[] {
								d.zustand.getDisplayText(item.getChecked()),
								"" + (d.buch.stock - d.buch.reservations) },
						d.toStringArray(gui.ds)));
				if (d.zustand == XKaufZustand.VERKAUFT)
					algunoVendido = true;
			}
			setAlgunoComprado();
		}

		private void setAlgunoComprado() {
			if (algunoVendido) {
				if (s.size() == 1)
					aviso.setText("Rechung(en) schon erstellt.");
				aviso.pack();
				aviso.setVisible(true);
				aviso.setForeground(red);
			} else {
				aviso.setForeground(green);
				// aviso.setText("Keine Rechungen für diesen Schüler erstellt.");
				aviso.setText("");
				aviso.setVisible(false);
				aviso.setSize(0, 0);
			}
		}

		//
		// private void updateStatus(DsvBuecher par) {
		// for (ChoosableBuch d : l) {
		// TableItem item = buttons.get(d.buch.code);
		// assert item != null;
		// updateStatus(d, item);
		// if (d.zustand == XKaufZustand.VERKAUFT)
		// algunocomprado = true;
		// }
		//
		// }
		//
		// private void updateStatus(ChoosableBuch d, TableItem item) {
		// // item.setChecked(d.zustand.active);
		// item.setText(0, d.zustand.getDisplayText(item.getChecked()));
		// }

		private void procesa(ArrayList<ChoosableBuch> wantedBooks)
				throws Exception {
			if (isDisposed())
				return;

			for (ChoosableBuch b : l)
				if (buttons.get(b.buch.code) != null) {
					TableItem butt = buttons.get(b.buch.code);
					if (butt.isDisposed()) {
						throw new Exception("error, button disposed");
					}
					b.bestellt = butt.getChecked();
				}

			df.comprar.clear();
			df.devolver.clear();
			df.rsvCanc.clear();
			BuchAction action;
			sum[pos] = 0;
			for (ChoosableBuch b : wantedBooks) {
				switch (b.zustand) {
				case BESTELLT:
					action = b.bestellt ? BuchAction.COMPRAR
							: BuchAction.RSVCANC;
					break;
				case NIX:
					action = b.bestellt ? BuchAction.COMPRAR : BuchAction.NADA;
					break;
				case DEVUELTO:
					action = b.bestellt ? BuchAction.COMPRAR : BuchAction.NADA;
					break;
				case VERKAUFT:
					action = b.bestellt ? BuchAction.NADA : BuchAction.DEVOLVER;
					break;
				default:
					action = BuchAction.NADA;
				}
				if (action == BuchAction.COMPRAR) {
					df.comprar.add(b);
					sum[pos] += b.buch.preis;
				} else if (action == BuchAction.DEVOLVER) {
					df.devolver.add(b);
					sum[pos] -= b.precioVenta;
				} else if (action == BuchAction.RSVCANC) {
					df.rsvCanc.add(b);
				}
			}
			summecalc();
		}

	}

	private final static HeaderDef[] headers = {
			new HeaderDef("Zustand", null, 15),
			new HeaderDef("Verfüg.", null, 10).setAlignment(SWT.RIGHT),
			new HeaderDef("Titel", "titel", 30),
			new HeaderDef("Verlag", "verlagname"),
			new HeaderDef("Fach", "fach"),
			new HeaderDef("Preis", "preis", 10).setAlignment(SWT.RIGHT),
			new HeaderDef("Bemerkung", null, 25) };

	private final DsvBuecher gui;

	private ArrayList<Schueler> s;
	double[] sum;
	private Text summeText;

	private TabFolder tf;
	double totalsum;

	public XVerkaufForm(DsvBuecher gui) {
		super();
		this.gui = gui;
	}

	private void alleWahlen() {
		for (TableItem i : getCurrentComposite().table.getItems()) {
			i.setChecked(true);
			i.setText(0,
					((ChoosableBuch) i.getData()).zustand.s[i.getChecked() ? 1
							: 0]);
		}

	}

	private CompositeExtension getCurrentComposite() {
		System.err.println("selected: " + tf.getSelectionIndex());
		if (tf.getSelectionIndex() < tf.getItemCount()) {
			assert tf != null;
			TabItem[] items = tf.getItems();
			assert items != null;
			TabItem tabItem = items[tf.getSelectionIndex()];
			assert tabItem != null;
			Control control = tabItem.getControl();
			assert control != null;
			CompositeExtension g = (CompositeExtension) control;
			return g;
		} else
			return null;
	}

	static String parentize(String s) {
		return "(" + s + ")";
	}

	private void checkEverythingWasVisited(final ArrayList<Schueler> s)
			throws SQLException {
		int p = 0;
		for (@SuppressWarnings("unused")
		Schueler schu : s) {
			TabItem tabItem = tf.getItems()[p++];
			final CompositeExtension panel = (CompositeExtension) tabItem
					.getControl();
			if (!panel.viewed) {
				tf.setSelection(p - 1);
				panel.viewed = true;
				tabItem.setText(panel.df.schueler.vorName + " "
						+ parentize(panel.df.schueler.klasse));
				throw new SQLException(
						"Es wurden nicht alle Schüler überprüft.");
			}

		}
	}

	private void keineWahlen() {
		for (TableItem i : getCurrentComposite().table.getItems()) {
			i.setChecked(false);
			i.setText(0,
					((ChoosableBuch) i.getData()).zustand.s[i.getChecked() ? 1
							: 0]);
		}
	}

	private void rechnungErstellen(final DsvBuecher par, final Shell shell,
			final ArrayList<Schueler> s) throws SQLException {
		final Connection conn = gui.ds.getConnection();
		try {
			ArrayList<DatosFactura> dfl = new ArrayList<DatosFactura>();
			int p = 0;

			checkEverythingWasVisited(s);
			for (@SuppressWarnings("unused")
			Schueler schu_unused : s) {
				final CompositeExtension panel = (CompositeExtension) tf
						.getItems()[p++].getControl();
				gui.ds.buchVw.saveVerkauf(conn, panel.df);
				if (!panel.df.comprar.isEmpty() || !panel.df.devolver.isEmpty()
						|| !panel.df.rsvCanc.isEmpty())
					dfl.add(panel.df);
			}
			if (dfl.isEmpty()) {
				MessageBox mb = new MessageBox(shell, SWT.OK);
				mb.setText("Fehler");
				mb.setMessage("Keine Operationen gewählt");
				mb.open();
				return;
			}
			File f = File.createTempFile("Rech-", ".pdf");
			new RechnungPdfMaker().makeit(gui.ds, f.toString(), dfl,
					gui.rtab.copias == 0 ? 1 : gui.rtab.copias);
			if (gui.rtab.copias == 0)
				Desktop.getDesktop().open(f);
			else
				RechnungForm.backgroundPrint(f, gui);

			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO
					| SWT.APPLICATION_MODAL);
			mb.setText("Verkauf bestätigen");

			// Bestätigen Sie
			mb.setMessage("Bestätigen Sie "
					+ (totalsum > 0 ? "die Bezahlung" : "die Rückbezahlung")
					+ " von "
					+ (s.size() == 1 ? "der erstellten Rechnung" : "den "
							+ s.size() + " erstellten Rechnungen") + "?" + ""
					+ "\n\nSumme: " + DsvBuecher.currency(totalsum));
			if (mb.open() == SWT.YES) {
				conn.commit();
				gui.modelChanged();
				for (TabItem schu : tf.getItems()) {
					final CompositeExtension panel = (CompositeExtension) schu
							.getControl();
					panel.loadData(par);
					panel.procesa(panel.l);
				}
			} else
				conn.close();
			summecalc();
		} catch (Exception e1) {
			conn.rollback();
			gui.showError(e1);
		}
	}

	public void showMarkingForm2(final ArrayList<Schueler> s)
			throws SQLException {
		this.s = s;
		final Shell s22 = new Shell(gui.getShell(), SWT.CLOSE | SWT.RESIZE
				| SWT.APPLICATION_MODAL | SWT.MAX | SWT.MIN);
		s22.setText("Rechnungen");
		// s22.setBounds(s22.getDisplay().getBounds());
		s22.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
			}

		});
		s22.setLayout(new GridLayout());
		tf = new TabFolder(s22, SWT.BORDER);

		tf.setLayout(new FillLayout());
		tf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sum = new double[s.size()];
		int pos = 0;
		for (final Schueler schu : s) {
			TabItem item = new TabItem(tf, SWT.NONE);
			item.setText(schu.vorName + " " + parentize(schu.klasse)
					+ (pos > 0 ? "*" : ""));

			CompositeExtension cx = new CompositeExtension(tf, 0, schu, pos);
			item.setControl(cx);
			if (pos == 0)
				cx.viewed = true;
			pos++;
		}
		tf.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.err.println("selected: " + tf.getSelectionIndex());
				if (tf.getSelectionIndex() < tf.getItemCount()) {
					assert tf != null;
					TabItem[] items = tf.getItems();
					assert items != null;
					TabItem tabItem = items[tf.getSelectionIndex()];
					assert tabItem != null;
					Control control = tabItem.getControl();
					assert control != null;
					CompositeExtension g = (CompositeExtension) control;
					g.viewed = true;
					tabItem.setText(g.df.schueler.vorName);
				}

			}

		});

		new Composite(s22, SWT.NONE) {
			{
				setLayout(new RowLayout());
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
						1));
				if (gui.ds.getBuchBestellungen() == 0) {
					MessageBox mb = new MessageBox(getShell(), SWT.ERROR
							| SWT.APPLICATION_MODAL);
					mb.setText("Warnung");
					mb.setMessage("No se pueden emitir facturas hasta haber cerrado el pedido.");
					mb.open();
				}

				RauUtil.createButton(
						this,
						s.size() == 1 ? "&Rechnung erstellen"
								: "Alle &Rechnungen erstellen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									if (gui.ds.getBuchBestellungen() == 0) {
										MessageBox mb = new MessageBox(
												getShell(), SWT.ERROR);
										mb.setText("No hay pedido");
										mb.setMessage("No hay pedido");
										mb.open();
										return;
									}
									rechnungErstellen(gui, s22, s);
								} catch (SQLException e1) {
									gui.showError(e1);
								}
							}
						}).setEnabled(gui.ds.getBuchBestellungen() > 0);
				RauUtil.createButton(this, "&Schließen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								s22.close();
							}
						});
			}
		};

		new Composite(s22, 0) {
			{
				setLayout(new RowLayout(SWT.HORIZONTAL));
				new Label(this, 0).setText("Summe: ");
				summeText = new Text(this, 0);
				if (s.size() > 1)
					new Label(this, 0)
							.setText("(von den Rechnungen von allen markierten Schülern)");
			}
		};
		summecalc();
		s22.layout();
		s22.setMaximized(true);
		s22.open();

		Display display = s22.getDisplay();
		while (!s22.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	private void summecalc() {
		if (summeText == null)
			return;

		totalsum = 0;
		for (double ss : sum)
			totalsum += ss;
		summeText.setText(DsvBuecher.currency(totalsum));
		summeText.pack();
	}
}
