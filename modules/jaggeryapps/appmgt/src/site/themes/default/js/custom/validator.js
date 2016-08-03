//Regex patterns
var ENV_KEY_REGEX = "^[A-Za-z0-9_]+$";


//Environment key validation
function validateEnvKey(envKey){
    var envKeyRegex = new RegExp(ENV_KEY_REGEX);
    var validator;
    if (!envKeyRegex.test(envKey)) {
        validator = {
            status: false,
            msg: "Invalid value for variable key:" + envKey + ", Valid characters are [A-Z, a-z, 0-9, _]."
        }
    } else {
        validator = {
            status: true,
            msg: "Environment variable key validated and successfully added."
        }
    }
    return validator;
};