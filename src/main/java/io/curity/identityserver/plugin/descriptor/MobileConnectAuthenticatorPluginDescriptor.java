package io.curity.identityserver.plugin.descriptor;

import io.curity.identityserver.plugin.authentication.CallbackRequestHandler;
import io.curity.identityserver.plugin.authentication.MobileConnectAuthenticatorRequestHandler;
import io.curity.identityserver.plugin.config.MobileConnectAuthenticatorPluginConfig;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.plugin.descriptor.AuthenticatorPluginDescriptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MobileConnectAuthenticatorPluginDescriptor implements AuthenticatorPluginDescriptor<MobileConnectAuthenticatorPluginConfig> {

    public final static String CALLBACK = "callback";

    @Override
    public String getPluginImplementationType() {
        return "identityserver.plugins.authenticators.mobile-connect";
    }

    @Override
    public Class<? extends MobileConnectAuthenticatorPluginConfig> getConfigurationType() {
        return MobileConnectAuthenticatorPluginConfig.class;
    }

    @Override
    public Map<String, Class<? extends AuthenticatorRequestHandler<?>>> getAuthenticationRequestHandlerTypes() {
        Map<String, Class<? extends AuthenticatorRequestHandler<?>>> handlers = new LinkedHashMap<>(2);
        handlers.put("index", MobileConnectAuthenticatorRequestHandler.class);
        handlers.put(CALLBACK, CallbackRequestHandler.class);

        return Collections.unmodifiableMap(handlers);
    }
}
