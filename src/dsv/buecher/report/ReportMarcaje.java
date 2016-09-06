package dsv.buecher.report;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dsv.buecher.Buch;
import dsv.buecher.BuchBemerkung;
import dsv.buecher.DsvBuecher;
import dsv.buecher.DsvBuecherDatasource;
import dsv.buecher.ParallellPhrase;
import dsv.buecher.RauUtil;

public class ReportMarcaje {
	private static final Font stdFont = new Chunk("").font();
	private static final float[] TABLE_COL_SIZES = new float[] { 5, 40, 15, 15,
			25, 15, 15 };
	private static final String headers[] = { "B.", "Titel", "Verlag", "Fach",
			"Isbn", "Bemerk.", "Preis" };
	private final ArrayList<String> klassenList;
	private final DsvBuecher gui;
	private final DsvBuecherDatasource ds;
	private final HashMap<Integer, BuchBemerkung> hm = new HashMap<Integer, BuchBemerkung>();
	private final boolean sinAclaraciones;
	private static final Font f10 = FontFactory.getFont(
			FontFactory.TIMES_ROMAN, Font.DEFAULTSIZE, Font.NORMAL, new Color(
					0x0, 0x00, 0x00));

	public ReportMarcaje(ArrayList<String> klassenList, DsvBuecher gui,
			boolean sinAclaraciones) throws SQLException {
		System.err.println("klassenList: " + klassenList);
		this.klassenList = klassenList;
		this.gui = gui;
		this.ds = gui.ds;
		this.sinAclaraciones = sinAclaraciones;
		ml = ds.getBuchMarken();
		for (BuchBemerkung bm : ml)
			hm.put(bm.id, bm);
	}

	static {
		f10.setSize(10);
	}

	public void makeit(String fich) throws DocumentException,
			MalformedURLException, IOException, SQLException {
		Document document = new Document(PageSize.A4);
		@SuppressWarnings("unused")
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(fich));
		document.open();

		for (String klasse : klassenList) {
			final ArrayList<Buch> bl = ds.buchVw.getMarcaje(klasse);
			System.err.println("marcaje: buchlist: " + bl);
			heading(document, klasse);
			blankSpace(document, 10);
			HashSet<BuchBemerkung> used = makeTable(document, bl);
			makeAclaraciones(document, used);
			if (klassenList.size() > 1)
				document.newPage();
		}
		document.close();
	}

	private HashSet<BuchBemerkung> makeTable(Document document,
			final ArrayList<Buch> bl) throws SQLException, DocumentException {
		PdfPTable table = new PdfPTable(TABLE_COL_SIZES);
		table.setSpacingBefore(50);
		table.setHeaderRows(1);
		table.setWidthPercentage(100);

		{
			final Font font = new Font(stdFont);
			font.setStyle(Font.BOLD);
			for (String h : headers) {
				final Phrase p = new Phrase(h, font);
				final PdfPCell cell = new PdfPCell(p);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

			}
		}
		HashSet<BuchBemerkung> usedBemerkungen = new HashSet<BuchBemerkung>();
		for (Buch x : bl) {
			table.addCell(new Phrase("", f10));
			table.addCell(new Phrase(x.titel, f10));
			table.addCell(new Phrase(x.verlag.name, f10));
			table.addCell(new Phrase(x.fach, f10));
			table.addCell(new Phrase(x.isbn, f10));
			table.addCell(getBemerkungenCell(usedBemerkungen, x));
			final PdfPCell preisCell = new PdfPCell(new Phrase(
					getPriceVisualization(x), f10));
			preisCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			preisCell.setRightIndent(10);
			table.addCell(preisCell);
		}
		document.add(table);
		return usedBemerkungen;
	}

	private void makeAclaraciones(Document document, HashSet<BuchBemerkung> used)
			throws DocumentException, SQLException {
		List aclaraciones = new List(List.UNORDERED, 10);
		if (!sinAclaraciones) {
			aclaraciones.setListSymbol("");
			ArrayList<ParallellPhrase> phl = new ArrayList<ParallellPhrase>();
			// gui.ds.setMarcajeText(gui.ds.getMarcajeText2());
			String cs = gui.ds.getMarcajeText();

			// Nota: quitamos los \r para que vaya bien en windogs
			for (String s : cs.replaceAll("\r", "").split("\n")) {
				String s2[] = s.split("\\|");
				if (s2.length == 2)
					phl.add(new ParallellPhrase(s2[0], s2[1]));
				else if (s2.length == 3)
					phl.add(new ParallellPhrase(s2[0], s2[1], s2[2]));
				else
					System.err.println("unaccepted: " + s);
			}

			for (ParallellPhrase ph : phl) {
				if (ph.de.length() < 40) {
					Phrase ph3 = new Phrase("- " + ph.de, stdFont);
					ph3.add(new Chunk(" (" + ph.es + ")", italize(stdFont)));
					if (ph.data == null)
						ph3.add(new Chunk("."));
					else
						ph3.add(new Chunk(": " + ph.data));
					aclaraciones.add(new ListItem(ph3));
				} else {
					aclaraciones.add(new ListItem(
							new Phrase("- " + ph.de
									+ (ph.data == null ? "." : ": " + ph.data),
									stdFont)));
					aclaraciones.add(new ListItem(new Chunk("   (" + ph.es
							+ ")")));

				}

			}
			blankSpace(document, 20);
		}
		if (!used.isEmpty()) {
			final Phrase phr = new Phrase("- Fußnote (", f10);
			phr.setLeading(20);
			phr.add(new Chunk("Leyenda", italize(phr.font())));
			phr.add(new Chunk(")", phr.font()));
			aclaraciones.add(new ListItem(phr));
			aclaraciones.add(bemerkungenToPdfList(used));
		}

		// /
		document.add(aclaraciones);
		if (!sinAclaraciones) {
			Paragraph dePar = new Paragraph(
					"Unterschrift eines Erziehungsberechtigten:", stdFont);
			Paragraph spaPar = new Paragraph(
					" (Firma del padre, madre o tutor)", italize(stdFont));
			dePar.setLeading(50);
			dePar.setAlignment(Element.ALIGN_RIGHT);
			spaPar.setAlignment(Element.ALIGN_RIGHT);
			Paragraph subLine = new Paragraph("_______________________________");
			subLine.setLeading(30);
			subLine.setAlignment(Element.ALIGN_RIGHT);

			document.add(dePar);
			document.add(spaPar);
			document.add(subLine);

		}

	}

	private void blankSpace(Document document, int size)
			throws DocumentException {
		final Paragraph blankPar = new Paragraph("");
		// blankPar.setSpacingBefore(size);
		document.add(blankPar);
	}

	private void heading(Document document, String klasse) throws SQLException,
			DocumentException, MalformedURLException, IOException {
		{
			com.lowagie.text.Image image = com.lowagie.text.Image
					.getInstance(getClass().getResource("/logo.png"));
			image.scaleAbsolute(100 * 180 / 100, 30 * 180 / 100);

			PdfPTable table = new PdfPTable(new float[] { 40, 60 });

			table.setWidthPercentage(100);
			PdfPTable table1 = new PdfPTable(2);
			table1.setWidthPercentage(100);
			PdfPCell icell = new PdfPCell(image);
			icell.setBorder(Rectangle.NO_BORDER);

			final PdfPCell cell2 = new PdfPCell(icell);
			cell2.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell2);

			final PdfPCell c1 = new PdfPCell(new Phrase("Schulbuchliste "
					+ ds.getRunningYearName()));
			final PdfPCell c2 = new PdfPCell(new Paragraph("Klasse: "
					+ klasse
					+ (sinAclaraciones ? " - "
							+ new SimpleDateFormat("dd.MM.yyyy")
									.format(new Date()) : "")));
			c1.setBorder(Rectangle.NO_BORDER);
			c2.setBorder(Rectangle.NO_BORDER);
			table1.addCell(c1);
			table1.addCell(c2);

			if (!sinAclaraciones) {
				final Paragraph p = new Paragraph("Name des Schülers ");
				PdfPCell cell = new PdfPCell(p);
				p.add(new Paragraph("(Nombre del alumno):", italize(p.font())));
				p.add(new Paragraph("", FontFactory.getFont(
						FontFactory.TIMES_ROMAN, 36, Font.NORMAL, new Color(
								0x0, 0x00, 0x00))));
				final Paragraph p2 = new Paragraph(
						"_______________________________");
				p.add(p2);

				cell.setTop(20);
				cell.top(20);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setColspan(2);
				table1.addCell(cell);
			}
			final PdfPCell cell3 = new PdfPCell(table1);
			cell3.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell3);
			document.add(table);
			// table.setConvert2pdfptable(true);
			// PdfPTable ptable = table.createPdfPTable();

		}
	}

	/**
	 * @param used
	 * @return
	 */
	public static List bemerkungenToPdfList(HashSet<BuchBemerkung> used) {
		List l2 = new List(List.UNORDERED, 20);
		l2.setListSymbol("");
		for (BuchBemerkung i : used) {
			Phrase p = new Phrase("", f10);
			final Chunk chunkShortname = new Chunk(i.shortname, f10);
			final Font f3 = new Font(chunkShortname.font());
			f3.setStyle(Font.BOLD);
			chunkShortname.setFont(f3);
			p.add(chunkShortname);
			p.add(new Chunk(": ", f10));
			String dsc[] = i.dsc.split("\\|");
			p.add(new Chunk(dsc[0], f10));
			p.add(" (");
			p.add(new Chunk(dsc[1], italize(f10)));
			p.add(")");
			l2.add(new ListItem(p));
		}
		return l2;
	}

	/**
	 * @param used
	 * @param x
	 * @return
	 * @throws SQLException
	 */
	private PdfPCell getBemerkungenCell(HashSet<BuchBemerkung> used, Buch x)
			throws SQLException {
		PdfPCell cell = new PdfPCell(new Paragraph(
				x.bemerkungen.isEmpty() ? "---" : getBemerkungen(x, used), f10));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		return cell;
	}

	public static Font italize(Font font) {
		font = new Font(font);
		font.setStyle(Font.ITALIC);
		return font;
	}

	private String getPriceVisualization(Buch x) {
		if (x.preis == 0)
			return "?";
		else
			return DsvBuecher.currency(x.preis);
	}

	private final ArrayList<BuchBemerkung> ml;

	private String getBemerkungen(Buch x, HashSet<BuchBemerkung> used)
			throws SQLException {
		ArrayList<String> salida = new ArrayList<String>();
		for (Integer i : x.bemerkungen) {
			salida.add(hm.get(i).shortname);
			used.add(hm.get(i));
		}
		return salida.isEmpty() ? "" : RauUtil.separate(salida
				.toArray(new String[salida.size()]), ", ");
	}

	public static void dale(final ArrayList<String> l,
			final DsvBuecher dsvBuecher, final boolean sinAclaraciones)
			throws IOException, DocumentException, SQLException {
		final File filename = File.createTempFile("marcaje", ".pdf");

		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			@Override
			public void run() {
				try {
					new ReportMarcaje(l, dsvBuecher, sinAclaraciones)
							.makeit(filename.toString());
					Desktop.getDesktop().open(filename);
				} catch (Exception e) {
					dsvBuecher.showError(e);
				}
			}

		});

	}
}
