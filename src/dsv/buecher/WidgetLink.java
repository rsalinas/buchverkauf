package dsv.buecher;

import org.eclipse.swt.widgets.Widget;

public class WidgetLink {
	public WidgetLink(Widget widget, Object o, String varname) {
		super();
		this.widget = widget;
		this.o = o;
		this.varname = varname;
	}

	final Widget widget;
	final String varname;
	final Object o;

	@Override
	public String toString() {
		return varname;
	}
}
