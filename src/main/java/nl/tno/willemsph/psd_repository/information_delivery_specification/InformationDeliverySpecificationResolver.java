package nl.tno.willemsph.psd_repository.information_delivery_specification;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.common.User;
import nl.tno.willemsph.psd_repository.common.UserRepository;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class InformationDeliverySpecificationResolver implements GraphQLResolver<InformationDeliverySpecification> {
	// private final static Logger LOGGER =
	// Logger.getLogger(PropertySetDefinitionResolver.class.getName());
	
	private UserRepository userRepository;

	public InformationDeliverySpecificationResolver(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String getName(InformationDeliverySpecification ids) throws IOException {
		if (ids.getName() == null) {
			ids.setName(getName(ids.getId()));
		}
		return ids.getName();
	}

	public InformationDeliverySpecification getParent(InformationDeliverySpecification ids) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", ids.getId());
		queryStr.append("SELECT ?parent ");
		queryStr.append("WHERE {");
		queryStr.append("	GRAPH ?graph {");
		queryStr.append("		?ids IFC4-PSD:parent ?parent . ");
		queryStr.append("	}");
		queryStr.append("}");
		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				JsonNode parentNode = node.get("parent");
				if (parentNode != null) {
					return new InformationDeliverySpecification(parentNode.get("value").asText(), null);
				}
			}
		}
		return null;
	}

	public List<RequiredPset> getReqPsets(InformationDeliverySpecification ids) throws URISyntaxException, IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", ids.getId());
		queryStr.append("SELECT ?pset ?psetName ?prop ?propName ");
		queryStr.append("WHERE {");
		queryStr.append("	GRAPH ?graph1 {");
		queryStr.append("		?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("		?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("	}");
		queryStr.append("	GRAPH ?graph2 {");
		queryStr.append("		?pset IFC4-PSD:name ?psetName . ");
		queryStr.append("	}");
		queryStr.append("	OPTIONAL {");
		queryStr.append("		GRAPH ?graph1 {");
		queryStr.append("			?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("		}");
		queryStr.append("		GRAPH ?graph2 {");
		queryStr.append("			?prop IFC4-PSD:name ?propName . ");
		queryStr.append("		}");
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
			Collections.sort(requiredPsets, new Comparator<RequiredPset>() {
				@Override
				public int compare(RequiredPset o1, RequiredPset o2) {
					return o1.getPropertySetName().compareTo(o2.getPropertySetName());
				}
			});
			;
			return requiredPsets;
		}
		return null;
	}

	private String getName(String id) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("id", id);
		queryStr.append("SELECT ?name ");
		queryStr.append("WHERE {");
		queryStr.append("	GRAPH ?graph {");
		queryStr.append("	?id IFC4-PSD:name ?name . ");
		queryStr.append("	}");
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
	
	public User getOwner(InformationDeliverySpecification ids) throws IOException, URISyntaxException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("owners", EmbeddedServer.OWNERS + "#");
		queryStr.append("SELECT ?user ");
		queryStr.append("WHERE {");
		queryStr.setIri("ids", ids.getId());
		queryStr.setIri("graph", EmbeddedServer.OWNERS);
		queryStr.append("  GRAPH ?graph { ");
		queryStr.append("    ?ids owners:owner ?user .");
		queryStr.append("  }");
		queryStr.append("}");
		
		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				JsonNode userNode = node.get("user");
				if (userNode != null) {
					return userRepository.findById(userNode.get("value").asText());
				}
			}
		}
		return null;
	}
}
