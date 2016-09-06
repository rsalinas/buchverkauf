package dsv.buecher.report;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.swt.widgets.Display;

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

import dsv.buecher.BestelllisteStaticized;
import dsv.buecher.Buch;
import dsv.buecher.DsvBuecher;
import dsv.buecher.DsvBuecherDatasource;
import dsv.buecher.Verlag;
import dsv.buecher.DsvBuecherDatasource.BestellungDetail;

public class ReportVerlagBestellungKompakt {
	String headers[] = { "#", "Titel", "Verlag", "ISBN",
			BestelllisteStaticized.klassenCaption };

	public ReportVerlagBestellungKompakt(DsvBuecher parent)
			throws SQLException, MalformedURLException, IOException,
			DocumentException {
		DsvBuecherDatasource ds = parent.ds;
		File fich = File.createTempFile("Bestellliste", ".pdf");
		{
			// String fich = "c:/x.pdf";
			// step 1: creation of a document-object
			Document document = new Document(PageSize.A4.rotate());
			// Create a new Paragraph for the footer

			@SuppressWarnings("unused")
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(fich));
			System.err.println("escribiendo marcaje a : " + fich);
			// step 3: we open the document
			{
				final HeaderFooter hf = new HeaderFooter(new Phrase("Seite "),
						new Phrase());
				hf.setAlignment(HeaderFooter.ALIGN_RIGHT);
				hf.setBorder(Rectangle.NO_BORDER);
				document.setFooter(hf);
			}

			document.open();
			// {
			// Paragraph par = new Paragraph("Page ");
			// par.setAlignment(Element.ALIGN_RIGHT);
			//
			// // Add the RtfPageNumber to the Paragraph
			// par.add(new com.lowagie.text.rtf.field.RtfPageNumber());
			//
			// // Create an RtfHeaderFooter with the Paragraph and set it
			// // as a footer for the document
			// RtfHeaderFooter footer = new RtfHeaderFooter(par);
			// document.setFooter(footer);
			//
			// }
			Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN,
					Font.DEFAULTSIZE, Font.NORMAL, new Color(0x0, 0x00, 0x00));

			f2.setSize(10);

			ArrayList<Buch> buecher = ds.getBuecher(null);

			for (String land : new String[] { "e", "d", "l" }) {
				ArrayList<Buch> l2 = new ArrayList<Buch>();
				HashSet<String> verlageImLand = new HashSet<String>();
				HashMap<String, ArrayList<Buch>> buecherNachVerlag = new HashMap<String, ArrayList<Buch>>();
				for (Buch b : buecher)
					if (b.verlag.land.equals(land)) {
						verlageImLand.add(b.verlag.name);
						ArrayList<Buch> list = buecherNachVerlag
								.get(b.verlag.name);
						if (list == null)
							buecherNachVerlag.put(b.verlag.name,
									list = new ArrayList<Buch>());
						list.add(b);
						l2.add(b);
					}

				com.lowagie.text.Image image = com.lowagie.text.Image
						.getInstance(getClass().getResource("/logo.png"));
				image.scaleAbsolute(100, 30);
				PdfPTable htable = new PdfPTable(new float[] { 40, 60 });
				// document.add(image);
				htable.setWidthPercentage(100);
				final PdfPCell c1 = new PdfPCell(image);
				final PdfPCell c2 = new PdfPCell(new Phrase(MessageFormat
						.format("Schuljahr: {0}", ds.getRunningYearName())));

				{
					c1.setBorder(Rectangle.NO_BORDER);
					c2.setBorder(Rectangle.NO_BORDER);
					htable.addCell(c1);
					PdfPTable htable2 = new PdfPTable(new float[] { 1 });
					htable2.addCell(c2);
					final Font f14 = new Font(new Phrase().font());
					// f14.setSize(12);
					final Phrase p3 = new Phrase("Bestellliste, Bücher aus: "
							+ Verlag.hm.get(land), f14);
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
				table = new PdfPTable(new float[] { 1.5f, 8, 6, 4, 6 });
				table.setWidthPercentage(100);

				for (String h : headers) {
					final PdfPCell cell = new PdfPCell(new Phrase(h));
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					// if (h.equals("Bücheranzahl"))
					// cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
				}
				table.setHeaderRows(1);
				String[] vArray = verlageImLand
						.toArray(new String[verlageImLand.size()]);
				Arrays.sort(vArray);
				for (String verlagName : vArray)
					for (Buch b : Buch.sort(buecherNachVerlag.get(verlagName))) {
						// int stock = ds.buchVw.getStock(b.code);
						BestellungDetail det = ds
								.getBestellungTotalForBook(b.code);
						final Paragraph p12 = new Paragraph((det.n) + " ");
						final PdfPCell c12 = new PdfPCell(p12);
						c12.setHorizontalAlignment(Element.ALIGN_RIGHT);
						c12.setRightIndent(10);
						table.addCell(c12);
						table.addCell(new Phrase(b.titel));
						table.addCell(new Phrase(b.verlag.name));
						final Font f10 = new Font(new Phrase().font());
						f10.setSize(10);
						table.addCell(new Phrase(b.isbn, f10));
						table.addCell(new Phrase(det.detail, f10));
					}
				//
				// }
				table.setSpacingBefore(50);
				document.add(table);
				document.newPage();
			}
			document.close();
			Desktop.getDesktop().open(fich);
			Display.getCurrent().beep();
		}
	}
}
