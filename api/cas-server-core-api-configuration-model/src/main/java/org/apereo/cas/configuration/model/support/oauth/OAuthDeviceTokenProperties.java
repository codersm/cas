package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link OAuthDeviceTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
public class OAuthDeviceTokenProperties implements Serializable {

    private static final long serialVersionUID = -6832081675586528350L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    private String maxTimeToLiveInSeconds = "PT5M";

    /**
     * The device refresh interval.
     * The client should attempt to acquire an access token every few seconds (at a rate specified by interval)
     * by POSTing to the access token endpoint on the server.
     */
    private String refreshInterval = "PT15S";

    /**
     * Length of the generated user code.
     */
    private int userCodeLength = 8;
}
