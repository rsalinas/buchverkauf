package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class BestellungMarkForm {
	private final DsvBuecher gui;
	private final static HeaderDef[] headers = {
			new HeaderDef("Bestellung", null, 10),
			new HeaderDef("Titel", "titel", 30),
			new HeaderDef("Verlag", "verlagname"),
			new HeaderDef("Fach", "fach"),
			new HeaderDef("Preis", "preis", 10).setAlignment(SWT.RIGHT),
			new HeaderDef("Bemerkung", null, 25) };
	private Table table;
	private static final String[][] changeMatrix = new String[][] {
			{ "", "Añadido" }, { "Quitado", "" } };

	private final boolean permitirEscrituras;

	public BestellungMarkForm(DsvBuecher gui) throws SQLException {
		super();
		permitirEscrituras = gui.ds.getBuchBestellungen() == 0;
		this.gui = gui;

	}

	final HashMap<Buch, TableItem> buttons = new HashMap<Buch, TableItem>();
	private ArrayList<ChoosableBuch> l;

	public void showMarkingForm2(final Schueler s) throws SQLException {
		DsvBuecher par = gui;
		final Shell s2 = new Shell(par.getShell(), SWT.CLOSE | SWT.RESIZE
				| SWT.APPLICATION_MODAL | SWT.MAX | SWT.MIN);
		s2.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				if (!permitirEscrituras)
					return;
				int count = changeCount(l, buttons);
				if (count != 0) {
					arg0.doit = false;
					avisoCambiosPendientes(s2);
				}
			}

		});
		s2.setLayout(new GridLayout(1, false));
		s2.setText("Bestellung von " + s);
		l = par.ds.getBooksForUser(s);
		System.err.println("books for user: " + l);
		table = new Table(s2, SWT.VIRTUAL | SWT.CHECK | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		for (final HeaderDef header : headers) {
			final TableColumn column = new TableColumn(table, SWT.NONE);

			column.setWidth(header.width);
			column.setText(header.name);
			if (header.alignment != 0)
				column.setAlignment(header.alignment);
		}

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setItemCount(l.size());
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				ChoosableBuch choosableBuch = l.get(event.index);
				if (!buttons.containsKey(choosableBuch))
					buttons.put(choosableBuch.buch, item);
				item.setText(DsvBuecher.joinArrays(new String[] { "" },
						choosableBuch.toStringArray(gui.ds)));
				item.setData(choosableBuch);
				item.setChecked(choosableBuch.bestellt);
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				table.getSelection()[0].setChecked(!table.getSelection()[0]
						.getChecked());
				TableItem it = table.getSelection()[0];
				it
						.setText(
								0,
								changeMatrix[((ChoosableBuch) it.getData()).bestellt ? 1
										: 0][it.getChecked() ? 1 : 0]);
			}
		});
		Menu m = new Menu(s2);
		table.setMenu(m);
		final MenuItem alleWahlenButton = new MenuItem(m, SWT.PUSH);
		alleWahlenButton.setText("Alle bestellen");
		alleWahlenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				alleWahlen();
			}
		});
		final MenuItem keineWahlenButton = new MenuItem(m, SWT.PUSH);
		keineWahlenButton.setText("Keine bestellen");
		keineWahlenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				keineWahlen();
			}
		});

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("selection " + e.item.getData());
				if (e.item instanceof TableItem) {
					TableItem it = (TableItem) e.item;
					ChoosableBuch choosableBuch = (ChoosableBuch) it.getData();
					if (choosableBuch == null)
						return;
					it.setText(0,
							changeMatrix[choosableBuch.bestellt ? 1 : 0][it
									.getChecked() ? 1 : 0]);

				}
			}
		});

		Composite botonera = new Composite(s2, SWT.NONE) {
			{
				setLayout(new RowLayout());
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
						1));

			}
		};
		// botonera.setLayout(new RowLayout(SWT.HORIZONTAL));

		{
			final Button alleWahlenButton2 = new Button(botonera, SWT.PUSH);
			alleWahlenButton2.setText("&Alle bestellen");
			alleWahlenButton2.setEnabled(permitirEscrituras);
			alleWahlenButton2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					alleWahlen();
				}
			});

		}
		{
			final Button alleWahlenButton2 = new Button(botonera, SWT.PUSH);
			alleWahlenButton2.setText("&Keine bestellen");
			alleWahlenButton2.setEnabled(permitirEscrituras);
			alleWahlenButton2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					keineWahlen();
				}
			});

		}
		final Button saveButton = new Button(botonera, SWT.PUSH);
		saveButton.setText("&Speichern");

		saveButton.setEnabled(permitirEscrituras);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if (!permitirEscrituras) {
				// s2.getDisplay().beep();
				// return;
				// } // XXX
				@SuppressWarnings("unused")
				int count = changeCount(l, buttons);
				ArrayList<Buch> wantedBooks = new ArrayList<Buch>();
				for (ChoosableBuch b : l)
					if (buttons.get(b.buch).getChecked()) {
						b.bestellt = true;
						wantedBooks.add(b.buch);
					} else
						b.bestellt = false;

				try {
					gui.ds.buchVw.saveBestellung(s, wantedBooks);
					s2.close();
				} catch (SQLException e1) {
					gui.showError(e1);
				}
			}
		});
		{
			Button closeButton = new Button(botonera, SWT.PUSH);
			closeButton.setText("Schließen");
			closeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!permitirEscrituras) {
						s2.close();
						return;
					}
					int count = changeCount(l, buttons);
					if (count == 0) {
						s2.close();
					} else {
						avisoCambiosPendientes(s2);
					}
				}
			});
		}
		botonera.pack();
		s2.pack();
		s2.open();
		Display display = s2.getDisplay();
		while (!s2.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	protected void keineWahlen() {
		for (TableItem i : table.getItems()) {
			i.setChecked(false);
			i
					.setText(
							0,
							changeMatrix[((ChoosableBuch) i.getData()).bestellt ? 1
									: 0][i.getChecked() ? 1 : 0]);
		}
	}

	protected void alleWahlen() {
		for (TableItem i : table.getItems()) {
			i.setChecked(true);
			i
					.setText(
							0,
							changeMatrix[((ChoosableBuch) i.getData()).bestellt ? 1
									: 0][i.getChecked() ? 1 : 0]);
		}

	}

	private void avisoCambiosPendientes(final Shell s2) {
		s2.getDisplay().beep();
		MessageBox mb = new MessageBox(gui.getShell(), SWT.APPLICATION_MODAL);
		mb.setText("Vorsicht");
		mb.setMessage("Zuerst Änderungen speichern oder rückgängig machen");

		mb.open();
	}

	/**
	 * @param l
	 * @param buttons
	 * @return
	 */
	private static int changeCount(final ArrayList<ChoosableBuch> l,
			final HashMap<Buch, TableItem> buttons) {
		int count = 0;
		for (ChoosableBuch b : l) {
			if (buttons.get(b.buch) == null) {
				for (Buch ii : buttons.keySet())
					System.err.println("libro: " + ii);
				// throw new RuntimeException("no encuentro libro: " + b.buch);
				// FIXME ignore
				continue;
			}
			if (buttons.get(b.buch).getChecked() != b.bestellt)
				count++;
		}
		return count;
	}

	public static void showMarkingForm(Schueler schueler, DsvBuecher gui)
			throws SQLException {
		new BestellungMarkForm(gui).showMarkingForm2(schueler);

	}
}
