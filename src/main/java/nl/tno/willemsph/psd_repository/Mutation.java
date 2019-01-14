package nl.tno.willemsph.psd_repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import graphql.GraphQLException;
import nl.tno.willemsph.psd_repository.common.AuthData;
import nl.tno.willemsph.psd_repository.common.SigninPayLoad;
import nl.tno.willemsph.psd_repository.common.User;
import nl.tno.willemsph.psd_repository.common.UserRepository;
import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecification;
import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecificationRepository;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionInput;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;

@Component
public class Mutation implements GraphQLMutationResolver {
	private final UserRepository userRepository;
	private final PropertySetDefinitionRepository propertySetDefinitionRepository;
	private final InformationDeliverySpecificationRepository informationDeliverySpecificationRepository;

	/**
	 * Mutation constructor with injected repositories as arguments.
	 * 
	 * @param propertySetDefinitionRepository
	 * @param informationDeliverySpecificationRepository
	 */
	public Mutation(UserRepository userRepository, PropertySetDefinitionRepository propertySetDefinitionRepository,
			InformationDeliverySpecificationRepository informationDeliverySpecificationRepository) {
		this.userRepository = userRepository;
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
		this.informationDeliverySpecificationRepository = informationDeliverySpecificationRepository;
	}

	/**
	 * Create a user by name and authorization data.
	 * 
	 * @param name User name
	 * @param auth User authorization data
	 * @return User object.
	 * @throws IOException
	 */
	public User createUser(String name, AuthData auth) throws IOException {
		User newUser = new User(name, auth.getEmail(), auth.getPassword());
		return userRepository.saveUser(newUser);
	}

	/**
	 * Sign in a user.
	 * 
	 * @param auth User authorization data
	 * @return Signin Pay Load data
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public SigninPayLoad signinUser(AuthData auth) throws IllegalAccessException, IOException {
		User user = userRepository.findByEmail(auth.getEmail());
		if (user != null && userRepository.verify(user, auth.getPassword())) {
			return new SigninPayLoad(user.getId(), user);
		}
		return new SigninPayLoad("Invalid credentials", (User) null);
		// throw new GraphQLException("Invalid credentials");
	}

	/**
	 * Create a property set definition specified by this PropertySetDefinitionInput
	 * instance
	 * 
	 * GRAPHQL: createPropertySetDefinition(psdInput: PropertySetDefinitionInput!):
	 * PropertySetDefinition
	 * 
	 * @param propertySetDefinitionInput Property set definition property values
	 * @return Created Property set definition
	 * @throws IOException
	 */
	public PropertySetDefinition createPropertySetDefinition(PropertySetDefinitionInput propertySetDefinitionInput)
			throws IOException {
		return propertySetDefinitionRepository.createPropertySetDefinition(propertySetDefinitionInput);
	}

	/**
	 * Update a property set definition specified by this PropertySetDefinitionInput
	 * instance
	 * 
	 * GRAPHQL: updatePropertySetDefinition(psdInput: PropertySetDefinitionInput!):
	 * PropertySetDefinition
	 * 
	 * @param propertySetDefinitionInput Property set definition property values
	 * @return Updated Property set definition
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public PropertySetDefinition updatePropertySetDefinition(PropertySetDefinitionInput propertySetDefinitionInput)
			throws IOException, URISyntaxException {
		return propertySetDefinitionRepository.updatePropertySetDefinition(propertySetDefinitionInput);
	}

	/**
	 * Delete a property set definition
	 * 
	 * GRAPHQL: deletePropertySetDefinition(psetId: ID!)
	 * 
	 * @param psetId Id of property set definition
	 * @throws IOException
	 */
	public boolean deletePropertySetDefinition(String psetId) throws IOException {
		return propertySetDefinitionRepository.deletePropertySetDefinition(psetId);
	}

	/**
	 * Create an information delivery specification
	 * 
	 * GRAPHQL: createInformationDeliverySpecification(idsId: ID!, name: String!,
	 * parentId: ID): InformationDeliverySpecification
	 * 
	 * @param idsId    Id of information delivery specification
	 * @param name     Name of information delivery specification
	 * @param parentId (Optional) parent ID
	 * @return Created information delivery specification
	 * @throws IOException
	 */
	public InformationDeliverySpecification createInformationDeliverySpecification(String idsId, String name,
			Optional<String> parentId) throws IOException {
		return informationDeliverySpecificationRepository.createInformationDeliverySpecification(idsId, name, parentId);
	}

	/**
	 * Add a mandatory property to a required property set to an information
	 * delivery specification
	 * 
	 * GRAPHQL: addPropPsetIds( idsId: ID!, psetId: ID!, propId: ID!):
	 * InformationDeliverySpecification
	 * 
	 * @param idsId  Id of information delivery specification
	 * @param psetId Id of property set definition
	 * @param propId Id of property definition
	 * @return Mutated information delivery specification
	 * @throws IOException
	 */
	public InformationDeliverySpecification addProp2Pset2Ids(String idsId, String psetId, String propId)
			throws IOException {
		return informationDeliverySpecificationRepository.addProp2Pset(idsId, psetId, propId);
	}

	/**
	 * Remove a mandatory property from a required property set to an information
	 * delivery specification
	 * 
	 * GRAPHQL: removePropPsetIds( idsId: ID!, psetId: ID!, propId: ID!):
	 * InformationDeliverySpecification
	 * 
	 * @param idsId  Id of information delivery specification
	 * @param psetId Id of property set definition
	 * @param propId Id of property definition
	 * @return Mutated information delivery specification
	 * @throws IOException
	 */
	public InformationDeliverySpecification removeProp2Pset2Ids(String idsId, String psetId, String propId)
			throws IOException {
		return informationDeliverySpecificationRepository.removeProp2Pset(idsId, psetId, propId);
	}

	/**
	 * Add a required property set to an information delivery specification
	 * 
	 * GRAPHQL: addPset2Ids(idsId: ID!, psetId: ID!, propIds: [ID]):
	 * InformationDeliverySpecification
	 * 
	 * @param idsId   Id of information delivery specification
	 * @param psetId  Id of property set definition
	 * @param propIds Ids of property definition
	 * @return Mutated information delivery specification
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public InformationDeliverySpecification addPset2Ids(String idsId, String psetId, Optional<List<String>> propIds)
			throws IOException, URISyntaxException {
		return informationDeliverySpecificationRepository.addPset2Ids(idsId, psetId, propIds);
	}

	/**
	 * Remove a required property set from an information delivery specification
	 * 
	 * GRAPHQL: removePset2Ids(idsId: ID!, psetId: ID!):
	 * InformationDeliverySpecification
	 * 
	 * @param idsId  Id of information delivery specification
	 * @param psetId Id of property set definition
	 * @return Mutated information delivery specification
	 * @throws IOException
	 */
	public InformationDeliverySpecification removePset2Ids(String idsId, String psetId) throws IOException {
		return informationDeliverySpecificationRepository.removePset2Ids(idsId, psetId);
	}

}
