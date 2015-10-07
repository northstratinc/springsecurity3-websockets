package com.northstrat.springsecurity.web.socket;

import java.util.Map;

public class DefaultSecurityWebSocketMessageBrokerConfigurer extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    private Map<String, String> interceptDestinationMap;

    public void setInterceptDestinationMap(Map<String, String> interceptDestinationMap) {
        this.interceptDestinationMap = interceptDestinationMap;
    }

    @Override
    protected Map<String, String> getInterceptDestinationMap() {
        return interceptDestinationMap;
    }
}
