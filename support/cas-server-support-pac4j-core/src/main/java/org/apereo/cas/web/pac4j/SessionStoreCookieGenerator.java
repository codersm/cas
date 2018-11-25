package org.apereo.cas.web.pac4j;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;

/**
 * This is {@link SessionStoreCookieGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SessionStoreCookieGenerator extends CookieRetrievingCookieGenerator {
    private static final long serialVersionUID = -1487195534967980725L;

    public SessionStoreCookieGenerator(final CookieValueManager valueManager,
                                       final String name, final String path, final int maxAge,
                                       final boolean secure, final String domain, final boolean httpOnly) {
        super(name, path, maxAge, secure, domain, httpOnly, valueManager);
    }

    public SessionStoreCookieGenerator(final DefaultCasCookieValueManager valueManager, final CookieProperties c) {
        this(valueManager, c.getName(), c.getPath(), c.getMaxAge(), c.isSecure(), c.getDomain(), c.isHttpOnly());
    }
}
