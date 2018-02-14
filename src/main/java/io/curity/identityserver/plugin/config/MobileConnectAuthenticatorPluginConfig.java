package io.curity.identityserver.plugin.config;


import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.DefaultBoolean;
import se.curity.identityserver.sdk.config.annotation.DefaultEnum;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;

import java.util.List;
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

    List<ACR_VALUES> getAuthenticationLevelOfAssurance();

    enum ACR_VALUES
    {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }

    @Description("Request a scope (profile) that enables the app to request profile information about the end user (excluding their email)")
    @DefaultBoolean(false)
    boolean isProfileAccess();

    @Description("Request a scope (email) that enables the app to request the end user's email and verified email addresses")
    @DefaultBoolean(false)
    boolean isEmailAccess();

    @Description("Request a scope (address) that enables the app to request the end user's postal address")
    @DefaultBoolean(false)
    boolean isAddressAccess();

    @Description("Request a scope (phone) that enables the app to request the end user's phone number")
    @DefaultBoolean(false)
    boolean isPhoneNumberAccess();

    @Description("Request a scope (offline_access) that enables the app to request a refresh token for offline access")
    @DefaultBoolean(false)
    boolean isOfflineAccess();

    SessionManager getSessionManager();

    ExceptionFactory getExceptionFactory();

    AuthenticatorInformationProvider getAuthenticatorInformationProvider();

    WebServiceClientFactory getWebServiceClientFactory();

    Json getJson();
}
