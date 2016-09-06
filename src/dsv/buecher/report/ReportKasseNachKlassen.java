package dsv.buecher.report;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.widgets.Display;

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

import dsv.buecher.DsvBuecher;
import dsv.buecher.DsvBuecherDatasource;
import dsv.buecher.Klasse;
import dsv.buecher.SchuelerBalance;

public class ReportKasseNachKlassen {
	String headers[] = { "", "Name", "Vorname", "Bezahlt", "Zurückb.", "Dif." };

	public ReportKasseNachKlassen(DsvBuecher parent) throws SQLException,
			MalformedURLException, IOException, DocumentException {
		DsvBuecherDatasource ds = parent.ds;
		File fich = File.createTempFile("kassenkontrolle_", ".pdf");

		HashMap<String, ArrayList<SchuelerBalance>> hm = new HashMap<String, ArrayList<SchuelerBalance>>();
		HashMap<String, Float> pagadohm = new HashMap<String, Float>();
		HashMap<String, Float> devueltohm = new HashMap<String, Float>();

		Document document = new Document(PageSize.A4);
		@SuppressWarnings("unused")
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(fich));
		document.open();
		Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN,
				Font.DEFAULTSIZE, Font.NORMAL, new Color(0x0, 0x00, 0x00));

		f2.setSize(10);
		String now = new SimpleDateFormat(ReportAlumnosPendientes.dateFormat)
				.format(new Date());
		ArrayList<Klasse> klassen = ds.klassenVw.getKlassen("jahr,klasse");

		for (Klasse klasse : klassen) {
			ArrayList<SchuelerBalance> l = parent.ds
					.getSchuelerBalanceInKlasse(klasse.name, "name,vorname");
			hm.put(klasse.name, l);
			if (!l.isEmpty()) {
				com.lowagie.text.Image image = com.lowagie.text.Image
						.getInstance(getClass().getResource("/logo.png"));
				image.scaleAbsolute(100, 30);
				PdfPTable htable = new PdfPTable(new float[] { 40, 60 });
				// document.add(image);
				htable.setWidthPercentage(100);
				final PdfPCell c1 = new PdfPCell(image);
				PdfPTable htable2 = new PdfPTable(new float[] { 1 });

				final PdfPCell c2 = new PdfPCell(new Phrase(
						MessageFormat.format("Klasse: {0} ({1})", klasse,
								ds.getRunningYearName())));
				c1.setBorder(Rectangle.NO_BORDER);
				c2.setBorder(Rectangle.NO_BORDER);
				htable.addCell(c1);
				htable2.addCell(c2);
				final Font f14 = new Font(new Phrase().font());
				// f14.setSize(12);
				final Phrase p3 = new Phrase("Kassenkontrolle - Schüleranzahl: "
						+ l.size(), f14);
				f14.setStyle(Font.BOLD);
				final PdfPCell c3 = new PdfPCell(p3);

				c3.setBorder(Rectangle.NO_BORDER);
				htable2.addCell(c3);
				final PdfPCell c4 = new PdfPCell(htable2);
				c4.setBorder(Rectangle.NO_BORDER);
				htable.addCell(c4);
				document.add(htable);
				document.add(new Paragraph());

				PdfPTable table;
				table = new PdfPTable(new float[] { 1, 5, 5, 2, 2, 2 });
				table.setWidthPercentage(100);

				for (String h : headers) {
					final PdfPCell cell = new PdfPCell(new Phrase(h));
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);
				}
				table.setHeaderRows(1);

				float pagado = 0, devuelto = 0;
				for (SchuelerBalance kl : l) {
					table.addCell(new Phrase(""));
					table.addCell(new Phrase(kl.name));
					table.addCell(new Phrase(kl.vorName));
					table.addCell(new Phrase(kl.pagado != 0 ? DsvBuecher
							.currency(kl.pagado) : ""));
					table.addCell(new Phrase(kl.devuelto != 0 ? DsvBuecher
							.currency(kl.devuelto) : ""));
					table.addCell(new Phrase(
							kl.pagado - kl.devuelto != 0 ? DsvBuecher
									.currency(kl.pagado - kl.devuelto) : ""));
					pagado += kl.pagado;
					devuelto += kl.devuelto;
					pagadohm.put(klasse.name, pagado);
					devueltohm.put(klasse.name, devuelto);
				}
				{
					table.addCell(new Phrase(""));
					table.addCell(new Phrase("Gesamtbetrag"));
					table.addCell(new Phrase(""));
					table.addCell(currencyCell(pagado));
					table.addCell(currencyCell(devuelto));
					table.addCell(currencyCell(pagado - devuelto));
				}
				//
				// }
				table.setSpacingBefore(50);
				document.add(table);
				// if (p++ == 0)

				// final Paragraph datePhrase = new Paragraph(
				// "Liste erstellt am: " + now);
				// datePhrase.setSpacingBefore(50);
				// document.add(datePhrase);
				document.newPage();
			}
		}

		if (true) {
			com.lowagie.text.Image image = com.lowagie.text.Image
					.getInstance(getClass().getResource("/logo.png"));
			image.scaleAbsolute(100, 30);
			PdfPTable htable = new PdfPTable(new float[] { 40, 60 });
			// document.add(image);
			htable.setWidthPercentage(100);
			final PdfPCell c1 = new PdfPCell(image);
			PdfPTable htable2 = new PdfPTable(new float[] { 1 });

			final PdfPCell c2 = new PdfPCell(new Phrase(MessageFormat.format(
					"Kassenkontrolle {0}", ds.getRunningYearName())));
			c1.setBorder(Rectangle.NO_BORDER);
			c2.setBorder(Rectangle.NO_BORDER);
			htable.addCell(c1);
			htable2.addCell(c2);
			final Font f14 = new Font(new Phrase().font());
			// f14.setSize(12);
			final Phrase p3 = new Phrase("Übersichtstabelle", f14);
			f14.setStyle(Font.BOLD);
			final PdfPCell c3 = new PdfPCell(p3);

			c3.setBorder(Rectangle.NO_BORDER);
			htable2.addCell(c3);
			final PdfPCell c4 = new PdfPCell(htable2);
			c4.setBorder(Rectangle.NO_BORDER);
			htable.addCell(c4);
			document.add(htable);
			document.add(new Paragraph());

			PdfPTable table;
			table = new PdfPTable(new float[] { 2, 1, 1, 1, 1 });
			table.setWidthPercentage(100);

			String headers2[] = { "Klasse", "Schülerzahl", "Bezahlt",
					"Zurückbz.", "Dif." };
			for (String h : headers2) {
				final PdfPCell cell = new PdfPCell(new Phrase(h));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
			}
			table.setHeaderRows(1);

			int sum = 0;
			float sumPagado = 0, sumDevuelto = 0;
			for (Klasse klasse : klassen) {
				if (hm.get(klasse.name).size() != 0) {
					PdfPCell cellname = new PdfPCell(new Phrase(klasse.name));
					cellname.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cellname);
					PdfPCell cellnum = new PdfPCell(new Phrase(""
							+ hm.get(klasse.name).size()));
					cellnum.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cellnum);
					sum += hm.get(klasse.name).size();

					table.addCell(currencyCell(pagadohm.get(klasse.name)));
					table.addCell(currencyCell(devueltohm.get(klasse.name)));
					table.addCell(currencyCell(pagadohm.get(klasse.name)
							- devueltohm.get(klasse.name)));
					sumPagado += pagadohm.get(klasse.name);
					sumDevuelto += devueltohm.get(klasse.name);
				}
			}

			{
				PdfPCell cellname = new PdfPCell(new Phrase(""));
				cellname.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cellname);
				PdfPCell cellnum = new PdfPCell(new Phrase("" + sum));
				cellnum.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cellnum);
				table.addCell(currencyCell(sumPagado));
				table.addCell(currencyCell(sumDevuelto));
				table.addCell(currencyCell(sumPagado - sumDevuelto));
			}
			{
				PdfPCell cellname = new PdfPCell(new Phrase("Insgesamt"));
				cellname.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cellname);
				PdfPCell cellnum = new PdfPCell(new Phrase("" + sum));
				cellnum.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cellnum);
			}
			//
			// }
			table.setSpacingBefore(50);
			document.add(table);
			// if (p++ == 0)

			final Paragraph datePhrase = new Paragraph("Liste erstellt am: "
					+ now);
			datePhrase.setSpacingBefore(50);
			document.add(datePhrase);
			document.newPage();

		}
		document.close();
		Desktop.getDesktop().open(fich);
		Display.getCurrent().beep();

	}

	PdfPCell currencyCell(float v) {
		{
			final PdfPCell c = new PdfPCell(new Paragraph(
					DsvBuecher.currency(v)));
			c.setHorizontalAlignment(Element.ALIGN_RIGHT);
			c.setRightIndent(10);
			return c;
		}
	}
}
