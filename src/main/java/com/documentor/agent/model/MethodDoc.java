package com.documentor.agent.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents a method in a Java class with its documentation details.
 */
@Data
@Builder
public class MethodDoc {
    private String name;
    private String description;
    private String returnType;
    private String returnDescription;
    private List<ParameterDoc> parameters;
    private List<String> exceptions;
    private Map<String, String> exceptionDescriptions;
    private String signature;
    private Map<String, String> annotations;
    private boolean isPublic;
    private boolean isStatic;
    private boolean isAbstract;
    private String sourceCode;
    private List<String> typeParameters; // For generic methods
    private String codeExample;
}
