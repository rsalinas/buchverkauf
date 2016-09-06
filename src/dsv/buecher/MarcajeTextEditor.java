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

public class MarcajeTextEditor {
	Shell shell;

	public MarcajeTextEditor(final DsvBuecher parent) throws SQLException {
		shell = new Shell(Display.getCurrent(), SWT.CLOSE | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Edición texto libre informe marcaje");
		new Label(shell, 0)
				.setText("Cada registro ha de ir en una línea aparte, apretando Intro");

		Label l0 = new Label(shell, 0);
		l0.setText("Texto en alemán | Texto en español | Otro comentario =>");
		l0.setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, false, 1, 1));

		Label l1 = new Label(shell, 0);
		l1.setText("Texto en alemán  (Texto en español): Otro comentario");
		l1.setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, false, 1, 1));
		final StyledText styledText = new StyledText(shell, SWT.MULTI
				| SWT.BORDER | SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		styledText.setText(parent.ds.getMarcajeText());
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					parent.ds.setMarcajeText(styledText.getText());
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
