package dsv.buecher;

import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AvisoProfesEditor {
	Shell shell;

	public AvisoProfesEditor(final DsvBuecher parent) throws SQLException {
		shell = new Shell(Display.getCurrent(), SWT.CLOSE | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell
				.setText("Textfeld-Bemerkung für Listen Kontrolle Dateneingabe PC");
		{
			Label l0 = new Label(shell, SWT.WRAP);
			l0
					.setText("Bemerkung eingeben für Listen 'Kontrolle Dateneingabe PC', die den Fachleitern zur Korrektur/Aktualisierung der Bücher gegeben werden.");

			l0.setLayoutData(new GridData(SWT.FILL, 0, true, false, 1, 1));
		}
		{
			Label l0 = new Label(shell, SWT.WRAP);
			l0
					.setText("Beim Schließen des Fensters wird die Bemerkung automatisch gespeichert.");
			l0.setLayoutData(new GridData(SWT.FILL, 0, true, false, 1, 1));
		}
		final StyledText styledText = new StyledText(shell, SWT.MULTI
				| SWT.BORDER | SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		styledText.setText(parent.ds.getAvisoProfeText());
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					parent.ds.setAvisoProfeText(styledText.getText());
				} catch (SQLException e) {
					parent.showError(e);
				}
			}

		});
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}

	}

}
