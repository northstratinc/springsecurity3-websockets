/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.northstrat.springsecurity.web.socket;

import com.northstrat.springsecurity.messaging.access.intercept.MessageSecurityMetadataSource;
import com.northstrat.springsecurity.messaging.access.intercept.MessageSecurityMetadataSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;
import org.springframework.security.messaging.access.expression.MessageExpressionVoter;
import org.springframework.security.messaging.access.intercept.ChannelSecurityInterceptor;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.util.Assert;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapted from org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
 *
 * Please see https://github.com/spring-projects/spring-security/blob/c8f598778f3d45e0092385d20593ade6f33e138f/config/src/main/java/org/springframework/security/config/annotation/web/socket/AbstractSecurityWebSocketMessageBrokerConfigurer.java
 * for original source
 */

public abstract class AbstractSecurityWebSocketMessageBrokerConfigurer extends AbstractWebSocketMessageBrokerConfigurer {

    private SecurityExpressionHandler<Message<Object>> expressionHandler = new DefaultMessageSecurityExpressionHandler<Object>();
    private boolean rejectIfNoRule = false;

    @Override
    public final void configureClientInboundChannel(ChannelRegistration registration) {
        ChannelSecurityInterceptor inboundChannelSecurity = inboundChannelSecurity();
        registration.setInterceptors(securityContextChannelInterceptor());

        registration.setInterceptors(inboundChannelSecurity);

        customizeClientInboundChannel(registration);
    }

    /**
     * Allows subclasses to customize the configuration of the {@link ChannelRegistration}
     * .
     *
     * @param registration the {@link ChannelRegistration} to customize
     */
    protected void customizeClientInboundChannel(ChannelRegistration registration) {
    }

    protected abstract Map<String, String> getInterceptDestinationMap ();


    @Bean
    public ChannelSecurityInterceptor inboundChannelSecurity() {
        ChannelSecurityInterceptor channelSecurityInterceptor = new ChannelSecurityInterceptor(
                inboundMessageSecurityMetadataSource());
        MessageExpressionVoter<Object> voter = new MessageExpressionVoter<Object>();
        if(expressionHandler != null) {
            voter.setExpressionHandler(expressionHandler);
        }

        List<AccessDecisionVoter> voters = new ArrayList<AccessDecisionVoter>();
        voters.add(voter);

        AffirmativeBased manager = new AffirmativeBased(voters);
        channelSecurityInterceptor.setAccessDecisionManager(manager);
        return channelSecurityInterceptor;
    }

    @Bean
    public SecurityContextChannelInterceptor securityContextChannelInterceptor() {
        return new SecurityContextChannelInterceptor();
    }

    @Bean
    public MessageSecurityMetadataSource inboundMessageSecurityMetadataSource() {
        MessageSecurityMetadataSourceBuilder messageSecurityMetadataSourceBuilder = new MessageSecurityMetadataSourceBuilder()
                .withInterceptDestinationMap(getInterceptDestinationMap())
                .withRejectIfNoRule(rejectIfNoRule)
                .withSecurityExpressionHandler(expressionHandler);

        return messageSecurityMetadataSourceBuilder.buildMessageSecurityMetadataSource();
    }

    public void registerStompEndpoints (final StompEndpointRegistry stompEndpointRegistry) {}

    @Autowired(required = false)
    public void setMessageExpessionHandler(SecurityExpressionHandler<Message<Object>> expressionHandler) {
        Assert.notNull(expressionHandler, "expressionHandler cannot be null!");
        this.expressionHandler = expressionHandler;
    }

    public void setRejectIfNoRule(boolean rejectIfNoRule) {
        this.rejectIfNoRule = rejectIfNoRule;
    }
}
