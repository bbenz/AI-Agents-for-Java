package com.documentor.agent.service;

import com.documentor.agent.model.JavaClassDoc;
import com.documentor.agent.model.MethodDoc;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating documentation using the Azure OpenAI LLM.
 */
public class DocumentationGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentationGeneratorService.class);
    
    private final ChatLanguageModel chatModel;

    public DocumentationGeneratorService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    /**
     * Generates project overview documentation.
     * 
     * @param classes List of parsed Java classes
     * @param repositoryName Name of the GitHub repository
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated README.md file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateProjectOverview(List<JavaClassDoc> classes, String repositoryName, Path outputBasePath) throws IOException {
        logger.info("Generating project overview documentation for {}", repositoryName);
        
        // Prepare data for the prompt
        String classListSummary = classes.stream()
            .map(c -> c.getFullyQualifiedName() + " (" + c.getType() + ")" + 
                 (c.getDescription() != null ? ": " + c.getDescription() : ""))
            .collect(Collectors.joining("\n"));
        
        // Count different types of classes
        long interfaceCount = classes.stream().filter(c -> "INTERFACE".equals(c.getType())).count();
        long classCount = classes.stream().filter(c -> "CLASS".equals(c.getType())).count();
        long enumCount = classes.stream().filter(c -> "ENUM".equals(c.getType())).count();
        
        // Create prompt template for project overview
        String projectOverviewTemplate = """
            You are DocuMentor, an expert Java documentation specialist with deep knowledge of software architecture, design patterns, and Java best practices.
            
            # Task
            Generate comprehensive project overview documentation in Markdown format for a Java project.
            
            # Project Information
            Repository Name: {{repositoryName}}
            Total Classes: {{totalClasses}}
            - Regular Classes: {{classCount}}
            - Interfaces: {{interfaceCount}}
            - Enums: {{enumCount}}
            
            # Classes Summary
            {{classSummary}}
            
            # Instructions
            1. Create a professional README.md file with the following sections:
               - Project Title and Brief Description
               - Table of Contents
               - Overview (project purpose and main functionality)
               - Features
               - Installation Instructions
               - Getting Started Guide
               - Prerequisites
               - Link to API Documentation
            
            2. Make educated guesses about the project's purpose based on class names and descriptions.
            3. The tone should be professional, concise, and technically precise.
            4. Include relevant sections based on the project's nature.
            5. Do not include placeholder text like "TODO" or "Insert here".
            
            # Output Format
            Provide the complete README.md content in valid Markdown format.
            """;
        
        PromptTemplate template = PromptTemplate.from(projectOverviewTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("repositoryName", repositoryName);
        variables.put("totalClasses", classes.size());
        variables.put("classCount", classCount);
        variables.put("interfaceCount", interfaceCount);
        variables.put("enumCount", enumCount);
        variables.put("classSummary", classListSummary);
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String readmeContent = chatModel.generate(prompt.text());
        
        // Ensure output directory exists
        Files.createDirectories(outputBasePath);
        
        // Write the README.md file
        Path readmePath = outputBasePath.resolve("README.md");
        Files.writeString(readmePath, readmeContent);
        
        logger.info("Generated project overview at {}", readmePath);
        return readmePath;
    }
    
    /**
     * Generates detailed API documentation for a Java class.
     * 
     * @param classDoc The Java class to document
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated class documentation file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateClassDocumentation(JavaClassDoc classDoc, Path outputBasePath) throws IOException {
        logger.info("Generating documentation for class: {}", classDoc.getFullyQualifiedName());
        
        // Create a summary of methods
        String methodsSummary = classDoc.getMethods().stream()
            .map(m -> m.getName() + "(" + 
                m.getParameters().stream()
                    .map(p -> p.getType() + " " + p.getName())
                    .collect(Collectors.joining(", ")) + 
                "): " + m.getReturnType())
            .collect(Collectors.joining("\n"));
        
        // Class documentation template
        String classDocTemplate = """
            You are DocuMentor, an expert Java documentation specialist with deep knowledge of software architecture, design patterns, and Java best practices.
            
            # Task
            Generate detailed class documentation in Markdown format for a Java class.
            
            # Class Information
            Class Name: {{className}}
            Package: {{packageName}}
            Type: {{classType}}
            Description: {{classDescription}}
            
            # Class Details
            ```java
            {{sourceCode}}
            ```
            
            # Methods Summary
            {{methodsSummary}}
            
            # Instructions
            1. Create professional, detailed class documentation following Oracle Java Documentation Standards.
            2. Include the following sections:
               - Class Name and Package
               - Overview
               - Dependencies (classes this class depends on)
               - Public Methods summary with links to detailed method sections
               - Detailed documentation for each method
            
            3. For each method, document:
               - Method signature
               - Description
               - Parameters with descriptions
               - Return value with description
               - Exceptions with conditions
               - Usage example (create a simple, realistic example)
            
            4. Use Technical Precision Mode:
               - Prioritize precision over readability
               - Include all technical details and edge cases
               - Use formal technical language
               - Focus on correctness and completeness
            
            # Output Format
            Provide the complete class documentation in valid Markdown format.
            """;
        
        PromptTemplate template = PromptTemplate.from(classDocTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("className", classDoc.getName());
        variables.put("packageName", classDoc.getPackageName());
        variables.put("classType", classDoc.getType());
        variables.put("classDescription", classDoc.getDescription() != null ? classDoc.getDescription() : "");
        variables.put("sourceCode", classDoc.getSourceCode());
        variables.put("methodsSummary", methodsSummary);
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String classDocContent = chatModel.generate(prompt.text());
        
        // Ensure output directory exists
        Path apiDir = outputBasePath.resolve("api");
        Files.createDirectories(apiDir);
        
        // Write the class documentation file
        Path classDocPath = apiDir.resolve(classDoc.getName() + ".md");
        Files.writeString(classDocPath, classDocContent);
        
        logger.info("Generated class documentation at {}", classDocPath);
        return classDocPath;
    }
    
    /**
     * Generates a getting started guide for the project.
     * 
     * @param classes List of parsed Java classes
     * @param repositoryName Name of the GitHub repository
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated getting-started.md file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateGettingStartedGuide(List<JavaClassDoc> classes, String repositoryName, Path outputBasePath) throws IOException {
        logger.info("Generating getting started guide for {}", repositoryName);
        
        // Find public classes that might be entry points or main components
        List<String> mainClasses = classes.stream()
            .filter(JavaClassDoc::isPublic)
            .map(JavaClassDoc::getFullyQualifiedName)
            .collect(Collectors.toList());
        
        // Getting started guide template
        String gettingStartedTemplate = """
            You are DocuMentor, an expert Java documentation specialist with deep knowledge of software architecture, design patterns, and Java best practices.
            
            # Task
            Generate a comprehensive "Getting Started" guide in Markdown format for a Java project.
            
            # Project Information
            Repository Name: {{repositoryName}}
            Main Public Classes:
            {{mainClasses}}
            
            # Instructions
            1. Create a beginner-friendly getting started guide with:
               - Quick Start section with basic usage examples
               - Common use cases with code examples
               - Configuration options
               - Best practices
            
            2. Use Developer Guide Mode:
               - Balance precision with readability
               - Include practical examples and common use cases
               - Use approachable yet professional language
               - Focus on helping developers accomplish tasks
            
            3. Include code snippets that demonstrate:
               - How to initialize/configure the library
               - How to use the most important features
               - How to handle common errors
            
            # Output Format
            Provide the complete getting-started.md content in valid Markdown format.
            """;
        
        PromptTemplate template = PromptTemplate.from(gettingStartedTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("repositoryName", repositoryName);
        variables.put("mainClasses", String.join("\n", mainClasses));
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String gettingStartedContent = chatModel.generate(prompt.text());
        
        // Write the getting-started.md file
        Path gettingStartedPath = outputBasePath.resolve("getting-started.md");
        Files.writeString(gettingStartedPath, gettingStartedContent);
        
        logger.info("Generated getting started guide at {}", gettingStartedPath);
        return gettingStartedPath;
    }
    
    /**
     * Generates an FAQ and troubleshooting guide.
     * 
     * @param classes List of parsed Java classes
     * @param repositoryName Name of the GitHub repository
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated faq.md file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateFaqAndTroubleshooting(List<JavaClassDoc> classes, String repositoryName, Path outputBasePath) throws IOException {
        logger.info("Generating FAQ and troubleshooting guide for {}", repositoryName);
        
        // Collect exception information from methods to identify potential issues
        List<String> exceptionTypes = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .flatMap(m -> m.getExceptions() != null ? m.getExceptions().stream() : List.<String>of().stream())
            .distinct()
            .collect(Collectors.toList());
        
        // FAQ template
        String faqTemplate = """
            You are DocuMentor, an expert Java documentation specialist with deep knowledge of software architecture, design patterns, and Java best practices.
            
            # Task
            Generate an FAQ and troubleshooting guide in Markdown format for a Java project.
            
            # Project Information
            Repository Name: {{repositoryName}}
            Exception Types Found:
            {{exceptionTypes}}
            
            # Instructions
            1. Create a helpful FAQ and troubleshooting guide with:
               - Common questions about installation and setup
               - Usage questions and answers
               - Troubleshooting for common errors and exceptions
               - Performance tips
            
            2. Based on the exceptions found in the codebase, create troubleshooting entries that:
               - Explain what might cause each exception
               - Provide solutions to resolve the issues
               - Include code examples showing proper handling
            
            3. Use Developer Guide Mode:
               - Balance precision with readability
               - Use approachable yet professional language
               - Focus on practical solutions
            
            # Output Format
            Provide the complete faq.md content in valid Markdown format.
            """;
        
        PromptTemplate template = PromptTemplate.from(faqTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("repositoryName", repositoryName);
        variables.put("exceptionTypes", String.join("\n", exceptionTypes));
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String faqContent = chatModel.generate(prompt.text());
        
        // Write the faq.md file
        Path faqPath = outputBasePath.resolve("faq.md");
        Files.writeString(faqPath, faqContent);
        
        logger.info("Generated FAQ and troubleshooting guide at {}", faqPath);
        return faqPath;
    }
}
