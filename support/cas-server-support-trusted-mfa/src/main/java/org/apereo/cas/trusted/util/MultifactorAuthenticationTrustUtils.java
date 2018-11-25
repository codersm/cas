package org.apereo.cas.trusted.util;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationTrustUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class MultifactorAuthenticationTrustUtils {

    /**
     * Generate key.
     *
     * @param r the r
     * @return the key for this trust record
     */
    public static String generateKey(final MultifactorAuthenticationTrustRecord r) {
        val builder = new StringBuilder(r.getPrincipal());
        return builder.append('@')
            .append(r.getRecordDate())
            .append('@')
            .append(r.getDeviceFingerprint())
            .toString();
    }

    /**
     * Generate geography.
     *
     * @return the geography
     */
    public static String generateGeography() {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val geography = clientInfo.getClientIpAddress().concat("@").concat(WebUtils.getHttpServletRequestUserAgentFromRequestContext());
        return geography;
    }

    /**
     * Track trusted multifactor authentication attribute.
     *
     * @param authn         the authn
     * @param attributeName the attribute name
     */
    public static void trackTrustedMultifactorAuthenticationAttribute(
        final Authentication authn,
        final String attributeName) {

        val newAuthn = DefaultAuthenticationBuilder.newInstance(authn)
            .addAttribute(attributeName, Boolean.TRUE)
            .build();
        LOGGER.debug("Updated authentication session to remember trusted multifactor record via [{}]", attributeName);
        authn.update(newAuthn);
    }

    /**
     * Is multifactor authentication trusted in scope boolean.
     *
     * @param requestContext the request context
     * @return the boolean
     */
    public static boolean isMultifactorAuthenticationTrustedInScope(final RequestContext requestContext) {
        return requestContext.getFlashScope().contains(
            AbstractMultifactorTrustedDeviceWebflowConfigurer.MFA_TRUSTED_AUTHN_SCOPE_ATTR);
    }

    /**
     * Sets multifactor authentication trusted in scope.
     *
     * @param requestContext the request context
     */
    public static void setMultifactorAuthenticationTrustedInScope(final RequestContext requestContext) {
        val flashScope = requestContext.getFlashScope();
        flashScope.put(AbstractMultifactorTrustedDeviceWebflowConfigurer.MFA_TRUSTED_AUTHN_SCOPE_ATTR, Boolean.TRUE);
    }
}
