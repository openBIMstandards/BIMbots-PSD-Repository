package nl.tno.willemsph.psd_repository.information_delivery_specification;

public class InformationDeliverySpecification {

	public InformationDeliverySpecification() {
	}

	public InformationDeliverySpecification(String id, String name) {
		this.id = id;
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
}
