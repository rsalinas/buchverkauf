package dsv.buecher;

import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class UserEditor {
	private final class ToArrayPasableListener implements Listener {
		@SuppressWarnings("unused")
		private final SimpleLogger sl;

		private ToArrayPasableListener(SimpleLogger parent) {
			this.sl = parent;
		}

		@Override
		public void handleEvent(Event event) {
			TableItem item = (TableItem) event.item;
			ToArrayPassable sch = langReq.get(event.index);
			if (sch != null) {
				item.setText(sch.toStringArray());
				item.setData(sch);
			}
		}
	}

	private final Table table;
	private final GenericRequestInterface<User> langReq;

	public UserEditor(final DsvBuecher parent) throws SQLException {
		this.shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.CLOSE);
		shell.setText("Lang editor");
		shell.setLayout(new GridLayout());
		table = new Table(shell, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		langReq = parent.ds.userVw.getUsers();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] headers = { "Login", "Name" };
		for (String header : headers) {
			int width = 20;
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth((width * 8));
			column.setText(header);
		}
		table.setItemCount(langReq.size());
		table.addListener(SWT.SetData, new ToArrayPasableListener(parent));
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
		shell.pack();
		shell.open();
	}

	private final Shell shell;
}
