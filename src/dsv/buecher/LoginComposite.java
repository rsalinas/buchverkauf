package dsv.buecher;

import java.sql.SQLException;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginComposite extends Composite {
	private final DsvBuecherDatasource ds;
	private final Text userText;
	private final Text passText;
	public String login;
	public String password;

	public LoginComposite(Composite parentComposite, final DsvBuecher gui)
			throws SQLException {
		super(parentComposite, SWT.NONE);
		setLayout(new GridLayout(2, false));
		this.ds = gui.ds;
		new Label(this, 0).setText("Benutzer");
		userText = new Text(this, 0);
		userText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		new Label(this, 0).setText("Kennwort");
		passText = new Text(this, SWT.PASSWORD);
		passText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		new Label(this, 0).setText("Schuljahr");
		final Combo schulJahrList = new Combo(this, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		schulJahrList.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false,
				false, 1, 1));
		schulJahrList.setSize(100, 100);
		// schulJahrList.setSize(100, 100);
		// new Label(this, 0).setText("Sprache");
		// List sprachList = new List(this, SWT.NONE);
		// sprachList
		// .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
		// 1));

		for (int j : ds.getSchuljahre())
			schulJahrList.add(j + "/"
					+ new DecimalFormat("00").format((j + 1) % 100));
		schulJahrList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				try {
					ds.setRunningYear(Integer.parseInt(schulJahrList.getText()
							.split("/")[0]));
				} catch (Exception e) {
					gui.showError(e);

				}
				getShell().close();
			}
		});
		if (schulJahrList.getItems().length > 0)
			schulJahrList.select(0);
		// for (String l : new String[] { "Deutsch" })
		// sprachList.add(l);

		Button but = new Button(this, SWT.PUSH);
		but.setText("Programm starten");
		getShell().setDefaultButton(but);
		getShell().addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				password = passText.getText();
				login = userText.getText();
				try {
					gui.ds.setRunningYear(Integer.parseInt(schulJahrList
							.getText().split("/")[0]));
				} catch (Exception e) {
					gui.showError(e);
				}
			}
		});
		but.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
		});

		getShell().pack();
		RauSwtUtil.center(getShell());
		// getShell().setSize(500, 300);
	}

	public static LoginComposite create(Display display, DsvBuecher gui)
			throws SQLException {
		final Shell shell = new Shell(display);
		shell.setText("Schuljahrwahl");
		shell.setLayout(new FillLayout());
		LoginComposite lc = new LoginComposite(shell, gui);
		RauSwtUtil.center(shell);
		shell.setImage(new Image(display, gui.getClass().getResourceAsStream(
				"/logo.png")));
		shell.open();
		return lc;
	}

	public void waitForClosed() {
		while (!isDisposed()) {
			if (!getDisplay().readAndDispatch()) {
				getDisplay().sleep();
			}
		}
	}
}
