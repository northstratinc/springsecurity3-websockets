package com.northstrat.springsecurity.messaging.access.intercept;

import com.northstrat.springsecurity.messaging.access.expression.DestinationExpression;
import org.springframework.expression.Expression;
import org.springframework.messaging.Message;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.messaging.access.expression.MessageExpressionConfigAttribute;
import org.springframework.security.messaging.util.matcher.MessageMatcher;
import org.springframework.security.messaging.util.matcher.SimpDestinationMessageMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageSecurityMetadataSourceBuilder {

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Map representing destination to security config attribute mappings
     *
     * key = destination (e.g. '/topic/foo'). Destination can be prefixed with specific message type if desired, using
     * colon as delimeter. (e.g. 'SUBSCRIBE:/topic/foo' would limit this rule to SUBSCRIBE messages,
     * 'MESSAGE:/queue/bar' would limit this rule to MESSAGE messages). If no prefix is included,
     * the attributes will apply to all message types
     *
     * value = config attribute expression (e.g. hasRole('ROLE_ADMIN'))
     *
     */
    private Map<String, String> interceptDestinationMap;
    private SecurityExpressionHandler<Message<Object>> handler;
    private boolean rejectIfNoRule = false;

    public MessageSecurityMetadataSourceBuilder withInterceptDestinationMap (final Map<String, String> interceptDestinationMap) {
        this.interceptDestinationMap = interceptDestinationMap;
        return this;
    }

    public MessageSecurityMetadataSourceBuilder withSecurityExpressionHandler (final SecurityExpressionHandler<Message<Object>> handler) {
        this.handler = handler;
        return this;
    }

    public MessageSecurityMetadataSourceBuilder withRejectIfNoRule (final boolean rejectIfNoRule) {
        this.rejectIfNoRule = rejectIfNoRule;
        return this;
    }

    public MessageSecurityMetadataSource buildMessageSecurityMetadataSource () {
        checkParameters();
        final LinkedHashMap<MessageMatcher<?>, Collection<ConfigAttribute>> messageMap =
                new LinkedHashMap<MessageMatcher<?>, Collection<ConfigAttribute>>();

        for (final String destinationPathExpr : interceptDestinationMap.keySet()) {
            messageMap.put(buildKey(destinationPathExpr),
                    Arrays.asList(parseConfigAttribute(interceptDestinationMap.get(destinationPathExpr))));
        }

        final MessageSecurityMetadataSource messageSecurityMetadataSource = new MessageSecurityMetadataSource(messageMap);
        messageSecurityMetadataSource.setRejectIfNoRule(rejectIfNoRule);

        return messageSecurityMetadataSource;
    }

    private SimpDestinationMessageMatcher buildKey (final String destinationPathExpr) {
        SimpDestinationMessageMatcher messageMatcher;
        final DestinationExpression destinationExpression = DestinationExpression.parse(destinationPathExpr);

        if (destinationExpression.getType() == null) {
            return new SimpDestinationMessageMatcher(destinationExpression.getPath(), pathMatcher);
        }

        switch (destinationExpression.getType()) {
            case SUBSCRIBE:
                messageMatcher = SimpDestinationMessageMatcher.createSubscribeMatcher(destinationExpression.getPath(), pathMatcher);
                break;
            case MESSAGE:
                messageMatcher = SimpDestinationMessageMatcher.createMessageMatcher(destinationExpression.getPath(), pathMatcher);
                break;
            default:
                messageMatcher = new SimpDestinationMessageMatcher(destinationExpression.getPath(), pathMatcher);
        }

        return messageMatcher;
    }

    private ConfigAttribute parseConfigAttribute (final String rawAttributeExpression) {
        Expression expression = handler.getExpressionParser().parseExpression(
                rawAttributeExpression);
        return new MessageExpressionConfigAttribute(expression);
    }

    private void checkParameters () {
        Assert.state(interceptDestinationMap != null, "interceptDestinationMap required to build MessageSecurityMetadataSource!");
        Assert.state(handler != null, "expressionHandler required to build MessageSecurityMetadataSource!");
    }
}
