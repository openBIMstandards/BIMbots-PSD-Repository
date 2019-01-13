package nl.tno.willemsph.psd_repository.common;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;

@Component
public class SigninPayLoadResolver implements GraphQLResolver<SigninPayLoad> {

	public User user(SigninPayLoad payload) {
		return payload.getUser();
	}

}
