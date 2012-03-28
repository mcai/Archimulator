package archimulator.util.chart;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ChartPdfExporter {
    public static void exportPdf(JFreeChart chart, int width, int height, String fileName) {
        try {
            Document document = new Document(new Rectangle(width, height));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
            Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2d, r2d);
            g2d.dispose();
            cb.addTemplate(tp, 0, 0);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
