package liquibase.wrapper;

import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.exception.ValidationFailedException;

public class OpenValidationFailedException extends ValidationFailedException {

    private static final long serialVersionUID = 5099785679959364747L;

    private ValidatingVisitor validatingVisitor;

    public OpenValidationFailedException(ValidatingVisitor validatingVisitor) {
        super(validatingVisitor);
        this.validatingVisitor = validatingVisitor;
    }

    public ValidatingVisitor getValidatingVisitor() {
        return validatingVisitor;
    }

}
