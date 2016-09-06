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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dsv.buecher.DsvBuecher;
import dsv.buecher.DsvBuecherDatasource;
import dsv.buecher.RechnungInfo;

public class ReportKasseDetail {
	private final static String headers[] = { "#", "Datum", "Vorname", "Name",
			"Klasse", "Summe" };
	final static float[] anchuras = new float[] { 3f, 7, 6, 8, 3, 3.5f };

	public ReportKasseDetail(DsvBuecher parent, ArrayList<RechnungInfo> l,
			Date data) throws SQLException, MalformedURLException, IOException,
			DocumentException {
		DsvBuecherDatasource ds = parent.ds;
		File fich = File.createTempFile("Inventar", ".pdf");
		Document document = new Document(PageSize.A4);

		@SuppressWarnings("unused")
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(fich));
		{
			final HeaderFooter hf = new HeaderFooter(new Phrase(
					new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())
							+ ". Seite "), new Phrase());
			hf.setAlignment(HeaderFooter.ALIGN_RIGHT);
			hf.setBorder(Rectangle.NO_BORDER);
			document.setFooter(hf);
		}

		document.open();
		Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN,
				Font.DEFAULTSIZE, Font.NORMAL, new Color(0x0, 0x00, 0x00));

		f2.setSize(10);

		com.lowagie.text.Image image = com.lowagie.text.Image
				.getInstance(getClass().getResource("/logo.png"));
		image.scaleAbsolute(100, 30);
		PdfPTable htable = new PdfPTable(new float[] { 40, 60 });
		// document.add(image);
		htable.setWidthPercentage(100);
		final PdfPCell c1 = new PdfPCell(image);
		final PdfPCell c2 = new PdfPCell(new Phrase(MessageFormat.format(
				"Schuljahr: {0}", ds.getRunningYearName())));

		{
			c1.setBorder(Rectangle.NO_BORDER);
			c2.setBorder(Rectangle.NO_BORDER);
			htable.addCell(c1);
			PdfPTable htable2 = new PdfPTable(new float[] { 1 });
			htable2.addCell(c2);
			final Font f14 = new Font(new Phrase().font());
			// f14.setSize(12);
			final Phrase p3 = new Phrase("Kassendetail "
					+ (data != null ? "für " + data : " (insgesamt)"), f14);
			f14.setStyle(Font.BOLD);
			final PdfPCell c3 = new PdfPCell(p3);

			c3.setBorder(Rectangle.NO_BORDER);
			htable2.addCell(c3);
			final PdfPCell c4 = new PdfPCell(htable2);
			c4.setBorder(Rectangle.NO_BORDER);
			htable.addCell(c4);
			document.add(htable);
		}

		document.add(new Paragraph());

		PdfPTable table;

		table = new PdfPTable(anchuras);
		table.setWidthPercentage(100);

		for (String h : headers) {
			final PdfPCell cell = new PdfPCell(new Phrase(h));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			// if (h.equals("Bücheranzahl"))
			// cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}
		table.setHeaderRows(1);
		double summe = 0;
		for (RechnungInfo ri : l) {
			{
				final PdfPCell c12 = new PdfPCell(new Paragraph(ri.id + " "));
				c12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				c12.setRightIndent(10);
				table.addCell(c12);
			}
			table.addCell(new Phrase(new SimpleDateFormat("dd.MM.yyyy HH:mm")
					.format(new Date(ri.datum.getTime()))));
			final Font f10 = new Font(new Phrase().font());
			f10.setSize(10);
			table.addCell(new Phrase(ri.schueler.vorName, f10));
			table.addCell(new Phrase(ri.schueler.name, f10));
			table.addCell(new Phrase(ri.schueler.klasse, f10));
			{
				final PdfPCell c12 = new PdfPCell(new Paragraph(DsvBuecher
						.currency(ri.amount)));
				summe += ri.amount;
				c12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				c12.setRightIndent(10);
				table.addCell(c12);
			}
			//
			// }
		}
		table.setSpacingBefore(50);
		document.add(table);
		document
				.add(new Paragraph("Insgesamt: " + DsvBuecher.currency(summe)));
		document.newPage();

		document.close();
		Desktop.getDesktop().open(fich);
	}
}
