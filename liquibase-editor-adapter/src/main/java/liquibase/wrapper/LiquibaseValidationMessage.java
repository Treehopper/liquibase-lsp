package liquibase.wrapper;

public class LiquibaseValidationMessage {

    private String changeSetId;

    private String theMessage;

    public LiquibaseValidationMessage(String message) {
        var split = message.split("::");
        changeSetId = split[split.length - 2];
        theMessage = split[0];
    }

    public String getChangeSetId() {
        return changeSetId;
    }
    
    public String getTheMessage() {
        return theMessage;
    }
}
