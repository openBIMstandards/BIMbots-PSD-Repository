package nl.tno.willemsph.psd_repository.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

@SuppressWarnings("serial")
public class InvalidCredentialsException extends RuntimeException implements GraphQLError {
	private Map<String, Object> extensions = new HashMap<>();

    public InvalidCredentialsException(String message, Long invalidBookId) {
        super(message);
        extensions.put("Invalid credentials", null);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }
}
