package com.documentor.agent.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Service to handle GitHub repository operations.
 */
public class GitHubService {
    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    private final Dotenv dotenv;    public GitHubService(Dotenv dotenv) {
        this.dotenv = dotenv;
    }
    
    /**
     * Clones a GitHub repository to a local directory.
     *
     * @param repoUrl GitHub repository URL (e.g., "https://github.com/username/repo.git")
     * @return Path to the cloned repository
     * @throws GitAPIException If there is an error with Git operations
     * @throws IOException     If there is an IO error
     */
    public Path cloneRepository(String repoUrl) throws GitAPIException, IOException {
        // Create directory in c:\githublocal
        String repoName = extractRepositoryName(repoUrl);
        Path targetDir = Path.of("c:\\githublocal", repoName);
        Files.createDirectories(targetDir);
        logger.info("Cloning repository {} to {}", repoUrl, targetDir);

        // Check if a GitHub token is provided for private repositories
        String githubToken = dotenv.get("GITHUB_TOKEN");
          try {
            if (githubToken != null && !githubToken.isEmpty()) {
                // Use token for authentication if provided
                Git.cloneRepository()
                   .setURI(repoUrl)
                   .setDirectory(targetDir.toFile())
                   .setCredentialsProvider(
                       new UsernamePasswordCredentialsProvider(githubToken, "")
                   )
                   .call();
            } else {
                // Clone without authentication for public repositories
                Git.cloneRepository()
                   .setURI(repoUrl)
                   .setDirectory(targetDir.toFile())
                   .call();
            }
            
            logger.info("Repository cloned successfully to {}", targetDir);
            return targetDir;
        } catch (GitAPIException e) {
            logger.error("Failed to clone repository: {}", e.getMessage(), e);
            cleanupDirectory(targetDir);
            throw e;
        }
    }

    /**
     * Cleans up the temporary directory when it's no longer needed.
     *
     * @param directory Directory to clean up
     */
    public void cleanupDirectory(Path directory) {
        logger.info("Cleaning up directory: {}", directory);
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
        } catch (IOException e) {
            logger.warn("Failed to clean up directory {}: {}", directory, e.getMessage());
        }
    }
    
    /**
     * Extracts the repository name from a GitHub URL.
     * 
     * @param repoUrl GitHub repository URL
     * @return Repository name without .git extension
     */
    private String extractRepositoryName(String repoUrl) {
        // Handle various GitHub URL formats
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("github\\.com[/:]([^/]+)/([^/.]+)");
        java.util.regex.Matcher matcher = pattern.matcher(repoUrl);
        
        if (matcher.find()) {
            return matcher.group(2);
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
