// The issue key
def issueKey = 'DS-1'

// Fetch the issue object from the key
def issue = get("/rest/api/2/issue/${issueKey}")
        .header('Content-Type', 'application/json')
        .asObject(Map)
        .body

// Get all the fields from the issue as a Map
def fields = issue.fields as Map

// Get the Custom field to get the option value from
def customField = get("/rest/api/2/field")
        .asObject(List)
        .body
        .find {
            (it as Map).name == 'Billability'
        } as Map

// Extract and store the option from the radio buttons custom field
def customFieldValue = (fields[customField.id] as Map).value


if (customFieldValue == 'Billable'){

    //1. Add a user to be inserted as a watcher
    addWatcher(issue.key)

    //2. Insert a comment
    String comment =  "The issue has been reviewed"
    addComment(issue.key, comment)

    //3. Updates the value
    updateIssue(issue.id, customFiel.id, 'Reviewed Billability')
    logger.info "The issue has been reviewed:"
}





/**
 * Add a comment into an issue
 * @param issue Issue where to put a comment
 * @param commentBody Text to write
 */
def addComment(String issueKey, String commentBody){
    post("/rest/api/2/issue/${issueKey}/comment")
            .header("Content-Type", "application/json")
            .body([
                    body: commentBody,
            ])
            .asString()
}


/**
 * Update the fields of fieldsUpdated map of the given issue
 * @param issueKey , the key of the issue to update
 * @param fieldsUpdated , a map with the fields to update, the format
 * is fieldKey: value as object
 */
void updateIssue(String issueKey, Map fieldsUpdated) {
    HttpResponse updateIssueResult
    if (issueKey && fieldsUpdated)
        updateIssueResult = put("/rest/api/2/issue/${issueKey}")
                .header('Content-Type', 'application/json')
                .body([
                        fields: fieldsUpdated
                ])
                .asString()

    assert updateIssueResult?.status == 204
}



/**
 * Add the current user as an issue watcher
 * @param issueKey Key of the issue to update
 */
void addWatcher(String issueKey) {
    HttpResponse result
    if (issueKey)
        result = post("/rest/api/2/issue/${issueKey}/watchers")
                .header('Content-Type', 'application/json')
                .asJson()
    assert result?.status == 204
}