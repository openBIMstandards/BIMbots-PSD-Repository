package nl.tno.willemsph.psd_repository.information_delivery_specification;

import java.net.URI;
import java.util.List;

public class RequiredPset {
	private String propertySetName;
	private List<URI> reqPropertyIds;

	public RequiredPset() {
	}
	
	public RequiredPset(String propertySetName, List<URI> reqPropertyIds) {
		this.propertySetName = propertySetName;
		this.reqPropertyIds = reqPropertyIds;
	}

	public String getPropertySetName() {
		return propertySetName;
	}

	public void setPropertySetName(String propertySetName) {
		this.propertySetName = propertySetName;
	}

	public List<URI> getReqPropertyIds() {
		return reqPropertyIds;
	}

	public void setReqPropertyIds(List<URI> reqPropertyIds) {
		this.reqPropertyIds = reqPropertyIds;
	}
	
}
