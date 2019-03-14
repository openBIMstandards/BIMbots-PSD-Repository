package nl.tno.willemsph.psd_repository.information_delivery_specification;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import nl.tno.willemsph.psd_repository.common.UserRepository;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinition;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinitionResolver;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;

public class PdfGenerator {

	public static String generate(InformationDeliverySpecification ids,
			PropertySetDefinitionRepository propertySetDefinitionRepository,
			UserRepository userRepository)
			throws IOException, DocumentException, URISyntaxException {
		String idsName = null;
		int lastIndexOfHashMark = ids.getId().lastIndexOf('#');
		if (lastIndexOfHashMark >= 0) {
			idsName = ids.getId().substring(lastIndexOfHashMark + 1);
		} else {
			int lastIndexOfSlash = ids.getId().lastIndexOf('/');
			idsName = ids.getId().substring(lastIndexOfSlash + 1);
		}
		InformationDeliverySpecificationResolver idsRslver = new InformationDeliverySpecificationResolver(userRepository);
		String title = idsRslver.getName(ids);

		Document document = new Document();
		Path fileToCreatePath = FileSystems.getDefault().getPath("src/main/resources/static/" + idsName + ".pdf");
		if (!Files.exists(fileToCreatePath)) {
			Files.createFile(fileToCreatePath);
		}
		PdfWriter.getInstance(document, new FileOutputStream(fileToCreatePath.toFile()));

		document.open();
		Paragraph titleParagraph = new Paragraph(title,
				FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLDITALIC, BaseColor.BLACK));
		Chapter chapter1 = new Chapter(titleParagraph, 1);
		Paragraph tableParagraph = new Paragraph();
		tableParagraph.setSpacingBefore(10f);

		PdfPTable table = new PdfPTable(2);
		Stream.of("PSET", "mandatory properties").forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(1);
			header.setHorizontalAlignment(Element.ALIGN_CENTER);
			header.setPhrase(new Phrase(columnTitle));
			table.addCell(header);
		});

		List<RequiredPset> reqPsets = idsRslver.getReqPsets(ids);
		if (reqPsets != null) {
			for (RequiredPset reqPset : reqPsets) {
				table.addCell(reqPset.getPropertySetName());
				String reqPropertiesStr = "";
				PropertyDefinitionResolver propRslvr = new PropertyDefinitionResolver();
				List<URI> reqPropertyIds = reqPset.getReqPropertyIds();
				for (int index = 0; index < reqPropertyIds.size(); index++) {
					URI reqPropertyId = reqPropertyIds.get(index);
					PropertyDefinition propertyDef = propertySetDefinitionRepository.getPropertyDef(reqPropertyId);
					String propname = propRslvr.getName(propertyDef);
					reqPropertiesStr += propname;
					if (index < reqPropertyIds.size() - 1) {
						reqPropertiesStr += ", ";
					}
				}
				table.addCell(reqPropertiesStr);
			}
		}

		tableParagraph.add(table);
		chapter1.add(tableParagraph);
		document.add(chapter1);
		document.close();

		return idsName + ".pdf";
	}

	public PdfGenerator() {
	}

	public static void main(String[] args)
			throws DocumentException, MalformedURLException, URISyntaxException, IOException {
		new PdfGenerator().generatePdf();
	}

	public void generatePdf() throws DocumentException, URISyntaxException, MalformedURLException, IOException {
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream("iTextHelloWorld.pdf"));
		document.open();

		Paragraph title2 = new Paragraph("This is Chapter 2",
				FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLDITALIC, BaseColor.BLACK));
		Chapter chapter2 = new Chapter(title2, 2);
		chapter2.setNumberDepth(0);
		Paragraph someText = new Paragraph("This is some text");
		chapter2.add(someText);
		Paragraph title21 = new Paragraph("This is Section 1 in Chapter 2",
				FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, BaseColor.BLACK));
		Section section1 = chapter2.addSection(title21);
		Paragraph someSectionText = new Paragraph(
				"This is some silly paragraph in a chapter and/or section. It contains some text to test the functionality of Chapters and Section.");
		someSectionText.setSpacingAfter(10.0f);
		section1.add(someSectionText);

		PdfPTable table = new PdfPTable(3);
		addTableHeader(table);
		addRows(table);
		addCustomRows(table);

//		Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
//		Chunk chunk = new Chunk("Hello World", font);
//		document.add(chunk);

//		Image img = Image.getInstance(path.toAbsolutePath().toString());
//		document.add(img);

		section1.add(table);
		document.add(chapter2);
		document.close();
	}

	private void addTableHeader(PdfPTable table) {
		Stream.of("column header 1", "column header 2", "column header 3").forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle));
			table.addCell(header);
		});
	}

	private void addRows(PdfPTable table) {
		table.addCell("row 1, col 1");
		table.addCell("row 1, col 2");
		table.addCell("row 1, col 3");
	}

	private void addCustomRows(PdfPTable table) throws URISyntaxException, BadElementException, IOException {
		Path path = Paths.get(PdfGenerator.class.getResource("Java_logo.png").toURI());
		Image img = Image.getInstance(path.toAbsolutePath().toString());
		img.scalePercent(10);

		PdfPCell imageCell = new PdfPCell(img);
		table.addCell(imageCell);

		PdfPCell horizontalAlignCell = new PdfPCell(new Phrase("row 2, col 2"));
		horizontalAlignCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(horizontalAlignCell);

		PdfPCell verticalAlignCell = new PdfPCell(new Phrase("row 2, col 3"));
		verticalAlignCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		table.addCell(verticalAlignCell);
	}
}
