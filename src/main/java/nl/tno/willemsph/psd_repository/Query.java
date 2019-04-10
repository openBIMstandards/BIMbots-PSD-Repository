package nl.tno.willemsph.psd_repository;

import java.io.IOException;
import java.util.List;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecification;
import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecificationRepository;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class Query implements GraphQLQueryResolver {

	private final PropertySetDefinitionRepository propertySetDefinitionRepository;
	private final InformationDeliverySpecificationRepository informationDeliverySpecificationRepository;

	public Query(PropertySetDefinitionRepository propertySetDefinitionRepository,
			InformationDeliverySpecificationRepository informationDeliverySpecificationRepository) {
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
		this.informationDeliverySpecificationRepository = informationDeliverySpecificationRepository;
	}

	//
	// Property Set Definition
	//

	public List<PropertySetDefinition> allPSDs() throws IOException {
		return this.propertySetDefinitionRepository.getAllPropertySetDefinitions();
	}

	/**
	 * GRAPHQL query: onePSD(name: String!): PropertySetDefinition
	 * 
	 * @param name Name of the demanded property set definition
	 * @return The demanded property set definition
	 */
	public PropertySetDefinition onePSD(String name) {
		return this.propertySetDefinitionRepository.getOnePropertySetDefinition(name);
	}

	/**
	 * GRAPHQL query: allPSDsForClass(classId: String!): [PropertySetDefinition]
	 * 
	 * @param classId IfcProduct URI
	 * @return Resulting property sets
	 * @throws IOException
	 */
	public List<PropertySetDefinition> allPSDsForClass(String classId) throws IOException {
		return this.propertySetDefinitionRepository.getAllPropertySetDefsForClass(classId);
	}

	//
	// Property Definition
	//

	/**
	 * GRAPHQL query: allPDs: [PropertyDefinition]
	 * 
	 * @return All property definitions
	 * @throws IOException
	 */
	public List<PropertyDefinition> allPDs() throws IOException {
		return this.propertySetDefinitionRepository.getAllPropertyDefinitions();
	}

	/**
	 * GRAPHQL query: searchPD(searchString: String!): [PropertyDefinition]
	 * 
	 * @param searchString specification of the search string
	 * @return All property definitions with names that contain search string;
	 * @throws IOException 
	 */
	public List<PropertyDefinition> searchPD(String searchString) throws IOException {
		return this.propertySetDefinitionRepository.searchPropertyDefinition(searchString);
	}

	//
	// Information Delivery Specification
	//

	/**
	 * GRAPHQL query: allIDSs: [InformationDeliverySpecification]
	 * 
	 * @return All information delivery specifications
	 * @throws IOException
	 */
	public List<InformationDeliverySpecification> allIDSs() throws IOException {
		return this.informationDeliverySpecificationRepository.getAllInformationDeliverySpecifications();
	}

	/**
	 * GRAPHQL query: oneIDS(id: String!): InformationDeliverySpecification
	 * 
	 * @param id Id (URI) of the demanded information delivery specification
	 * @return The demanded information delivery specification
	 */
	public InformationDeliverySpecification oneIDS(String id) {
		return this.informationDeliverySpecificationRepository.getOneInformationDeliverySpecification(id);
	}

	//
	// SPARQL Query
	//

	public Object sparql(String query) throws IOException {
		ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(
				EmbeddedServer.getPrefixMapping());
		parameterizedSparqlString.append(query);
		JsonNode responseNode = EmbeddedServer.instance.query(parameterizedSparqlString);
		return responseNode;
	}

}
