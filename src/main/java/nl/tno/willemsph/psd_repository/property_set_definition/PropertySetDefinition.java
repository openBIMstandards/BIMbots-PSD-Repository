package nl.tno.willemsph.psd_repository.property_set_definition;

import java.util.List;

public class PropertySetDefinition {

	public PropertySetDefinition() {
	}

	public PropertySetDefinition(String name) {
		this.name = name;
	}

	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String definition;

	String getDefinition() {
		return this.definition;
	}

	void setDefinition(String definition) {
		this.definition = definition;
	}

	List<String> applicableClasses;

	List<String> getApplicableClasses() {
		return this.applicableClasses;
	}

	void setApplicableClasses(List<String> applicableClasses) {
		this.applicableClasses = applicableClasses;
	}
}
