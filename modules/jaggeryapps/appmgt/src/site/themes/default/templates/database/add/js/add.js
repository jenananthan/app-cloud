/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
var isNewUser = true;
var dbNameValidationErrorMsg = "";
$(document).ready(function () {
    if (isDatabaseCountThrottled == 'true') {
        $('#outerContainer').empty();
                $('#outerContainer').html('<div class="container-fluid"><div class="row row-centered">' +
                    '<div class="col-centered col-xs-10 col-sm-7  col-md-7 col-lg-6"><div class="cloud-new-content">' +
                    '<h3>You cannot create more than ' + maxDatabases + ' databases on your subscription. ' +
                    'Please upgrade your subscription plan to continue...</h3></div></div></div></div>');
                return;
    }
//select 2
    //$('select').select2(); //select2 init for stages dropdown
    var $select = $('#user-name-select.select2')
            .select2({
                         placeholder: "Enter username or select existing",
                         multiple: true,
                         maximumInputLength: 5,
                         tags: true,
                         selectOnBlur: true,
                         createSearchChoice: function (term, data) {
                             if ($(data).filter(function () {
                                         return this.text.localeCompare(term) === 0;
                                     }).length === 0) {
                                 return {
                                     id: term,
                                     text: term
                                 };
                             }
                         },

                     });
    $select.on("select2:select", function (e) {
        var l = $select.select2('data');
        if (e.params.data.isNew != undefined && e.params.data.isNew) {
            isNewUser = true;
            $("#passwordSection").show();
            if (l.length > 1) {
                $("#user-name-select.select2 [value='" + e.params.data.id + "']").remove();
                $('#user-name-select.select2').trigger('change');
            }
            validateForm();
        } else {
            isNewUser = false;
            $("#passwordSection").hide();
            if (l.length > 1) {
                $('#user-name-select.select2 option[value="' + e.params.data.id + '"]:selected').removeAttr('selected');
                $select.trigger('change');
            }
            validateForm();
        }
    });

    $select.on("select2:close", function (e) {
        var highlighted = $(e.target).data('select2').$dropdown.find('.select2-results__option--highlighted');
        if (highlighted) {
            var data = highlighted.data('data');
            var id = data.id;
            var display = data.name;
            if(id != 0) {
                $select.select2("val",id);
            }
        }
    });

    $select.on("select2:open", function (e) {
        if($("#user-name-select").val()){
            $select.select2("val", "");
        }
    });

    getExistingUsersForSelectedStage();


    $('#stage').on("select2:select", function (e) {
        getExistingUsersForSelectedStage();
    });


    /**
     * According to the selected stage, users available will be set and listed in users dropdown
     */
    function getExistingUsersForSelectedStage() {
        var dbUsersJsonArray = JSON.parse(dbUsers);
        var dbUsersInStage = [];
        for (var i in dbUsersJsonArray) {
            var user = dbUsersJsonArray[i];
            var id = 0;
            if (user.rssInstanceName == $('#stage').val()) {
                var exUser = {};
                exUser.text = user.name;
                exUser.existing = true;
                exUser.id = id;
                id++;
                dbUsersInStage.push(exUser);
            }
        }
        setExistingUsers(dbUsersInStage);
    }

    /**
     * Filling data to user-name-select dropdown
     * @param data = [{id:0, text: "Admin"}, {id:1, text: "Root"}, {id:2, text: "User"}];
     */
    function setExistingUsers(data) {
        $select.empty();
        $select.trigger('change');
        $select = $('#user-name-select.select2')
                .select2({
                             placeholder: "Enter username or select existing",
                             data: data,
                             multiple: true,
                             maximumInputLength: 5,
                             tags: true,
                             selectOnBlur: true,
                             createSearchChoice: function (term, data) {
                                 if ($(data).filter(function () {
                                             return this.text.localeCompare(term) === 0;
                                         }).length === 0) {
                                     return {
                                         id: term,
                                         text: term
                                     };
                                 }
                             },
                             createTag: function (tag) {
                                 return {
                                     id: tag.term,
                                     text: tag.term,
                                     isNew: true
                                 };
                             }
                         });
    }

//add show /hide option on user passsword field
    $('input[type=password]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fa fa-eye"></i> </span>');
    var highPass = $('.hide-pass');
    $('.hide-pass').click(function () {
        if ($(this).find('i').hasClass("fa-eye-slash")) {
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass("fa-eye-slash");
        } else {
            $(this).find('i').addClass("fa-eye-slash");
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
        }
    });

//password strength meter logic
    $("#password").on("focus keyup", function () {
        var score = 0;
        var a = $(this).val();
        var desc = new Array();
        // strength desc
        desc[0] = "Too short";
        desc[1] = "Weak";
        desc[2] = "Good";
        desc[3] = "Strong";
        desc[4] = "Best";
        // password length
        var valid = '<i class="fa fa-check"></i>';
        var invalid = '<i class="fa fa-times"></i>';
        if (a.length >= 6) {
            $("#length").removeClass("invalid").addClass("valid");
            $("#length .status_icon").html(valid);
            score++;
        } else {
            $("#length").removeClass("valid").addClass("invalid");
            $("#length .status_icon").html(invalid);
        }
        // at least 1 digit in password
        if (a.match(/\d/)) {
            $("#pnum").removeClass("invalid").addClass("valid");
            $("#pnum .status_icon").html(valid);
            score++;
        } else {
            $("#pnum").removeClass("valid").addClass("invalid");
            $("#pnum .status_icon").html(invalid);
        }
        // at least 1 capital & lower letter in password
        if (a.match(/[A-Z]/) && a.match(/[a-z]/)) {
            $("#capital").removeClass("invalid").addClass("valid");
            $("#capital .status_icon").html(valid);
            score++;
        } else {
            $("#capital").removeClass("valid").addClass("invalid");
            $("#capital .status_icon").html(invalid);
        }
        // at least 1 special character in password {
        if (a.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/)) {
            $("#spchar").removeClass("invalid").addClass("valid");
            $("#spchar .status_icon").html(valid);
            score++;
        } else {
            $("#spchar").removeClass("valid").addClass("invalid");
            $("#spchar .status_icon").html(invalid);
        }
        if (a.length > 0) {
            //show strength text
            $("#passwordDescription").text(desc[score]);
            // show indicator
            $("#passwordStrength").removeClass().addClass("strength" + score);
        } else {
            $("#passwordDescription").text("Password not entered");
            $("#passwordStrength").removeClass().addClass("strength" + score);
        }
    });
    $("#password").popover({
                               title: 'Password strength meter',
                               html: true,
                               content: $("#password_strength_wrap").html(),
                               placement: 'top',
                               trigger: 'focus keypress'
                           });
    $("#password").blur(function () {
        $(".password_strength_meter .popover").popover("hide");
    });
    //password generator
    $('.password-generator')
            .pGenerator({
                            'bind': 'click',
                            'passwordElement': '#password',
                            'displayElement': '#password-confirm',
                            'passwordLength': 10,
                            'uppercase': true,
                            'lowercase': true,
                            'numbers': true,
                            'specialChars': true,
                            'onPasswordGenerated': function (generatedPassword) {
                                var backslashRegex = new RegExp("\\\\", "g");
                                var quoteRegex = new RegExp("\'");
                                //backslash and single quote are not allowed as special characters in the password
                                if (backslashRegex.test(generatedPassword) || quoteRegex.test(generatedPassword)) {
                                    $(".password-generator").trigger("click");
                                } else {
                                    generatedPassword = 'Your password has been generated : ' + generatedPassword;
                                    $(".password-generator").attr('data-original-title', generatedPassword)
                                        .tooltip('show', {
                                            placement: 'right'
                                        });
                                    $("#password").trigger('focus');
                                    if (!$(highPass).find('i').hasClass("fa-eye-slash")) {
                                        $(highPass).click();
                                    }
                                }
                            }
                        });

    // binding jquery validation to the form
    validateForm();

}); // end of document.ready

function formEvent() { // fires on every keyup & blur
    if (isNewUser && $('#database-name').val() && $('#user-name-select').val() && $('#password').val() && $('#password-confirm').val()) {
        $("#add-database").prop("disabled", false);
    } else if (!isNewUser && $('#database-name').val() && $('#user-name-select').val()) {
        $("#add-database").prop("disabled", false);
    } else {
        $("#add-database").prop("disabled", true);
    }
}

function validateForm(){
    var addDatabaseForm = $("#addDatabaseForm");
    addDatabaseForm.on('focusout keyup blur select2:select', formEvent);
}

function getValidationOptions(){
    //Add custom validator for database name
    $.validator.addMethod("validateDatabaseName", validateDatabaseName , dbNameValidationErrorMsg);
    if(isNewUser){
        return getNewUserValidationOptions();
    } else {
        return getExistingValidationOptions();
    }
}

/**
*Defining validation options
*/
function getExistingValidationOptions(){
    return {
        rules: {
            "database-name": {
                required: true,
                maxlength: 30,
                validateDatabaseName: true
            },
            "user-name-select": {
                required: true,
                maxlength: 7
            }
        },
        messages: {
            "password-confirm": {
                equalTo: "The password and confirm password does not match"
            }
        },
        onsubmit: false,    // Since we are handling on submit validation on click event of the "Create" button,
                            // here we disabled the form validation on submit
        onkeyup: function (event, validator) {
            return false;
        },
        showErrors: function (event, validator) {
            // Disable add user button if the form is not valid
            if (this.numberOfInvalids() > 0) {
                $("#add-database").prop("disabled", true);
            }
            this.defaultShowErrors();
        },
        errorPlacement: function (error, element) {
            if ($(element).hasClass("eye-icon")) {
                error.insertAfter($(element).parent().find('span.hide-pass'));
            } else {
                error.insertAfter(element);
            }
        }
    };
}

/**
*Defining validation options
*/
function getNewUserValidationOptions(){
    return {
        rules: {
            "database-name": {
                required: true,
                maxlength: 30,
                validateDatabaseName: true
            },
            "user-name-select": {
                required: true,
                maxlength: 7
            },
            "password": {
                required: true
            },
            "password-confirm": {equalTo: '#password'}
        },
        messages: {
            "password-confirm": {
                equalTo: "The password and confirm password does not match"
            }
        },
        onsubmit: false,    // Since we are handling on submit validation on click event of the "Create" button,
                            // here we disabled the form validation on submit
        onkeyup: function (event, validator) {
            return false;
        },
        showErrors: function (event, validator) {
            // Disable add user button if the form is not valid
            if (this.numberOfInvalids() > 0) {
                $("#add-database").prop("disabled", true);
            }
            this.defaultShowErrors();
        },
        errorPlacement: function (error, element) {
            if ($(element).hasClass("eye-icon")) {
                error.insertAfter($(element).parent().find('span.hide-pass'));
            } else {
                error.insertAfter(element);
            }
        }
    };
}

/**
 *  Adding new database
 */
function addNewDatabase() {
    var validator = $("#addDatabaseForm").validate(getValidationOptions());
    var formValidated = validator.form();
    if (formValidated) {  
        $("#add-database").loadingButton({action:'show'});
        jagg.post("../blocks/database/add/ajax/add.jag", {
            action: "createDatabaseAndAttachUser",
            databaseName: $("#database-name").val().trim(),
            databaseServerInstanceName: $("#stage option:selected").val(),
            isBasic: isNewUser,
            customPassword: $('#password').val().trim(),
            userName: $('#user-name-select').select2('data')[0].text,
            templateName: null,
            copyToAll: false
        }, function (result) {
            result = $.trim(result);
            result = JSON.parse(result);
            if (result.value == 'success') {
                window.location.href = "databases.jag";
            } else {
                jagg.message({content: 'An error occurred while creating the database.', type: 'error', id: 'databasecreation'});
            }
        }, function (jqXHR, textStatus, errorThrown) {
            jagg.message({
                content: jqXHR.responseText,
                type: 'error',
                id: 'databasecreation',
                timeout: 8000
            });
            $("#add-database").loadingButton({action:'hide'});
        });
    }
}

function validateDatabaseName(value) {
    var dbNameValidation = validateDbName(value);
    if (!dbNameValidation.status) {
        dbNameValidationErrorMsg = dbNameValidation.msg;
    }
    return dbNameValidation.status;
}

$(document).on('focusout keyup blur change', '#database-name', function() {
    var validator = $("#addDatabaseForm").validate(getValidationOptions());
    $('#database-name').valid();
});