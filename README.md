# springsecurity3-websockets

This plugin is a backport of a subset of the Spring Security 4.0 websocket security features to be Spring Security 3.2 compatible. 
It provides pattern/expression based security for individual websocket destinations. 

The initial connection to a websocket endpoint should still be secured via the usual Spring Security 3.2 methods, this simply
provides the ability to secure/authorize each message that is sent to the server.
  
## Usage

Until this is added to the Central Maven Repository, you will need to clone this repository and build the plugin yourself:

```
mvn clean install
```

After it is built/installed, you can add it to your dependency list:

```xml
<dependency>
    <groupId>com.northstrat</groupId>
    <artifactId>springsecurity3-websockets</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

To actually use the features of this plugin, you must declare a spring bean in your spring configuration:
  
```xml
<bean id="securityWebSocketMessageBrokerConfigurer" class="com.northstrat.springsecurity.web.socket.DefaultSecurityWebSocketMessageBrokerConfigurer">
    <property name="interceptDestinationMap">
        <map>
            <entry key="/topic/foo" value="hasRole('ROLE_ADMIN')" />
            <entry key="/topic/bar" value="hasAnyRole('ROLE_ADMIN','ROLE_USER')" />
            <entry key="SUBSCRIBE:/topic/test/**" value="hasRole('ROLE_ADMIN')" />
            <entry key="MESSAGE:/topic/test/**" value="hasRole('ROLE_USER')" />
        </map>
    </property>
</bean>
```

The above example uses the default implementation of the class `AbstractSecurityWebSocketMessageBrokerConfigurer`. If you would
like to customize the behavior of this bean, simply write your own implementation.

### Intercept Destination Map

The only required property in the default implementation above is the `interceptDestinationMap`. This is a map of message
matching expressions to [Spring Security Expressions](https://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html).

The message matching expressions are much like the expressions used to match URLs in classic Spring Security, only they
 are matching websocket destinations. Just like the Spring Security Intercept URL map, order matters. Message patterns
 higher in the list take precedence over lower ones. Also like the Spring Security Intercept URL map, wildcards (**) are
 supported.

In addition, some extra syntax has been added to the message matcher expressions to support differentiating between message 
 types. In the last two entries in the example above, note the prefixes on the message matcher patterns. Specifically,
 the second-to-last entry says that only users with `ROLE_ADMIN` may subscribe to the destinations matching `/topic/test/**`.
 Likewise, the last entry says that only users with `ROLE_USER` may send messages to the same destinations. This adds a
 bit more granularity to the security controls.
 
If no prefix is included, it is assumed that the rule should apply to all message types. Currently only `SUBSCRIBE` and 
`MESSAGE` types are supported
