package liquibase.wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import liquibase.wrapper.exception.ExceptionChangeSetPair;
import liquibase.wrapper.exception.ExceptionHandler;

import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;

public class FailureResilientChangeLogIterator extends ChangeLogIterator {
		private final DatabaseChangeLog databaseChangeLog;
		private ExceptionHandler exceptionHandler;

		FailureResilientChangeLogIterator(ExceptionHandler exceptionHandler, DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
			super(databaseChangeLog, changeSetFilters);
			this.exceptionHandler = exceptionHandler;
			this.databaseChangeLog = databaseChangeLog;
		}

		@Override
		public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException  {
		    databaseChangeLog.setRuntimeEnvironment(env);
		    try {
		    	var changeSetList = new ArrayList<>(databaseChangeLog.getChangeSets());
		        if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
		            Collections.reverse(changeSetList);
		        }

		        for (ChangeSet changeSet : changeSetList) {
		            boolean shouldVisit = true;
		            Set<ChangeSetFilterResult> reasonsAccepted = new HashSet<>();
		            Set<ChangeSetFilterResult> reasonsDenied = new HashSet<>();
		            if (getChangeSetFilters() != null) {
		                for (var filter : getChangeSetFilters()) {
		                	var acceptsResult = filter.accepts(changeSet);
		                    if (acceptsResult.isAccepted()) {
		                        reasonsAccepted.add(acceptsResult);
		                    } else {
		                        shouldVisit = false;
		                        reasonsDenied.add(acceptsResult);
		                        break;
		                    }
		                }
		            }

		            try {
		                if (shouldVisit && !alreadySaw(changeSet)) {
		                    visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsAccepted);
		                    markSeen(changeSet);
		                } else {
		                    if (visitor instanceof SkippedChangeSetVisitor v) {
		                        v.skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
		                    }
		                }
		            } catch (MigrationFailedException exception) {
		            	// handle exception and continue with next changeSet
		            	exceptionHandler.accept(new ExceptionChangeSetPair(exception, changeSet));
					}
		        }
		    } finally {
		        databaseChangeLog.setRuntimeEnvironment(null);
		    }
		}
	}
