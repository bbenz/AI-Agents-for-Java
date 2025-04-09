package com.documentor.agent.service;

import com.documentor.agent.model.FieldDoc;
import com.documentor.agent.model.JavaClassDoc;
import com.documentor.agent.model.MethodDoc;
import com.documentor.agent.model.ParameterDoc;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.JavadocBlockTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service to parse Java source files and extract relevant documentation data.
 */
public class JavaParserService {
    private static final Logger logger = LoggerFactory.getLogger(JavaParserService.class);

    /**
     * Find all Java files in the given repository path.
     *
     * @param repoPath Path to the repository
     * @return List of Java file paths
     * @throws IOException If there is an error accessing the files
     */
    public List<Path> findJavaFiles(Path repoPath) throws IOException {
        logger.info("Finding Java files in repository: {}", repoPath);
        
        try (Stream<Path> walk = Files.walk(repoPath)) {
            return walk
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());
        }
    }

    /**
     * Parse a Java file and extract class documentation.
     *
     * @param javaFile Java file to parse
     * @return JavaClassDoc containing parsed class information or null if parsing fails
     */
    public JavaClassDoc parseJavaFile(Path javaFile) {
        logger.info("Parsing Java file: {}", javaFile);
        
        try {
            JavaParser parser = new JavaParser();
            ParseResult<CompilationUnit> result = parser.parse(javaFile);
            
            if (!result.isSuccessful()) {
                logger.warn("Failed to parse {}: {}", javaFile, result.getProblems());
                return null;
            }
            
            CompilationUnit cu = result.getResult().orElse(null);
            if (cu == null) {
                return null;
            }
            
            // Extract the primary type declaration from the file
            Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
            if (primaryType.isEmpty() || !(primaryType.get() instanceof ClassOrInterfaceDeclaration)) {
                return null;
            }
            
            ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) primaryType.get();
            
            // Build the JavaClassDoc
            JavaClassDoc.JavaClassDocBuilder builder = JavaClassDoc.builder()
                .name(classDecl.getNameAsString())
                .packageName(cu.getPackageDeclaration().isPresent() ?
                             cu.getPackageDeclaration().get().getNameAsString() : "")
                .fullyQualifiedName(
                    (cu.getPackageDeclaration().isPresent() ?
                    cu.getPackageDeclaration().get().getNameAsString() + "." : "") + 
                    classDecl.getNameAsString()
                )
                .type(classDecl.isInterface() ? "INTERFACE" : "CLASS")
                .isPublic(classDecl.isPublic())
                .isAbstract(classDecl.isAbstract())
                .sourceCode(classDecl.toString());
            
            // Extract class description from Javadoc
            classDecl.getJavadoc().ifPresent(javadoc -> 
                builder.description(javadoc.getDescription().toText())
            );
            
            // Extract implemented interfaces
            if (!classDecl.getImplementedTypes().isEmpty()) {
                List<String> interfaces = classDecl.getImplementedTypes().stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .collect(Collectors.toList());
                builder.implementedInterfaces(interfaces);
            }
            
            // Extract super class
            if (!classDecl.getExtendedTypes().isEmpty()) {
                builder.superClass(classDecl.getExtendedTypes(0).getNameAsString());
            }
            
            // Extract type parameters
            if (!classDecl.getTypeParameters().isEmpty()) {
                List<String> typeParams = classDecl.getTypeParameters().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
                builder.typeParameters(typeParams);
            }
            
            // Extract fields
            List<FieldDoc> fields = new ArrayList<>();
            for (FieldDeclaration field : classDecl.getFields()) {
                for (VariableDeclarator var : field.getVariables()) {
                    FieldDoc.FieldDocBuilder fieldBuilder = FieldDoc.builder()
                        .name(var.getNameAsString())
                        .type(var.getTypeAsString())
                        .isPublic(field.isPublic())
                        .isStatic(field.isStatic())
                        .isFinal(field.isFinal());
                    
                    // Extract field javadoc
                    field.getJavadoc().ifPresent(javadoc ->
                        fieldBuilder.description(javadoc.getDescription().toText())
                    );
                    
                    // Extract field initial value if present
                    var.getInitializer().ifPresent(init ->
                        fieldBuilder.initialValue(init.toString())
                    );
                    
                    // Extract annotations
                    if (!field.getAnnotations().isEmpty()) {
                        Map<String, String> annotations = extractAnnotations(field.getAnnotations());
                        fieldBuilder.annotations(annotations);
                    }
                    
                    fields.add(fieldBuilder.build());
                }
            }
            builder.fields(fields);
            
            // Extract methods
            List<MethodDoc> methods = new ArrayList<>();
            for (MethodDeclaration method : classDecl.getMethods()) {
                MethodDoc.MethodDocBuilder methodBuilder = MethodDoc.builder()
                    .name(method.getNameAsString())
                    .returnType(method.getTypeAsString())
                    .signature(method.getDeclarationAsString())
                    .isPublic(method.isPublic())
                    .isStatic(method.isStatic())
                    .isAbstract(method.isAbstract())
                    .sourceCode(method.toString());
                  // Extract method Javadoc
                if (method.getJavadoc().isPresent()) {
                    com.github.javaparser.javadoc.Javadoc javadoc = method.getJavadoc().get();
                    methodBuilder.description(javadoc.getDescription().toText());
                    
                    // Extract return description
                    List<JavadocBlockTag> returnTags = javadoc.getBlockTags().stream()
                        .filter(tag -> tag.getType() == JavadocBlockTag.Type.RETURN)
                        .collect(Collectors.toList());
                    
                    if (!returnTags.isEmpty()) {
                        methodBuilder.returnDescription(returnTags.get(0).getContent().toText());
                    }
                      // Extract exception information
                    List<JavadocBlockTag> exceptionTags = javadoc.getBlockTags().stream()
                        .filter(tag -> tag.getType() == JavadocBlockTag.Type.THROWS ||
                                      tag.getType() == JavadocBlockTag.Type.EXCEPTION)
                        .collect(Collectors.toList());
                    
                    if (!exceptionTags.isEmpty()) {
                        List<String> exceptions = new ArrayList<>();
                        Map<String, String> exceptionDescriptions = new HashMap<>();
                        
                        for (JavadocBlockTag tag : exceptionTags) {
                            String exceptionName = tag.getName().isPresent() ? tag.getName().get() : "";
                            exceptions.add(exceptionName);
                            exceptionDescriptions.put(exceptionName, tag.getContent().toText());
                        }
                        
                        methodBuilder.exceptions(exceptions);
                        methodBuilder.exceptionDescriptions(exceptionDescriptions);
                    }
                }
                
                // Extract parameters
                List<ParameterDoc> parameters = new ArrayList<>();
                for (Parameter param : method.getParameters()) {
                    ParameterDoc.ParameterDocBuilder paramBuilder = ParameterDoc.builder()
                        .name(param.getNameAsString())
                        .type(param.getTypeAsString())
                        .isRequired(!param.isVarArgs());
                      // If javadoc present, try to find parameter description
                    if (method.getJavadoc().isPresent()) {
                        List<JavadocBlockTag> paramTags = method.getJavadoc().get().getBlockTags().stream()
                            .filter(tag -> tag.getType() == JavadocBlockTag.Type.PARAM &&
                                          tag.getName().isPresent() &&
                                          tag.getName().get().equals(param.getNameAsString()))
                            .collect(Collectors.toList());
                        
                        if (!paramTags.isEmpty()) {
                            paramBuilder.description(paramTags.get(0).getContent().toText());
                        }
                    }
                    
                    parameters.add(paramBuilder.build());
                }
                methodBuilder.parameters(parameters);
                
                // Extract method annotations
                if (!method.getAnnotations().isEmpty()) {
                    Map<String, String> annotations = extractAnnotations(method.getAnnotations());
                    methodBuilder.annotations(annotations);
                }
                
                // Extract method type parameters
                if (!method.getTypeParameters().isEmpty()) {
                    List<String> typeParams = method.getTypeParameters().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                    methodBuilder.typeParameters(typeParams);
                }
                
                methods.add(methodBuilder.build());
            }
            builder.methods(methods);
            
            // Extract class annotations
            if (!classDecl.getAnnotations().isEmpty()) {
                Map<String, String> annotations = extractAnnotations(classDecl.getAnnotations());
                builder.annotations(annotations);
            }
            
            return builder.build();
        } catch (Exception e) {
            logger.error("Error parsing Java file {}: {}", javaFile, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract annotation names and their values.
     *
     * @param annotations List of annotations to process
     * @return Map of annotation names to their value expressions
     */
    private Map<String, String> extractAnnotations(NodeList<AnnotationExpr> annotations) {
        Map<String, String> result = new HashMap<>();
        
        for (AnnotationExpr annotation : annotations) {
            String name = annotation.getNameAsString();
            String value = "";
            
            if (annotation.isSingleMemberAnnotationExpr()) {
                value = annotation.asSingleMemberAnnotationExpr().getMemberValue().toString();
            } else if (annotation.isNormalAnnotationExpr()) {
                List<MemberValuePair> pairs = annotation.asNormalAnnotationExpr().getPairs();
                if (!pairs.isEmpty()) {
                    value = pairs.stream()
                        .map(pair -> pair.getNameAsString() + "=" + pair.getValue().toString())
                        .collect(Collectors.joining(", "));
                }
            }
            
            result.put(name, value);
        }
        
        return result;
    }
}
