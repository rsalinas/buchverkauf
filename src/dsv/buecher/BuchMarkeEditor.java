package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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

public class BuchMarkeEditor {
	protected final Shell shell;
	private final Button button;
	private final Table table;
	private final static String[] headers = { "Id", "Abkürzung", "Auf Deutsch",
			"Auf Spanisch" };
	private ArrayList<BuchBemerkung> marken;
	private final DsvBuecher gui;

	public BuchMarkeEditor(final DsvBuecher gui) throws SQLException {
		this.shell = new Shell(gui.getShell(), SWT.RESIZE | SWT.CLOSE);
		this.gui = gui;
		shell.setLayout(new GridLayout());
		shell.setText("Bücherbemerkungen-Verwaltung");
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				System.err.println("Close! " + changes);
				if (changes != 0)
					gui.forceShutdown();

			}
		});
		shell.open();

		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData());

		table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 640, 320);
		// table.setFont(font);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

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
				item.setText(marken.get(event.index).toStringArray());
				item.setData(marken.get(event.index));
			}
		});
		loadData();

		button = new Button(shell, SWT.PUSH);
		button.setBounds(300, 325, 50, 30);
		button.setText("&Neue Bemerkung");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final BuchBemerkung bemerkung = new BuchBemerkung();
				final BuchBemerkungFrame frame = new BuchBemerkungFrame(shell,
						bemerkung);
				frame.open();
				changes++;
				if (frame.mustSave)
					try {
						gui.ds.saveBemerkung(bemerkung);
						gui.modelChanged();
						loadData();
					} catch (SQLException e1) {
						gui.showError(e1);
					}
			}
		});
		{
			Button bearbeitenButton = new Button(shell, SWT.PUSH);
			bearbeitenButton.setBounds(300, 325, 50, 30);
			bearbeitenButton.setText("&Bearbeiten");
			bearbeitenButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					eintragBearbeiten();
					changes++;
				}
			});

		}

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
				final MenuItem bearbeitenButton = new MenuItem(m, SWT.PUSH);
				bearbeitenButton.setText("Bemerkung bearbeiten");
				bearbeitenButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						eintragBearbeiten();
						changes++;
					}

				});

				new MenuItem(m, SWT.SEPARATOR);
				final MenuItem loeschen = new MenuItem(m, SWT.PUSH);
				loeschen.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							MessageBox mb = new MessageBox(shell, SWT.YES
									| SWT.NO);
							mb.setText("Löschen bestätigen");
							mb.setMessage("Möchten Sie den Eintrag löschen?");
							if (mb.open() == SWT.YES) {
								gui.ds.buchVw
										.removeBookMark((BuchBemerkung) table
												.getSelection()[0].getData());
								loadData();
							}
						} catch (SQLException e1) {
							gui.showError(e1);
						}
						changes++;
					}
				});
				loeschen.setText("Löschen");
			}

		});
		table.setMenu(m);

		shell.pack();

	}

	int changes = 0;

	private void loadData() throws SQLException {
		marken = gui.ds.getBuchMarken();
		table.setItemCount(marken.size());
		table.clearAll();
	}

	private void eintragBearbeiten() {
		if (table.getSelectionCount() != 1) {
			Display.getCurrent().beep();
			return;
		}
		final BuchBemerkung bemerkung = (BuchBemerkung) table.getSelection()[0]
				.getData();
		final BuchBemerkungFrame frame = new BuchBemerkungFrame(shell,
				bemerkung);
		frame.open();
		if (frame.mustSave)
			try {
				gui.ds.saveBemerkung(bemerkung);
				loadData();
			} catch (SQLException e) {
				gui.showError(e);
			}
	}
}
