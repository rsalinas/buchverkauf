package dsv.buecher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import com.lowagie.text.DocumentException;

public class RechnungForm {

	public static void show(Shell shell, final ArrayList<Schueler> l2,
			final DsvBuecher par) throws SQLException {

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				try {
					File filename;
					filename = File.createTempFile("Rechnung-", ".pdf");
					new RechnungPdfMaker().makeit(par.ds, filename.toString(),
							new ArrayList<DatosFactura>());
					backgroundPrint(filename, par);
					// Desktop.getDesktop().open(filename);
				} catch (IOException e1) {
					par.showError(e1);
				} catch (DocumentException e) {
					par.showError(e);
				} catch (SQLException e) {
					par.showError(e);
				}

			}
		});
	}

	public static void show2(Shell shell, final ArrayList<Schueler> l2,
			DsvBuecher par) throws SQLException {

		// final ArrayList<LineaFactura> l = par.ds.getRechnung(l2.num);
		// Shell s2 = new Shell(shell, SWT.RESIZE | SWT.CLOSE);
		// s2.setLayout(new GridLayout());
		// s2.setText(Arrays.toString(l2.toStringArray()));
		// new Label(s2, SWT.NONE).setText("label");
		// Button pdfButt = new Button(s2, SWT.PUSH);
		// pdfButt.setText("PDF");
		// pdfButt.addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseUp(MouseEvent e) {
		// try {
		// final File filename = File.createTempFile("pref", ".pdf");
		// System.err.println(filename);
		// new RechnungPdfMaker(l2, l, )
		// .makeit(filename.toString());
		// Desktop.getDesktop().open(filename);
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// }
		// });
		//
		// Table t = new Table(s2, SWT.VIRTUAL);
		//
		// String[] headers = { "Titel", "Preis" };
		// t.setHeaderVisible(true);
		// t.setLinesVisible(true);
		// for (int i = 0; i < headers.length; i++) {
		// int width = i != 0 ? 20 : 50;
		// TableColumn column = new TableColumn(t, SWT.NONE);
		// column.setWidth((int) (width * 8));
		// column.setText(headers[i]);
		// }
		// t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// t.setItemCount(l.size());
		// t.addListener(SWT.SetData, new Listener() {
		// @Override
		// public void handleEvent(final Event event) {
		// final TableItem item = (TableItem) event.item;
		// item.setText(l.get(event.index).toStringArray());
		// }
		// });
		// s2.open();
	}

	static void backgroundPrint(File filename, final DsvBuecher gui)
			throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("printer.cfg"));

		String[] cmd = { props.getProperty("acrobat"), "/t",
				filename.toString() };
		final Process p = Runtime.getRuntime().exec(cmd);
		new Thread() {
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(p
						.getInputStream()));
				String l;
				try {
					while ((l = br.readLine()) != null)
						System.err.println("line: " + l);
				} catch (IOException e) {
					gui.showError(e);
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(p
						.getErrorStream()));
				String l;
				try {
					while ((l = br.readLine()) != null)
						System.err.println("eline: " + l);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

}
