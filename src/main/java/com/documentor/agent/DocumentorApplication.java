package com.documentor.agent;

import com.documentor.agent.model.JavaClassDoc;
import com.documentor.agent.service.AzureOpenAiService;
import com.documentor.agent.service.DocumentationGeneratorService;
import com.documentor.agent.service.GitHubService;
import com.documentor.agent.service.JavaParserService;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main application class for the Java Documentation AI Agent.
 * This agent autonomously generates high-quality documentation for Java repositories
 * using LangChain4J and Azure OpenAI.
 */
public class DocumentorApplication {
    private static final Logger logger = LoggerFactory.getLogger(DocumentorApplication.class);
    
    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments (expects GitHub repository URL)
     */
    public static void main(String[] args) {
        try {
            // Validate command line arguments
            if (args.length == 0) {
                System.err.println("Error: GitHub repository URL is required");
                System.err.println("Usage: java -jar java-documentation-agent.jar https://github.com/username/repo.git");
                System.exit(1);
            }
            
            String repoUrl = args[0];
            logger.info("Starting documentation generation for repository: {}", repoUrl);
            
            // Extract repository name from URL
            String repoName = extractRepositoryName(repoUrl);
            
            // Load environment variables from .env file
            logger.info("Loading environment variables from .env file");
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
            
            // Initialize services
            logger.info("Initializing services");
            AzureOpenAiService azureOpenAiService = new AzureOpenAiService(dotenv);
            GitHubService gitHubService = new GitHubService(dotenv);
            JavaParserService javaParserService = new JavaParserService();
            DocumentationGeneratorService docGeneratorService = 
                new DocumentationGeneratorService(azureOpenAiService.getChatModel());
            
            // Clone the repository
            logger.info("Cloning repository: {}", repoUrl);
            Path repoPath = null;
            try {
                repoPath = gitHubService.cloneRepository(repoUrl);
                
                // Find all Java files
                logger.info("Finding Java files in repository");
                List<Path> javaFiles = javaParserService.findJavaFiles(repoPath);
                logger.info("Found {} Java files", javaFiles.size());
                
                // Parse Java files
                logger.info("Parsing Java files");
                List<JavaClassDoc> classes = new ArrayList<>();
                for (Path javaFile : javaFiles) {
                    JavaClassDoc classDoc = javaParserService.parseJavaFile(javaFile);
                    if (classDoc != null) {
                        classes.add(classDoc);
                        logger.info("Parsed class: {}", classDoc.getFullyQualifiedName());
                    }
                }
                logger.info("Successfully parsed {} classes", classes.size());
                
                // Create output directory for documentation
                Path outputPath = Path.of("docs");
                
                // Generate documentation
                logger.info("Generating documentation");
                docGeneratorService.generateProjectOverview(classes, repoName, outputPath);
                
                for (JavaClassDoc classDoc : classes) {
                    docGeneratorService.generateClassDocumentation(classDoc, outputPath);
                }
                  docGeneratorService.generateGettingStartedGuide(classes, repoName, outputPath);
                docGeneratorService.generateFaqAndTroubleshooting(classes, repoName, outputPath);
                
                Path readmePath = outputPath.resolve("README.md");
                Path absoluteReadmePath = readmePath.toAbsolutePath();
                String fileUrl = "file:///" + absoluteReadmePath.toString().replace("\\", "/");
                
                logger.info("Documentation generated successfully in: {}", outputPath.toAbsolutePath());
                System.out.println("Documentation generated successfully in: " + outputPath.toAbsolutePath());
                System.out.println("View documentation here: " + fileUrl);
                System.out.println("View README directly: " + readmePath.toUri().toString());
                
            } finally {
                // We don't clean up the repository since it's stored in a permanent location
                if (repoPath != null) {
                    logger.info("Cleaning up temporary files");
                    gitHubService.cleanupDirectory(repoPath);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error generating documentation: {}", e.getMessage(), e);
            System.err.println("Error generating documentation: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Extracts the repository name from a GitHub URL.
     * 
     * @param repoUrl GitHub repository URL
     * @return Repository name in format "username/repo"
     */
    private static String extractRepositoryName(String repoUrl) {
        // Handle various GitHub URL formats
        Pattern pattern = Pattern.compile("github\\.com[/:]([^/]+)/([^/.]+)");
        Matcher matcher = pattern.matcher(repoUrl);
        
        if (matcher.find()) {
            String username = matcher.group(1);
            String repo = matcher.group(2);
            return username + "/" + repo;
        }
        
        // Fallback: extract the last part of the URL
        String[] parts = repoUrl.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            if (lastPart.endsWith(".git")) {
                lastPart = lastPart.substring(0, lastPart.length() - 4);
            }
            return lastPart;
        }
        
        return "unknown-repository";
    }
}
