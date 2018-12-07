package nl.tno.willemsph.psd_repository.information_delivery_specification;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;

import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinition;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionRepository;

@Component
public class RequiredPsetResolver implements GraphQLResolver<RequiredPset> {

	private PropertySetDefinitionRepository propertySetDefinitionRepository;

	public RequiredPsetResolver(PropertySetDefinitionRepository propertySetDefinitionRepository) {
		this.propertySetDefinitionRepository = propertySetDefinitionRepository;
	}

	public PropertySetDefinition getPropertySetDef(RequiredPset reqPset) {
		String propertySetName = reqPset.getPropertySetName();

		return this.propertySetDefinitionRepository.getOnePropertySetDefinition(propertySetName);
	}

	public List<PropertyDefinition> getReqProps(RequiredPset reqPset) {
		List<URI> reqPropIds = reqPset.getReqPropertyIds();
		if (reqPropIds != null && reqPropIds.size() > 0) {
			List<PropertyDefinition> reqProps = new ArrayList<>();
			for (URI reqPropId : reqPropIds) {
				reqProps.add(this.propertySetDefinitionRepository.getPropertyDef(reqPropId));
			}
			return reqProps;
		}
		return null;
	}

}
