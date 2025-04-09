package com.documentor.agent.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a method parameter with its documentation details.
 */
@Data
@Builder
public class ParameterDoc {
    private String name;
    private String type;
    private String description;
    private boolean isRequired;
}
