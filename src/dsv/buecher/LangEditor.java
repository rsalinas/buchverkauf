package dsv.buecher;

import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class LangEditor {
	private int changes;
	private ArrayList<Language> langReq;
	private final Shell shell;

	private final Table table;
	private final DsvBuecher gui;

	public LangEditor(final DsvBuecher parent) throws SQLException {
		this.shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.CLOSE);
		this.gui = parent;
		shell.setText("Sprachenverwaltung");
		shell.setLayout(new GridLayout());
		table = new Table(shell, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] headers = { "Id", "Name" };
		for (int i = 0; i < headers.length; i++) {
			int width = 20;
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth((width * 8));
			column.setText(headers[i]);
		}
		langReq = parent.ds.getLanguages();
		table.setItemCount(langReq.size());
		table.addListener(SWT.SetData, new Listener() {

			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				item.setText(toStringArray(langReq.get(event.index)));
				item.setData(langReq.get(event.index));

			}
		});
		{
			final TableEditor editor = new TableEditor(table);
			// The editor must have the same size as the cell and must
			// not be any smaller than 50 pixels.
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.minimumWidth = 50;
			final int EDITABLECOLUMN = 1;

			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					// Clean up any previous editor control
					Control oldEditor = editor.getEditor();
					if (oldEditor != null)
						oldEditor.dispose();

					// Identify the selected row
					TableItem item = table.getSelection()[0];
					if (item == null)
						return;

					// The control that will be the editor must be a child of
					// the
					// Table
					Text newEditor = new Text(table, SWT.NONE);
					newEditor.setText(item.getText(EDITABLECOLUMN));
					newEditor.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent me) {
							Text text = (Text) editor.getEditor();
							editor.getItem().setText(EDITABLECOLUMN,
									text.getText());
						}
					});
					newEditor.selectAll();
					newEditor.setFocus();
					editor.setEditor(newEditor, item, EDITABLECOLUMN);
				}
			});
		}
		if (parent.pm.can(DsvbPermission.NEWYEARCHANGE))
			new Composite(shell, 0) {
				{
					setLayout(new RowLayout(SWT.HORIZONTAL));

					RauUtil.createButton(this, "Hinzufügen",
							new SelectionAdapter() {

								@Override
								public void widgetSelected(SelectionEvent arg0) {
									Language f = new Language();
									new LangFrame(gui, shell, f);
									gui.modelChanged();
									syncLoadData();
									changes++;
								}
							});
					RauUtil.createButton(this, "Bearbeiten",
							new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent arg0) {
									eintragBearbeiten();
									changes++;
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
												parent.ds.sprachVw
														.removeLanguage((Language) i
																.getData());
											changes++;
											parent.modelChanged();
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
				try {
					gui.ds.loadLanguages();
				} catch (SQLException e) {
					gui.showError(e);
				}
				gui.buecherTab.modelChanged();
				if (changes > 0)
					gui.forceShutdown();

			}
		});
		shell.pack();
		shell.open();
	}

	protected String[] toStringArray(Language language) {
		return new String[] { language.id, language.name };
	}

	private void loadData() {
		try {

			langReq = gui.ds.getLanguages();
			System.err.println("new size: " + langReq.size());
			gui.getShell().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					table.clearAll();

					table.setItemCount(langReq.size());
				}
			});

		} catch (SQLException e) {
			gui.showError(e);
		}
	}

	private void eintragBearbeiten() {
		// TODO Auto-generated method stub

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
