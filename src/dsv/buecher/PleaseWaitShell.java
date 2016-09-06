package dsv.buecher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class PleaseWaitShell {
	private final Shell s3;

	public PleaseWaitShell(Shell s2, String text) {
		s3 = new Shell(s2, SWT.CENTER | SWT.APPLICATION_MODAL);
		s3.setLayout(new GridLayout(1, false));
		new Label(s3, SWT.BORDER).setText(text);
		s3.pack();
		RauSwtUtil.center(s3);
		s3.open();
	}

	public void close() {
		s3.close();

	}

}
