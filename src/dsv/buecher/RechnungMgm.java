package dsv.buecher;

import java.awt.Desktop;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class RechnungMgm extends Composite {
	protected static final HeaderDef headers0[] = { new HeaderDef("Id", "id"),
			new HeaderDef("Datum", "datum", 12),
			new HeaderDef("Summe", "summe").setAlignment(SWT.RIGHT) };
	protected static final HeaderDef headers[] = {
			new HeaderDef("Op.", null, 10), new HeaderDef("Titel", null, 40),
			new HeaderDef("Preis", "preis").setAlignment(SWT.RIGHT) };
	private final Table t;
	private final DsvBuecherDatasource ds;
	private final Schueler schueler;
	private Table t2;

	public RechnungMgm(Composite parentComposite,
			final DsvBuecherDatasource ds, Schueler schueler)
			throws SQLException {
		super(parentComposite, 0);
		this.schueler = schueler;
		this.ds = ds;
		setLayout(new GridLayout(1, false));
		new Label(this, 0).setText("Rechnungen:");
		t = new Table(this, SWT.MULTI | SWT.FULL_SELECTION);
		t.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				pdfZeigen();
			}
		});
		t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		new Composite(this, 0) {
			{
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
						1));
				setLayout(new RowLayout(SWT.HORIZONTAL));
				RauUtil.createButton(this, "PDF zeigen",
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								pdfZeigen();
							}
						});
			}
		};
		for (final HeaderDef header : headers0) {
			final org.eclipse.swt.widgets.TableColumn column = new org.eclipse.swt.widgets.TableColumn(
					t, SWT.NONE);

			column.setWidth(header.width);
			column.setText(header.name);
			if (header.alignment != 0)
				column.setAlignment(header.alignment);
			// column.addSelectionListener(new SelectionAdapter() {
			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// if (header.field != null) {
			// sortBy = header.field;
			// loadData();
			// } else
			// gui.display.beep();
			// }
			//
			// });

		}

		t.setHeaderVisible(true);

		new Group(this, SWT.NONE) {
			{
				setText("Operationen");
				setLayout(new FillLayout());
				t2 = new Table(this, SWT.MULTI | SWT.FULL_SELECTION);
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				for (final HeaderDef header : headers) {
					final org.eclipse.swt.widgets.TableColumn column = new org.eclipse.swt.widgets.TableColumn(
							t2, SWT.NONE);

					column.setWidth(header.width);
					column.setText(header.name);
					if (header.alignment != 0)
						column.setAlignment(header.alignment);
					// column.addSelectionListener(new SelectionAdapter() {
					// @Override
					// public void widgetSelected(SelectionEvent e) {
					// if (header.field != null) {
					// sortBy = header.field;
					// loadData();
					// } else
					// gui.display.beep();
					// }
					//
					// });

				}

				t2.setHeaderVisible(true);
			}

			@Override
			protected void checkSubclass() {
			}

		};
		loadData();
		Menu tMenu = new Menu(t);
		RauUtil.createMenuItem(tMenu, SWT.PUSH, "PDF zeigen",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						// TODO Auto-generated method stub
						super.widgetSelected(arg0);
					}
				});
		t.setMenu(tMenu);
		t.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (t.getSelectionCount() != 1)
					return;
				DatosFactura df = (DatosFactura) t.getSelection()[0].getData();
				for (TableItem i : t2.getItems())
					i.dispose();
				try {
					ArrayList<LineaFactura> rl = ds.rechnungVw
							.getRechnungLines(df.id);
					double summe = 0;
					for (LineaFactura i : rl) {
						new TableItem(t2, 0).setText(new String[] {
								i.zustand.name(),
								i.titel,
								(i.zustand == XKaufZustand.DEVUELTO ? "-" : "")
										+ i.price });
						if (i.zustand == XKaufZustand.VERKAUFT)
							summe += i.price;
						else if (i.zustand == XKaufZustand.DEVUELTO)
							summe -= i.price;

					}
					new TableItem(t2, SWT.SEPARATOR);

					new TableItem(t2, 0).setText(new String[] { "", "Summe",
							DsvBuecher.currency(summe) });
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	private void loadData() throws SQLException {
		List<DatosFactura> l = ds.rechnungVw.getRechnungen(schueler);
		t.setItemCount(l.size());
		for (TableItem i : t.getItems())
			i.dispose();
		for (DatosFactura df : l) {
			double summe = 0;
			for (LineaFactura ii : ds.rechnungVw.getRechnungLines(df.id))
				summe += (ii.zustand == XKaufZustand.DEVUELTO ? -1 : 1)
						* ii.price;
			df.summe = summe;
			TableItem ti = new TableItem(t, 0);
			ti.setText(new String[] { "" + df.id, "" + df.datum,
					DsvBuecher.currency(df.summe) });
			ti.setData(df);
		}
	}

	private void pdfZeigen() {
		if (t.getSelectionCount() == 0)
			return;

		ArrayList<DatosFactura> l = new ArrayList<DatosFactura>();

		try {
			for (TableItem i : t.getSelection()) {
				final DatosFactura df = ((DatosFactura) i.getData());
				l.add(pasa(ds, df));
			}
			File filename;
			filename = File.createTempFile("Rechnung-", ".pdf");
			new RechnungPdfMaker().makeit(ds, filename.toString(), l);
			Desktop.getDesktop().open(filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static DatosFactura pasa(DsvBuecherDatasource ds,
			final DatosFactura idf) throws SQLException {
		DatosFactura df = new DatosFactura(idf.id, idf.schueler, idf.datum);
		ArrayList<LineaFactura> lines = ds.rechnungVw.getRechnungLines(df.id);

		for (LineaFactura j : lines) {
			final Buch nb = new Buch();

			nb.titel = j.titel;
			nb.verlag = new Verlag(-1, j.verlag, null);
			nb.preis = j.price;
			nb.isbn = j.isbn;
			nb.code = j.buchcode;

			ChoosableBuch cb = new ChoosableBuch(nb, true);

			if (j.zustand == XKaufZustand.VERKAUFT) {
				cb.precioVenta = nb.preis;
				df.comprar.add(cb);
			} else if (j.zustand == XKaufZustand.DEVUELTO) {
				cb.precioVenta = nb.preis;
				df.devolver.add(cb);
			}
		}
		return df;
	}
}
