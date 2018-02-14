package io.curity.identityserver.plugin.authentication;

import io.curity.identityserver.plugin.config.MobileConnectAuthenticatorPluginConfig;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.http.HttpRequest;
import se.curity.identityserver.sdk.http.HttpResponse;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.WebServiceClient;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CallbackRequestHandler implements AuthenticatorRequestHandler<CallbackRequestModel>
{
    private final static Logger _logger = LoggerFactory.getLogger(CallbackRequestHandler.class);

    private final String TOKEN_ENDPOINT;

    private final ExceptionFactory _exceptionFactory;
    private final MobileConnectAuthenticatorPluginConfig _config;
    private final Json _json;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;
    private final WebServiceClientFactory _webServiceClientFactory;

    public CallbackRequestHandler(MobileConnectAuthenticatorPluginConfig config)
    {
        _exceptionFactory = config.getExceptionFactory();
        _config = config;
        _json = config.getJson();
        _webServiceClientFactory = config.getWebServiceClientFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
        TOKEN_ENDPOINT = _config.getSessionManager().get("tokenEndpoint").getValue().toString();

    }

    @Override
    public CallbackRequestModel preProcess(Request request, Response response)
    {
        if (request.isGetRequest())
        {
            return new CallbackRequestModel(request);
        }
        else
        {
            throw _exceptionFactory.methodNotAllowed();
        }
    }

    @Override
    public Optional<AuthenticationResult> post(CallbackRequestModel requestModel, Response response)
    {
        throw _exceptionFactory.methodNotAllowed();
    }

    @Override
    public Optional<AuthenticationResult> get(CallbackRequestModel requestModel, Response response)
    {
        validateState(requestModel.getState());
        handleError(requestModel);

        Map<String, Object> tokenResponseData = redeemCodeForTokens(requestModel);

        //parse claims without need of key
        try
        {
            Map claimsMap = new JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build()
                    .processToClaims(tokenResponseData.get("id_token").toString()).getClaimsMap();

            validateNonce(claimsMap.get("nonce").toString());

        } catch (InvalidJwtException e)
        {
            throw new IllegalStateException("Error while parsing id_token");
        }

        List<Attribute> contextAttributers = new ArrayList<>();
        contextAttributers.add(Attribute.of("access_token", tokenResponseData.get("access_token").toString()));
        if (tokenResponseData.get("refresh_token") != null)
        {
            contextAttributers.add(Attribute.of("refresh_token", tokenResponseData.get("refresh_token").toString()));
        }

        Map<String, Object> userInfo = getUserInfo(tokenResponseData.get("access_token").toString());

        AuthenticationAttributes attributes = AuthenticationAttributes.of(
                SubjectAttributes.of(userInfo.get("phone_number").toString().replace("+", ""), Attributes.fromMap(userInfo)),
                ContextAttributes.of(Attributes.of(contextAttributers)));
        AuthenticationResult authenticationResult = new AuthenticationResult(attributes);
        return Optional.ofNullable(authenticationResult);
    }

    private Map<String, Object> redeemCodeForTokens(CallbackRequestModel requestModel)
    {
        URI tokenEndpointUri;
        try
        {
            tokenEndpointUri = new URI(TOKEN_ENDPOINT);
        } catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Invalid token endpoint");
        }

        String clientId = _config.getSessionManager().get("client_id").getValue().toString();
        String clientSecret = _config.getSessionManager().get("client_secret").getValue().toString();
        HttpResponse tokenResponse = getWebServiceClient(tokenEndpointUri)
                .withPath(tokenEndpointUri.getPath())
                .request()
                .contentType("application/x-www-form-urlencoded")
                .body(getFormEncodedBodyFrom(createPostData(requestModel.getCode(), requestModel.getRequestUrl())))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()))
                .method("POST")
                .response();
        int statusCode = tokenResponse.statusCode();

        if (statusCode != 200)
        {
            if (_logger.isInfoEnabled())
            {
                _logger.info("Got error response from token endpoint: error = {}, {}", statusCode,
                        tokenResponse.body(HttpResponse.asString()));
            }

            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }

        return _json.fromJson(tokenResponse.body(HttpResponse.asString()));
    }

    private Map<String, Object> getUserInfo(String accessToken)
    {
        URI userInfoEndpointUri;
        try
        {
            userInfoEndpointUri = new URI(_config.getSessionManager().get("userInfoEndpoint").getValue().toString());
        } catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Invalid userinfo endpoint");
        }

        HttpResponse tokenResponse = getWebServiceClient(userInfoEndpointUri)
                .withPath(userInfoEndpointUri.getPath())
                .request()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .method("GET")
                .response();
        int statusCode = tokenResponse.statusCode();

        if (statusCode != 200)
        {
            if (_logger.isInfoEnabled())
            {
                _logger.info("Got error response from token endpoint: error = {}, {}", statusCode,
                        tokenResponse.body(HttpResponse.asString()));
            }

            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }

        return _json.fromJson(tokenResponse.body(HttpResponse.asString()));
    }

    private WebServiceClient getWebServiceClient(URI uri)
    {
        Optional<HttpClient> httpClient = _config.getHttpClient();

        if (httpClient.isPresent())
        {
            return _webServiceClientFactory.create(httpClient.get()).withHost(uri.getHost());
        }
        else
        {
            return _webServiceClientFactory.create(URI.create("https://" + uri.getHost()));
        }
    }

    private void handleError(CallbackRequestModel requestModel)
    {
        if (!Objects.isNull(requestModel.getError()))
        {
            if ("access_denied".equals(requestModel.getError()))
            {
                _logger.debug("Got an error from MobileConnect: {} - {}", requestModel.getError(), requestModel.getErrorDescription());

                throw _exceptionFactory.redirectException(
                        _authenticatorInformationProvider.getAuthenticationBaseUri().toASCIIString());
            }

            _logger.warn("Got an error from MobileConnect: {} - {}", requestModel.getError(), requestModel.getErrorDescription());

            throw _exceptionFactory.externalServiceException("Login with MobileConnect failed");
        }
    }

    private static Map<String, String> createPostData(String code, String callbackUri)
    {
        Map<String, String> data = new HashMap<>(5);

        data.put("code", code);
        data.put("grant_type", "authorization_code");
        data.put("redirect_uri", callbackUri);

        return data;
    }

    public static HttpRequest.BodyProcessor getFormEncodedBodyFrom(Map<String, String> data)
    {
        StringBuilder stringBuilder = new StringBuilder();

        data.entrySet().forEach(e -> appendParameter(stringBuilder, e));

        return HttpRequest.fromString(stringBuilder.toString());
    }

    private static void appendParameter(StringBuilder stringBuilder, Map.Entry<String, String> entry)
    {
        String key = entry.getKey();
        String value = entry.getValue();
        String encodedKey = urlEncodeString(key);
        stringBuilder.append(encodedKey);

        if (!Objects.isNull(value))
        {
            String encodedValue = urlEncodeString(value);
            stringBuilder.append("=").append(encodedValue);
        }

        stringBuilder.append("&");
    }

    private static String urlEncodeString(String unencodedString)
    {
        try
        {
            return URLEncoder.encode(unencodedString, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("This server cannot support UTF-8!", e);
        }
    }

    private void validateState(String state)
    {
        String sessionState = _config.getSessionManager().get("state").getValue().toString();
        try
        {
            sessionState = URLDecoder.decode(sessionState, "utf-8");
        } catch (UnsupportedEncodingException e)
        {
        }

        if (state.equals(sessionState))
        {
            _logger.debug("State matches session");
        }
        else
        {
            _logger.debug("State did not match session");

            throw _exceptionFactory.badRequestException(ErrorCode.INVALID_SERVER_STATE, "Bad state provided");
        }
    }

    private void validateNonce(String nonce)
    {
        @Nullable Attribute sessionAttribute = _config.getSessionManager().get("nonce");

        if (sessionAttribute != null && nonce.equals(sessionAttribute.getValueOfType(String.class)))
        {
            _logger.debug("Nonce matches session");
        }
        else
        {
            _logger.debug("Nonce did not match session");

            throw _exceptionFactory.badRequestException(ErrorCode.INVALID_SERVER_STATE, "Bad state provided");
        }
    }
}
