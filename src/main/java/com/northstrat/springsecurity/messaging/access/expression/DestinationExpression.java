package com.northstrat.springsecurity.messaging.access.expression;

import org.springframework.messaging.simp.SimpMessageType;

public class DestinationExpression {

    private static final String SUBSCRIBE_PREFIX = "SUBSCRIBE";
    private static final String MESSAGE_PREFIX = "MESSAGE";

    private String path;
    private SimpMessageType type;

    public static DestinationExpression parse (final String destinationPathExpr) {
        final DestinationExpression destinatinationExpression = new DestinationExpression();
        int delimPos = destinationPathExpr.indexOf(":");

        if (delimPos == -1) {
            destinatinationExpression.path = destinationPathExpr;
        } else {
            destinatinationExpression.path = destinationPathExpr.substring(delimPos + 1);
            final String messageTypeStr = destinationPathExpr.substring(0, delimPos);

            if (SUBSCRIBE_PREFIX.equals(messageTypeStr)) {
                destinatinationExpression.type = SimpMessageType.SUBSCRIBE;
            } else if (MESSAGE_PREFIX.equals(messageTypeStr)) {
                destinatinationExpression.type = SimpMessageType.MESSAGE;
            } else {
                throw new IllegalArgumentException("Unsupported message type prefix found in destination expression: " + messageTypeStr);
            }
        }

        return destinatinationExpression;
    }

    public String getPath() {
        return path;
    }

    public SimpMessageType getType() {
        return type;
    }
}
