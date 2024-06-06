// The issue key
def issueKey = 'DS-1'

// Fetch the issue object from the key
def issue = get("/rest/api/2/issue/${issue.key}")
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

assert customField : "Failed to find custom field with given name"



// Extract and store the option from the custom field
def value = (fields[customField.id] as Map)?.value
if (value == 'Billable'){
    //1. Adds a watcher   
    String accountId = '557058:17217ad2-09b8-4692-8726-8e644e401aec'
    addWatcher(issue.key, accountId)

    //2. Adds a comment
    String comment = 'Hey, this issue has changed'
    addComment(issue.key, comment)

    //3. Update fields    
    updateIssue(issue.key, customField.id, 'Billability Reviewed')
}



/**
 * Add a watcher given the accountId of the user into the issueKey
 */
void addWatcher(String issueKey, String accountId){
    HttpResponse result = post("/rest/api/3/issue/${issueKey}/watchers")
                    .header('Content-Type', 'application/json')
                    .body("\"${accountId}\"")
                    .asJson()

    result
}


/**
 * Add a comment in an issue given its issueKey
 */
void addComment(String issueKey, String commentBody){
   post("/rest/api/2/issue/${issueKey}/comment")
        .header('Content-Type', 'application/json')
        .body([
                body: commentBody,
                // Make comment visible in the customer portal
                public: true,
        ])
        .asObject(Map)
}

/**
 * Updates the value of a field inside the issue
 */
void updateIssue(String issueKey, String customFieldId, String valueToModify) {
    HttpResponse updateIssueResult
    if (issueKey && customFieldId)
        updateIssueResult = put("/rest/api/2/issue/${issueKey}")
                .header('Content-Type', 'application/json')
                .body([
                       fields: [
                            "${customFieldId}": [value: valueToModify]
                    ]
                ])
                .asString()
    assert updateIssueResult?.status == 204
}
