package nl.tno.willemsph.psd_repository.common;

public class User {

	private final String id;
	private final String name;
	private final String email;
	private String salt;
	private final String password;

	public User(String name, String email, String password) {
		this(null, name, email, null, password);
	}

	public User(String id, String name, String email, String salt, String password) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.salt = salt;
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getSalt() {
		return salt;
	}

	public String getPassword() {
		return password;
	}

}
