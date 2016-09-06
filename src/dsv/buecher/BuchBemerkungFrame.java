package dsv.buecher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BuchBemerkungFrame {
	private final Shell shell;
	private final Text abrev;
	private final Text spa;
	private final Text deu;
	private final Button save;
	public boolean mustSave = false;

	public BuchBemerkungFrame(Shell pshell, final BuchBemerkung buchMarke) {
		this.shell = new Shell(pshell, SWT.RESIZE | SWT.CLOSE
				| SWT.APPLICATION_MODAL);
		shell.setText("Bücherbemerkung");
		shell.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
		new Label(shell, 0).setText("Abkürzung:");
		abrev = new Text(shell, 0);
		new Label(shell, 0).setText("Beschreibung auf Deutsch:");
		deu = new Text(shell, 0);
		new Label(shell, 0).setText("Beschreibung auf Spanisch:");
		spa = new Text(shell, 0);

		for (Control i : new Control[] { abrev, deu, spa })
			i
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false, 1, 1));
		save = new Button(shell, SWT.PUSH);
		save.setText("Speichern");
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (deu.getText().length() == 0 || spa.getText().length() == 0) {
					Display.getCurrent().syncExec(new Runnable() {
						@Override
						public void run() {

							MessageBox mb = new MessageBox(
									shell != null ? shell : new Shell(shell
											.getDisplay()), SWT.ERROR
											| SWT.APPLICATION_MODAL);
							mb.setMessage("Alle Felder ausfüllen");
							mb.setText("Fehler");
							mb.open();
						}
					});
					return;
				}
				buchMarke.shortname = abrev.getText();
				buchMarke.dsc = deu.getText() + "|" + spa.getText();
				mustSave = true;
				shell.close();
			}
		});
		shell.pack();
		shell.setSize(500, shell.getSize().y);
		shell.open();

		abrev.setText(String.valueOf(buchMarke.shortname));
		final String[] ss = buchMarke.dsc.split("\\|");
		if (ss.length == 2) {
			deu.setText(ss[0]);
			spa.setText(ss[1]);
		}

	}

	public void open() {
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

}
