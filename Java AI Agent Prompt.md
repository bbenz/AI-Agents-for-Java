Please create a Java AI Agent designed to autonomously generate high-quality documentation for Java libraries or applications stored in a GitHub repository. You will use the LangChain4J framework in Java and integrate with Azure OpenAI Large Language Models (LLMs).  

The generated code should be compatible with Java 21.

The primary task of the generated code is:
Use Langchain4j and Java 21 Automatically create comprehensive, concise, and professional documentation in Markdown following the Oracle Java Documentation Standards (examples at https://docs.oracle.com/en/java/) , including:

An Overview clearly describing the project.

An organized Table of Contents linking to all sections.

Installation instructions (including prerequisites).

Getting Started Guide with examples of basic usage.

Detailed API documentation (class-level and method-level documentation), including method signatures, parameters, return values, and exceptions.

Brief code examples showcasing common scenarios or usage.

A clear and concise FAQ or Troubleshooting section (optional, if applicable).

Input you will receive:
A link to a GitHub repository containing Java source code.

What your solution must do:
Clone or access the Java source code provided by the GitHub repository URL.

Use an Azure-hosted LLM via LangChain4J to analyze and interpret the Java source files.

The LLM definition, key, endpoint and other information for connecting to the LLM is in the .env file in this directory.  

Please refer to the values in the .env file, but do not add those values to any coe that will be shared.  

Extract meaningful descriptions from code: class purposes, public methods, parameters, exceptions, return values, and example usages.

Autonomously generate structured Markdown documentation files organized clearly into sections and folders suitable for direct commit into GitHub.

Output Requirements:
Generate Markdown (.md) files following the structure below:

pgsql
Copy
Edit
docs/
├── README.md                 (Overview, Table of Contents, Installation)
├── getting-started.md        (Basic examples and how-to guide)
├── api/
│   ├── ClassName1.md         (Detailed class & method docs)
│   └── ClassName2.md
└── faq.md                    (Optional FAQ or troubleshooting guide)
The main README should have a clear Table of Contents linking to all other markdown files.

Documentation Style Guidelines (Oracle Standard in Markdown):
Headers: Use Markdown headers (#, ##, ###) to structure content logically.

Method signatures: Use code blocks (```java ... ```) clearly formatted in Java.

Parameter and return descriptions: Clearly labeled bullet points.

Concise professional tone: Clear, direct, accurate language, and precise explanations without unnecessary elaboration.

Example (Brief Illustration):

markdown
Copy
Edit
# Project Name

Brief overview of project purpose and functionality.

## Table of Contents
- [Installation](#installation)
- [Getting Started](getting-started.md)
- [API Documentation](api/ClassName1.md)
- [FAQ](faq.md)

## Installation

1. **Prerequisites**:
    - Java 21
    - Maven 3.9.9

2. **Build**:
    ```bash
    mvn clean install
    mvn: spring-boot
    ```

## API Documentation

### [ClassName1](api/ClassName1.md)

Brief purpose of ClassName1.

---

*(Separate detailed markdown files for each class)*

## [FAQ](faq.md)

Common issues and resolutions.
Tone and Language:
Use a professional, concise tone.

Avoid casual language; be precise and clear.

Maintain readability and clarity throughout.

When provided with a GitHub repository URL containing Java code, you should autonomously produce the complete documentation structure outlined above. Your output must be directly usable by developers and suitable for immediate commit into the project's repository.

