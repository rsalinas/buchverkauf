package dsv.buecher;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class BillEmailSender {
	private ProgressBar bar;
	private final Display display;
	private final DsvBuecherDatasource ds;

	public BillEmailSender(DsvBuecher dsvBuecher) {
		display = dsvBuecher.display;
		ds = dsvBuecher.ds;
	}

	private Shell shell;
	private Label state;
	private final DecimalFormat df2 = new DecimalFormat("#.#");

	public void sendFacturasPorEmail() throws SQLException {
		final long t0 = System.currentTimeMillis();
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell = new Shell(display);
				shell.setLayout(new GridLayout(1, false));
				bar = new ProgressBar(shell, SWT.SMOOTH);
				bar.setBounds(10, 10, 200, 32);
				state = new Label(shell, SWT.NONE);
				state.setText("ETA: (estimating)");
				Button button = new Button(shell, SWT.PUSH);
				button.setText("Cancel");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						shell.close();
					}
				});
				state.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true, 1, 1));
				shell.pack();
				shell.open();
			}
		});

		final ArrayList<Schueler> schueler = ds.getSchueler();
		final int i[] = { 0 };

		for (Schueler s : schueler) {
			synchronized (i) {
				if (cancelled)
					break;
			}
			sendFacturasPorEmail(s);
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (shell.isDisposed()) {
						synchronized (i) {
							cancelled = true;
						}
						return;
					}
					bar.setSelection((int) (100.0 * i[0]++ / schueler.size()));
					long t1 = System.currentTimeMillis();
					double tpu = (1.0 * t1 - t0) / i[0];
					if (i[0] > 30)
						state.setText("ETA is: "
								+ df2.format((schueler.size() - i[0]) * tpu
										/ 1000));
				}
			});

		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!shell.isDisposed())
					shell.close();
			}
		});

	}

	boolean cancelled = false;

	private void sendFacturasPorEmail(Schueler s) {
		// // new RechnungPdfMaker().makeit(s, ds, dsvBuecher, "/tmp/Rechnung-"
		// // + s.email + ".pdf", new ArrayList<ChoosableBuch>(),
		// // new ArrayList<ChoosableBuch>());

	}
}
