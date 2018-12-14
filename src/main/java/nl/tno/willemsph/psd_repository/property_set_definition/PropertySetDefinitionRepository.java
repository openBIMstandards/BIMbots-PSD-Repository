package nl.tno.willemsph.psd_repository.property_set_definition;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinition;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinitionInput;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class PropertySetDefinitionRepository {
	private final static Logger LOGGER = Logger.getLogger(PropertySetDefinitionRepository.class.getName());

	public List<PropertySetDefinition> getAllPropertySetDefinitions() throws IOException {
		List<PropertySetDefinition> allPropertySetDefinitions = new ArrayList<>();
		List<String> allPropertySetDefinitionNames = getAllPropertySetDefinitionNames(null);
		for (String psetName : allPropertySetDefinitionNames) {
			PropertySetDefinition propertySetDefinition = new PropertySetDefinition(psetName);
			allPropertySetDefinitions.add(propertySetDefinition);
		}
		return allPropertySetDefinitions;
	}

	public PropertySetDefinition getOnePropertySetDefinition(String psetName) {
		PropertySetDefinition propertySetDefinition = new PropertySetDefinition(psetName);
		return propertySetDefinition;
	}

	public List<PropertySetDefinition> getAllPropertySetDefsForClass(String classId) throws IOException {
		List<PropertySetDefinition> allPropertySetDefinitions = new ArrayList<>();
		List<String> allPropertySetDefinitionNames = getAllPropertySetDefinitionNames(classId);
		for (String psetName : allPropertySetDefinitionNames) {
			PropertySetDefinition propertySetDefinition = new PropertySetDefinition(psetName);
			allPropertySetDefinitions.add(propertySetDefinition);
		}
		return allPropertySetDefinitions;
	}

	private List<String> getAllPropertySetDefinitionNames(String classId) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.append("SELECT ?pset ?name ");
		queryStr.append("WHERE {");
		if (classId == null) {
			queryStr.append("	?pset rdf:type IFC4-PSD:PropertySetDef ; ");
			queryStr.append("	   IFC4-PSD:name ?name . ");
		} else {
			queryStr.append("	?pset rdf:type IFC4-PSD:PropertySetDef ; ");
			queryStr.append("	   IFC4-PSD:name ?name . ");
			queryStr.setIri("classId", classId);
			queryStr.append("	  ?classId rdfs:subClassOf* ?parentClass . ");
			queryStr.append("	  ?pset IFC4-PSD:applicableClass ?parentClass . ");
			queryStr.append("	  ?parentClass rdf:type owl:Class . ");
			queryStr.append("FILTER ( ");
			queryStr.append("	!EXISTS { ?parentClass owl:unionOf ?union } ");
			queryStr.append("	&& !EXISTS {?parentClass rdfs:subClassOf expr:SELECT } ");
			queryStr.append("	&& ?parentClass != owl:Thing ");
			queryStr.append(") ");
		}
		queryStr.append("}");
		queryStr.append("ORDER BY ?name ");

		List<String> psetNames = new ArrayList<>();
		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				JsonNode nameNode = node.get("name");
				if (nameNode != null) {
					String name = nameNode.get("value").asText();
					LOGGER.info("PSET: " + name);
					psetNames.add(name);
				}
			}
		}
		return psetNames;
	}

	public PropertySetDefinition createPropertySetDefinition(PropertySetDefinitionInput propertySetDefinitionInput)
			throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("psd", propertySetDefinitionInput.getId());
		queryStr.setLiteral("name", propertySetDefinitionInput.getName());
		queryStr.append("INSERT { ");
		queryStr.append("  ?psd rdf:type IFC4-PSD:PropertySetDef . ");
		queryStr.append("  ?psd IFC4-PSD:name ?name . ");
		if (propertySetDefinitionInput.getDefinition() != null) {
			queryStr.setLiteral("definition", propertySetDefinitionInput.getDefinition().toString());
			queryStr.append("	?psd IFC4-PSD:definition ?definition . ");
		}
		if (propertySetDefinitionInput.getIfcVersion() != null) {
			queryStr.setLiteral("ifcVersion", propertySetDefinitionInput.getIfcVersion());
			queryStr.append(
					"	?psd IFC4-PSD:ifcVersion [ rdf:type IFC4-PSD:IfcVersion ; IFC4-PSD:version ?ifcVersion ] . ");
		}
		if (propertySetDefinitionInput.getPropertyDefs() != null
				&& propertySetDefinitionInput.getPropertyDefs().size() > 0) {
			for (PropertyDefinitionInput pDefInput : propertySetDefinitionInput.getPropertyDefs()) {
				queryStr.setIri("pd", pDefInput.getId());
				queryStr.setLiteral("pdName", pDefInput.getName());
				queryStr.append("  ?pd rdf:type IFC4-PSD:PropertyDef . ");
				queryStr.append("  ?pd IFC4-PSD:name ?pdName . ");
				queryStr.append("  ?psd IFC4-PSD:propertyDef ?pd; ");
			}
		}
		queryStr.append("} ");
		queryStr.append("WHERE { } ");

		EmbeddedServer.instance.update(queryStr);

		return getOnePropertySetDefinition(propertySetDefinitionInput.getName());
	}

	public PropertyDefinition getPropertyDef(URI reqPropertyId) {
		return new PropertyDefinition(reqPropertyId.toString());
	}

}
