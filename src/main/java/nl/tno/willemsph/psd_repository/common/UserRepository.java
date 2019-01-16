package nl.tno.willemsph.psd_repository.common;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import graphql.servlet.GraphQLContext;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class UserRepository {
	private final static Logger LOGGER = Logger.getLogger(UserRepository.class.getName());

	class SignedIn {
		private final String token;
		private final User user;
		private Date lastSeen;

		SignedIn(String token, User user, Date timestamp) {
			this.token = token;
			this.user = user;
			this.lastSeen = timestamp;
		}

		public Date getLastSeen() {
			return lastSeen;
		}

		public void setLastSeen(Date lastSeen) {
			this.lastSeen = lastSeen;
		}

		public String getToken() {
			return token;
		}

		public User getUser() {
			return user;
		}
	}

	private static Map<String, SignedIn> signedIn = new HashMap<>();

	public User findByEmail(String email) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("usr", EmbeddedServer.USERS + '#');
		queryStr.setLiteral("email", email);
		queryStr.append("SELECT ?user ?name ?salt ?password ");
		queryStr.append("WHERE {");
		queryStr.append(
				"  ?user rdf:type usr:User ; usr:name ?name ; usr:email ?email ; usr:salt ?salt ; usr:password ?password . ");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				String userId = null;
				String name = null;
				String salt = null;
				String password = null;
				JsonNode userNode = node.get("user");
				if (userNode != null) {
					userId = userNode.get("value").asText();
				}
				JsonNode nameNode = node.get("name");
				if (nameNode != null) {
					name = nameNode.get("value").asText();
				}
				JsonNode saltNode = node.get("salt");
				if (saltNode != null) {
					salt = saltNode.get("value").asText();
				}
				JsonNode passwordNode = node.get("password");
				if (passwordNode != null) {
					password = passwordNode.get("value").asText();
				}
				return new User(userId, name, email, salt, password);
			}
		}

		return null;
	}

	public User findById(String userId) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("usr", EmbeddedServer.USERS + '#');
		queryStr.setIri("user", userId);
		queryStr.append("SELECT ?name ?email ?salt ?password ");
		queryStr.append("WHERE {");
		queryStr.append(
				"  ?user rdf:type usr:User ; usr:name ?name ; usr:email ?email ; usr:salt ?salt ; usr:password ?password . ");
		queryStr.append("}");

		JsonNode responseNodes = EmbeddedServer.instance.query(queryStr);
		if (responseNodes.size() > 0) {
			for (JsonNode node : responseNodes) {
				String name = null;
				String email = null;
				String salt = null;
				String password = null;
				JsonNode nameNode = node.get("name");
				if (nameNode != null) {
					name = nameNode.get("value").asText();
				}
				JsonNode emailNode = node.get("email");
				if (emailNode != null) {
					email = emailNode.get("value").asText();
				}
				JsonNode saltNode = node.get("salt");
				if (saltNode != null) {
					salt = saltNode.get("value").asText();
				}
				JsonNode passwordNode = node.get("password");
				if (passwordNode != null) {
					password = passwordNode.get("value").asText();
				}
				return new User(userId, name, email, salt, password);
			}
		}

		return null;
	}

	public boolean verify(User user, String providedPassword) {
		return PasswordUtils.verifyUserPassword(providedPassword, user.getPassword(), user.getSalt());
	}

	public User saveUser(User user) throws IOException {
		String userId = EmbeddedServer.USERS + "#_" + UUID.randomUUID().toString();

		String salt = PasswordUtils.getSalt(30);
		String password = PasswordUtils.generateSecurePassword(user.getPassword(), salt);

		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("usr", EmbeddedServer.USERS + '#');
		queryStr.setIri("user", userId);
		queryStr.setLiteral("name", user.getName());
		queryStr.setLiteral("email", user.getEmail());
		queryStr.setLiteral("salt", salt);
		queryStr.setLiteral("password", password);
		queryStr.append("INSERT {");
		queryStr.append(
				"  ?user rdf:type usr:User ; usr:name ?name ; usr:email ?email ; usr:salt ?salt ; usr:password ?password . ");
		queryStr.append("}");
		queryStr.append("WHERE {");
		queryStr.append("}");

		EmbeddedServer.instance.update(queryStr);

		return findById(userId);
	}

	public SigninPayLoad signinUser(User user) {
		String token = UUID.randomUUID().toString();
		signedIn.put(token, new SignedIn(token, user, new Date()));
		return new SigninPayLoad(token, user);
	}

	public boolean sessionActive(GraphQLContext context) {
		String header = context.getHttpServletRequest().get().getHeader("Authorization");
		if (header == null) {
			throw new SessionTimeOutException("No valid session", null);
		}
		LOGGER.info("Authorization: " + header);

		int indexOfToken = header.indexOf("Bearer") + 7;
		String token = header.substring(indexOfToken);
		SignedIn session = signedIn.get(token);
		if (session != null) {
			Date now = new Date();
			boolean activeSession = (now.getTime() - session.getLastSeen().getTime()) < 1800000;
			if (!activeSession) {
				signedIn.remove(token);
			} else {
				session.setLastSeen(now);
			}
			return activeSession;
		}
		return false;
	}

}
