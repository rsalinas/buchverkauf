package dsv.buecher;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class PresDesktop {
	private final Display display = Display.getDefault();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PresDesktop();
	}

	int x = 0;

	public PresDesktop() {
		Image image = new Image(display, getClass().getResourceAsStream(
				"/mailbox-icon.png"));
		Shell shell = new Shell(display);
		Tray tray = display.getSystemTray();
		if (tray == null)
			System.exit(1);

		final ToolTip tip = new ToolTip(shell, SWT.BALLOON
				| SWT.ICON_INFORMATION);
		tip.setMessage("Balloon Message Goes Here!");
		final TrayItem item = new TrayItem(tray, SWT.NONE);

		item.setImage(image);
		tip.setText("Balloon Title goes here.");
		item.setToolTip(tip);
		item.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.err.println("selected!");
				tip.setText("Active: " + x++);
				tip.setVisible(true);

				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						display.asyncExec(new Runnable() {

							@Override
							public void run() {

								tip.setVisible(false);
							}
						});

					}
				}, 1000);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		final Menu menu = new Menu(shell, SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("&Conectar");
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("&Preferencias");

		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Fichar entrada");
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Fichar salida");
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Show &Tooltip");
		MenuItem exitButton = new MenuItem(menu, SWT.PUSH);
		exitButton.setText("Salir");
		exitButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				item.dispose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		// Add tooltip visibility to menu item.
		menuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				tip.setVisible(true);
				tip.setText("Date: " + new Date());
			}
		});
		// Add menu detection listener to tray icon.
		item.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});

		System.err.println("esperando q acabe bucle pral");
		while (!item.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		System.err.println("salió del bucle pral");
		timer.cancel();
	}

	Timer timer = new Timer();
}
