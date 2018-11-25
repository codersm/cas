package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.LegacyValidateController;
import org.apereo.cas.web.ProxyController;
import org.apereo.cas.web.ProxyValidateController;
import org.apereo.cas.web.ServiceValidateController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.v3.V3ProxyValidateController;
import org.apereo.cas.web.v3.V3ServiceValidateController;
import org.apereo.cas.web.view.Cas10ResponseView;
import org.apereo.cas.web.view.Cas20ResponseView;
import org.apereo.cas.web.view.Cas30ResponseView;
import org.apereo.cas.web.view.attributes.AttributeValuesPerLineProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.InlinedCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;
import org.apereo.cas.web.view.json.Cas30JsonResponseView;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casValidationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasValidationConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private ObjectProvider<ProtocolAttributeEncoder> protocolAttributeEncoder;

    @Autowired
    @Qualifier("cas3SuccessView")
    private ObjectProvider<View> cas3SuccessView;

    @Autowired
    @Qualifier("authenticationAttributeReleasePolicy")
    private ObjectProvider<AuthenticationAttributeReleasePolicy> authenticationAttributeReleasePolicy;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas20ProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas20ProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas10ProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas10ProtocolValidationSpecification;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("cas2ServiceFailureView")
    private ObjectProvider<View> cas2ServiceFailureView;

    @Autowired
    @Qualifier("cas2SuccessView")
    private ObjectProvider<View> cas2SuccessView;

    @Autowired
    @Qualifier("serviceValidationAuthorizers")
    private ObjectProvider<ServiceTicketValidationAuthorizersExecutionPlan> serviceValidationAuthorizers;

    @Autowired
    @Qualifier("cas3ServiceFailureView")
    private ObjectProvider<View> cas3ServiceFailureView;

    @Autowired
    @Qualifier("cas2ProxySuccessView")
    private ObjectProvider<View> cas2ProxySuccessView;

    @Autowired
    @Qualifier("cas2ProxyFailureView")
    private ObjectProvider<View> cas2ProxyFailureView;

    @Autowired
    @Qualifier("proxy10Handler")
    private ObjectProvider<ProxyHandler> proxy10Handler;

    @Autowired
    @Qualifier("proxy20Handler")
    private ObjectProvider<ProxyHandler> proxy20Handler;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("requestedContextValidator")
    private ObjectProvider<RequestedContextValidator> requestedContextValidator;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceSuccessView")
    public View cas1ServiceSuccessView() {
        return new Cas10ResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            authenticationAttributeReleasePolicy.getIfAvailable(),
            authenticationServiceSelectionPlan.getIfAvailable(),
            cas1ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceFailureView")
    public View cas1ServiceFailureView() {
        return new Cas10ResponseView(false,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            authenticationAttributeReleasePolicy.getIfAvailable(),
            authenticationServiceSelectionPlan.getIfAvailable(),
            cas1ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas2ServiceSuccessView")
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            cas2SuccessView.getIfAvailable(),
            authenticationAttributeReleasePolicy.getIfAvailable(),
            authenticationServiceSelectionPlan.getIfAvailable(),
            new NoOpProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceJsonView")
    public View cas3ServiceJsonView() {
        return new Cas30JsonResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            authenticationAttributeReleasePolicy.getIfAvailable(),
            authenticationServiceSelectionPlan.getIfAvailable(),
            cas3ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ProtocolAttributesRenderer")
    public CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer() {
        switch (casProperties.getView().getCas3().getAttributeRendererType()) {
            case INLINE:
                return new InlinedCas30ProtocolAttributesRenderer();
            case DEFAULT:
            default:
                return new DefaultCas30ProtocolAttributesRenderer();
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas1ProtocolAttributesRenderer")
    public CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer() {
        switch (casProperties.getView().getCas1().getAttributeRendererType()) {
            case VALUES_PER_LINE:
                return new AttributeValuesPerLineProtocolAttributesRenderer();
            case DEFAULT:
            default:
                return new NoOpProtocolAttributesRenderer();
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceSuccessView")
    public View cas3ServiceSuccessView() {
        return new Cas30ResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            cas3SuccessView.getIfAvailable(),
            authenticationAttributeReleasePolicy.getIfAvailable(),
            authenticationServiceSelectionPlan.getIfAvailable(),
            cas3ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public ProxyController proxyController() {
        return new ProxyController(cas2ProxySuccessView.getIfAvailable(),
            cas2ProxyFailureView.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            webApplicationServiceFactory,
            applicationContext);
    }


    @Bean
    @ConditionalOnMissingBean(name = "v3ServiceValidateController")
    public V3ServiceValidateController v3ServiceValidateController() {
        return new V3ServiceValidateController(
            cas20WithoutProxyProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            requestedContextValidator.getIfAvailable(),
            cas3ServiceJsonView(),
            cas3ServiceSuccessView(),
            cas3ServiceFailureView.getIfAvailable(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public V3ProxyValidateController v3ProxyValidateController() {
        return new V3ProxyValidateController(
            cas20ProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            requestedContextValidator.getIfAvailable(),
            cas3ServiceJsonView(),
            cas3ServiceSuccessView(),
            cas3ServiceFailureView.getIfAvailable(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyValidateController")
    public ProxyValidateController proxyValidateController() {
        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            return new ProxyValidateController(
                cas20ProtocolValidationSpecification.getIfAvailable(),
                authenticationSystemSupport.getIfAvailable(),
                servicesManager.getIfAvailable(),
                centralAuthenticationService.getIfAvailable(),
                proxy20Handler.getIfAvailable(),
                argumentExtractor.getIfAvailable(),
                requestedContextValidator.getIfAvailable(),
                cas3ServiceJsonView(),
                cas3ServiceSuccessView(),
                cas3ServiceFailureView.getIfAvailable(),
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                serviceValidationAuthorizers.getIfAvailable(),
                casProperties.getSso().isRenewAuthnEnabled()
            );
        }

        return new ProxyValidateController(
            cas20ProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            requestedContextValidator.getIfAvailable(),
            cas3ServiceJsonView(),
            cas2ServiceSuccessView(),
            cas2ServiceFailureView.getIfAvailable(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "legacyValidateController")
    public LegacyValidateController legacyValidateController() {
        return new LegacyValidateController(
            cas10ProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy10Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            requestedContextValidator.getIfAvailable(),
            cas3ServiceJsonView(),
            cas1ServiceSuccessView(),
            cas1ServiceFailureView(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceValidateController")
    public ServiceValidateController serviceValidateController() {
        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            return new ServiceValidateController(
                cas20WithoutProxyProtocolValidationSpecification.getIfAvailable(),
                authenticationSystemSupport.getIfAvailable(),
                servicesManager.getIfAvailable(),
                centralAuthenticationService.getIfAvailable(),
                proxy20Handler.getIfAvailable(),
                argumentExtractor.getIfAvailable(),
                requestedContextValidator.getIfAvailable(),
                cas3ServiceJsonView(),
                cas3ServiceSuccessView(),
                cas3ServiceFailureView.getIfAvailable(),
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                serviceValidationAuthorizers.getIfAvailable(),
                casProperties.getSso().isRenewAuthnEnabled()
            );
        }

        return new ServiceValidateController(
            cas20WithoutProxyProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            requestedContextValidator.getIfAvailable(),
            cas3ServiceJsonView(),
            cas2ServiceSuccessView(),
            cas2ServiceFailureView.getIfAvailable(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }
}
