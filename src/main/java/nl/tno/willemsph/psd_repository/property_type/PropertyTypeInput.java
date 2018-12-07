package nl.tno.willemsph.psd_repository.property_type;

import java.util.List;

public class PropertyTypeInput {

	public PropertyTypeInput() {
	}

	public PropertyTypeInput(String type, String dataType) {
		this.type = type;
		this.dataType = dataType;
	}

	private String type;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String dataType;

	public String getDataType() {
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	private List<String> enumItems;

	public List<String> getEnumItems() {
		return this.enumItems;
	}

	public void setEnumItems(List<String> enumItems) {
		this.enumItems = enumItems;
	}

}
