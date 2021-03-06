package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class FachEditor {
	private final Table table;
	private ArrayList<Fach> req;
	private final DsvBuecher gui;

	public FachEditor(final DsvBuecher parent) throws SQLException {
		this.gui = parent;
		this.shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.CLOSE);
		shell.setText("Fachverwaltung");
		shell.setLayout(new GridLayout());
		table = new Table(shell, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] headers = { "Name" };
		for (String header : headers) {
			int width = 20;
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth((width * 8));
			column.setText(header);
		}
		loadData();
		if (gui.pm.can(DsvbPermission.NEWYEARCHANGE))
			new Composite(shell, 0) {
				{
					setLayout(new RowLayout(SWT.HORIZONTAL));

					RauUtil.createButton(this, "Hinzufügen",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent arg0) {
									Fach f = new Fach("");
									new FachFrame(gui, shell, f);
									gui.modelChanged();
									syncLoadData();
								}
							});
					RauUtil.createButton(this, "Bearbeiten",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent arg0) {
									eintragBearbeiten();
								}
							}).setEnabled(false);
					RauUtil.createButton(this, "Löschen",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent arg0) {
									try {
										MessageBox mb = new MessageBox(shell,
												SWT.YES | SWT.NO);
										mb.setText("Löschen bestätigen");
										mb
												.setMessage("Möchten Sie den Eintrag löschen?");
										if (mb.open() == SWT.YES) {
											for (TableItem i : table
													.getSelection())
												parent.ds.faecherVw
														.removeFach((Fach) i
																.getData());
											gui.modelChanged();
											loadData();
										}
									} catch (SQLException e1) {
										parent.showError(e1);
									}
								}

							});
				}
			};

		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				gui.buecherTab.modelChanged();

			}
		});
		shell.pack();
		shell.open();
	}

	private void loadData() {
		try {
			req = gui.ds.faecherVw.getFaecher();
			table.setItemCount(req.size());
			gui.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (TableItem ti : table.getItems())
						ti.dispose();
					table.clearAll();
					for (Fach fach : req) {
						final TableItem ti = new TableItem(table, SWT.NONE);
						ti.setData(fach);
						ti.setText(fach.toStringArray());
					}
				}

			});
		} catch (SQLException e) {
			gui.showError(e);
		}

	}

	private final Shell shell;

	private void eintragBearbeiten() {
		if (table.getSelectionCount() != 1) {
			shell.getDisplay().beep();
		} else {
			new FachFrame(gui, shell, (Fach) table.getSelection()[0].getData());
			gui.modelChanged();
			syncLoadData();
		}
	}

	private void syncLoadData() {
		BusyIndicator.showWhile(gui.display, new Runnable() {
			@Override
			public void run() {
				loadData();
			}

		});
	}

	@SuppressWarnings("unused")
	private void asyncLoadData() {
		gui.xs.execute(new Runnable() {
			@Override
			public void run() {
				loadData();
			}

		});
	}

}
