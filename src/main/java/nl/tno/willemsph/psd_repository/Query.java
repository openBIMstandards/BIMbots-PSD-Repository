package nl.tno.willemsph.psd_repository;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecification;
import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecificationRepository;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;

@Component
public class Query implements GraphQLQueryResolver {

	private final PropertySetDefinitionRepository propertySetDefinitionRepository;
	private final InformationDeliverySpecificationRepository informationDeliverySpecificationRepository;

	public Query(PropertySetDefinitionRepository propertySetDefinitionRepository,
			InformationDeliverySpecificationRepository informationDeliverySpecificationRepository) {
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
		this.informationDeliverySpecificationRepository = informationDeliverySpecificationRepository;
	}

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


}
