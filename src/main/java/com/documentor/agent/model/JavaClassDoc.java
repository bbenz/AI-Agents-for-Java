package com.documentor.agent.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents a Java class or interface with its documentation details.
 */
@Data
@Builder
public class JavaClassDoc {
    private String name;
    private String packageName;
    private String fullyQualifiedName;
    private String description;
    private String type; // CLASS, INTERFACE, ENUM, RECORD, ANNOTATION
    private List<String> implementedInterfaces;
    private String superClass;
    private List<FieldDoc> fields;
    private List<MethodDoc> methods;
    private List<String> dependencies;
    private Map<String, String> annotations;
    private String sourceCode;
    private boolean isPublic;
    private boolean isAbstract;
    private List<String> typeParameters; // For generic classes
}
