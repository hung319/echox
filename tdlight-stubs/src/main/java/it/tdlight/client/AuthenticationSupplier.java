package it.tdlight.client;

import it.tdlight.jni.TdApi;

@FunctionalInterface
public interface AuthenticationSupplier {
    TdApi.PhoneNumberAuthenticationSettings supply(long clientId);
}
