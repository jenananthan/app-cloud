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
    $("#defaultVersion").val(defaultHostName);
    $("#defaultVersion").addClass("cursorText");
    if(customURL != "null"){
        $('#productionCustom').val(stripedUrl(customURL));
        $("#customDomainForm").css('display', "block");
        $("#productionCustom").prop('disabled', "disabled");
        $("#configureButtonText").html("Update custom url");
    } else{
        $("#customDomainForm").css('display', "none");
    }
});

function stripedUrl(url){
    var stripedUrl = url.replace(/.*?:\/\//g, "");
    return stripedUrl;
}

function configureCustomUrl() {
    var pointedUrl = defaultHostName;
    var customURLParamater = "";
    if (customURL != "null") {
        customURLParamater = "&customUrl=" + stripedUrl(customURL);
    }
    window.location = cloudMgtURL + customUrlSettingsPageUrl + "?cloud-type=app-cloud&appName=" + applicationName + "&defaultDomain="
        + pointedUrl + customURLParamater + "&backUrl=" + window.location.href;
}





