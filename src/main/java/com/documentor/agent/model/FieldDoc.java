package com.documentor.agent.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * Represents documentation for a field in a Java class.
 */
@Data
@Builder
public class FieldDoc {
    private String name;
    private String type;
    private String description;
    private boolean isPublic;
    private boolean isStatic;
    private boolean isFinal;
    private String initialValue;
    private Map<String, String> annotations;
}
