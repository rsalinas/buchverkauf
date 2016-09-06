package dsv.buecher.report;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dsv.buecher.Buch;
import dsv.buecher.DsvBuecher;
import dsv.buecher.DsvBuecherDatasource;
import dsv.buecher.Schueler;

public class ReportBuchKaeufer {
	public final static String dateFormat = "dd.MM.yyyy HH:mm:ss";
	String headers[] = { "Kl.", "Name", "Vorname", "" };

	public ReportBuchKaeufer(DsvBuecher parent, List<Buch> buecher,
			boolean mitRechnung, final String heading) throws SQLException,
			MalformedURLException, IOException, DocumentException {

		boolean empty = true;
		final DsvBuecherDatasource ds = parent.ds;
		assert ds != null;
		File fich = File.createTempFile("BuchKaeufer", ".pdf");
		// hm.put(klasse.name, parent.ds.getSchuelerBestellungenInKlasse(
		// klasse.name, BestellungState.NICHT_BESTELLTE,
		// "klasse,name,vorname"));

		Document document = new Document(PageSize.A4);
		@SuppressWarnings("unused")
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(fich));
		System.err.println("escribiendo informe a : " + fich);
		document.open();
		Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN,
				Font.DEFAULTSIZE, Font.NORMAL, new Color(0x0, 0x00, 0x00));

		f2.setSize(10);
		for (Buch buch : buecher) {
			final List<Schueler> kaeufer = mitRechnung ? parent.ds
					.getBuyersForBook(buch.code) : parent.ds
					.getBestellerOhneRechungForBook(buch.code);
			if (kaeufer.isEmpty())
				continue;
			else
				empty = false;

			com.lowagie.text.Image image = com.lowagie.text.Image
					.getInstance(getClass().getResource("/logo.png"));
			image.scaleAbsolute(100, 30);
			PdfPTable htable = new PdfPTable(new float[] { 40, 60 });
			// document.add(image);
			htable.setWidthPercentage(100);
			final PdfPCell c1 = new PdfPCell(image);
			PdfPTable htable2 = new PdfPTable(new float[] { 1 });

			final PdfPCell c2 = new PdfPCell(new Phrase(kaeufer.size() + " "
					+ heading + ":"));
			c1.setBorder(Rectangle.NO_BORDER);
			c2.setBorder(Rectangle.NO_BORDER);
			htable.addCell(c1);
			htable2.addCell(c2);
			final Font f14 = new Font(new Phrase().font());
			// f14.setSize(12);
			Schueler[] kaeuferArray = kaeufer.toArray(new Schueler[0]);
			Arrays.sort(kaeuferArray, new Comparator<Schueler>() {

				@Override
				public int compare(Schueler o1, Schueler o2) {
					if (!o1.klasse.equals(o2.klasse))
						return o1.klasse.compareTo(o2.klasse);
					else if (!o1.name.equals(o2.name))
						return o1.name.compareTo(o2.name);
					else
						return o1.vorName.compareTo(o2.vorName);
				}
			});
			final Phrase p3b = new Phrase(buch.verlag.name);
			f14.setStyle(Font.BOLD);
			final Phrase p3 = new Phrase(buch.titel, f14);

			final PdfPCell c3 = new PdfPCell(p3);
			final PdfPCell c3b = new PdfPCell(p3b);

			c3.setBorder(Rectangle.NO_BORDER);
			c3b.setBorder(Rectangle.NO_BORDER);

			htable2.addCell(c3);
			htable2.addCell(c3b);
			final PdfPCell c4 = new PdfPCell(htable2);
			c4.setBorder(Rectangle.NO_BORDER);
			htable.addCell(c4);
			document.add(htable);
			document.add(new Paragraph());

			PdfPTable table;
			table = new PdfPTable(new float[] { 1, 6, 4, 1 });
			table.setWidthPercentage(80);

			for (String h : headers) {
				final PdfPCell cell = new PdfPCell(new Phrase(h));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
			}
			table.setHeaderRows(1);

			for (Schueler s : kaeuferArray) {
				table.addCell(new Phrase(s.klasse));
				table.addCell(new Phrase(s.name));
				table.addCell(new Phrase(s.vorName));
				table.addCell(new Phrase(""));
			}
			table.setSpacingBefore(50);
			document.add(table);

			String now = new SimpleDateFormat(dateFormat).format(new Date());

			final Paragraph datePhrase = new Paragraph("Liste erstellt am: "
					+ now + ".    Schuljahr: " + ds.getRunningYearName());
			datePhrase.setSpacingBefore(50);
			document.add(datePhrase);
			document.newPage();
		}

		if (!empty) {
			document.close();
			Desktop.getDesktop().open(fich);
		} else {
			MessageBox mb = new MessageBox(parent.getShell());
			mb.setText("Beachte");
			mb.setMessage("Die gewählten Bücher haben keine "
					+ (mitRechnung ? " Käufer" : "Besteller"));
			mb.open();

		}
		Display.getCurrent().beep();
	}
}
