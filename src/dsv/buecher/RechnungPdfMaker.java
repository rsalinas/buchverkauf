package dsv.buecher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dsv.buecher.report.ReportMarcaje;

public class RechnungPdfMaker {
	final private Properties props = new Properties();

	private static final String headers[] = { "Titel", "Verlag", "ISBN",
			"Preis" };
	private static final String headers_es[] = { "Nombre del libro",
			"Editorial", null, "Precio" };

	private static final String CONFIG_FILENAME = "factura-messages.txt";

	public static String par(String s) {
		return "(" + s + ")";
	}

	public void makeit(DsvBuecherDatasource ds, String fich,
			ArrayList<DatosFactura> dfl, int copias) throws SQLException,
			DocumentException, MalformedURLException, IOException {
		Document document = new Document();

		PdfWriter.getInstance(document, new FileOutputStream(fich));
		Font small = new Font(new Paragraph("").font());
		small.setSize(10);
		document.open();
		for (int copia = 0; copia < copias; copia++) {
			if (copia > 0)
				document.newPage();

			for (DatosFactura df : dfl) {
				final URL imageResource = ds.getClass()
						.getResource("/logo.png");
				if (imageResource != null) {
					com.lowagie.text.Image image = com.lowagie.text.Image
							.getInstance(imageResource);
					image.scaleAbsolute(100 * 1.5f, 30 * 1.5f);
					document.add(image);
				}

				final Paragraph p0 = new Paragraph("Bücherrechnung für");
				Font superscriptFont = new Font(p0.font());
				superscriptFont.setSize(6);
				p0.setSpacingBefore(15);
				p0.add(new Phrase(" " + par("factura para"), ReportMarcaje
						.italize(p0.font())));
				p0.add(new Phrase(": "));
				p0.add(new Phrase(
						df.schueler.name + ", " + df.schueler.vorName,
						boldify(p0.font())));
				p0.add(new Phrase("   (Klasse/"));
				p0.add(new Phrase("clase", ReportMarcaje.italize(p0.font())));
				p0.add(new Phrase(": "));
				p0.add(new Phrase(df.schueler.klasse, boldify(p0.font())));
				p0.add(new Phrase(")"));
				document.add(p0);
				// final Paragraph p2 = new Paragraph("Klasse");
				// p2
				// .add(new Phrase(" (clase)", ReportMarcaje.italize(p0
				// .font())));
				// p2.add(": " + df.schueler.klasse);
				// p2.setSpacingAfter(10);
				// document.add(p2);

				// PdfPCell cell = new PdfPCell(new
				// Paragraph("header with colspan 3"));
				// cell.setColspan(3);
				// table.addCell(cell);
				double sum = 0;
				Schueler s2 = ds.getSchueler(df.schueler.num);
				df.schueler.bemerkDeu = s2.bemerkDeu;
				df.schueler.bemerkSpa = s2.bemerkSpa;
				Paragraph schuelerBemerkung = new Paragraph();
				schuelerBemerkung.setAlignment(Element.ALIGN_JUSTIFIED);
				if (!df.schueler.bemerkDeu.isEmpty())
					schuelerBemerkung.add(new Chunk(df.schueler.bemerkDeu,
							small));
				if (!df.schueler.bemerkSpa.isEmpty())
					schuelerBemerkung.add(new Chunk(par(df.schueler.bemerkSpa),
							ReportMarcaje.italize(small)));
				if (!df.schueler.bemerkDeu.isEmpty()
						|| !df.schueler.bemerkSpa.isEmpty())
					document.add(schuelerBemerkung);

				ArrayList<Paragraph> notas = new ArrayList<Paragraph>();
				final float[] anchuras = new float[] { 45, 15, 25, 10 };
				if (!df.comprar.isEmpty()) {
					if (df.devolver.isEmpty()) {
						final Paragraph p1 = new Paragraph("Anzahl der Bücher");
						p1.add(new Phrase(" " + par("número de libros"),
								ReportMarcaje.italize(p1.font())));
						p1.add(new Phrase(": "
								+ (df.comprar.size() + df.devolver.size())));
						document.add(p1);
					} else {
						final Paragraph p1 = new Paragraph("Verkaufte Bücher");
						p1.add(new Phrase(" " + par("libros vendidos"),
								ReportMarcaje.italize(p1.font())));
						p1.add(new Phrase(" (" + df.comprar.size() + "):"));
						document.add(p1);
					}

					PdfPTable table = new PdfPTable(anchuras);
					table.setSpacingBefore(15);
					table.setSpacingAfter(20);
					table.setWidthPercentage(90);
					table.setHeaderRows(1);
					setDualHeaders(table);

					for (ChoosableBuch x : df.comprar) {
						System.err.println("nota: " + x.buch.bemerkDeu);
						Buch buch2 = ds.getBuch(x.buch.code);
						x.buch.bemerkDeu = buch2.bemerkDeu;
						x.buch.bemerkSpa = buch2.bemerkSpa;
						if (!x.buch.bemerkDeu.isEmpty()
						/* || !x.buch.bemerkSpa.isEmpty() */) {

							Paragraph p = new Paragraph();

							p.add(new Chunk(x.buch.bemerkDeu, small));
							p.add(new Chunk("(" + x.buch.bemerkSpa + ")",
									ReportMarcaje.italize(small)));
							notas.add(p);
							Chunk superindex = new Chunk("" + notas.size(),
									superscriptFont);
							superindex.setTextRise(6);
							Phrase phrase = new Phrase(x.buch.titel);
							phrase.add(superindex);
							table.addCell(phrase);
						} else {
							table.addCell(x.buch.titel);
						}
						table.addCell(x.buch.verlag.name);
						table.addCell(x.buch.isbn);
						table.addCell(DsvBuecher.currency(x.buch.preis));
						sum += x.buch.preis;
					}
					document.add(table);
				}
				if (!df.devolver.isEmpty()) {
					document.add(new Paragraph("Zurückgegebene Bücher "
							+ par("libros devueltos") + " ("
							+ df.devolver.size() + "): "));
					PdfPTable table = new PdfPTable(anchuras);
					table.setWidthPercentage(90);
					table.setSpacingBefore(15);
					table.setSpacingAfter(15);
					table.setHeaderRows(1);
					setDualHeaders(table);

					for (ChoosableBuch x : df.devolver) {
						table.addCell(x.buch.titel);
						table.addCell(x.buch.verlag.name);
						table.addCell(x.buch.isbn);
						table.addCell("-" + DsvBuecher.currency(x.precioVenta));
						sum -= x.precioVenta;
					}
					document.add(table);
				}
				if (!notas.isEmpty()) {
					Paragraph p = new Paragraph();
					p.add(new Chunk("Notas/", small));
					p.add(new Chunk("Bemerkungen:", ReportMarcaje
							.italize(small)));
					document.add(p);
					for (int i = 0; i < notas.size(); i++) {
						final Paragraph f = new Paragraph();
						f.setAlignment(Element.ALIGN_JUSTIFIED);
						f.add(new Phrase(par("" + (i + 1)), small));
						f.add(notas.get(i));
						document.add(f);
					}
				}
				final Paragraph summe = new Paragraph();
				summe.add(new Chunk(" (Rechnungsnummer/"));
				summe.add(new Chunk("número de factura", ReportMarcaje
						.italize(p0.font())));
				summe.add(": ");
				summe.add(new Chunk(df.id + ")               "));
				summe.add(new Chunk("Summe (total): "));
				summe.setSpacingBefore(5);
				Font f2 = new Font(summe.font());
				f2.setStyle(Font.BOLD);
				summe.add(new Chunk(DsvBuecher.currency(sum), f2));
				summe.setAlignment(Element.ALIGN_RIGHT);
				document.add(summe);
				final Paragraph valencia = rightAlign("Valencia, "
						+ new SimpleDateFormat("dd.MM.yyyy").format(new Date(
								df.datum.getTime())));
				valencia.setSpacingBefore(0);
				document.add(valencia);
				Paragraph piePar;

				try {
					props.load(new FileInputStream(CONFIG_FILENAME));
				} catch (Exception e) {

					props.setProperty("rechnung.a-pagar.de",
							"Betrag dankend erhalten");
					props.setProperty("rechnung.a-pagar.es", "Importe recibido");
					props.setProperty("rechnung.a-devolver.de",
							"Betrag zurückbezahlt");
					props.setProperty("rechnung.a-devolver.es",
							"Importe recibido");
					props.setProperty("rechnung.zero.de",
							"Betrag dankend erhalten (0)");
					props.setProperty("rechnung.zero.es",
							"Importe recibido (0)");
					props.setProperty("rechnung.extra0.de", "");
					props.setProperty("rechnung.extra0.es", "");

					props.setProperty("rechnung.extra1.de", "");
					props.setProperty("rechnung.extra1.es", "");
					props.save(new FileOutputStream(CONFIG_FILENAME), "autogen");
					props.clear();
					props.load(new FileInputStream(CONFIG_FILENAME));
				}
				// if (sum > 0)
				// piePar = x1("Betrag dankend erhalten", "Importe recibido");
				// else if (sum < 0)
				// piePar = x1("Betrag zurückbezahlt", "Importe devuelto");
				// else
				// piePar = x1("Betrag dankend erhalten", "Importe recibido");

				if (sum > 0)
					piePar = x1("rechnung.a-pagar");
				else if (sum < 0)
					piePar = x1("rechnung.a-devolver");
				else
					piePar = x1("rechnung.zero");

				final Paragraph pieParAlineado = rightAlign(piePar);
				pieParAlineado.setSpacingBefore(10);
				document.add(pieParAlineado);

				for (int i = 0;; i++) {
					final String basename = "rechnung.extra" + i;
					System.err.println("mirando: " + basename);
					if (props.getProperty(basename + ".de") == null
							|| props.getProperty(basename + ".de").isEmpty())
						break;
					document.add(justifyAlign(x1(basename)));
				}
				if (dfl.size() > 1)
					document.newPage();
			}

		}
		document.close();

	}

	private Paragraph x1(String prefix) {
		final String de = props.getProperty(prefix + ".de");
		if (!de.isEmpty()) {
			Paragraph p = new Paragraph(de);
			final String es = props.getProperty(prefix + ".es");
			if (!es.isEmpty())
				p.add(new Phrase(" (" + es + ")", ReportMarcaje.italize(p
						.font())));
			return p;
		} else
			return new Paragraph();
	}

	private static void setDualHeaders(PdfPTable table) {
		for (int i = 0; i < headers.length; i++) {
			PdfPCell c = new PdfPCell();
			final Font normal = new Paragraph().font();
			Paragraph p = new Paragraph(headers[i], boldify(normal));
			c.addElement(p);
			if (headers_es[i] != null) {
				final Font claraFont = ReportMarcaje.italize(normal);
				claraFont.setSize(10);
				c.addElement(new Chunk(headers_es[i], claraFont));
			}
			table.addCell(c);
		}
	}

	private static Paragraph rightAlign(String string) {
		final Paragraph p = new Paragraph(string);
		p.setAlignment(Element.ALIGN_RIGHT);
		return p;
	}

	private static Paragraph justifyAlign(String string) {
		final Paragraph p = new Paragraph(string);
		p.setAlignment(Element.ALIGN_JUSTIFIED_ALL);
		return p;
	}

	private static Paragraph rightAlign(Paragraph p) {
		p.setAlignment(Element.ALIGN_RIGHT);
		return p;
	}

	private static Paragraph justifyAlign(Paragraph p) {
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		return p;
	}

	public static Font boldify(Font f0) {
		Font f = new Font(f0);
		f.setStyle(Font.BOLD);
		return f;
	}

	public void makeit(DsvBuecherDatasource ds, String string,
			ArrayList<DatosFactura> l) throws MalformedURLException,
			SQLException, DocumentException, IOException {
		makeit(ds, string, l, 1);

	}
}
