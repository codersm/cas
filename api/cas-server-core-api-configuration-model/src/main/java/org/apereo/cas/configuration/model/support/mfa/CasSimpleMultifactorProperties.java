package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CasSimpleMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
public class CasSimpleMultifactorProperties extends BaseMultifactorProviderProperties {
    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-simple";

    private static final long serialVersionUID = -9211748853833491119L;

    /**
     * Time in seconds that CAS tokens should be considered live in CAS server.
     */
    private long timeToKillInSeconds = 30;

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();

    public CasSimpleMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
