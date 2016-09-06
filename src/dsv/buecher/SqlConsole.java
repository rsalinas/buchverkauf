package dsv.buecher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class SqlConsole {

	private final Shell shell;
	private final Table table;
	private final Text input;
	private final Connection c;

	public SqlConsole(Shell pshell, DsvBuecherDatasource ds,
			final SimpleLogger sl) throws SQLException {
		shell = new Shell(pshell, SWT.RESIZE | SWT.CLOSE
				| SWT.APPLICATION_MODAL);
		c = ds.getConnection();
		shell.setText("SQL-Konsole");
		shell.setLayout(new GridLayout());
		table = new Table(shell, SWT.NONE);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		input = new Text(shell, 0);
		input.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					processQuery();
				} catch (SQLException e1) {
					sl.showError(e1);
				}
			}
		});
		input
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
						1));
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					c.close();
				} catch (SQLException e) {
				}
			}

		});
		shell.open();
		input.setFocus();
	}

	/**
	 * @throws SQLException
	 */
	private void processQuery() throws SQLException {
		final String query = input.getText();
		for (TableColumn i : table.getColumns())
			i.dispose();
		table.clearAll();
		for (TableItem i : table.getItems())
			i.dispose();
		if (query.trim().toLowerCase().startsWith("select ")) {
			ResultSet rs = c.createStatement().executeQuery(query);
			final ResultSetMetaData metaData = rs.getMetaData();
			for (int c = 1; c <= metaData.getColumnCount(); c++) {
				final TableColumn col = new org.eclipse.swt.widgets.TableColumn(
						table, 0);
				final String colname = metaData.getColumnName(c);
				col.setText(colname);
				col.setWidth(20 * 8);

			}

			while (rs.next()) {
				String[] a = new String[metaData.getColumnCount()];
				for (int c = 1; c <= metaData.getColumnCount(); c++) {
					switch (metaData.getColumnType(c)) {
					// case java.sql.Types.INTEGER:
					// a[c - 1] = "" + rs.getInt(c);
					// break;
					// case java.sql.Types.VARCHAR:
					// a[c - 1] = rs.getString(c);
					// break;
					default:
						a[c - 1] = String.valueOf(rs.getObject(c));
					}
				}
				TableItem ti = new TableItem(table, 0);
				ti.setText(a);
			}
		} else {
			int n = c.createStatement().executeUpdate(query);
			final TableColumn col = new org.eclipse.swt.widgets.TableColumn(
					table, 0);
			col.setText("result");
			col.setWidth(20 * 8);

			TableItem ti = new TableItem(table, 0);
			ti.setText("" + n);

		}
		input.setText("");
	}
}
