package com.northstrat.springsecurity.messaging.access.intercept;

import org.springframework.messaging.Message;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.messaging.util.matcher.MessageMatcher;

import java.util.*;

/**
 * Adapted from org.springframework.security.messaging.access.intercept.DefaultMessageSecurityMetadataSource
 *
 * See https://github.com/spring-projects/spring-security/blob/c8f598778f3d45e0092385d20593ade6f33e138f/messaging/src/main/java/org/springframework/security/messaging/access/intercept/DefaultMessageSecurityMetadataSource.java
 * for original source
 */

public class MessageSecurityMetadataSource implements SecurityMetadataSource {
    protected static final Collection<ConfigAttribute> DENY = Collections.singletonList((ConfigAttribute)new SecurityConfig("_DENY_"));

    private final Map<MessageMatcher<?>, Collection<ConfigAttribute>> messageMap;
    private boolean rejectIfNoRule = false;

    public MessageSecurityMetadataSource(
            LinkedHashMap<MessageMatcher<?>, Collection<ConfigAttribute>> messageMap) {
        this.messageMap = messageMap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<ConfigAttribute> getAttributes(Object object)
            throws IllegalArgumentException {
        final Message message = (Message) object;
        for (Map.Entry<MessageMatcher<?>, Collection<ConfigAttribute>> entry : messageMap
                .entrySet()) {
            if (entry.getKey().matches(message)) {
                return entry.getValue();
            }
        }
        return rejectIfNoRule ? DENY : null;
    }

    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>();

        for (Collection<ConfigAttribute> entry : messageMap.values()) {
            allAttributes.addAll(entry);
        }

        return allAttributes;
    }

    public boolean supports(Class<?> clazz) {
        return Message.class.isAssignableFrom(clazz);
    }

    public void setRejectIfNoRule(boolean rejectIfNoRule) {
        this.rejectIfNoRule = rejectIfNoRule;
    }
}
