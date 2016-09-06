package dsv.buecher;

import java.awt.Desktop;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import dsv.buecher.report.ReportKasseDetail;

public class KasseDetailForm extends Composite {
	protected String sortBy;
	private final DsvBuecher gui;
	private ArrayList<RechnungInfo> l;
	private final Table t2;
	private final Date datum;

	public KasseDetailForm(final Shell s12, final DsvBuecher gui,
			final Date data, final Table table) throws SQLException {
		super(s12, SWT.NONE);
		this.gui = gui;
		this.setLayout(new GridLayout(1, false));
		getShell().setText(
				"Kassendetail "
						+ (data != null ? "für " + data : " (insgesamt)"));
		HeaderDef[] headers = { new HeaderDef("#", "rid"),
				new HeaderDef("Vorname", "vorname"),
				new HeaderDef("Name", "name"),
				new HeaderDef("Klasse", "klasse"),
				new HeaderDef("Datum", "zahlungsdatum"),
				new HeaderDef("Summe", "summe"),
				new HeaderDef("Verkauft", "summe0"),
				new HeaderDef("Zurück", "summe1") };
		new Composite(this, SWT.NONE) {
			{
				setLayout(new RowLayout(SWT.HORIZONTAL));
				RauUtil.createButton(this, "Rechnungliste drücken",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								try {
									new ReportKasseDetail(gui, l, data);
								} catch (Exception e) {
									gui.showError(e);
								}
							}
						});
			}
		};
		t2 = new Table(this, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION);
		this.datum = data;
		loadData();

		t2.setHeaderVisible(true);
		t2.setLinesVisible(true);
		for (int i = 0; i < headers.length; i++) {
			int width = i > 0 ? 20 : 10;
			TableColumn column = new TableColumn(t2, SWT.NONE);
			column.setWidth((width * 8));
			final HeaderDef header = headers[i];
			column.setText(header.name);
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.err.println("ordenar por: " + header.name);
					if (header.field != null) {
						sortBy = header.field;
						try {
							loadData();
						} catch (SQLException e1) {
							gui.showError(e1);
						}
					} else
						gui.display.beep();
				}

			});
		}
		t2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		t2.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				RechnungInfo r = l.get(event.index);
				item.setText(new String[] { "" + r.id, r.schueler.vorName,
						r.schueler.name, r.schueler.klasse,
						RechnungInfo.dtf.format(r.datum.getTime()),
						DsvBuecher.currency(r.amount),
						DsvBuecher.currency(r.totalVentas),
						DsvBuecher.currency(r.totalDev) });
				item.setData(l.get(event.index));
			}
		});
		final Menu m = new Menu(t2);
		t2.setMenu(m);
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

				RauUtil.createMenuItem(m, SWT.PUSH, "PDF erzeugen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								generatePdf(getShell(), t2);
							}
						});

				// {
				// final MenuItem devolverParcial = new MenuItem(m, SWT.PUSH);
				// devolverParcial.setText("Hacer devolución");
				// devolverParcial
				// .addSelectionListener(new SelectionAdapter() {
				// @Override
				// public void widgetSelected(SelectionEvent e) {
				//
				// ArrayList<Schueler> l = new ArrayList<Schueler>();
				// l.add(((RechnungInfo) t2.getSelection()[0]
				// .getData()).schueler);
				// try {
				// new XVerkaufForm(gui)
				// .showMarkingForm2(l);
				// } catch (SQLException e1) {
				// gui.showError(e1);
				// }
				//
				// }
				// });
				//
				// }
			}
		});

		t2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				generatePdf(getShell(), t2);
			}
		});
	}

	private void loadData() throws SQLException {
		l = this.gui.ds.rechnungVw.getRechungenInDatum(datum, sortBy);
		t2.setItemCount(l.size());
		t2.clearAll();
	}

	/**
	 * @param s2
	 * @param t2
	 */
	private void generatePdf(final Shell s2, final Table t2) {
		if (t2.getSelectionCount() == 0)
			return;
		try {
			PleaseWaitShell pws = new PleaseWaitShell(s2, "Bitte warten Sie...");
			ArrayList<DatosFactura> l = new ArrayList<DatosFactura>();
			for (TableItem i : t2.getSelection()) {
				final RechnungInfo rechnungInfo = (RechnungInfo) i.getData();
				DatosFactura df = new DatosFactura(rechnungInfo.id,
						rechnungInfo.schueler, rechnungInfo.datum);
				l.add(RechnungMgm.pasa(gui.ds, df));
			}
			File filename = File.createTempFile("Rechnung-", ".pdf");

			new RechnungPdfMaker().makeit(gui.ds, filename.toString(), l);
			Desktop.getDesktop().open(filename);
			pws.close();

		} catch (Exception e) {
			gui.showError(e);
		}

	}
}
