//Regex patterns
var ALPHA_NUMERIC_PLUS_UNDERSCORE_REGEX = "^[A-Za-z0-9_]+$";


//Environment key validation
function validateEnvKey(envKey){
    var envKeyRegex = new RegExp(ALPHA_NUMERIC_PLUS_UNDERSCORE_REGEX);
    var validator;
    if (!envKeyRegex.test(envKey)) {
        validator = {
            status: false,
            msg: "Only alphanumeric characters and underscore are allowed."
        }
    } else {
        validator = {
            status: true,
            msg: "Environment variable key is successfully added."
        }
    }
    return validator;
}

function validateDbName(databaseName) {
    var databaseNameRegex = new RegExp(ALPHA_NUMERIC_PLUS_UNDERSCORE_REGEX);
    var validator;
    if (!databaseNameRegex.test(databaseName)) {
        validator = {
            status: false,
            msg: "Only alphanumeric characters and underscore are allowed."
        }
    } else {
        validator = {
            status: true,
            msg: "Database name is successfully added."
        }
    }
    return validator;
}