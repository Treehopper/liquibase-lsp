package liquibase.wrapper.exception;

import liquibase.changelog.ChangeSet;
import liquibase.exception.LiquibaseException;

public class ExceptionChangeSetPair {
    private LiquibaseException exception;
    private ChangeSet changeSet;

    public ExceptionChangeSetPair(LiquibaseException exception, ChangeSet changeSet) {
        this.exception = exception;
        this.changeSet = changeSet;
    }

    public LiquibaseException exception() {
        return exception;
    }

    public ChangeSet changeSet() {
        return changeSet;
    }
}
