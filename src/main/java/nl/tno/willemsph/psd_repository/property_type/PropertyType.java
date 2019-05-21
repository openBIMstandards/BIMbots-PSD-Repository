package nl.tno.willemsph.psd_repository.property_type;

import java.util.List;

public class PropertyType {

	public PropertyType() {
	}

	public PropertyType(String type, String dataType) {
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

	private String reftype;

	public String getReftype() {
		return this.reftype;
	}

	public void setReftype(String reftype) {
		this.reftype = reftype;
	}

	private String definingValue;

	public String getDefiningValue() {
		return this.definingValue;
	}

	public void setDefiningValue(String definingValue) {
		this.definingValue = definingValue;
	}

	private String definedValue;

	public String getDefinedValue() {
		return this.definedValue;
	}

	public void setDefinedValue(String definedValue) {
		this.definedValue = definedValue;
	}
}
