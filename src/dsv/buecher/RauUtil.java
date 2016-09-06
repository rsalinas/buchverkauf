package dsv.buecher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class RauUtil {

	public static String parentize(Object o0, Object... oo) {
		StringBuilder sb = new StringBuilder('(');
		sb.append(o0.toString());
		for (Object o : oo) {
			sb.append(", ");
			if (o != null)
				sb.append(o.toString());
			else
				sb.append("(null)");
		}
		return sb.append(')').toString();
	}

	public static String separate(Object[] key, String string) {
		StringBuilder sb = null;
		for (Object s : key)
			if (sb == null)
				sb = new StringBuilder(String.valueOf(s));
			else
				sb.append(string).append(s);
		return sb == null ? "" : sb.toString();
	}

	public static String[] postfix(String[] in, String string) {
		String[] out = new String[in.length];
		for (int i = 0; i < out.length; i++)
			out[i] = in[i] + string;
		return out;
	}

	public static String[] getRepetition(int length, String string) {
		String[] a = new String[length];
		for (int i = 0; i < length; i++)
			a[i] = string;
		return a;
	}

	public static MenuItem createMenuItem(Menu helpmenu, int push,
			String string, SelectionAdapter selectionAdapter) {
		MenuItem mi = new MenuItem(helpmenu, push);
		mi.setText(string);
		mi.addSelectionListener(selectionAdapter);
		return mi;
	}

	public static Button createButton(Composite compo, String string,
			SelectionAdapter selectionAdapter) {
		Button b = new Button(compo, SWT.PUSH);
		b.setText(string);
		b.addSelectionListener(selectionAdapter);
		return b;

	}

}
