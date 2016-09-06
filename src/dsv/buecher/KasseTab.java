package dsv.buecher;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import dsv.buecher.report.ReportKasseNachKlassen;

public class KasseTab extends TabFolderHolder {
	private final Table table;

	private final ArrayList<String[]> l2 = new ArrayList<String[]>();
	private ArrayList<KasseDetail> l;

	public KasseTab(TabFolder tabFolder, final DsvBuecher par)
			throws SQLException {
		super(tabFolder, "&Kasse", par);
		Composite composite = new Composite(tabFolder, SWT.PUSH);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData());

		// new Label(composite, SWT.None).setText("1");
		// new Label(composite, SWT.None).setText("2");
		composite.pack();

		item.setControl(composite);
		table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalSpan = 1;
		gd.horizontalSpan = 1;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		table.setLayoutData(gd);
		table.setBounds(0, 0, 640, 320);
		// table.setFont(font);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// Table virtualTable = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION
		// | SWT.VIRTUAL | SWT.BORDER);
		table.setHeaderVisible(true);
		// {
		// TableColumn column = new TableColumn(table, SWT.RIGHT);
		// column.setText("My Virtual Table");
		// column.setWidth(480);
		// }
		int width = 0;

		// Headers
		String[] headers = { "Datum", "Bücher", "erst. Rechnungen",
				"Einnahmen", "Ausgaben", "Tagesabrechnung" };
		for (String header : headers) {
			width = 20;
			TableColumn column = new TableColumn(table, SWT.FULL_SELECTION
					| SWT.MULTI);
			column.setWidth((width * 8)); // Characters by pixels... rough
			// guess
			column.setText(header);
		}

		getData();

		final Label totalLabel = new Label(composite, SWT.PUSH);
		totalLabel.setText("total: ");

		final MouseAdapter selectionShowMouseAdapter = new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				showSelected();
			}
		};
		table.addMouseListener(selectionShowMouseAdapter);
		totalLabel.addMouseListener(selectionShowMouseAdapter);
	}

	/**
	 * @param gui
	 */
	private void getData() {
		gui.xs.execute(new Runnable() {
			@Override
			public void run() {
				try {
					l = gui.ds.getKasse();
					int sn = 0;
					double sa = 0, sv = 0, sd = 0;
					l2.clear();
					for (KasseDetail d : l) {
						sn += d.n2;
						sa += d.amount;
						sv += d.ventas;
						sd += d.devs;
						l2.add(new String[] { d.date.toString(), "" + d.n,
								"" + d.n2, DsvBuecher.currency(d.ventas),
								"-" + DsvBuecher.currency(d.devs),
								DsvBuecher.currency(d.amount) });
					}

					l2.add(new String[] { "insgesamt", "", "" + sn,
							DsvBuecher.currency(sv),
							"-" + DsvBuecher.currency(sd),
							DsvBuecher.currency(sa) });
					final String[][] data = l2.toArray(new String[l2.size()][]);
					final TableItem[] items = new TableItem[data.length];
					gui.getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							for (TableItem i : table.getItems())
								i.dispose();
							table.clearAll();
							for (int i = 0; i < data.length; i++) {
								items[i] = new TableItem(table, SWT.NONE);
								items[i].setText(data[i]);
								if (i < data.length - 1)
									items[i].setData(l.get(i).date);
							}
						}
					});
				} catch (SQLException e) {
					gui.showError(e);
				}
			}
		});
	}

	private void showSelected() {
		try {
			for (TableItem i : table.getSelection()) {
				final Shell s2 = new Shell(gui.getShell(), SWT.RESIZE
						| SWT.CLOSE);
				s2.setLayout(new FillLayout());
				new KasseDetailForm(s2, gui, ((Date) i.getData()), table);
				s2.open();
			}
		} catch (SQLException e1) {
			gui.showError(e1);
		}
	}

	@Override
	public void f5() {
		table.setItemCount(0);
		getData();
	}

	@Override
	public void modelChanged() {
		getData();
	}

	public void fill(HashMap<StandardMenus, Menu> hm) {
		RauUtil.createMenuItem(hm.get(StandardMenus.REPORTS), SWT.PUSH,
				"&Kassenkontrolle", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						BusyIndicator.showWhile(Display.getCurrent(),
								new Runnable() {
									public void run() {
										try {
											new ReportKasseNachKlassen(gui);
										} catch (Exception e) {
											gui.showError(e);
										}
									}
								});

					}
				});
	}
}
