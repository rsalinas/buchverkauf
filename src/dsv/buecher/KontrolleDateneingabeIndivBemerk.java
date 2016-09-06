package dsv.buecher;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dsv.buecher.report.ReportMarcaje;

public class KontrolleDateneingabeIndivBemerk {

	@SuppressWarnings("unchecked")
	public KontrolleDateneingabeIndivBemerk(DsvBuecher parent, boolean spaced)
			throws SQLException, MalformedURLException, IOException,
			DocumentException {
		DsvBuecherDatasource ds = parent.ds;
		ArrayList<BuchBemerkung> ml = ds.getBuchMarken();
		for (BuchBemerkung bm : ml)
			hm.put(bm.id, bm);
		ArrayList<Buch> bl = parent.ds.getBuecher(null);
		HashMap<String, HashMap<Integer, HashSet<Buch>>> l2 = new HashMap<String, HashMap<Integer, HashSet<Buch>>>();
		for (Buch b : bl) {
			HashMap<Integer, HashSet<Buch>> l = l2.get(b.fach);
			if (l == null)
				l2.put(b.fach, l = new HashMap<Integer, HashSet<Buch>>());
			for (Klasse k : b.klassen) {
				HashSet<Buch> hs = l.get(k.jahr);
				if (hs == null)
					l.put(k.jahr, hs = new HashSet<Buch>());
				hs.add(b);
			}
		}

		File fich = File.createTempFile("KDEG-", ".pdf");
		{
			// String fich = "c:/x.pdf";
			// step 1: creation of a document-object
			Document document = new Document(PageSize.A4.rotate());
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
			Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN,
					Font.DEFAULTSIZE, Font.NORMAL, new Color(0x0, 0x00, 0x00));

			f2.setSize(10);
			// PdfContentByte cb = writer.getDirectContent();
			// cb.saveState();
			// cb.beginText();

			Entry<String, HashMap<Integer, HashSet<Buch>>>[] xx = l2.entrySet()
					.toArray(new Entry[l2.entrySet().size()]);
			final Comparator<Entry<String, HashMap<Integer, HashSet<Buch>>>> comparator = new Comparator<Entry<String, HashMap<Integer, HashSet<Buch>>>>() {

				@Override
				public int compare(
						Entry<String, HashMap<Integer, HashSet<Buch>>> o1,
						Entry<String, HashMap<Integer, HashSet<Buch>>> o2) {
					return o1.getKey().compareTo(o2.getKey());
				}

			};
			Arrays.sort(xx, comparator);
			for (Entry<String, HashMap<Integer, HashSet<Buch>>> e0 : xx) {
				if (!hasBemerkungen(e0))
					continue;

				HashSet<BuchBemerkung> bemerkungen = new HashSet<BuchBemerkung>();
				String fach = e0.getKey();
				com.lowagie.text.Image image = com.lowagie.text.Image
						.getInstance(getClass().getResource("/logo.png"));
				image.scaleAbsolute(100, 30);
				PdfPTable htable = new PdfPTable(new float[] { 40, 60 });
				// document.add(image);
				htable.setWidthPercentage(100);
				final PdfPCell c1 = new PdfPCell(image);
				// PdfPCell spaceCell = new PdfPCell(new Phrase(""));
				// spaceCell.setColspan(7);
				// spaceCell.setFixedHeight(30);
				// phrase.add(spaceCell);
				// phrase.setLeading(10);
				final Font f14 = new Font(new Phrase().font());
				f14.setStyle(Font.BOLD);
				final Phrase p4 = new Phrase(ds.getAvisoProfeText(), f14);
				// p4.setLeading(10);
				final PdfPCell c2 = new PdfPCell(new Phrase(
						MessageFormat.format("Fach: {0} ({1})", fach,
								ds.getRunningYearName())));
				final PdfPCell c3 = new PdfPCell(p4);

				c1.setBorder(Rectangle.NO_BORDER);
				c2.setBorder(Rectangle.NO_BORDER);
				c3.setBorder(Rectangle.NO_BORDER);
				htable.addCell(c1);
				PdfPTable htable2 = new PdfPTable(new float[] { 100 });
				htable2.addCell(c2);
				if (spaced)
					htable2.addCell(c3);

				PdfPCell pdfPCellt2 = new PdfPCell(htable2);
				pdfPCellt2.setBorder(Rectangle.NO_BORDER);
				htable.addCell(pdfPCellt2);

				document.add(htable);

				// com.lowagie.text.Image image = com.lowagie.text.Image
				// .getInstance(getClass().getResource("/logo_dsv.jpg"));
				// image.scaleAbsolute(100, 30);

				// document.add(image);
				// document.right(1);
				// document.left(1);
				// document.add(new Paragraph());
				PdfPTable table;

				table = new PdfPTable(new float[] { 5, 15, 40, 25, 15, 15, 15 });
				table.setSpacingBefore(20);
				table.setWidthPercentage(100);
				String headers[] = { "SJ", "Verlag", "Titel", "Isbn", "Preis",
						"Klassen", "Bemerkungen" };
				for (String h : headers) {
					final PdfPCell preisCell = new PdfPCell(new Phrase(h));
					preisCell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(preisCell);
				}

				final Set<Entry<Integer, HashSet<Buch>>> s0 = e0.getValue()
						.entrySet();
				Entry<Integer, HashSet<Buch>>[] a0 = s0.toArray(new Entry[s0
						.size()]);
				Arrays.sort(a0,
						new Comparator<Entry<Integer, HashSet<Buch>>>() {

							@Override
							public int compare(
									Entry<Integer, HashSet<Buch>> o1,
									Entry<Integer, HashSet<Buch>> o2) {

								return o1.getKey().compareTo(o2.getKey());
							}

						});
				for (Entry<Integer, HashSet<Buch>> e1 : a0) {
					final HashSet<Buch> l5 = e1.getValue();
					Buch[] a5 = l5.toArray(new Buch[l5.size()]);
					Arrays.sort(a5);
					for (Buch x : a5) {
						if (x.bemerkDeu.isEmpty() && x.bemerkSpa.isEmpty())
							continue;
						table.addCell(new Phrase("" + e1.getKey()));
						table.addCell(new Phrase(x.verlag.name));
						table.addCell(new Phrase(x.titel));
						table.addCell(new Phrase(x.isbn));
						table.addCell(new Phrase(getPriceVisualization(x)));
						table.addCell(new Phrase(x.klassen.toString()));
						table.addCell(new Phrase(getBemerkungen(x)));
						for (Integer ii : x.bemerkungen)
							bemerkungen.add(hm.get(ii));
						{
							{

								PdfPCell spaceCell = new PdfPCell(
										new Phrase(
												x.bemerkDeu.isEmpty() ? "DEUTSCHER EINTRAG FEHLT!!!"
														: x.bemerkDeu));
								spaceCell.setColspan(7);
								table.addCell(spaceCell);

							}
							{
								PdfPCell spaceCell = new PdfPCell(
										new Phrase(
												x.bemerkSpa.isEmpty() ? "SPANISCHER EINTRAG FEHLT!!!"
														: x.bemerkSpa));
								spaceCell.setColspan(7);
								table.addCell(spaceCell);

							}
						}
					}

				}
				table.setHeaderRows(1);
				document.add(table);
				document.add(ReportMarcaje.bemerkungenToPdfList(bemerkungen));
				if (l2.size() > 1)
					document.newPage();
			}
			// cb.endText();
			document.close();
			Desktop.getDesktop().open(fich);

		}

	}

	private boolean hasBemerkungen(
			Entry<String, HashMap<Integer, HashSet<Buch>>> e0) {
		for (Entry<Integer, HashSet<Buch>> i : e0.getValue().entrySet()) {
			for (Buch b : i.getValue()) {
				if (!b.bemerkDeu.isEmpty() || !b.bemerkSpa.isEmpty())
					return true;
			}
		}
		return false;
	}

	private String getPriceVisualization(Buch x) {
		if (x.preis == 0)
			return "?";
		else
			return DsvBuecher.currency(x.preis);
	}

	private String getBemerkungen(Buch x) throws SQLException {
		ArrayList<String> salida = new ArrayList<String>();
		for (Integer i : x.bemerkungen) {
			salida.add(hm.get(i).shortname);
		}
		return salida.isEmpty() ? "" : RauUtil.separate(
				salida.toArray(new String[salida.size()]), ", ");
	}

	private final HashMap<Integer, BuchBemerkung> hm = new HashMap<Integer, BuchBemerkung>();
}
