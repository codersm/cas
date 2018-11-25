package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link BasePac4jOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
public abstract class BasePac4jOidcClientProperties extends Pac4jIdentifiableClientProperties {

    private static final long serialVersionUID = 3359382317533639638L;

    /**
     * The discovery endpoint to locate the provide metadata.
     */
    @RequiredProperty
    private String discoveryUri;

    /**
     * Logout url used for this provider.
     */
    private String logoutUrl;

    /**
     * Whether an initial nonce should be to used
     * initially for replay attack mitigation.
     */
    private boolean useNonce;

    /**
     * Requested scope(s).
     */
    private String scope;

    /**
     * The JWS algorithm to use forcefully when validating ID tokens.
     * If none is defined, the first algorithm from metadata will be used.
     */
    private String preferredJwsAlgorithm;

    /**
     * Clock skew in order to account for drift, when validating id tokens.
     */
    private int maxClockSkew;

    /**
     * Custom parameters to send along in authZ requests, etc.
     */
    private Map<String, String> customParams = new HashMap<>();
}
