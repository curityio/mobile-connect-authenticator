package io.curity.identityserver.plugin.authentication;

import io.curity.identityserver.plugin.config.MobileConnectAuthenticatorPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Produces;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static io.curity.identityserver.plugin.descriptor.MobileConnectAuthenticatorPluginDescriptor.CALLBACK;
import static java.util.Collections.emptyMap;
import static se.curity.identityserver.sdk.web.ResponseModel.templateResponseModel;

@Produces(Produces.ContentType.HTML)
public class MobileConnectAuthenticatorRequestHandler implements AuthenticatorRequestHandler<RequestModel>
{
    private static final Logger _logger = LoggerFactory.getLogger(MobileConnectAuthenticatorRequestHandler.class);
    private static final String AUTHORIZATION_ENDPOINT = "";

    private final MobileConnectAuthenticatorPluginConfig _config;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;
    private final ExceptionFactory _exceptionFactory;

    public MobileConnectAuthenticatorRequestHandler(MobileConnectAuthenticatorPluginConfig config)
    {
        _config = config;
        _exceptionFactory = config.getExceptionFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
    }

    @Override
    public Optional<AuthenticationResult> get(RequestModel request, Response response)
    {
//        _logger.debug("GET request received for authentication");
//
//        String redirectUri = createRedirectUri();
//        String state = UUID.randomUUID().toString();
//        Map<String, Collection<String>> queryStringArguments = new LinkedHashMap<>(5);
//        Set<String> scopes = new LinkedHashSet<>(7);
//
//        _config.getSessionManager().put(Attribute.of("state", state));
//
//        queryStringArguments.put("client_id", Collections.singleton(_config.getClientId()));
//        queryStringArguments.put("redirect_uri", Collections.singleton(redirectUri));
//        queryStringArguments.put("state", Collections.singleton(state));
//        queryStringArguments.put("response_type", Collections.singleton("code"));
//
//        queryStringArguments.put("scope", Collections.singleton(String.join(" ", scopes)));
//
//        _logger.debug("Redirecting to {} with query string arguments {}", AUTHORIZATION_ENDPOINT,
//                queryStringArguments);
//
//        throw _exceptionFactory.redirectException(AUTHORIZATION_ENDPOINT,
//                RedirectStatusCode.MOVED_TEMPORARILY, queryStringArguments, false);
        return Optional.empty();
    }

    private String createRedirectUri()
    {
        try
        {
            URI authUri = _authenticatorInformationProvider.getFullyQualifiedAuthenticationUri();

            return new URL(authUri.toURL(), authUri.getPath() + "/" + CALLBACK).toString();
        } catch (MalformedURLException e)
        {
            throw _exceptionFactory.internalServerException(ErrorCode.INVALID_REDIRECT_URI,
                    "Could not create redirect URI");
        }
    }

    @Override
    public Optional<AuthenticationResult> post(RequestModel request, Response response)
    {
        throw _exceptionFactory.methodNotAllowed();
    }

    @Override
    public RequestModel preProcess(Request request, Response response)
    {
        if (request.isGetRequest())
        {
            // GET request
            response.setResponseModel(templateResponseModel(emptyMap(), "authenticate/get"),
                    Response.ResponseModelScope.NOT_FAILURE);
        }

        return new RequestModel(request);
    }
}
