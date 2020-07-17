package liquibase.wrapper;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.IgnoreChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.wrapper.exception.ExceptionHandler;

public class FailureResilientLiquibase extends Liquibase {
    private final ExceptionHandler exceptionHandler;

    public FailureResilientLiquibase(DatabaseChangeLog changeLog, ResourceAccessor resourceAccessor, Database database,
    		ILiquibaseErrorReporter liquibaseReporter) {
        super(changeLog, resourceAccessor, database);
        this.exceptionHandler = new ExceptionHandler(liquibaseReporter);
    }

    @Override
    protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, LabelExpression labelExpression,
            DatabaseChangeLog databaseChangeLog) throws DatabaseException {
        return new FailureResilientChangeLogIterator(exceptionHandler, databaseChangeLog,
                new ShouldRunChangeSetFilter(database, false),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }
    
    @Override
    public void validate() throws LiquibaseException {
        var changeLog = new ValidationWrappingDatabaseChangeLog(getDatabaseChangeLog());
        changeLog.validate(database);
    }

}
