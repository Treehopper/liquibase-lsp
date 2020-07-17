package liquibase.wrapper.exception;

import com.github.bsideup.jabel.Desugar;

import liquibase.changelog.ChangeSet;
import liquibase.exception.LiquibaseException;

@Desugar
public record ExceptionChangeSetPair(LiquibaseException exception, ChangeSet changeSet) {
}
