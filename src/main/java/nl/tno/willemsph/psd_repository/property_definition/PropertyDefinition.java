package nl.tno.willemsph.psd_repository.property_definition;

public class PropertyDefinition {

	public PropertyDefinition() {
	}

	public PropertyDefinition(String id) {
		this.id = id;
	}

	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
