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

// page initialization
$(document).ready(function () {
    $("#defaultVersion").text(defaultHostName);
    if(customURL != "null"){
        $('#productionCustom').val(stripedUrl(customURL));
        uiElementStateChange(true, true, true);
        showEditButton();
    }else {
        $('#updateCustomUrl').prop('disabled', true);
        showUpdateButton();
    }
});

function setExistingCustomUrl() {
    if (customUrl !== "null") {
        $('#productionCustom').val(stripedUrl(customUrl));
        uiElementStateChange(true, true, true);
        showEditButton();
    } else {
        $('#updateCustomUrl').prop('disabled', true);
        showUpdateButton();
    }
}

function verifyCustomUrl() {
    var pointedUrl = defaultHostName;
    var customUrl = $('#productionCustom').val().trim();

    if (!checkHostNamePattern(customUrl)) {
        jagg.message({
            content: "Domain name format is invalid. Please add correctly formatted domain name",
            type: 'error',
            id: 'view_log'
        });
    } else {
        jagg.post("../blocks/urlmapper/urlmapper.jag", {
                action: "verifyCustomDomain",
                customUrl: customUrl,
                pointedUrl: pointedUrl
            }, verifyCustomUrlSuccess,
            function (jqXHR, textStatus, errorThrown) {
                jagg.message({content: jqXHR.responseText, type: 'error', id: 'view_log'});
            });
    }
}

function stripedUrl(url){
    var stripedUrl = url.replace(/.*?:\/\//g, "");
    return stripedUrl;
}

function verifyCustomUrlSuccess() {
    jagg.message({content: "The custom domain was successfully added to the application.", type: 'success', id: 'view_log'});
    uiElementStateChange(true, true, true);
    showUpdateButton();
    $('#updateCustomUrl').prop('disabled', false);
}

function uiElementStateChange(prodVersion, prodCustom, verifyUrl, updateBtn) {
    $("#productionVersion").prop('disabled', prodVersion);
    $("#productionCustom").prop('disabled', prodCustom);
    $("#verifyUrl").prop('disabled', verifyUrl);
    $("#updateCustomUrl").prop('disabled', updateBtn);
}

function showUpdateButton() {
    $('#editCustomUrl').hide();
    $('#updateCustomUrl').show();
}

function showEditButton() {
    $('#editCustomUrl').show();
    $('#updateCustomUrl').hide();
}

function updateCustomUrl() {
    var pointedUrl = defaultHostName;
    var customUrl = $('#productionCustom').val().trim();
    jagg.post("../blocks/urlmapper/urlmapper.jag", {
        action: "updateCustomUrl",
        pointedUrl: pointedUrl,
        customUrl: customUrl,
        applicationName: applicationName
    }, function (result) {
        jagg.message({content: "The custom domain is successfully updated.", type: 'success', id: 'view_log'});
        showEditButton();
    }, function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: jqXHR.responseText, type: 'error', id: 'view_log'});

    });
}

function checkHostNamePattern(hostName) {
    var hostNamePattern = /^([a-z0-9]+(-[a-z0-9]+)*\.)+[a-z]{2,}$/gm;
    return hostNamePattern.test(hostName);
}

function editCustomUrl() {
    uiElementStateChange(false, false, false);
}






