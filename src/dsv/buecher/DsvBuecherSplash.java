package dsv.buecher;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DsvBuecherSplash {
	private final Shell splash = new Shell(SWT.ON_TOP);
	private Image image;
	private final Device display;
	private final long startts;
	private final String imagefile;

	public DsvBuecherSplash(Device display, long startts, String imagefile) {
		super();
		this.display = display;
		this.startts = startts;
		this.imagefile = imagefile;
		showSplash();
	}

	private void showSplash() {
		final InputStream imageInputStream = getClass().getResourceAsStream(
				imagefile);
		if (imageInputStream == null) {
			System.err.println("cannot find image: " + imagefile);
			return;
		}
		image = new Image(display, imageInputStream);
		// final ProgressBar bar = new ProgressBar(splash, SWT.NONE);
		// bar.setMaximum(count[0]);
		Label label = new Label(splash, SWT.NONE);
		label.setImage(image);
		label.setSize(120 * 3, 30 * 3);
		// label.pack();
		FormLayout layout = new FormLayout();
		splash.setLayout(layout);
		FormData labelData = new FormData();
		labelData.right = new FormAttachment(100, 0);
		labelData.bottom = new FormAttachment(100, 0);
		label.setLayoutData(labelData);
		FormData progressData = new FormData();
		progressData.left = new FormAttachment(0, 5);
		progressData.right = new FormAttachment(100, -5);
		progressData.bottom = new FormAttachment(100, -5);
		// bar.setLayoutData(progressData);
		splash.pack();
		Rectangle splashRect = splash.getBounds();
		Rectangle displayRect = display.getBounds();
		int x = (displayRect.width - splashRect.width) / 2;
		int y = (displayRect.height - splashRect.height) / 2;
		splash.setLocation(x, y);
		splash.open();

	}

	/**
	 * @param minSplashTime
	 * 
	 */
	public void close(int minSplashTime) {
		final long dif = System.currentTimeMillis() - startts;
		if (dif < minSplashTime)
			try {
				Thread.sleep(minSplashTime - dif);
			} catch (InterruptedException unused) {
			}

		splash.close();
		if (image != null)
			image.dispose();
	}

}
