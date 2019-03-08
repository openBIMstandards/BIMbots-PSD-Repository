package nl.tno.willemsph.psd_repository.property_definition;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.common.LanguageTaggedString;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_type.PropertyType;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class PropertyDefinitionResolver implements GraphQLResolver<PropertyDefinition> {
//	private final static Logger LOGGER = Logger.getLogger(PropertyDefinitionResolver.class.getName());

	public String getName(PropertyDefinition propertyDefinition) throws IOException, URISyntaxException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#name");
		return EmbeddedServer.getStringValue(subject, predicate, true);
	}

	public String getNameAlias(PropertyDefinition propertyDefinition, String language)
			throws URISyntaxException, IOException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#nameAlias");
		return EmbeddedServer.getLanguageTaggedStringValue(subject, predicate, language, true);
	}

	public List<LanguageTaggedString> getNameAliases(PropertyDefinition propertyDefinition)
			throws URISyntaxException, IOException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#nameAlias");
		return EmbeddedServer.getLanguageTaggedStringValues(subject, predicate, true);
	}

	public String getIfdguid(PropertyDefinition propertyDefinition) throws IOException, URISyntaxException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#ifdguid");
		return EmbeddedServer.getStringValue(subject, predicate, true);
	}

	public String getDefinition(PropertyDefinition propertyDefinition) throws IOException, URISyntaxException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#definition");
		return EmbeddedServer.getStringValue(subject, predicate, true);
	}

	public String getDefinitionAlias(PropertyDefinition propertyDefinition, String language)
			throws URISyntaxException, IOException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#definitionAlias");
		return EmbeddedServer.getLanguageTaggedStringValue(subject, predicate, language, true);
	}

	public List<LanguageTaggedString> getDefinitionAliases(PropertyDefinition propertyDefinition)
			throws URISyntaxException, IOException {
		URI subject = new URI(propertyDefinition.getId());
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#definitionAlias");
		return EmbeddedServer.getLanguageTaggedStringValues(subject, predicate, true);
	}

	public PropertyType getPropertyType(PropertyDefinition propertyDefinition) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("subject", propertyDefinition.getId());
		queryStr.setIri("propertyTypePred", EmbeddedServer.IFC4_PSD + "#propertyType");
		queryStr.setIri("dataTypePred", EmbeddedServer.IFC4_PSD + "#dataType");
		queryStr.setIri("enumItemPred", EmbeddedServer.IFC4_PSD + "#enumItem");
		queryStr.append("SELECT ?type ?dataType ?enumItem ");
		queryStr.append("WHERE { ");
		queryStr.append("	GRAPH ?graph { ");
		queryStr.append("		?subject ?propertyTypePred ?propertyType . ");
		queryStr.append("		?propertyType rdf:type ?type . ");
		queryStr.append("		OPTIONAL { ?propertyType ?dataTypePred ?dataType } . ");
		queryStr.append("		OPTIONAL { ?propertyType ?enumItemPred ?enumItem } . ");
		queryStr.append("	} ");
		queryStr.append("} ");
		queryStr.append("ORDER BY ?enumItem ");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			String type = null;
			String dataType = null;
			List<String> enumItems = new ArrayList<>();
			for (JsonNode node : responseNodes) {
				JsonNode typeNode = node.get("type");
				if (typeNode != null) {
					type = typeNode.get("value").asText();
				}
				JsonNode dataTypeNode = node.get("dataType");
				if (dataTypeNode != null) {
					dataType = dataTypeNode.get("value").asText();
				}
				JsonNode enumItemNode = node.get("enumItem");
				if (enumItemNode != null) {
					enumItems.add(enumItemNode.get("value").asText());
				}

			}
			PropertyType propertyType = new PropertyType(type, dataType);
			if (!enumItems.isEmpty()) {
				propertyType.setEnumItems(enumItems);
			}
			return propertyType;
		}
		return null;
	}

	public List<PropertySetDefinition> getInvPropertySetDefinitions(PropertyDefinition propertyDefinition) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("propDef", propertyDefinition.getId());
		queryStr.append("SELECT ?name ");
		queryStr.append("WHERE { ");
		queryStr.append("	GRAPH ?graph { ");
		queryStr.append("		?pset  IFC4-PSD:propertyDef ?propDef . ");
		queryStr.append("		?pset IFC4-PSD:name ?name . ");
		queryStr.append("	} ");
		queryStr.append("} ");
		queryStr.append("ORDER BY ?name ");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			List<PropertySetDefinition> psets = new ArrayList<>();
			for (JsonNode node : responseNodes) {
				JsonNode nameNode = node.get("name");
				if (nameNode != null) {
					psets.add(new PropertySetDefinition(nameNode.get("value").asText()));
				}
			}
			return psets;
		}
		return null;
	}
}
