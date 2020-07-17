package liquibase.wrapper;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public class ValidationWrappingDatabaseChangeLog extends DatabaseChangeLog {

	private DatabaseChangeLog databaseChangeLog;
	
	
	public ValidationWrappingDatabaseChangeLog(DatabaseChangeLog databaseChangeLog) {
		this.databaseChangeLog = databaseChangeLog;
	}

	@Override
    public void validate(Database database, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        database.setObjectQuotingStrategy(databaseChangeLog.getObjectQuotingStrategy());

        var validatingVisitor = new ValidatingVisitor(database.getRanChangeSetList());

        var logIterator = 
        		new ChangeLogIterator(databaseChangeLog,
        				new DbmsChangeSetFilter(database),
        				new ContextChangeSetFilter(contexts),
        				new LabelChangeSetFilter(labelExpression));

        validatingVisitor.validate(database, this);
        logIterator.run(validatingVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

        if (!validatingVisitor.validationPassed()) {
            throw new OpenValidationFailedException(validatingVisitor);
        }
    }
	
}
