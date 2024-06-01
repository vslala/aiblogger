package com.aiblogger.workflows.articlegenerator;

public record JobVariables(
        String promptKey,
        String promptFilePathKey,
        String llmModelIdKey,
        String blogPostContentKey,
        String transformedBlocksContentKey) {
}
