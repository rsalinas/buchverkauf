package dsv.buecher;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MassivePriceChanger extends Composite {
	private boolean mustsave = false;
	private final HashMap<Buch, Text> texts = new HashMap<Buch, Text>();
	private final DsvBuecherDatasource ds;
	private final DsvBuecher gui;

	public MassivePriceChanger(Shell s2, final ArrayList<Buch> buecher,
			DsvBuecherDatasource ds, final DsvBuecher gui) {
		super(s2, 0);
		shell = getShell();
		shell.setText("Buchpreisbearbeitung (mit Tab-Taste zum nächsten Buch)");
		display = shell.getDisplay();

		this.ds = ds;
		this.gui = gui;

		setLayout(new GridLayout(1, false));

		getShell().addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				for (Entry<Buch, Text> i : texts.entrySet())
					if (i.getKey().preis != parseHumanPrice(i.getValue()
							.getText())) {
						arg0.doit = false;
						getDisplay().beep();
						System.err.println("mal: " + i.getKey().preis + " vs "
								+ parseHumanPrice(i.getValue().getText()));
					}
			}
		});
		final ScrolledComposite sc = new ScrolledComposite(this, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// sc.setLayout(new FillLayout());
		Composite child = new Group(sc, SWT.NONE) {
			@Override
			protected void checkSubclass() {
			}

			{
				setText("Bücher");
				setLayout(new GridLayout(5, false));
				new Label(this, 0).setText("Kl.stufe");
				new Label(this, 0).setText("Verlag");
				new Label(this, 0).setText("Titel");
				new Label(this, 0).setText("ISBN");
				new Label(this, 0).setText("Preis (€)");
				for (Buch buch : buecher) {
					Label stufe = new Label(this, 0);
					stufe.setText("" + buch.minjahr);
					stufe.setAlignment(SWT.RIGHT);
					new Label(this, 0).setText(buch.verlag.name);
					new Label(this, 0).setText(buch.titel);
					new Label(this, 0).setText(buch.isbn);
					final Text price = new Text(this, 0);
					price.setData(buch);
					texts.put(buch, price);
					price.setText(humanizePrice(buch.preis));
					price.addVerifyListener(new VerifyListener() {

						@Override
						public void verifyText(VerifyEvent arg0) {
							try {
								parseHumanPrice(price.getText() + arg0.text);
							} catch (NumberFormatException e) {
								getDisplay().beep();
								arg0.doit = false;
							}
						}

					});
					// price.pack();

					price.setSize(60, price.getSize().y);
					price.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false, 1, 1));

				}
			}
		};
		sc.setContent(child);

		sc.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(sc.getParent().computeSize(r.width, SWT.DEFAULT));
			}
		});

		// Set the minimum size
		sc.setMinSize(400, 400);

		// Expand both horizontally and vertically
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		new Composite(this, 0) {
			{

				setLayout(new RowLayout(SWT.HORIZONTAL));
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
						1));
				Button save = new Button(this, SWT.PUSH);
				{
					Button close = new Button(this, SWT.PUSH);
					close.setText("S&chließen");
					close.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							for (Entry<Buch, Text> i : texts.entrySet())
								if (i.getKey().preis != parseHumanPrice(i
										.getValue().getText())) {
									getDisplay().beep();
									System.err.println("mal: "
											+ i.getKey().preis
											+ " vs "
											+ parseHumanPrice(i.getValue()
													.getText()));
									return;
								}
							getShell().dispose();
						}

					});

				}
				save.setText("&Speichern");
				save.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							save();
						} catch (SQLException e1) {
							gui.showError(e1);
						}
					}

				});

			}
		};
	}

	protected String humanizePrice(double preis) {
		DecimalFormat df2 = new DecimalFormat("0.00###");
		return df2.format(preis);
	}

	private final static NumberFormat nf = NumberFormat.getInstance();

	protected double parseHumanPrice(String s) throws NumberFormatException {
		try {
			return nf.parse(s).doubleValue();
		} catch (ParseException e) {
			gui.showError(e);
			throw new NumberFormatException();
		}
	}

	private final Shell shell;
	private final Display display;

	public boolean show() {

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return mustsave;
	}

	private void save() throws SQLException {
		for (Entry<Buch, Text> i : texts.entrySet()) {
			final Buch buch = i.getKey();
			final double oldPrice = buch.preis;
			final double newPrice = parseHumanPrice(i.getValue().getText());

			if (oldPrice != newPrice) {
				buch.preis = newPrice;
				ds.buchVw.saveBuchPrice(buch);
				mustsave = true;
			}
		}

	}
}
