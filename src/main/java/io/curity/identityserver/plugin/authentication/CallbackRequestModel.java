package io.curity.identityserver.plugin.authentication;

import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.web.Request;

import java.util.function.Function;

class CallbackRequestModel
{
    @Nullable
    private final String _error;

    @Nullable
    private final String _errorDescription;

    private final String _url;
    private final String _code;
    private final String _state;

    CallbackRequestModel(Request request)
    {
        Function<String, ? extends RuntimeException> invalidParameter = (s) -> new RuntimeException(String.format(
                "Expected only one query string parameter named %s, but found multiple.", s));
        _code = getParameterValue("code", invalidParameter, request);
        _state = getParameterValue("state", invalidParameter, request);
        _error = getParameterValue("error", invalidParameter, request);
        _errorDescription = getParameterValue("error_description", invalidParameter, request);

        _url = request.getUrl();
    }

    private String getParameterValue(String name, Function<String, ? extends RuntimeException> invalidParameter, Request request)
    {
        if (request.isPostRequest())
        {
            return request.getFormParameterValueOrError(name, invalidParameter);
        }
        else
        {
            return request.getQueryParameterValueOrError(name, invalidParameter);
        }
    }
    public String getCode()
    {
        return _code;
    }

    public String getState()
    {
        return _state;
    }

    @Nullable
    public String getErrorDescription()
    {
        return _errorDescription;
    }

    public String getRequestUrl()
    {
        return _url;
    }

    @Nullable
    public String getError()
    {
        return _error;
    }
}
