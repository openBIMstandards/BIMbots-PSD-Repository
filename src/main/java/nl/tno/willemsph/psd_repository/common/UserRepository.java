package nl.tno.willemsph.psd_repository.common;

import java.io.IOException;
import java.util.UUID;

import org.apache.jena.query.ParameterizedSparqlString;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

@Component
public class UserRepository {

	public User findByEmail(String email) throws IOException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString(EmbeddedServer.getPrefixMapping());
		queryStr.setNsPrefix("usr", EmbeddedServer.USERS + '#');
		queryStr.setIri("email", email);
		queryStr.append("SELECT ?user ?name ?salt ?password ");
		queryStr.append("WHERE {");
		queryStr.append("  ?user rdf:type usr:User ; usr:name ?name ; usr:email ?email ; usr:salt ?salt ; usr:password ?password . ");
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

}
