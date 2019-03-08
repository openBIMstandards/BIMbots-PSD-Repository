package nl.tno.willemsph.psd_repository.property_set_definition;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.common.LanguageTaggedString;
import nl.tno.willemsph.psd_repository.common.User;
import nl.tno.willemsph.psd_repository.common.UserRepository;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinition;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinitionResolver;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class PropertySetDefinitionResolver implements GraphQLResolver<PropertySetDefinition> {
	// private final static Logger LOGGER =
	// Logger.getLogger(PropertySetDefinitionResolver.class.getName());

	private UserRepository userRepository;

	public PropertySetDefinitionResolver(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String getId(PropertySetDefinition propertySetDefinition) throws URISyntaxException, IOException {
		String id = propertySetDefinition.getId();
		if (id == null) {
			id = getId(propertySetDefinition.getName(), "PropertySetDef");
			propertySetDefinition.setId(id);
		}
		return id;
	}

	public String getApplicableTypeValue(PropertySetDefinition propertySetDefinition)
			throws IOException, URISyntaxException {
		URI subject = new URI(getId(propertySetDefinition));
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#applicableTypeValue");
		return EmbeddedServer.getStringValue(subject, predicate, true);
	}

	public List<String> getApplicableClasses(PropertySetDefinition propertySetDefinition)
			throws IOException, URISyntaxException {
		String id = getId(propertySetDefinition);
		if (id != null) {
			URI subject = new URI(id);
			URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#applicableClass");
			return EmbeddedServer.getStringValues(subject, predicate, true);
		}
		return null;
	}

	public String getDefinition(PropertySetDefinition propertySetDefinition) throws IOException, URISyntaxException {
		String id = getId(propertySetDefinition);
		if (id != null) {
			URI subject = new URI(id);
			URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#definition");
			return EmbeddedServer.getStringValue(subject, predicate, true);
		}
		return null;
	}

	public String getDefinitionAlias(PropertySetDefinition propertySetDefinition, String language)
			throws URISyntaxException, IOException {
		URI subject = new URI(getId(propertySetDefinition));
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#definitionAlias");
		return EmbeddedServer.getLanguageTaggedStringValue(subject, predicate, language, true);
	}

	public List<LanguageTaggedString> getDefinitionAliases(PropertySetDefinition propertySetDefinition)
			throws URISyntaxException, IOException {
		URI subject = new URI(getId(propertySetDefinition));
		URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#definitionAlias");
		return EmbeddedServer.getLanguageTaggedStringValues(subject, predicate, true);
	}

	public List<PropertyDefinition> getPropertyDefs(PropertySetDefinition propertySetDefinition)
			throws URISyntaxException, IOException {
		List<PropertyDefinition> propertyDefs = new ArrayList<>();
		String id = getId(propertySetDefinition);
		if (id != null) {
			URI subject = new URI(id);
			URI predicate = new URI(EmbeddedServer.IFC4_PSD + "#propertyDef");
			String valuesString = EmbeddedServer.getStringValue(subject, predicate, true);
			if (valuesString != null) {
				String[] values = valuesString.split(" ");
				List<String> valueList = Arrays.asList(values);
				for (String value : valueList) {
					propertyDefs.add(new PropertyDefinition(value));
				}
				Collections.sort(propertyDefs, new Comparator<PropertyDefinition>() {
					@Override
					public int compare(PropertyDefinition o1, PropertyDefinition o2) {
						PropertyDefinitionResolver propertyDefinitionResolver = new PropertyDefinitionResolver();
						try {
							return propertyDefinitionResolver.getName(o1)
									.compareTo(propertyDefinitionResolver.getName(o2));
						} catch (IOException | URISyntaxException e) {
							e.printStackTrace();
						}
						return 0;
					}
				});
			}
		}
		return propertyDefs;
	}

	private String getId(String name, String type) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setIri("type", EmbeddedServer.IFC4_PSD + "#" + type);
		queryStr.setLiteral("name", name);
		queryStr.append("SELECT ?id ");
		queryStr.append("WHERE {");
		queryStr.append("	GRAPH ?graph {");
		queryStr.append("		?id rdf:type ?type ; ");
		queryStr.append("	   		IFC4-PSD:name ?name . ");
		queryStr.append("	}");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				JsonNode idNode = node.get("id");
				if (idNode != null) {
					return idNode.get("value").asText();
				}
			}
		}
		return null;
	}
	
	public User getOwner(PropertySetDefinition propertySetDefinition) throws IOException, URISyntaxException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("owners", EmbeddedServer.OWNERS + "#");
		queryStr.append("SELECT ?user ");
		queryStr.append("WHERE {");
		queryStr.setIri("pset", getId(propertySetDefinition));
		queryStr.setIri("graph", EmbeddedServer.OWNERS);
		queryStr.append("  GRAPH ?graph { ");
		queryStr.append("    ?pset owners:owner ?user .");
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
