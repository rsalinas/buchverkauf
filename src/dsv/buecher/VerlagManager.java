package dsv.buecher;

import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class VerlagManager {
	public class VerlagFrame {

		private final Shell sh;
		@SuppressWarnings("unused")
		private final Verlag data;

		public VerlagFrame(Shell pshell, final Verlag data) {
			sh = new Shell(pshell, SWT.RESIZE | SWT.CLOSE
					| SWT.APPLICATION_MODAL);
			sh.setText("Verlagsverwaltung");
			this.data = data;

			sh.setLayout(new GridLayout(2, false));
			if (data.code != -1) {
				new Label(sh, 0).setText("Id:");
				final Text codeText = new Text(sh, 0);
				codeText.setText("" + data.code);
				codeText.setEnabled(false);
				codeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false, 1, 1));
			}
			new Label(sh, 0).setText("Name:");
			final Text nameText = new Text(sh, 0);

			nameText.setText(data.name);
			new Label(sh, 0).setText("Land:");
			final Combo landText = new Combo(sh, SWT.READ_ONLY);
			for (String i : Verlag.hm.values())
				landText.add(i);
			if (Verlag.hm.containsKey(data.land))
				landText.setText(Verlag.hm.get(data.land));
			else
				landText.setText("");

			for (Control w : new Control[] { nameText, landText })
				w.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
						1, 1));
			// new Label(sh, 0).setText("arg0");
			final Button bSave = new Button(sh, SWT.PUSH);
			bSave.setText("&Save");
			bSave.setLayoutData(new GridData(1, 1, false, false));
			bSave.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					BusyIndicator.showWhile(sh.getDisplay(), new Runnable() {
						@Override
						public void run() {
							try {
								data.name = nameText.getText();
								data.land = Verlag.hm2.get(landText.getText());
								gui.ds.verlagVw.saveVerlag(data);
							} catch (SQLException e) {
								gui.showError(e);
							}
						}
					});
					MessageBox mb = new MessageBox(sh, SWT.OK);
					mb.setText("Grabación correcta");
					mb.setMessage("Los datos han sido correctamente salvados: "
							+ data);
					mb.open();
					sh.close();
				}
			});

			final Button b2 = new Button(sh, SWT.PUSH);
			b2.setText("&Cancel");
			b2.setLayoutData(new GridData(1, 1, false, false));
			b2.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					sh.close();
				}

			});
			// sh.setSize(300, 200);
			sh.pack();
			sh.setSize(4 * 100, sh.getSize().y);
			sh.open();
			Display display = sh.getDisplay();
			while (!sh.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}

	}

	private final static String[] headers = { "Code", "Name", "Land" };
	private final DsvBuecher gui;
	final Table table;
	private final Shell shell;

	public VerlagManager(final DsvBuecher parent) throws SQLException {
		this.gui = parent;
		this.shell = parent.getShell();
		final Shell shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.CLOSE
				| SWT.APPLICATION_MODAL);
		table = new Table(shell, SWT.FULL_SELECTION | SWT.MULTI);
		shell.setText("Verlageverwaltung");
		shell.setLayout(new GridLayout());
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		for (String h : headers) {
			TableColumn column1 = new TableColumn(table, SWT.LEFT);
			column1.setText(h);
			column1.setWidth(100);
		}
		Menu m = new Menu(shell, SWT.POP_UP);
		MenuItem mi = new MenuItem(m, SWT.PUSH);
		mi.setText("Löschen");
		mi.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO);
					mb.setText("Löschen bestätigen");
					mb.setMessage("Möchten Sie den Eintrag löschen?");
					if (mb.open() == SWT.YES) {
						for (TableItem i : table.getSelection())
							parent.ds.removeVerlag((Verlag) i.getData());
						gui.modelChanged();
						loadData();
					}
				} catch (SQLException e1) {
					parent.showError(e1);
				}
			}

		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				eintragBearbeiten();

			}
		});
		table.setMenu(m);
		{
			Button newButton = new Button(shell, SWT.PUSH);
			newButton.setText("Bearbeiten");
			newButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					eintragBearbeiten();
				}
			});

		}
		Button newButton = new Button(shell, SWT.PUSH);
		newButton.setText("Neu");
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new VerlagFrame(shell, new Verlag());
				asyncLoadData();
				gui.modelChanged();
			}
		});
		loadData();
		// shell.pack();
		newButton.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event e) {
				gui.buecherTab.modelChanged();
			}
		});
		shell.open();

	}

	/**
	 * @param gui
	 * @param t
	 * @throws SQLException
	 */
	private void loadData() {
		final GenericRequestInterface<Verlag> verlage;
		try {
			verlage = gui.ds.verlagVw.getVerlage();
		} catch (SQLException e) {
			gui.showError(e);
			return;
		}
		gui.getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (TableItem ti : table.getItems())
					ti.dispose();
				table.clearAll();
				for (Verlag verlag : verlage) {
					final TableItem ti = new TableItem(table, SWT.NONE);
					ti.setData(verlag);
					ti.setText(verlag.toStringArray());
				}
			}

		});

	}

	private void syncLoadData() {
		BusyIndicator.showWhile(gui.display, new Runnable() {
			@Override
			public void run() {
				loadData();
			}

		});
	}

	private void asyncLoadData() {
		gui.xs.execute(new Runnable() {
			@Override
			public void run() {
				loadData();
			}

		});
	}

	private void eintragBearbeiten() {
		if (table.getSelectionCount() != 1) {
			shell.getDisplay().beep();
			return;
		}
		new VerlagFrame(shell, (Verlag) table.getSelection()[0].getData());
		gui.modelChanged();
		syncLoadData();
	}
}
