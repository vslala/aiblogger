package com.aiblogger.workflows.llms;

import org.json.JSONObject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;
import software.amazon.awssdk.services.bedrockruntime.model.ResponseStream;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ClassicChatbot {
    private static Scanner scan;

    private final BedrockRuntimeAsyncClient bedrockClient;
    private final String modelId;

    public ClassicChatbot(String modelId) {
        this.bedrockClient = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.modelId = modelId;
        ClassicChatbot.scan = new Scanner(System.in);
    }

    public ClassicChatbot(String modelId, InputStream inputStream) {
        this.bedrockClient = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.modelId = modelId;
        ClassicChatbot.scan = new Scanner(inputStream);
    }

    public String input(String prompt) {
        var payload = new JSONObject()
                .put("anthropic_version", "bedrock-2023-05-31")
                .put("max_tokens", 32000)
                .append("messages", new JSONObject()
                        .put("role", "user")
                        .append("content", new JSONObject()
                                .put("type", "text")
                                .put("text", prompt)
                        ));

        var request = InvokeModelWithResponseStreamRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .body(SdkBytes.fromUtf8String(payload.toString()))
                .build();

        JSONObject structuredResponse = new JSONObject();
        AtomicReference<String> response = new AtomicReference<>("");
        bedrockClient.invokeModelWithResponseStream(request, responseStreamHandler(structuredResponse, response)).join();

        System.out.println("\n==========================================================");
        return response.get();
    }

    private InvokeModelWithResponseStreamResponseHandler responseStreamHandler(JSONObject jsonObject, AtomicReference<String> completeMessage) {
        Consumer<ResponseStream> responseStreamHandler = new Consumer<ResponseStream>() {
            @Override
            public void accept(ResponseStream responseStream) {
                var streamHandler = InvokeModelWithResponseStreamResponseHandler.Visitor.builder().onChunk(c -> {
                    var chunk = new JSONObject(c.bytes().asUtf8String());

                    if ("content_block_delta".equals(chunk.get("type"))) {
                        var text = chunk.optJSONObject("delta").optString("text");
                        if (text.equals("\n")) {
                            System.out.println();
                        } else {
                            System.out.print(text);
                        }
                        completeMessage.getAndUpdate(current -> current + text);
                    }
                }).build();

                responseStream.accept(streamHandler);
            }
        };

        return InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(responseStreamHandler))
                .onComplete(() ->
                        // Add the complete message to the response object
                        jsonObject.append("content", new JSONObject()
                                .put("type", "text")
                                .put("text", completeMessage.get())))
                .build();
    }
}

