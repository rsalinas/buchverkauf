package dsv.buecher;

import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FachFrame {

	private final Shell sh;

	public FachFrame(final DsvBuecher gui, Shell pshell, final Fach data) {
		sh = new Shell(pshell, SWT.RESIZE | SWT.CLOSE | SWT.APPLICATION_MODAL);
		sh.setText("Fachverwaltung");

		sh.setLayout(new GridLayout(2, false));

		new Label(sh, 0).setText("Name:");
		final Text nameText = new Text(sh, 0);

		nameText.setText(data.name);

		for (Control w : new Control[] { nameText })
			w
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false, 1, 1));
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
							gui.ds.faecherVw.insertFach(data);
						} catch (SQLException e) {
							gui.showError(e);
						}
					}
				});
				// MessageBox mb = new MessageBox(sh, SWT.OK);
				// mb.setText("Grabación correcta");
				// mb.setMessage("Los datos han sido correctamente salvados: "
				// + data);
				// mb.open();
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
