package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowAction;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
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
 * This is {@link RadiusMultifactorConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Nagai Takayuki
 * @since 5.0.0
 */
@Configuration("radiusMfaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusMultifactorConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

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
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CookieGenerator> warnCookieGenerator;

    @Bean
    public FlowDefinitionRegistry radiusFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices.getIfAvailable());
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-radius/*-webflow.xml");
        return builder.build();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowAction")
    public Action radiusAuthenticationWebflowAction() {
        return new RadiusAuthenticationWebflowAction(radiusAuthenticationWebflowEventResolver());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowEventResolver")
    public CasWebflowEventResolver radiusAuthenticationWebflowEventResolver() {
        return new RadiusAuthenticationWebflowEventResolver(authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            servicesManager.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            multifactorAuthenticationProviderSelector.getIfAvailable(RankedMultifactorAuthenticationProviderSelector::new),
            casProperties.getAuthn().getMfa().getRadius().getAllowedAuthenticationAttempts());
    }

    @ConditionalOnMissingBean(name = "radiusMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer radiusMultifactorWebflowConfigurer() {
        return new RadiusMultifactorWebflowConfigurer(flowBuilderServices.getIfAvailable(),
            loginFlowDefinitionRegistry.getIfAvailable(),
            radiusFlowRegistry(),
            applicationContext,
            casProperties);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(radiusMultifactorWebflowConfigurer());
    }

    /**
     * The Radius multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.radius", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("radiusMultifactorTrustConfiguration")
    public class RadiusMultifactorTrustConfiguration implements CasWebflowExecutionPlanConfigurer {

        @ConditionalOnMissingBean(name = "radiusMultifactorTrustConfiguration")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer radiusMultifactorTrustConfiguration() {
            val deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
            return new RadiusMultifactorTrustWebflowConfigurer(flowBuilderServices.getIfAvailable(),
                loginFlowDefinitionRegistry.getIfAvailable(), deviceRegistrationEnabled,
                loginFlowDefinitionRegistry.getIfAvailable(), applicationContext, casProperties);
        }

        @Override
        public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
            plan.registerWebflowConfigurer(radiusMultifactorTrustConfiguration());
        }
    }
}
