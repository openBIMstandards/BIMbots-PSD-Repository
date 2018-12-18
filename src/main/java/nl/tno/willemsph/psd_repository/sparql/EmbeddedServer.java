package nl.tno.willemsph.psd_repository.sparql;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.tno.willemsph.psd_repository.common.LanguageTaggedString;

@Service
public class EmbeddedServer {
	private final static Logger LOGGER = Logger.getLogger(EmbeddedServer.class.getName());

	public static final String QUERY_URL = "http://localhost:3330/rdf/query";
	public static final String UPDATE_URL = "http://localhost:3330/rdf/update";
	public static final String IFC4 = "http://ifcowl.openbimstandards.org/IFC4";
	public static final String IFC4_PSD = "http://www.buildingsmart-tech.org/ifcOWL/IFC4-PSD";
	public static final String[] PSETS = { "Pset_BeamCommon", "Pset_BuildingCommon", "Pset_BuildingElementProxyCommon",
			"Pset_BuildingStoreyCommon", "Pset_ChimneyCommon", "Pset_ColumnCommon", "Pset_ConcreteElementGeneral",
			"Pset_CoveringCommon", "Pset_DoorCommon", "Pset_DoorWindowGlazingType",
			"Pset_EnvironmentalImpactIndicators", "Pset_EnvironmentalImpactValues", "Pset_MaterialCommon",
			"Pset_MaterialConcrete", "Pset_MemberCommon", "Pset_OpeningElementCommon", "Pset_PileCommon",
			"Pset_PlateCommon", "Pset_PrecastConcreteElementGeneral", "Pset_PrecastSlab", "Pset_RoofCommon",
			"Pset_SlabCommon", "Pset_SpaceCommon", "Pset_StairCommon", "Pset_WallCommon", "Pset_WindowCommon" };
	public static final String[] IDSS = { "Basic_IDM", "Kalkzandsteen_IDS" };
	private static Dataset ds;
	private ClassPathResource ifc4Resource, ifc4PsdResource;
	private List<ClassPathResource> psetResources, idsResources;
	public static FusekiServer sparql;
	public static EmbeddedServer instance;
	private static PrefixMapping prefixMapping;

	public EmbeddedServer() throws IOException {
		instance = this;
		ifc4Resource = new ClassPathResource("IFC4.ttl");
		ifc4PsdResource = new ClassPathResource("psetdef.ttl");

		psetResources = new ArrayList<>();
		for (String pset : PSETS) {
			psetResources.add(new ClassPathResource("psets/" + pset + ".ttl"));
		}
		idsResources = new ArrayList<>();
		for (String ids : IDSS) {
			idsResources.add(new ClassPathResource("psets/" + ids + ".ttl"));
		}

		startServer();
	}

	public void startServer() throws IOException {
		Model defaultModel = ModelFactory.createDefaultModel();
		defaultModel.read(ifc4Resource.getInputStream(), null, "TURTLE");
		defaultModel.read(ifc4PsdResource.getInputStream(), null, "TURTLE");
		for (ClassPathResource psetResource : psetResources) {
			LOGGER.info("Reading " + psetResource.getFilename());
			defaultModel.read(psetResource.getInputStream(), null, "TURTLE");
		}
		for (ClassPathResource idsResource : idsResources) {
			LOGGER.info("Reading " + idsResource.getFilename());
			defaultModel.read(idsResource.getInputStream(), null, "TURTLE");
		}
		ds = DatasetFactory.create(defaultModel);
		sparql = FusekiServer.create().add("/rdf", ds, true).build();
		sparql.start();
	}

	public static PrefixMapping getPrefixMapping() {
		if (prefixMapping == null) {
			prefixMapping = PrefixMapping.Factory.create();
			prefixMapping.setNsPrefix("rdf", RDF.uri);
			prefixMapping.setNsPrefix("rdfs", RDFS.uri);
			prefixMapping.setNsPrefix("owl", OWL.NS);
			prefixMapping.setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace/");
			prefixMapping.setNsPrefix("IFC4", IFC4 + '#');
			prefixMapping.setNsPrefix("IFC4-PSD", IFC4_PSD + '#');
			prefixMapping.setNsPrefix("expr", "https://w3id.org/express#");
		}
		return prefixMapping;
	}

	public JsonNode query(ParameterizedSparqlString queryStr) throws IOException {
		String query = queryStr.toString();
		HttpURLConnection con = getQueryConnection();
		JsonNode bindings = sendQuery(con, query);
		return bindings;
	}

	private HttpURLConnection getQueryConnection() throws IOException {
		URL obj = new URL(QUERY_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/sparql-query");
		return con;
	}

	private JsonNode sendQuery(HttpURLConnection con, String query) throws IOException {
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(query);
		wr.flush();
		wr.close();
		// int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jTree = mapper.readTree(in);
		JsonNode bindings = jTree.findValue("bindings");
		return bindings;
	}

	public void update(ParameterizedSparqlString queryStr) throws IOException {
		String query = queryStr.toString();
		HttpURLConnection con = getUpdateConnection();
		sendUpdate(con, query);
	}

	private HttpURLConnection getUpdateConnection() throws IOException {
		URL obj = new URL(UPDATE_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/sparql-update");
		return con;
	}

	private void sendUpdate(HttpURLConnection con, String query) throws IOException {
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(query);
		wr.flush();
		wr.close();
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + EmbeddedServer.class);
		System.out.println("Post parameters : " + query);
		System.out.println("Response Code : " + responseCode);
	}

	public static String getStringValue(URI subject, URI predicate) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("subject", subject.toString());
		queryStr.setIri("predicate", predicate.toString());
		queryStr.append("SELECT ?value ");
		queryStr.append("WHERE {");
		queryStr.append("	?subject ?predicate ?value .");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			StringBuffer values = new StringBuffer();
			for (JsonNode node : responseNodes) {
				if (values.length() > 0) {
					values.append(" ");
				}
				JsonNode valueNode = node.get("value");
				if (valueNode != null) {
					String type = valueNode.get("type").asText();
					String value = valueNode.get("value").asText();
					LOGGER.info("type: " + type + " value: " + value);
					switch (type) {
					case "uri":
						break;
					case "literal":
						break;
					}
					values.append(value);
				}
			}
			return values.toString();
		}
		return null;
	}

	public static List<String> getStringValues(URI subject, URI predicate) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("subject", subject.toString());
		queryStr.setIri("predicate", predicate.toString());
		queryStr.append("SELECT ?value ");
		queryStr.append("WHERE {");
		queryStr.append("	?subject ?predicate ?value .");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			List<String> values = new ArrayList<>();
			for (JsonNode node : responseNodes) {
				JsonNode valueNode = node.get("value");
				if (valueNode != null) {
					String value = valueNode.get("value").asText();
					values.add(value);
				}
			}
			return values;
		}
		return null;
	}

	public static List<LanguageTaggedString> getLanguageTaggedStringValues(URI subject, URI predicate)
			throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("subject", subject.toString());
		queryStr.setIri("predicate", predicate.toString());
		queryStr.append("SELECT ?value ");
		queryStr.append("WHERE {");
		queryStr.append("	?subject ?predicate ?value .");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			List<LanguageTaggedString> langTagStrs = new ArrayList<>();
			for (JsonNode node : responseNodes) {
				LanguageTaggedString langTagStr = new LanguageTaggedString();
				JsonNode valueNode = node.get("value");
				if (valueNode != null) {
					langTagStr.setContent(valueNode.get("value").asText());
					langTagStr.setLanguage(valueNode.get("xml:lang").asText());
				}
				langTagStrs.add(langTagStr);
			}
			return langTagStrs;
		}
		return null;
	}

	public static String getLanguageTaggedStringValue(URI subject, URI predicate, String language) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("subject", subject.toString());
		queryStr.setIri("predicate", predicate.toString());
		queryStr.setLiteral("language", language);
		queryStr.append("SELECT ?value ");
		queryStr.append("WHERE {");
		queryStr.append("	?subject ?predicate ?value .");
		queryStr.append("	FILTER langMatches(lang(?value), ?language)");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				JsonNode valueNode = node.get("value");
				if (valueNode != null) {
					return valueNode.get("value").asText();
				}
			}
		}
		return null;
	}

}
