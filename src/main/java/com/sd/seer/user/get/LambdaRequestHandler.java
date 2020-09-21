package com.sd.seer.user.get;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sd.seer.model.User;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;

import java.util.HashMap;

public class LambdaRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Input : " + event + "\n");

        String email = event.getPathParameters().get("email");

        logger.log("Requesting user for : " + email + "\n");

        // Create a connection to DynamoDB
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapper m = new DynamoDBMapper(client);

        logger.log("Mapper created" + "\n");

        User userFound = m.load(User.class, email);
        if(userFound != null) userFound.setTracking(null);

        logger.log("Output : " + userFound + "\n");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(userFound != null ? HttpStatus.SC_OK : HttpStatus.SC_NOT_FOUND)
                .withHeaders(new HashMap<String, String>() {
                    {
                        put("Access-Control-Allow-Origin", "*");
                        put("Access-Control-Allow-Headers", "*");
                    }
                })
                .withBody(mapper.writeValueAsString(userFound));
    }

}
