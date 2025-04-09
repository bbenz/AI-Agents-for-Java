```markdown
# Class Documentation: IndexController

## Class Name and Package
**Class Name:** IndexController  
**Package:** com.microsoft.azure.samples

## Overview
The `IndexController` class is a managed bean in a JavaServer Faces (JSF) application. It is annotated with `@Named` and `@ViewScoped`, indicating that it is a named bean with a view scope. This class is responsible for handling user input and updating the view accordingly. It implements the `Serializable` interface to support serialization, which is necessary for JSF view-scoped beans.

## Dependencies
The `IndexController` class depends on the following classes and interfaces:
- `javax.inject.Named`: Used to declare a named bean.
- `javax.faces.view.ViewScoped`: Used to define the scope of the bean.
- `java.io.Serializable`: Implemented to support serialization.
- `lombok.Setter`: Used to generate setter methods for fields.
- `lombok.Getter`: Used to generate getter methods for fields.

## Public Methods Summary
- [submitButtonAction()](#submitbuttonaction): void

## Detailed Method Documentation

### submitButtonAction

#### Method Signature
```java
public void submitButtonAction()
```

#### Description
The `submitButtonAction` method is invoked when a user interacts with a specific UI component, typically a button. This method appends the current value of the `inputValue` field with a counter value, separated by a colon, and then increments the counter. This allows the application to track and display the number of times the button has been pressed.

#### Parameters
This method does not take any parameters.

#### Return Value
This method does not return any value.

#### Exceptions
This method does not throw any exceptions.

#### Usage Example
```java
@Named("indexcontroller")
@ViewScoped
public class IndexController implements Serializable {

    private static final long serialVersionUID = 8485377386286855408L;

    @Setter
    @Getter
    private String inputValue;

    private int counter;

    public void submitButtonAction() {
        inputValue = inputValue + " : " + counter;
        counter++;
    }
}

// Usage in a JSF page (index.xhtml)
<h:form>
    <h:inputText value="#{indexcontroller.inputValue}" />
    <h:commandButton value="Submit" action="#{indexcontroller.submitButtonAction}" />
    <h:outputText value="#{indexcontroller.inputValue}" />
</h:form>
```

In this example, the `submitButtonAction` method is bound to a command button in a JSF page. When the button is clicked, the method is executed, updating the `inputValue` field with the current counter value and incrementing the counter.

## Additional Information
The `IndexController` class uses Lombok annotations (`@Setter` and `@Getter`) to automatically generate getter and setter methods for the `inputValue` field. This reduces boilerplate code and enhances readability. The `ViewScoped` annotation ensures that the bean is alive as long as the user is interacting with the same JSF view, making it suitable for handling view-specific data and actions.

The `serialVersionUID` field is a unique identifier for the `Serializable` class, ensuring that the class can be reliably serialized and deserialized across different JVMs.

This class is designed to be simple and focused on demonstrating basic JSF managed bean functionality, making it a suitable example for educational purposes or as a starting point for more complex JSF applications.
```