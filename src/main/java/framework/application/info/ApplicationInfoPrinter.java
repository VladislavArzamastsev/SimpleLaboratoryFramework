package framework.application.info;

import framework.command.NamedCommand;
import framework.command.holder.CommandHolder;
import framework.command.holder.CommandHolderAware;
import framework.enums.PropertyName;
import framework.enums.VariableType;
import framework.exception.LaboratoryFrameworkException;
import framework.utils.ConsoleUtils;
import framework.utils.ValidationUtils;
import framework.variable.entity.MatrixVariable;
import framework.variable.entity.Variable;
import framework.variable.entity.VectorVariable;
import framework.variable.holder.VariableHolder;
import framework.variable.holder.VariableHolderAware;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Class is created to build and print greeting and manual
 */
@Setter
public class ApplicationInfoPrinter implements CommandHolderAware, VariableHolderAware {

    private final String greeting;

    private CommandHolder commandHolder;

    private VariableHolder variableHolder;

    public ApplicationInfoPrinter(Properties applicationProperties) {
        this.greeting = buildGreeting(applicationProperties);
    }

    public void printManual() {
        ConsoleUtils.println(buildManual());
    }

    public void printGreeting() {
        ConsoleUtils.println(this.greeting);
    }

    private String buildGreeting(Properties applicationProperties) {
        StringBuilder stringBuilder = new StringBuilder();
        appendApplicationPart(stringBuilder, applicationProperties);
        return stringBuilder.toString();
    }


    private String buildManual() {
        StringBuilder stringBuilder = new StringBuilder();
        appendVariablesPart(stringBuilder, variableHolder);
        appendCommandsPart(stringBuilder, commandHolder);
        return stringBuilder.toString();
    }

    /**
     * Appends 'application' part and adds line separator character to the end
     *
     * @throws LaboratoryFrameworkException if any parameter in properties is null or empty
     */
    private void appendApplicationPart(StringBuilder destination, Properties applicationProperties)
            throws LaboratoryFrameworkException {
        String applicationName = applicationProperties.getProperty(PropertyName.APPLICATION_NAME.getName());
        ValidationUtils.requireNotEmpty(applicationName, "Application name is not specified");
        destination.append(String.format("Application name: %s%n", applicationName));

        String applicationAuthor = applicationProperties.getProperty(PropertyName.APPLICATION_AUTHOR.getName());
        ValidationUtils.requireNotEmpty(applicationAuthor, "Application author is not specified");
        destination.append(String.format("Application author: %s%n", applicationAuthor));

        String applicationDescription = applicationProperties.getProperty(PropertyName.APPLICATION_DESCRIPTION.getName());
        ValidationUtils.requireNotEmpty(applicationDescription, "Application description is not specified");
        destination.append(String.format("Application description: %s%n", applicationDescription));
    }

    /**
     * Appends 'variables' part and adds line separator character to the end
     */
    private void appendVariablesPart(StringBuilder destination, VariableHolder variableHolder) {
        Map<String, Variable> variables = variableHolder.getVariables();
        destination.append(String.format("Variables:%n"));
        variables.values().stream()
                .filter((e) -> !e.isCannotBeSetFromInput())
                .sorted(Comparator.comparing(Variable::getName))
                .forEach((e) -> appendGeneralVariableInfo(destination, e));
        destination.append(System.lineSeparator());
        destination.append(String.format("Variables that cannot be set:%n"));
        variables.values().stream()
                .filter(Variable::isCannotBeSetFromInput)
                .sorted(Comparator.comparing(Variable::getName))
                .forEach((e) -> appendGeneralVariableInfo(destination, e));
        destination.append(System.lineSeparator());
    }

    private void appendGeneralVariableInfo(StringBuilder destination, Variable variable) {
        destination.append(String.format("* %s:%n", variable.getName()));
        destination.append(String.format("\tDescription: %s%n", variable.getDescription()));
        destination.append(String.format("\tType: %s%n", variable.getType()));
        if (variable.getType() == VariableType.VECTOR && Objects.equals(variable.getClass(), VectorVariable.class)) {
            int length = ((VectorVariable) variable).getLength();
            if (length > 0) {
                destination.append(String.format("\tLength: %d%n", length));
            }
        } else if (variable.getType() == VariableType.MATRIX && Objects.equals(variable.getClass(), MatrixVariable.class)) {
            int rowCount = ((MatrixVariable) variable).getRowCount();
            if (rowCount > 0) {
                destination.append(String.format("\tRow count: %d%n", rowCount));
            }
            int columnCount = ((MatrixVariable) variable).getColumnCount();
            if (columnCount > 0) {
                destination.append(String.format("\tColumn count: %d%n", columnCount));
            }
        }
    }

    /**
     * Appends 'commands' part and adds line separator character to the end
     */
    private void appendCommandsPart(StringBuilder destination, CommandHolder commandHolder) {
        Map<String, ? extends NamedCommand> commands = commandHolder.getCommands();
        destination.append(String.format("Commands:%n"));
        commands.values().stream()
                .sorted(Comparator.comparing(NamedCommand::getName))
                .forEach(e -> {
                    destination.append(String.format("* %s:%n", e.getName()));
                    destination.append(String.format("\tDescription: %s%n", e.getDescription()));
                    if (!e.getOptions().isEmpty()) {
                        destination.append(String.format("\tOptions: %s%n", String.join(", ", e.getOptions())));
                    }
                });
        destination.append(System.lineSeparator());
    }
}
