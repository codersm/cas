package org.apereo.cas.configuration.model.support.wsfed;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link WsFederationDelegatedCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-wsfederation-webflow")
@Getter
public class WsFederationDelegatedCookieProperties extends CookieProperties {
    private static final long serialVersionUID = 7392972818105536350L;

    /**
     * Crypto settings that determine how the cookie should be signed and encrypted.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public WsFederationDelegatedCookieProperties() {
        super.setName("WSFEDDELSESSION");
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
