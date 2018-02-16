package io.curity.identityserver.plugin.authentication;

import io.curity.identityserver.plugin.config.MobileConnectAuthenticatorPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.http.HttpResponse;
import se.curity.identityserver.sdk.http.HttpStatus;
import se.curity.identityserver.sdk.http.RedirectStatusCode;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.WebServiceClient;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Produces;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;
import se.curity.identityserver.sdk.web.alerts.ErrorMessage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static io.curity.identityserver.plugin.authentication.CallbackRequestHandler.getFormEncodedBodyFrom;
import static io.curity.identityserver.plugin.authentication.RequestModel.MobileModel.MOBILE_NUMBER_PARAM;
import static io.curity.identityserver.plugin.config.MobileConnectAuthenticatorPluginConfig.ACR_VALUES;
import static io.curity.identityserver.plugin.descriptor.MobileConnectAuthenticatorPluginDescriptor.CALLBACK;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static se.curity.identityserver.sdk.web.ResponseModel.templateResponseModel;

@Produces(Produces.ContentType.HTML)
public class MobileConnectAuthenticatorRequestHandler implements AuthenticatorRequestHandler<RequestModel>
{
    private static final Logger _logger = LoggerFactory.getLogger(MobileConnectAuthenticatorRequestHandler.class);
    private String AUTHORIZATION_ENDPOINT = null;
    private String SCOPE = null;
    private String CLIENT_ID = null;
    private String CLIENT_SECRET = null;
    private String SUBSCRIBER_ID = null;

    private final MobileConnectAuthenticatorPluginConfig _config;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;
    private final ExceptionFactory _exceptionFactory;
    private final WebServiceClientFactory _webServiceClientFactory;
    private final String discoveryUrlHost;
    private final String discoveryPath;
    private final Json _json;
    private final SessionManager _sessionManager;

    public MobileConnectAuthenticatorRequestHandler(MobileConnectAuthenticatorPluginConfig config)
    {
        _config = config;
        _exceptionFactory = config.getExceptionFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
        _webServiceClientFactory = config.getWebServiceClientFactory();
        _sessionManager = _config.getSessionManager();
        _json = _config.getJson();
        switch (_config.getEnvironment())
        {
            case SANDBOX:
                discoveryUrlHost = "discovery.sandbox.mobileconnect.io";
                discoveryPath = "/v2/discovery";
                break;
            case PRE_PRODUCTION:
                discoveryUrlHost = "discover.mobileconnect.io";
                discoveryPath = "/gsma/v2/discovery";
                break;
            case PRODUCTION:
                throw new IllegalArgumentException("It is not implemented yet.");
            default:
                discoveryPath = null;
                discoveryUrlHost = null;
        }
    }

    @Override
    public Optional<AuthenticationResult> get(RequestModel request, Response response)
    {
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
        if (request.getPostRequestModel().getMobileNumber() != null || request.getPostRequestModel().getMCCNumber() != null)
        {
            getMNOInfo(request);
            redirectToAuthorizationEndpoint();
        }
        return Optional.empty();
    }

    private void getMNOInfo(RequestModel requestModel)
    {
        Map<String, String> postData = new HashMap<>(2);
        postData.put("Redirect_URL", createRedirectUri());
        if (requestModel.getPostRequestModel().getMobileNumber() != null)
        {
            postData.put("MSISDN", requestModel.getPostRequestModel().getMobileNumber());
            _config.getUserPreferenceManager().saveUsername(requestModel.getPostRequestModel().getMobileNumber());
        }
        else
        {
            postData.put("Selected-MCC", requestModel.getPostRequestModel().getMCCNumber());
            postData.put("Selected-MNC", requestModel.getPostRequestModel().getMNCNumber());

        }
        HttpResponse userResponseData = getWebServiceClient()
                .withPath(discoveryPath)
                .request()
                .contentType("application/x-www-form-urlencoded")
                .body(getFormEncodedBodyFrom(postData))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((_config.getClientId() + ":" + _config.getClientSecret()).getBytes()))
                .header("Accept", "application/json")
                .method("POST")
                .response();

        int statusCode = userResponseData.statusCode();

        if (statusCode != 200)
        {
            if (_logger.isInfoEnabled())
            {
                _logger.info("Got error response from token endpoint: error = {}, {}", statusCode,
                        userResponseData.body(HttpResponse.asString()));
            }

            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }

        Map<String, Object> userMNOInfo = _json.fromJson(userResponseData.body(HttpResponse.asString()));
        Map<String, Object> response = (Map<String, Object>) userMNOInfo.get("response");
        SUBSCRIBER_ID = userMNOInfo.get("subscriber_id").toString();
        CLIENT_ID = response.get("client_id").toString();
        CLIENT_SECRET = response.get("client_secret").toString();
        _sessionManager.put(Attribute.of("client_id", CLIENT_ID));
        _sessionManager.put(Attribute.of("client_secret", CLIENT_SECRET));


        ArrayList<Map<String, Object>> authorizationData = (ArrayList<Map<String, Object>>) (((Map<String, Object>) ((Map<String, Object>) response.get("apis")).get("operatorid")).get("link"));
        authorizationData.forEach(item ->
        {
            if (item.get("rel").toString().equals("authorization"))
            {
                AUTHORIZATION_ENDPOINT = item.get("href").toString();
            }
            else if (item.get("rel").toString().equals("token"))
            {
                _sessionManager.put(Attribute.of("tokenEndpoint", item.get("href").toString()));
            }
            else if (item.get("rel").toString().equals("userinfo"))
            {
                _sessionManager.put(Attribute.of("userInfoEndpoint", item.get("href").toString()));
            }
            else if (item.get("rel").toString().equals("scope"))
            {
                SCOPE = item.get("href").toString();
                _sessionManager.put(Attribute.of("scope", SCOPE));
            }

        });
    }

    private WebServiceClient getWebServiceClient()
    {
        Optional<HttpClient> httpClient = _config.getHttpClient();

        if (httpClient.isPresent())
        {
            return _webServiceClientFactory.create(httpClient.get()).withHost(discoveryUrlHost);
        }
        else
        {
            return _webServiceClientFactory.create(URI.create("https://" + discoveryUrlHost));
        }
    }

    @Override
    public RequestModel preProcess(Request request, Response response)
    {
        if (request.isGetRequest())
        {
            // GET request
            response.setResponseModel(templateResponseModel(singletonMap(MOBILE_NUMBER_PARAM, _config.getUserPreferenceManager().getUsername()), "authenticate/get"),
                    Response.ResponseModelScope.NOT_FAILURE);
        }

        // on request validation failure, we should use the same template as for NOT_FAILURE
        response.setResponseModel(templateResponseModel(emptyMap(),
                "authenticate/get"), HttpStatus.BAD_REQUEST);

        return new RequestModel(request);
    }

    @Override
    public void onRequestModelValidationFailure(Request request, Response response, Set<ErrorMessage> errors)
    {
        response.addErrorMessages(errors);
    }

    private void redirectToAuthorizationEndpoint()
    {
        String state = getRandomString();
        String nonce = getRandomString();
        Map<String, Collection<String>> queryStringArguments = new LinkedHashMap<>(5);
        Set<String> acrValues = new HashSet<>();
        Set<String> scopes = new HashSet<>();

        _sessionManager.put(Attribute.of("state", state));
        _sessionManager.put(Attribute.of("nonce", nonce));


        queryStringArguments.put("client_id", Collections.singleton(CLIENT_ID));
        queryStringArguments.put("redirect_uri", Collections.singleton(createRedirectUri()));
        queryStringArguments.put("state", Collections.singleton(state));
        queryStringArguments.put("response_type", Collections.singleton("code"));
        queryStringArguments.put("nonce", Collections.singleton(nonce));
        queryStringArguments.put("login_hint", Collections.singleton("ENCR_MSISDN:" + SUBSCRIBER_ID));

        _config.getAuthenticationLevelOfAssurance().forEach(item ->
        {
            if (item == ACR_VALUES.LOW)
            {
                acrValues.add("1");
            }
            else if (item == ACR_VALUES.MEDIUM)
            {
                acrValues.add("2");
            }
            else if (item == ACR_VALUES.HIGH)
            {
                acrValues.add("3");
            }
            else if (item == ACR_VALUES.VERY_HIGH)
            {
                acrValues.add("4");
            }
        });

        queryStringArguments.put("acr_values", Collections.singleton(String.join(" ", acrValues)));

        handleScopes(scopes);
        queryStringArguments.put("scope", Collections.singleton(String.join(" ", scopes)));


        _logger.debug("Redirecting to {} with query string arguments {}", AUTHORIZATION_ENDPOINT,
                queryStringArguments);

        throw _exceptionFactory.redirectException(AUTHORIZATION_ENDPOINT,
                RedirectStatusCode.MOVED_TEMPORARILY, queryStringArguments, false);
    }

    private void handleScopes(Set<String> scopes)
    {
        scopes.add("openid");
        if (_config.isProfileAccess())
        {
            scopes.add("profile");
        }
        if (_config.isEmailAccess())
        {
            scopes.add("email");
        }
        if (_config.isAddressAccess())
        {
            scopes.add("address");
        }
        if (_config.isPhoneNumberAccess())
        {
            scopes.add("phone");
        }
        if (_config.isOfflineAccess())
        {
            scopes.add("offline_access");
        }

    }

    private String getRandomString()
    {
        byte[] buf = new byte[16];

        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = (byte) ThreadLocalRandom.current().nextInt(0x20, 0x7e);
        }

        String result = new String(Base64.getUrlEncoder().encode(buf));
        return result.replace("=", "");
    }

}
