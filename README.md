# AI-Agents-for-Java


Working repo for Microsoft AI Agents Hack 2025 session: AI Agents for Java using Azure AI Foundry and GitHub Copilot

AI Agents for Java using Azure AI Foundry and GitHub Copilot
9 April, 2025 | 12:00 PM - 1:00 PM (UTC-07:00) Pacific Time (US & Canada)

AgentsHack 2025 Link: https://techcommunity.microsoft.com/blog/azuredevcommunityblog/microsoft-ai-agents-hack-april-8-30th-2025/4395595
Session link: https://developer.microsoft.com/en-us/reactor/events/25336/

Shortlink to this repo: https://aka.ms/AIAgentsforJava

Format:
alt##LivestreamLivestream
Topic: Using AI Products

Language: English

In this session we'll show you how to embed advanced AI Agent capabilities into your Java applications using Azure AI Foundry, including setting project goals and experimenting with models and securely deploying production-ready solutions at scale. Along the way, you'll learn how GitHub Copilot (in IntelliJ, VS Code, and Eclipse) can streamline coding and prompt creation, while best practices in model selection, fine-tuning, and agentic workflows ensure responsible and efficient development. Whether you're new to AI Agents or looking for advanced agent-building techniques, this session will equip you to deliver next-level experiences with the tooling you already know.

## Project Overview

This project contains a Java AI Agent that automatically generates professional documentation for Java codebases. The agent uses LangChain4J to integrate with Azure OpenAI services to analyze Java source code and generate comprehensive Markdown documentation following Oracle's Java documentation standards.

## Prerequisites

- Java 21 JDK
- Maven 3.x
- Azure OpenAI API access
- Git

## Configuration

Before running the application, create a `.env` file in the project root with your Azure OpenAI credentials:

```
# Azure OpenAI Configuration
AZURE_OPENAI_ENDPOINT=https://your-azure-openai-resource.openai.azure.com/
AZURE_OPENAI_API_KEY=your_api_key_here
AZURE_OPENAI_DEPLOYMENT_ID=your_deployment_id
AZURE_OPENAI_API_VERSION=2023-05-15

# GitHub Access (optional for private repositories)
GITHUB_TOKEN=your_github_token_here
```

## Building the Project

Build the project using Maven:

```bash
mvn clean package
```

This will create a runnable JAR file in the `target` directory.

## Usage

Run the application with a GitHub repository URL as an argument:

```bash
java -jar target/java-documentation-agent-1.0-SNAPSHOT.jar https://github.com/username/repo.git
```

The application will:
1. Clone the specified GitHub repository
2. Parse all Java files in the repository
3. Generate comprehensive documentation using Azure OpenAI
4. Output the documentation to a `docs` directory with the following structure:
   - `README.md` - Project overview
   - `getting-started.md` - Getting started guide
   - `api/` - Detailed API documentation for each class
   - `faq.md` - FAQ and troubleshooting guide

## Example

```bash
java -jar target/java-documentation-agent-1.0-SNAPSHOT.jar https://github.com/spring-projects/spring-petclinic.git

