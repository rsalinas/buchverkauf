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
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dsv.buecher.Buch;
import dsv.buecher.BuchBemerkung;
import dsv.buecher.ChoosableBuch;
import dsv.buecher.DsvBuecher;
import dsv.buecher.DsvBuecherDatasource;
import dsv.buecher.Klasse;
import dsv.buecher.RauUtil;
import dsv.buecher.Schueler;

public class ReportBestellteBuecher {
	private static final Font stdFont = new Chunk("").font();
	private static final float[] TABLE_COL_SIZES = new float[] { 5, 40, 15, 15,
			25, 15 };
	private static final String headers[] = { "B.", "Titel", "Verlag", "Fach",
			"Isbn", "Preis" };
	private final ArrayList<Klasse> klassenList;
	private final DsvBuecherDatasource ds;
	private final HashMap<Integer, BuchBemerkung> hm = new HashMap<Integer, BuchBemerkung>();
	private static final Font f10 = FontFactory.getFont(
			FontFactory.TIMES_ROMAN, Font.DEFAULTSIZE, Font.NORMAL, new Color(
					0x0, 0x00, 0x00));

	public ReportBestellteBuecher(DsvBuecher gui) throws SQLException {
		this.klassenList = gui.ds.klassenVw.getKlassen("jahr,klasse");
		this.ds = gui.ds;
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
		PdfWriter.getInstance(document, new FileOutputStream(fich));
		{
			final HeaderFooter hf = new HeaderFooter(new Phrase(
					new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())
							+ ". Seite "), new Phrase());
			hf.setAlignment(HeaderFooter.ALIGN_RIGHT);
			hf.setBorder(Rectangle.NO_BORDER);
			document.setFooter(hf);
		}
		document.open();

		for (Klasse klasse : klassenList) {
			for (Schueler s : ds.getSchueler(klasse)) {
				heading(document, klasse.name, s);
				blankSpace(document, 10);
				makeTable(document, ds.getBooksForUser(s));
				document.newPage();
			}
		}
		document.close();
	}

	private void makeTable(Document document, final ArrayList<ChoosableBuch> l2)
			throws SQLException, DocumentException {
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
		for (ChoosableBuch cb : l2) {
			Buch x = cb.buch;
			table.addCell(new Phrase(cb.bestellt ? "Ja" : "-", f10));
			table.addCell(new Phrase(x.titel, f10));
			table.addCell(new Phrase(x.verlag.name, f10));
			table.addCell(new Phrase(x.fach, f10));
			table.addCell(new Phrase(x.isbn, f10));
			final PdfPCell preisCell = new PdfPCell(new Phrase(
					getPriceVisualization(x), f10));
			preisCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			preisCell.setRightIndent(10);
			table.addCell(preisCell);
		}
		document.add(table);
	}

	private void blankSpace(Document document, int size)
			throws DocumentException {
		final Paragraph blankPar = new Paragraph("");
		// blankPar.setSpacingBefore(size);
		document.add(blankPar);
	}

	private void heading(Document document, String klasse, Schueler schueler)
			throws SQLException, DocumentException, MalformedURLException,
			IOException {
		{
			document.add(new Paragraph("Bestellungen für " + schueler.name
					+ ", " + schueler.vorName));
			document.add(new Paragraph("Klasse: " + schueler.klasse));
		}
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

	@SuppressWarnings("unused")
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

	public static void dale(final DsvBuecher dsvBuecher) throws IOException,
			DocumentException, SQLException {
		System.err.println("ReportBestellteBücher");
		final File filename = File.createTempFile("bestellteBuecher", ".pdf");

		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			@Override
			public void run() {
				try {
					new ReportBestellteBuecher(dsvBuecher).makeit(filename
							.toString());
					Desktop.getDesktop().open(filename);
				} catch (Exception e) {
					dsvBuecher.showError(e);
				}
			}
		});
	}
}
