package com.yifeng.utils;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

public class SecurePasswordStorage {
    private static final String SERVICE_NAME = "NacosPlugin";

    public static void savePassword(String key, String password) {
        CredentialAttributes attributes = new CredentialAttributes(SERVICE_NAME + "." + key);
        PasswordSafe.getInstance().set(attributes, new Credentials(key, password));
    }

    public static String loadPassword(String key) {
        CredentialAttributes attributes = new CredentialAttributes(SERVICE_NAME + "." + key);
        Credentials credentials = PasswordSafe.getInstance().get(attributes);
        return credentials != null ? credentials.getPasswordAsString() : null;
    }
}
