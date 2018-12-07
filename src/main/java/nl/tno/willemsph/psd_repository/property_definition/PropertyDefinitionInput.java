package nl.tno.willemsph.psd_repository.property_definition;

import java.util.List;

import nl.tno.willemsph.psd_repository.common.LanguageTaggedStringInput;
import nl.tno.willemsph.psd_repository.property_type.PropertyTypeInput;

public class PropertyDefinitionInput {

	public PropertyDefinitionInput() {
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
	// Ifdguid
	//
	private String ifdguid;

	public String getIfdguid() {
		return this.ifdguid;
	}

	public void setIfdguid(String ifdguid) {
		this.ifdguid = ifdguid;
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
	// Name Aliases
	//

	private List<LanguageTaggedStringInput> nameAliases;

	public List<LanguageTaggedStringInput> getNameAliases() {
		return this.nameAliases;
	}

	public void setNameAliases(List<LanguageTaggedStringInput> nameAliases) {
		this.nameAliases = nameAliases;
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
	// PropertyType
	//
	private PropertyTypeInput propertyType;

	public PropertyTypeInput getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(PropertyTypeInput propertyType) {
		this.propertyType = propertyType;
	}

	@Override
	public String toString() {
		return "{id: \"" + id + "\", name: \"" + name + "\", definition: \"" + definition + "\" }";
	}

}
