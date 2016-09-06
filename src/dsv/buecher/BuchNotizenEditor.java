package dsv.buecher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

public class BuchNotizenEditor {

	public BuchNotizenEditor(DsvBuecher dsvBuecher, Buch b) {
		Shell shell = new Shell(dsvBuecher.getShell(), SWT.RESIZE | SWT.CLOSE
				| SWT.APPLICATION_MODAL);
		shell.setText("Edición de notas para " + b.titel);
		shell.setLayout(new FillLayout());
		new StyledText(shell, SWT.MULTI | SWT.WRAP)
				.setText("Esta ventana está por acabar");
		shell.open();
	}

}
