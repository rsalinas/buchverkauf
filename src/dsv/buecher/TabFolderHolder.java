package dsv.buecher;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class TabFolderHolder {
	protected TabItem item;
	protected DsvBuecher gui;
	protected Composite composite;
	protected String tabStatusLine = "";

	public TabFolderHolder(TabFolder tabFolder, String title, DsvBuecher par2) {
		item = new TabItem(tabFolder, SWT.NONE);
		composite = new Composite(tabFolder, SWT.NONE);
		item.setControl(composite);
		item.setData(this);
		item.setText(title);
		this.gui = par2;
		tabStatusLine = title;
	}

	public void onFocus() {
		gui.setStatus(tabStatusLine);
	}

	public void setStatusLine(String l) {
		tabStatusLine = l;
		gui.setStatus(tabStatusLine);
	}

	public void f5() {

	}

	@Override
	public String toString() {
		return item.getText();
	}

	public void fill(HashMap<StandardMenus, Menu> hm) {
		
	}

	public void modelChanged() {

	}
}
