package nl.tno.willemsph.psd_repository.information_delivery_specification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.text.DocumentException;

import nl.tno.willemsph.psd_repository.common.UserRepository;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class InformationDeliverySpecificationRepository {
	private final static Logger LOGGER = Logger.getLogger(InformationDeliverySpecificationRepository.class.getName());

	private final PropertySetDefinitionRepository propertySetDefinitionRepository;
	private final UserRepository userRepository;

	public InformationDeliverySpecificationRepository(PropertySetDefinitionRepository propertySetDefinitionRepository,
			UserRepository userRepository) {
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
		this.userRepository = userRepository;
	}

	public List<InformationDeliverySpecification> getAllInformationDeliverySpecifications() throws IOException {
		List<InformationDeliverySpecification> allInformationDeliverySpecifications = new ArrayList<>();
		List<String[]> allInformationDeliverySpecificationNames = getAllInformationDeliverySpecificationIdsAndNames();
		for (String[] idsAndName : allInformationDeliverySpecificationNames) {
			InformationDeliverySpecification informationDeliverySpecification = new InformationDeliverySpecification(
					idsAndName[0], idsAndName[1]);
			allInformationDeliverySpecifications.add(informationDeliverySpecification);
		}
		return allInformationDeliverySpecifications;
	}

	public InformationDeliverySpecification getOneInformationDeliverySpecification(String id) {
		InformationDeliverySpecification informationDeliverySpecification = new InformationDeliverySpecification(id,
				null);
		return informationDeliverySpecification;
	}

	private List<String[]> getAllInformationDeliverySpecificationIdsAndNames() throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.append("SELECT ?ids ?name ");
		queryStr.append("WHERE {");
		queryStr.append("	GRAPH ?graph {");
		queryStr.append("		?ids rdf:type IFC4-PSD:InformationDeliverySpecification ; ");
		queryStr.append("	   	IFC4-PSD:name ?name . ");
		queryStr.append("	}");
		queryStr.append("}");
		queryStr.append("ORDER BY ?name ");

		List<String[]> idsAndNames = new ArrayList<>();
		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				String[] idsAndName = new String[2];
				idsAndNames.add(idsAndName);
				JsonNode idsNode = node.get("ids");
				if (idsNode != null) {
					idsAndName[0] = idsNode.get("value").asText();
				}
				JsonNode nameNode = node.get("name");
				if (nameNode != null) {
					String name = nameNode.get("value").asText();
					LOGGER.info("IDS: " + name);
					idsAndName[1] = name;
				}
			}
		}
		return idsAndNames;
	}

	public InformationDeliverySpecification addProp2Pset(String idsId, String psetId, String propId)
			throws IOException {
		String idsGraph = idsId.substring(0, idsId.indexOf('#'));
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", idsId);
		queryStr.setIri("pset", psetId);
		queryStr.setIri("prop", propId);
		queryStr.setIri("idsGraph", idsGraph);
		queryStr.append("INSERT { ");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("  } ");
		queryStr.append("} ");
		queryStr.append("WHERE {");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	 ?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("  } ");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);
		EmbeddedServer.instance.saveIdsModel(idsGraph);

		return new InformationDeliverySpecification(idsId, null);
	}

	public InformationDeliverySpecification removeProp2Pset(String idsId, String psetId, String propId)
			throws IOException {
		String idsGraph = idsId.substring(0, idsId.indexOf('#'));
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", idsId);
		queryStr.setIri("pset", psetId);
		queryStr.setIri("prop", propId);
		queryStr.setIri("idsGraph", idsGraph);
		queryStr.append("DELETE { ");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("  } ");
		queryStr.append("} ");
		queryStr.append("WHERE { ");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	 ?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("  } ");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);
		EmbeddedServer.instance.saveIdsModel(idsGraph);

		return new InformationDeliverySpecification(idsId, null);
	}

	public InformationDeliverySpecification addPset2Ids(String idsId, String psetId, Optional<List<String>> propIds)
			throws IOException, URISyntaxException {
		String psetName = psetId.substring(psetId.indexOf('#') + 1);
		InformationDeliverySpecification ids = getOneInformationDeliverySpecification(idsId);
		boolean psetFound = false;

		InformationDeliverySpecificationResolver idsResolver = new InformationDeliverySpecificationResolver(
				this.userRepository);
		List<RequiredPset> reqPsets = idsResolver.getReqPsets(ids);
		if (reqPsets != null) {
			for (RequiredPset reqPset : reqPsets) {
				if (reqPset.getPropertySetName().equals(psetName)) {
					psetFound = true;
					break;
				}
			}
		}
		if (!psetFound) {
			String idsGraph = idsId.substring(0, idsId.indexOf('#'));
			ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
			queryStr.setIri("ids", idsId);
			queryStr.setIri("pset", psetId);
			queryStr.setIri("idsGraph", idsGraph);
			queryStr.append("INSERT {");
			queryStr.append("  GRAPH ?idsGraph { ");
			queryStr.append("	 ?ids IFC4-PSD:requiredPset [IFC4-PSD:propertySetDef ?pset] . ");
			queryStr.append("  }");
			queryStr.append("}");
			queryStr.append("WHERE {");
			queryStr.append("}");

			EmbeddedServer.instance.update(queryStr);
			EmbeddedServer.instance.saveIdsModel(idsGraph);
		}
		if (propIds.isPresent()) {
			for (String propId : propIds.get()) {
				addProp2Pset(idsId, psetId, propId);
			}
		}
		return ids;
	}

	public InformationDeliverySpecification removePset2Ids(String idsId, String psetId) throws IOException {
		InformationDeliverySpecification ids = getOneInformationDeliverySpecification(idsId);
		String idsGraph = idsId.substring(0, idsId.indexOf('#'));
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", idsId);
		queryStr.setIri("pset", psetId);
		queryStr.setIri("idsGraph", idsGraph);
		queryStr.append("DELETE {");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("	 ?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("	 ?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("  }");
		queryStr.append("}");
		queryStr.append("WHERE {");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	 ?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("  }");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);
		EmbeddedServer.instance.saveIdsModel(idsGraph);
		return ids;
	}

	public InformationDeliverySpecification createInformationDeliverySpecification(String idsId, String name,
			String ownerId, Optional<String> parentId) throws IOException {
		String idsGraph = idsId.substring(0, idsId.indexOf('#'));
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("owners", EmbeddedServer.OWNERS + "#");
		queryStr.setIri("idsGraph", idsGraph);
		queryStr.setIri("ownersGraph", EmbeddedServer.OWNERS);
		queryStr.setIri("ids", idsId);
		queryStr.setLiteral("name", name);
		queryStr.setIri("owner", ownerId);
		queryStr.append("INSERT {");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("	 ?idsGraph rdf:type owl:Ontology . ");
		queryStr.append("	 ?ids rdf:type IFC4-PSD:InformationDeliverySpecification ; ");
		queryStr.append("	   IFC4-PSD:name ?name . ");
		if (parentId.isPresent()) {
			queryStr.append("	 ?ids IFC4-PSD:requiredPset [");
			queryStr.append("      IFC4-PSD:propertySetDef ?propertySetDef ; ");
			queryStr.append("      IFC4-PSD:requiredProp ?propertyDef ");
			queryStr.append("    ] . ");
		}
		queryStr.append("  }");
		queryStr.append("  GRAPH ?ownersGraph { ?ids owners:owner ?owner }");
		queryStr.append("}");
		queryStr.append("WHERE {");
		if (parentId.isPresent()) {
			String parentGraph = parentId.get().substring(0, parentId.get().indexOf('#'));
			queryStr.setIri("parentGraph", parentGraph);
			queryStr.setIri("parent", parentId.get());
			queryStr.append("  GRAPH ?parentGraph { ");
			queryStr.append("    ?parent IFC4-PSD:requiredPset ?requiredPset . ");
			queryStr.append("    ?requiredPset IFC4-PSD:propertySetDef ?propertySetDef ; ");
			queryStr.append("      IFC4-PSD:requiredProp ?propertyDef . ");
			queryStr.append("  }");
		}
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);
		EmbeddedServer.instance.saveIdsModel(queryStr.getParam("idsGraph").toString());
		EmbeddedServer.instance.saveOwnersModel();

		return getOneInformationDeliverySpecification(idsId);
	}

	public boolean deleteInformationDeliverySpecification(String idsId) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		String idsGraph = idsId.substring(0, idsId.indexOf('#'));
		queryStr.setIri("idsGraph", idsGraph);
		queryStr.setIri("ownersGraph", EmbeddedServer.OWNERS);
		queryStr.setNsPrefix("owners", EmbeddedServer.OWNERS + "#");
		queryStr.setIri("ids", idsId);
		queryStr.append("DELETE { ");
		queryStr.append("  GRAPH ?ownersGraph { ?ids owners:owner ?owner . } ");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("    ?ids ?pred1 ?obj1 . ");
		queryStr.append("    ?obj1 ?pred2 ?obj2 . ");
		queryStr.append("    ?sub ?inv ?ids . ");
		queryStr.append("  } ");
		queryStr.append("} ");
		queryStr.append("WHERE { ");
		queryStr.append("  GRAPH ?ownersGraph { ?ids owners:owner ?owner . } ");
		queryStr.append("  GRAPH ?idsGraph { ");
		queryStr.append("    ?ids ?pred1 ?obj1 . ");
		queryStr.append("    OPTIONAL { ?obj1 ?pred2 ?obj2  . } ");
		queryStr.append("    OPTIONAL { ?sub ?inv ?ids . } ");
		queryStr.append("  } ");
		queryStr.append("} ");

		EmbeddedServer.instance.update(queryStr);
		EmbeddedServer.instance.saveOwnersModel();
		EmbeddedServer.instance.deleteIdsModel(idsId);

		return true;
	}

	public String exportIDS(String id, ExportFormat format) throws IOException, DocumentException, URISyntaxException {
		switch (format) {
		case PDF:
			return PdfGenerator.generate(getOneInformationDeliverySpecification(id), propertySetDefinitionRepository,
					userRepository);
		case JSON:
			return null;
		default:
			return null;
		}
	}

	public InformationDeliverySpecification addIds2Ids(String thisIdsId, String otherIdsId) throws IOException {
		String thisIdsGraph = thisIdsId.substring(0, thisIdsId.indexOf('#'));
		String otherIdsGraph = otherIdsId.substring(0, otherIdsId.indexOf('#'));
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("thisIdsGraph", thisIdsGraph);
		queryStr.setIri("thisIdsId", thisIdsId);
		queryStr.setIri("otherIdsGraph", otherIdsGraph);
		queryStr.setIri("otherIdsId", otherIdsId);
		queryStr.append("INSERT {");
		queryStr.append("  GRAPH ?thisIdsGraph { ");
		queryStr.append("	 ?thisIdsId IFC4-PSD:requiredPset [");
		queryStr.append("      IFC4-PSD:propertySetDef ?propertySetDef ; ");
		queryStr.append("      IFC4-PSD:requiredProp ?propertyDef ");
		queryStr.append("    ] . ");
		queryStr.append("  } ");
		queryStr.append("} ");
		queryStr.append("WHERE {");
		queryStr.append("  GRAPH ?otherIdsGraph { ");
		queryStr.append("    ?otherIdsId IFC4-PSD:requiredPset ?requiredPset . ");
		queryStr.append("    ?requiredPset IFC4-PSD:propertySetDef ?propertySetDef ; ");
		queryStr.append("      IFC4-PSD:requiredProp ?propertyDef . ");
		queryStr.append("  } ");
		queryStr.append("} ");

		EmbeddedServer.instance.update(queryStr);
		EmbeddedServer.instance.saveIdsModel(queryStr.getParam("thisIdsGraph").toString());
		
		return getOneInformationDeliverySpecification(thisIdsId);
	}

}
