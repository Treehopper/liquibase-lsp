package liquibase.wrapper.exception;

import static org.eclipse.lsp4j.DiagnosticSeverity.Error;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.exception.DatabaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.exception.ValidationFailedException;
import liquibase.wrapper.ILiquibaseErrorReporter;

public class ExceptionHandler implements Consumer<ExceptionChangeSetPair> {
    
    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());

    private ILiquibaseErrorReporter reporter;

    public ExceptionHandler(ILiquibaseErrorReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void accept(ExceptionChangeSetPair pair) {
        var exception = pair.exception();
        var changeSet = pair.changeSet();
        
        if (!(MigrationFailedException.class.isAssignableFrom(exception.getClass()))) {
            LOGGER.log(Level.SEVERE, "Unexpected Error", exception);
            return;
        }
        
        var message = "";
        if (exception instanceof ValidationFailedException e) {
            message = e.getLocalizedMessage();
        }
        
        if (exception.getCause() instanceof DatabaseException e) {
            message = e.getLocalizedMessage();
        }
        reporter.report(changeSet, message, Error);
    }

}
