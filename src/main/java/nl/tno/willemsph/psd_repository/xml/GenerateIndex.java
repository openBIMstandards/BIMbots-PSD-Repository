package nl.tno.willemsph.psd_repository.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

public class GenerateIndex {

	public static void main(String[] argv) throws FileNotFoundException {
		new GenerateIndex();
	}

	public GenerateIndex() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File("src/main/resources/static/psets/IFC4/index.html"));

		writeBefore(writer);
		List<String> ifc4Psets = EmbeddedServer.getIfc4Psets();
		for (String ifc4Pset : ifc4Psets) {
			if (ifc4Pset.endsWith("html")) {
				continue;
			}
			String psetFileName = ifc4Pset.substring(ifc4Pset.lastIndexOf("Pset_"));
			writer.println("<li><a href=\"" + psetFileName + "\">" + psetFileName + "</a></li>");
		}
		writeAfter(writer);
		writer.close();
	}

	private void writeAfter(PrintWriter writer) {
		writer.println("	</ul> ");
		writer.println("	</body> ");
		writer.println("</html>");

	}

	private void writeBefore(PrintWriter writer) {
		writer.println("<!DOCTYPE html>");
		writer.println("<html>");
		writer.println("<head>");
		writer.println("<meta charset=\"ISO-8859-1\"> ");
		writer.println("<title>psets/IFC4</title> ");
		writer.println("</head> ");
		writer.println("<body> ");
		writer.println("	<h3>pset ontology</h3> ");
		writer.println("	<ul>");
		writer.println("	<li><a href=\"../psetdef.ttl\">../psetdef.ttl</a></li> ");
		writer.println("	</ul> ");
		writer.println("	<h3>psets/IFC4</h3> ");
		writer.println("	<ul> ");
	}

}
