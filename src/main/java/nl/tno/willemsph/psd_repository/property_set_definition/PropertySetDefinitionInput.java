package nl.tno.willemsph.psd_repository.property_set_definition;

import java.util.List;

import org.springframework.stereotype.Component;

import nl.tno.willemsph.psd_repository.common.LanguageTaggedStringInput;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinitionInput;

@Component
public class PropertySetDefinitionInput {

	public PropertySetDefinitionInput() {
	}

	//
	// Id
	//
	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	//
	// Name
	//
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//
	// Definition
	//
	private String definition;

	public String getDefinition() {
		return this.definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	//
	// Definition Aliases
	//

	private List<LanguageTaggedStringInput> definitionAliases;

	public List<LanguageTaggedStringInput> getDefinitionAliases() {
		return this.definitionAliases;
	}

	public void setDefinitionAliases(List<LanguageTaggedStringInput> definitionAliases) {
		this.definitionAliases = definitionAliases;
	}

	//
	// IfcVersion
	//
	private String ifcVersion;

	public String getIfcVersion() {
		return ifcVersion;
	}

	public void setIfcVersion(String ifcVersion) {
		this.ifcVersion = ifcVersion;
	}

	//
	// ApplicableClasses
	//
	private List<String> applicableClasses;

	public List<String> getApplicableClasses() {
		return this.applicableClasses;
	}

	public void setApplicableClasses(List<String> applicableClasses) {
		this.applicableClasses = applicableClasses;
	}

	//
	// ApplicableTypeValue
	//
	private String applicableTypeValue;

	public String getApplicableTypeValue() {
		return this.applicableTypeValue;
	}

	public void setApplicableTypeValue(String applicableTypeValue) {
		this.applicableTypeValue = applicableTypeValue;
	}

	//
	// PropertyDefs
	//
	private List<PropertyDefinitionInput> propertyDefs;

	public List<PropertyDefinitionInput> getPropertyDefs() {
		return this.propertyDefs;
	}

	public void setPropertyDefs(List<PropertyDefinitionInput> propertyDefs) {
		this.propertyDefs = propertyDefs;
	}
	
	//
	// OwnerId
	//
	private String ownerId;
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public String toString() {
		return "{id: \"" + id + "\", name: \"" + name + "\", definition: \"" + definition + "\", ifcVersion: \""
				+ ifcVersion + "\", applicableclasses: " + applicableClasses.toString() + ", applicableTypeValue: \""
				+ applicableTypeValue + "\", propertyDefs: \"" + propertyDefs + "\" }";
	}

}
