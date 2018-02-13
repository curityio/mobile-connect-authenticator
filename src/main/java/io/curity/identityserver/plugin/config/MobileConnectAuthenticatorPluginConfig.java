package io.curity.identityserver.plugin.config;


import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.DefaultEnum;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;

import java.util.Optional;

@SuppressWarnings("InterfaceNeverImplemented")
public interface MobileConnectAuthenticatorPluginConfig extends Configuration
{

    @Description("Client id")
    String getClientId();

    @Description("Secret key")
    String getClientSecret();

    @Description("The HTTP client with any proxy and TLS settings")
    Optional<HttpClient> getHttpClient();

    @DefaultEnum("SANDBOX")
    Environment getEnvironment();

    enum Environment
    {
        SANDBOX, PRE_PRODUCTION, PRODUCTION
    }

    SessionManager getSessionManager();

    ExceptionFactory getExceptionFactory();

    AuthenticatorInformationProvider getAuthenticatorInformationProvider();

    WebServiceClientFactory getWebServiceClientFactory();

    Json getJson();
}
