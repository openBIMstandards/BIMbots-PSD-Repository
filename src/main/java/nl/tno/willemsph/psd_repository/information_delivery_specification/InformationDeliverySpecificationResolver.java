package nl.tno.willemsph.psd_repository.information_delivery_specification;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class InformationDeliverySpecificationResolver implements GraphQLResolver<InformationDeliverySpecification> {
	// private final static Logger LOGGER =
	// Logger.getLogger(PropertySetDefinitionResolver.class.getName());

	public String getName(InformationDeliverySpecification ids) throws IOException {
		if (ids.getName() == null) {
			ids.setName(getName(ids.getId()));
		}
		return ids.getName();
	}

	public List<RequiredPset> getReqPsets(InformationDeliverySpecification ids) throws URISyntaxException, IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", ids.getId());
		queryStr.append("SELECT ?pset ?psetName ?prop ?propName ");
		queryStr.append("WHERE {");
		queryStr.append("	?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("	?pset IFC4-PSD:name ?psetName . ");
		queryStr.append("	OPTIONAL {");		
		queryStr.append("		?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("		?prop IFC4-PSD:name ?propName . ");
		queryStr.append("	}");
		queryStr.append("}");
		queryStr.append("ORDER BY ?psetName ?propName ");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			List<RequiredPset> requiredPsets = new ArrayList<>();
			Map<String, List<URI>> reqPsets = new HashMap<>();
			for (JsonNode node : responseNodes) {
				String psetName = null;
				JsonNode psetNameNode = node.get("psetName");
				if (psetNameNode != null) {
					psetName = psetNameNode.get("value").asText();
					List<URI> reqPropertyIds = reqPsets.get(psetName);
					if (reqPropertyIds == null) {
						reqPsets.put(psetName, new ArrayList<>());
					}
				}
				JsonNode propNode = node.get("prop");
				if (propNode != null) {
					reqPsets.get(psetName).add(new URI(propNode.get("value").asText()));
				}
			}
			for (String psetName : reqPsets.keySet()) {
				requiredPsets.add(new RequiredPset(psetName, reqPsets.get(psetName)));
			}
			return requiredPsets;
		}
		return null;
	}

	private String getName(String id) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("id", id);
		queryStr.append("SELECT ?name ");
		queryStr.append("WHERE {");
		queryStr.append("	?id IFC4-PSD:name ?name . ");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				JsonNode nameNode = node.get("name");
				if (nameNode != null) {
					return nameNode.get("value").asText();
				}
			}
		}
		return null;
	}
}
