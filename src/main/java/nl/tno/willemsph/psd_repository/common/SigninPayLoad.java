package nl.tno.willemsph.psd_repository.common;

public class SigninPayLoad {
	private final String token;
	private final User user;
	private String error;

	public SigninPayLoad(String token, User user) {
		this.token = token;
		this.user = user;
	}

	public SigninPayLoad(String error) {
		this(null, null);
		this.error = error;
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

	public String getError() {
		return error;
	}

}
