package dsv.buecher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DsvBuecher implements SimpleLogger {
	public final static DecimalFormat df = new DecimalFormat("0.00");

	public static void close(Connection c) {
		if (c != null)
			try {
				c.close();
			} catch (SQLException e) {
			}
	}

	public static void close(ResultSet rs) {
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
			}

	}

	public static String currency(double x) {
		return df.format(x) + " €";
	}

	public static String[] joinArrays(final String[] a1, final String[] a2) {
		final String[] a3 = Arrays.copyOf(a1, a1.length + a2.length);
		System.arraycopy(a2, 0, a3, a1.length, a2.length);
		return a3;
	}

	public static void main(String[] args) throws Exception {
		String filename = "buecher.db";
		if (args.length == 1)
			filename = args[0];
		boolean deleted = new File("error.txt").delete();

		try {
			new DsvBuecher(filename).makeGui();
		} catch (Exception e) {
			try {
				PrintWriter pw = new PrintWriter(new FileOutputStream(
						"error.txt", true));
				e.printStackTrace(pw);
				pw.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

		}
	}

	public final Display display = new Display();
	public final DsvBuecherDatasource ds;

	private Shell shell;

	private final boolean showSplash = true;

	private DsvBuecherSplash splash;

	public TabFolder tabFolder;

	public final ExecutorService xs = Executors
			.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					return t;
				}

			});
	public SchuelerTab schuelerTab;
	private Label statusLabel;
	public RechnungenTab rtab;
	public KlassenTab klassenTab;
	public BuecherTab buecherTab;

	public DsvBuecher(String filename) throws SQLException,
			ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		ds = new DsvBuecherDatasource("jdbc:sqlite:" + filename);
	}

	public void f5() {
		final Object h = tabFolder.getSelection()[0].getData();
		if (h instanceof TabFolderHolder)
			((TabFolderHolder) h).f5();
	}

	public void modelChanged() {
		for (TabItem h : tabFolder.getItems())
			if (h.getData() instanceof TabFolderHolder) {
				System.err.println("notificando " + h.getData());
				((TabFolderHolder) h.getData()).modelChanged();
			}

	}

	public Shell getShell() {
		return shell;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private String getTitle() throws SQLException {
		return "DSV - Bücherverwaltung - "
				+ ds.getRunningYear()
				+ "/"
				+ new DecimalFormat("00")
						.format((ds.getRunningYear() + 1) % 100);
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void makeGui() throws Exception {
		LoginComposite lc = LoginComposite.create(display, this);
		lc.waitForClosed();
		System.err.println("user: " + lc.login);
		try {
			pm = new PermissionManager(lc.login, lc.password);
			int anyoPedido = ds.getRunningYear();
			ArrayList<Integer> anyosDisponibles = ds.getSchuljahre();
			Integer ultimoAnyo = anyosDisponibles.get(0);
			if (!pm.can(DsvbPermission.VIEW_OLD_YEARS)
					&& anyoPedido != ultimoAnyo)
				throw new Exception(
						"Vd no puede acceder a datos de años pasados. Consulte con un administrador.");
		} catch (Exception e) {
			showError(e);
			return;
		}
		makeGui2();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		ds.close();
		ds.dump();
	}

	/**
	 * @throws SQLException
	 */
	private void makeGui2() throws SQLException {
		long t0 = System.currentTimeMillis();
		if (showSplash)
			splash = new DsvBuecherSplash(display, t0, "/logo.png");
		shell = new Shell(display);
		shell.setImage(new Image(display, getClass().getResourceAsStream(
				"/logo.png")));
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.numColumns = 1;
		shell.setLayout(gridLayout);
		shell.setText(getTitle());
		// if (pm.can(DsvbPermission.MENU))
		new DsvBuecherMenuBar(shell, this);
		tabFolder = new TabFolder(shell, SWT.BORDER);
		tabFolder.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item.getData() instanceof TabFolderHolder)
					((TabFolderHolder) e.item.getData()).onFocus();
			}
		});
		statusLabel = new Label(shell, SWT.BOTTOM);
		statusLabel.setText("Status");
		if (pm.can(DsvbPermission.ADVANCED))
			klassenTab = new KlassenTab(tabFolder, this);
		if (pm.can(DsvbPermission.ADVANCED))
			schuelerTab = new SchuelerTab(tabFolder, this);
		if (pm.can(DsvbPermission.ADVANCED))
			buecherTab = new BuecherTab(tabFolder, this);
		if (pm.can(DsvbPermission.ADVANCED))
			new BestellungenTab(tabFolder, this);
		if ((pm.can(DsvbPermission.RECHNUNGTAB) /*
												 * && ds.getBuchBestellungen() >
												 * 0
												 */)
				|| pm.can(DsvbPermission.NEWYEARCHANGE)) {
			rtab = new RechnungenTab(tabFolder, this);
			rtab.onFocus();
		}
		if (pm.can(DsvbPermission.KASSETAB))
			new KasseTab(tabFolder, this);
		tabFolder.setSelection(tabFolder.getItems().length - 2);
		tabFolder.pack();

		System.err.println("getBuchungTypen()");
		System.err.println(ds.buchungstypVw .getBuchungTypen());
		System.err.println("getRemesas()");
		System.err.println(ds.remesaVw.getRemesas());
		
		shell.setMaximized(true);
		shell.open();
		if (splash != null)
			splash.close(2000);
	}

	public void showError(final Exception e1) {
		if (Display.getCurrent() == null) {
			System.err.println("Display is null");
			return;
		}
		Display.getCurrent().syncExec(new Runnable() {
			@Override
			public void run() {
				e1.printStackTrace();
				try {
					PrintWriter pw = new PrintWriter(new FileOutputStream(
							"error.txt", true));
					e1.printStackTrace(pw);
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				MessageBox mb = new MessageBox(shell != null ? shell
						: new Shell(display), SWT.ERROR | SWT.APPLICATION_MODAL);
				mb.setMessage(e1.getMessage());
				mb.setText("Fehler");
				mb.open();
			}
		});
	}

	public void setStatus(final String stateText) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				statusLabel.setText(stateText);
				statusLabel.pack();
			}
		});
	}

	public PermissionManager pm;

	public void forceShutdown() {

		MessageBox mb = new MessageBox(shell != null ? shell : new Shell(
				display), SWT.OK);
		mb.setMessage("Die Anwendung wird geschloßen. Bitte Anwendung neu starten. ");
		mb.setText("Neustart nötig");
		mb.open();
		System.exit(0);

	}

	public void showWarning(final String string) {
		Display.getCurrent().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mb = new MessageBox(shell != null ? shell
						: new Shell(display), SWT.ERROR | SWT.APPLICATION_MODAL);
				mb.setMessage(string);
				mb.setText("Vorsicht");
				mb.open();
			}
		});
	}
}
