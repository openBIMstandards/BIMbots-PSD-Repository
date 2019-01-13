package nl.tno.willemsph.psd_repository.common;

public class SigninPayLoad {
	private final String token;
	private final User user;

	public SigninPayLoad(String token, User user) {
		this.token = token;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

}
