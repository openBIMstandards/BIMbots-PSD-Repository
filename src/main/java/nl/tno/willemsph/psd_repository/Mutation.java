package nl.tno.willemsph.psd_repository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.itextpdf.text.DocumentException;

import graphql.schema.DataFetchingEnvironment;
import nl.tno.willemsph.psd_repository.common.AuthData;
import nl.tno.willemsph.psd_repository.common.SessionTimeOutException;
import nl.tno.willemsph.psd_repository.common.SigninPayLoad;
import nl.tno.willemsph.psd_repository.common.User;
import nl.tno.willemsph.psd_repository.common.UserRepository;
import nl.tno.willemsph.psd_repository.information_delivery_specification.ExportFormat;
import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecification;
import nl.tno.willemsph.psd_repository.information_delivery_specification.InformationDeliverySpecificationRepository;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionInput;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;
import nl.tno.willemsph.psd_repository.sparql.AwsSendEmail;

@Component
public class Mutation implements GraphQLMutationResolver {
//	private final static Logger LOGGER = Logger.getLogger(Mutation.class.getName());

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
	 * Sign up a user.
	 * 
	 * @param auth User authorization data
	 * @return result successful
	 * @throws MessagingException
	 * @throws IOException
	 */
	public boolean signupUser(AuthData auth) throws MessagingException, IOException {
		AwsSendEmail awsSendEmail = AwsSendEmail.getInstance();

		Map<String, String> arguments = new HashMap<>();
		arguments.put("name", auth.getName());
		arguments.put("email", auth.getEmail());
		arguments.put("password", auth.getPassword());

		User user = userRepository.findByEmail(auth.getEmail());
		if (user == null) {
			return awsSendEmail.signupUser(arguments);
		} else {
			return awsSendEmail.resetPassword(arguments);
		}
	}

	/**
	 * Sign in a user.
	 * 
	 * @param auth User authorization data
	 * @return Signin Pay Load data
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public SigninPayLoad signinUser(AuthData auth) throws IOException {
		User user = userRepository.findByEmail(auth.getEmail());
		if (user != null && userRepository.verify(user, auth.getPassword())) {
			return userRepository.signinUser(user);
		} else {
			return new SigninPayLoad("Invalid credentials");
		}
	}

	/**
	 * Sign out a user.
	 * 
	 * @param token Session token
	 * @return result successful
	 */
	public boolean signoutUser(String token) {
		return userRepository.signoutUser(token);
	}

	/**
	 * Create a property set definition specified by this PropertySetDefinitionInput
	 * instance
	 * 
	 * GRAPHQL: createPropertySetDefinition(psdInput: PropertySetDefinitionInput!):
	 * PropertySetDefinition
	 * 
	 * @param propertySetDefinitionInput Property set definition property values
	 * @param env                        Data fetching environment
	 * @return Created Property set definition
	 * @throws IOException
	 */
	public PropertySetDefinition createPropertySetDefinition(PropertySetDefinitionInput propertySetDefinitionInput,
			DataFetchingEnvironment env) throws IOException {
		boolean activeSession = userRepository.sessionActive(env.getContext());
		if (!activeSession) {
			throw new SessionTimeOutException("Session timed out", null);
		}
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
	 * GRAPHQL: deletePropertySetDefinition(psetId: ID!): Boolean
	 * 
	 * @param psetId Id of property set definition
	 * @return result
	 * @throws IOException
	 */
	public boolean deletePropertySetDefinition(String psetId, DataFetchingEnvironment env) throws IOException {
		boolean activeSession = userRepository.sessionActive(env.getContext());
		if (!activeSession) {
			throw new SessionTimeOutException("Session timed out", null);
		}
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
	 * @param env      Data fetching environment
	 * @return Created information delivery specification
	 * @throws IOException
	 */
	public InformationDeliverySpecification createInformationDeliverySpecification(String idsId, String name,
			String ownerId, Optional<String> parentId, DataFetchingEnvironment env) throws IOException {
		boolean activeSession = userRepository.sessionActive(env.getContext());
		if (!activeSession) {
			throw new SessionTimeOutException("Session timed out", null);
		}
		return informationDeliverySpecificationRepository.createInformationDeliverySpecification(idsId, name, ownerId,
				parentId);
	}

	/**
	 * Delete an information delivery specification
	 * 
	 * GRAPHQL: deleteInformationDeliverySpecification(idsId: ID!):
	 * InformationDeliverySpecification
	 * 
	 * @param idsId Id of information delivery specification
	 * @param env   Data fetching environment
	 * @return result
	 * @throws IOException
	 */
	public boolean deleteInformationDeliverySpecification(String idsId, DataFetchingEnvironment env)
			throws IOException {
		boolean activeSession = userRepository.sessionActive(env.getContext());
		if (!activeSession) {
			throw new SessionTimeOutException("Session timed out", null);
		}
		return informationDeliverySpecificationRepository.deleteInformationDeliverySpecification(idsId);
	}

	/**
	 * Add the content of another information delivery specification to this
	 * information delivery specification.
	 * 
	 * GRAPHQL: addIds2Ids(thisIdsId: ID!, otherIdsId: ID!):
	 * InformationDeliverySpecification
	 * 
	 * @param thisIdsId  Id of the information delivery specification that will be
	 *                   mutated
	 * @param otherIdsId Id of the information delivery specification that will be
	 *                   copied
	 * @param env        Data fetching environment
	 * @return Updated information delivery specification
	 * @throws IOException
	 */
	public InformationDeliverySpecification addIds2Ids(String thisIdsId, String otherIdsId, DataFetchingEnvironment env)
			throws IOException {
		boolean activeSession = userRepository.sessionActive(env.getContext());
		if (!activeSession) {
			throw new SessionTimeOutException("Session timed out", null);
		}
		return informationDeliverySpecificationRepository.addIds2Ids(thisIdsId, otherIdsId);
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

	/**
	 * Export IDS, a link to the result is the return value
	 * 
	 * GRAPHQL exportIDS(id: ID!, format: ExportFormat!): String
	 * 
	 * @param id
	 * @param format
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 * @throws URISyntaxException
	 */
	public String exportIDS(String id, ExportFormat format) throws IOException, DocumentException, URISyntaxException {
		return this.informationDeliverySpecificationRepository.exportIDS(id, format);
	}

}
