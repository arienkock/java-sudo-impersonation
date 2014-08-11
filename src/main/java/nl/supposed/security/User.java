package nl.supposed.security;

public class User {
	public static final User ROOT = new User();
	private String name;

	public User() {

	}

	public User(String aName) {
		name = aName;
	}

	public String toString() {
		return "User(name=" + name + ")";
	}
}
