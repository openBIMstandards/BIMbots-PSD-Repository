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

import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class InformationDeliverySpecificationRepository {
	private final static Logger LOGGER = Logger.getLogger(InformationDeliverySpecificationRepository.class.getName());

	private final PropertySetDefinitionRepository propertySetDefinitionRepository;
	
	public InformationDeliverySpecificationRepository(PropertySetDefinitionRepository propertySetDefinitionRepository) {
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
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
		queryStr.append("	?ids rdf:type IFC4-PSD:InformationDeliverySpecification ; ");
		queryStr.append("	   IFC4-PSD:name ?name . ");
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
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", idsId);
		queryStr.setIri("pset", psetId);
		queryStr.setIri("prop", propId);
		queryStr.append("INSERT {");
		queryStr.append("	?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("}");
		queryStr.append("WHERE {");
		queryStr.append("	?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);

		return new InformationDeliverySpecification(idsId, null);
	}

	public InformationDeliverySpecification removeProp2Pset(String idsId, String psetId, String propId)
			throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", idsId);
		queryStr.setIri("pset", psetId);
		queryStr.setIri("prop", propId);
		queryStr.append("DELETE {");
		queryStr.append("	?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("}");
		queryStr.append("WHERE {");
		queryStr.append("	?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);

		return new InformationDeliverySpecification(idsId, null);
	}

	public InformationDeliverySpecification addPset2Ids(String idsId, String psetId, Optional<List<String>> propIds)
			throws IOException, URISyntaxException {
		String psetName = psetId.substring(psetId.indexOf('#') + 1);
		InformationDeliverySpecification ids = getOneInformationDeliverySpecification(idsId);
		boolean psetFound = false;

		InformationDeliverySpecificationResolver idsResolver = new InformationDeliverySpecificationResolver();
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
			ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
			queryStr.setIri("ids", idsId);
			queryStr.setIri("pset", psetId);
			queryStr.append("INSERT {");
			queryStr.append("	?ids IFC4-PSD:requiredPset [IFC4-PSD:propertySetDef ?pset] . ");
			queryStr.append("}");
			queryStr.append("WHERE {");
			queryStr.append("}");

			EmbeddedServer.instance.update(queryStr);
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
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("ids", idsId);
		queryStr.setIri("pset", psetId);
		queryStr.append("DELETE {");
		queryStr.append("	?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("	?reqPset IFC4-PSD:requiredProp ?prop . ");
		queryStr.append("	?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("}");
		queryStr.append("WHERE {");
		queryStr.append("	?ids IFC4-PSD:requiredPset ?reqPset . ");
		queryStr.append("	?reqPset IFC4-PSD:propertySetDef ?pset . ");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);
		return ids;
	}

	public InformationDeliverySpecification createInformationDeliverySpecification(String idsId, String name,
			Optional<String> parentId) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.append("INSERT {");
		queryStr.setIri("ids", idsId);
		queryStr.setLiteral("name", name);
		queryStr.append("	?ids rdf:type IFC4-PSD:InformationDeliverySpecification ; ");
		queryStr.append("	   IFC4-PSD:name ?name . ");
		if (parentId.isPresent()) {
			queryStr.setIri("parent", parentId.get());
		}
		queryStr.append("}");
		queryStr.append("WHERE {");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);

		return getOneInformationDeliverySpecification(idsId);
	}

	public String exportIDS(String id, ExportFormat format) throws IOException, DocumentException, URISyntaxException {
		switch (format) {
		case PDF:
			return PdfGenerator.generate(getOneInformationDeliverySpecification(id), propertySetDefinitionRepository);
		case JSON:
			return null;
		default:
			return null;
		}
	}
}
