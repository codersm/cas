package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationProviderFactoryBean;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Implementation of {@link MultifactorAuthenticationProviderFactoryBean} that provides instances of
 * {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DuoProviderFactory implements MultifactorAuthenticationProviderFactoryBean<DuoMultifactorAuthenticationProvider,
                                                                                    DuoSecurityMultifactorProperties> {
    private final HttpClient httpClient;

    @Override
    public DuoMultifactorAuthenticationProvider create(final DuoSecurityMultifactorProperties properties) {
        val provider = new DefaultDuoMultifactorAuthenticationProvider();
        provider.setRegistrationUrl(properties.getRegistrationUrl());
        provider.setDuoAuthenticationService(new BasicDuoSecurityAuthenticationService(properties, httpClient));
        provider.setFailureMode(properties.getFailureMode());
        provider.setBypassEvaluator(MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(properties.getBypass()));
        provider.setOrder(properties.getRank());
        provider.setId(properties.getId());
        return provider;
    }
}
