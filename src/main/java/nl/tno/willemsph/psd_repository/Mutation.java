package nl.tno.willemsph.psd_repository;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionInput;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;

@Component
public class Mutation implements GraphQLMutationResolver {
	private final PropertySetDefinitionRepository propertySetDefinitionRepository;

	public Mutation(PropertySetDefinitionRepository propertySetDefinitionRepository) {
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
	}

	public PropertySetDefinition createPropertySetDefinition(PropertySetDefinitionInput propertySetDefinitionInput) throws IOException {
		return propertySetDefinitionRepository.createPropertySetDefinition(propertySetDefinitionInput);
	}

}
