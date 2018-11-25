package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowAction;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.rest.SwivelTuringImageGeneratorController;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SwivelConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("swivelConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SwivelConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private ObjectProvider<MultifactorAuthenticationProviderSelector> multifactorAuthenticationProviderSelector;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CookieGenerator> warnCookieGenerator;

    @Bean
    public FlowDefinitionRegistry swivelAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-swivel/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "swivelMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer swivelMultifactorWebflowConfigurer() {
        return new SwivelMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry.getIfAvailable(),
            swivelAuthenticatorFlowRegistry(), applicationContext, casProperties);
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver swivelAuthenticationWebflowEventResolver() {
        return new SwivelAuthenticationWebflowEventResolver(authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            servicesManager.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            multifactorAuthenticationProviderSelector.getIfAvailable(RankedMultifactorAuthenticationProviderSelector::new));
    }

    @Bean
    public SwivelTuringImageGeneratorController swivelTuringImageGeneratorController() {
        val swivel = this.casProperties.getAuthn().getMfa().getSwivel();
        return new SwivelTuringImageGeneratorController(swivel);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(swivelMultifactorWebflowConfigurer());
    }

    @Bean
    @RefreshScope
    public Action swivelAuthenticationWebflowAction() {
        return new SwivelAuthenticationWebflowAction(swivelAuthenticationWebflowEventResolver());
    }

    /**
     * The swivel multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.swivel", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("swivelMultifactorTrustConfiguration")
    public class SwivelMultifactorTrustConfiguration implements CasWebflowExecutionPlanConfigurer {

        @ConditionalOnMissingBean(name = "swivelMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer swivelMultifactorTrustWebflowConfigurer() {
            return new SwivelMultifactorTrustWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry.getIfAvailable(),
                casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(),
                swivelAuthenticatorFlowRegistry(), applicationContext, casProperties);
        }

        @Override
        public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
            plan.registerWebflowConfigurer(swivelMultifactorTrustWebflowConfigurer());
        }
    }
}
