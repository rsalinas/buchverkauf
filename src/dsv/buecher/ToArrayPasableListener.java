/**
 * 
 */
package dsv.buecher;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

final class ToArrayPasableListener<T extends ToArrayPassable> implements
		Listener {
	@SuppressWarnings("unused")
	private final SimpleLogger sl;
	private final PassableGenericRequestInterface<T> req;

	ToArrayPasableListener(SimpleLogger parent,
			PassableGenericRequestInterface<T> req) {
		this.req = req;
		this.sl = parent;
	}

	@Override
	public void handleEvent(Event event) {
		TableItem item = (TableItem) event.item;
		ToArrayPassable sch;
		sch = req.get(event.index);
		if (sch != null) {
			item.setText(sch.toStringArray());
			item.setData(sch);
		}
	}
}