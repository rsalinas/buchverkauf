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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class AbteilungenEditor {
	private final Table table;
	private ArrayList<Abteilung> req;
	private final DsvBuecher gui;

	public AbteilungenEditor(final DsvBuecher parent) throws SQLException {
		this.gui = parent;
		this.shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.CLOSE);
		shell.setText("Abteilungenverwaltung");
		shell.setLayout(new GridLayout());
		table = new Table(shell, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] headers = { "Abteilung" };
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
									Abteilung f = new Abteilung();
									new AbteilungFrame(gui, shell, f);
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
					RauUtil.createButton(this, "Nach oben sortieren",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent arg0) {
									if (table.getSelectionIndex() <= 0)
										return;
									int sel = table.getSelectionIndex();
									try {
										parent.ds.abteilungenVw
												.swap(
														(Abteilung) table
																.getItem(
																		table
																				.getSelectionIndex() - 1)
																.getData(),
														(Abteilung) table
																.getItem(
																		table
																				.getSelectionIndex())
																.getData());
										gui.modelChanged();
										loadData();
										table.setSelection(sel - 1);
									} catch (SQLException e) {
										gui.showError(e);
									}
								}
							});
					RauUtil.createButton(this, "Nach unten sortieren",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent arg0) {
									if (table.getSelectionIndex() < 0)
										return;
									if (table.getSelectionIndex() + 1 == table
											.getItemCount())
										return;
									int sel = table.getSelectionIndex();
									try {
										parent.ds.abteilungenVw
												.swap(
														(Abteilung) table
																.getItem(
																		table
																				.getSelectionIndex() + 1)
																.getData(),
														(Abteilung) table
																.getItem(
																		table
																				.getSelectionIndex())
																.getData());
										gui.modelChanged();
										loadData();
										table.setSelection(sel);
									} catch (SQLException e) {
										gui.showError(e);
									}
								}
							});
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
												parent.ds.abteilungenVw
														.deleteAbteilung((Abteilung) i
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

		shell.pack();
		shell.open();
	}

	private void loadData() {
		try {
			req = gui.ds.klassenVw.getAbteilungen();
			table.setItemCount(req.size());
			gui.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (TableItem ti : table.getItems())
						ti.dispose();
					table.clearAll();
					for (Abteilung fach : req) {
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
			new AbteilungFrame(gui, shell, (Abteilung) table.getSelection()[0]
					.getData());
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
