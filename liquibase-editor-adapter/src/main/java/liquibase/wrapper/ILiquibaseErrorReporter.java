package liquibase.wrapper;

import org.eclipse.lsp4j.DiagnosticSeverity;

import liquibase.changelog.ChangeSet;

public interface ILiquibaseErrorReporter {

    public void report(ChangeSet changeSet, String message, DiagnosticSeverity severity);

    public void report(String changeSetId, String message, DiagnosticSeverity severity);

    public void reportOnDocument(String message, DiagnosticSeverity severity);

    public void report(String message, DiagnosticSeverity severity, int start, int end);
}
