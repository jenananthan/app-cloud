/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
// page initialization
var timerId;
$(document).ready(function() {
    // add upload app icon listener
    $("#change_app_icon").change(function(event) {
        submitChangeAppIcon(this);
    });
    initPageView();
    var uploadRevisionUrl = appCreationPageBaseUrl+"?appTypeName="+application.applicationType +
                            "&applicationName="+applicationName + "&encodedLabels="+encodedLabels + "&encodedEnvs="
                                    + encodedEnvs + "&newVersion=true&conSpecCpu=" + conSpecCpu + "&conSpecMemory="
                                    + conSpecMemory + "&versionArray=" + encodeURI(versionArray);
    $('#upload-revision').attr("href", uploadRevisionUrl);

    if(selectedApplicationRevision.status==APPLICATION_INACTIVE){
        displayApplicationInactiveMessage();
    }

    loadEndpointView();
});

// wrapping functions
function initPageView() {
    loadAppIcon();
    var deploymentURL = selectedApplicationRevision.deploymentURL;
    var repoUrlHtml = generateLunchUrl(deploymentURL, selectedApplicationRevision.status);
    $("#version-url-link").html(repoUrlHtml);
    $('#appVersionList li').click(function() {
        var newRevision = this.textContent;
        changeSelectedRevision(newRevision);
    });

    $('body').on('click', '#btn-launchApp', launchApp);
    $('body').on('click', '#launch-version-url-a', displayMessage);
    $('body').on('click', '#launch-default-url-a', displayMessage);

    $('#btn-dashboard').click(function() {
        var appUrl = $('#btn-dashboard').attr("url");
        var newWindow = window.open('','_blank');
        newWindow.location = appUrl;
    });

    listTags();
    listEnvs();
}

function generateDefaultLaunchUrl() {
    var defaultAppLaunchURL = selectedApplicationRevision.deploymentURL;
    if (application.defaultVersion == selectedRevision) {
        if (application.customURL != null) {
            defaultAppLaunchURL = application.customURL;
        } else if (application.defaultURL != null) {
            defaultAppLaunchURL = application.defaultURL;
        }
    }

    return defaultAppLaunchURL;
}

function loadEndpointView() {
    clearInterval(timerId);
    if (selectedApplicationRevision.status == APPLICATION_RUNNING) {
        // This is not implemented for mss 1.0.0 runtimes.
        if (application.applicationType == "mss" && selectedApplicationRevision.runtimeId == 2) {
            // if mss 1.0.0 do not show endpoints section
        } else {
            if (application.applicationType == "wso2dataservice") {
                displayEndpointNotloadingMessage();
            }
            showLoadingEndpointView();
            var deploymentURL = generateDefaultLaunchUrl();
            loadEndpoints(deploymentURL, applicationType, selectedApplicationRevision.versionId);
            timerId = setInterval(function () {
                loadEndpoints(deploymentURL, applicationType, selectedApplicationRevision.versionId);
            }, 3000);
        }
    } else {
        $("#app-type-data").html('');
    }
}

function displayEndpointNotloadingMessage() {
    jagg.message({
        modalStatus: true,
        type: 'warning',
        timeout: 15000,
        content: "The endpoints of your application might not be available if you created it before <b>2016/8/25</b>." +
            " Please recreate the application or create a new version of it to see the endpoints."
    });
}

function showLoadingEndpointView() {
    $("#app-type-data").html('<div class="block-endpoints "><h5>' +
        '<span><i class="fw fw-loader2 fw-spin fw-2x"></i></span>' +
        ' &nbsp; Endpoints of ' + selectedApplicationRevision.runtimeName + ' runtime is loading ...</h5></div>');
}

function loadEndpoints(deploymentURL, applicationType, versionId) {
    jagg.post("../blocks/application/application.jag", {
     action: "loadEndpoints",
     appType: applicationType,
     deploymentURL: deploymentURL,
     versionId: versionId
     }, function(result) {
        var endpoints = JSON.parse(result);
        if (endpoints == null) {
            showLoadingEndpointView();
        } else {
            // Generate SOAP Services Section
            var soap_html = "";
            if (endpoints.data.soapEndpoints != null) {
                soap_html += '<h4><i class="fw fw-soap fw-2x"></i> &nbsp; SOAP Services</h4>' +
                    '<table class="table table-responsive">' +
                    '<thead class="thead">' +
                    '<tr>' +
                    '<th width="30%">Name</th>' +
                    '<th>WSDL</th>' +
                    '</tr>' +
                    '</thead>' +
                    '<tbody>';
                for (var i = 0; i < endpoints.data.soapEndpoints.length; i++) {
                    var proxy = endpoints.data.soapEndpoints[i];

                    soap_html += '<tr>' +
                        '<td>' + proxy.name + '</td>' +
                        '<td><a href="' + proxy.wsdl + '" target="_blank">' + proxy.wsdl + '</a></td>' +
                        '</tr>';
                }
                soap_html += '</tbody>' +
                    '</table>';
            }

            // Generate REST APIs Section
            var rest_html = "";
            if (endpoints.data.restEndpoints != null) {
                rest_html += '<h4><i class="fw fw-rest-api fw-2x"></i> &nbsp; REST APIs</h4>' +
                    '<table class="table table-responsive">' +
                    '<thead class="thead">' +
                    '<tr>' +
                    '<th width="30%">Name</th>' +
                    '<th>URL</th>' +
                    '</tr>' +
                    '</thead>' +
                    '<tbody>';

                for (var j = 0; j < endpoints.data.restEndpoints.length; j++) {
                    var api = endpoints.data.restEndpoints[j];
                    rest_html += '<tr>' +
                        '<td>' + api.name + '</td>' +
                        '<td><a href="' + api.url + '" target="_blank">' + api.url + '</a></td>' +
                        '</tr>';
                }
                rest_html += '</tbody></table>';

            }

            // Generate Web URLs Section
            var web_html = "";
            if (endpoints.data.webEndpoints != null) {
                web_html += '<h4><i class="fw fw-endpoint fw-2x"></i> &nbsp; Web URLs</h4>' +
                    '<table class="table table-responsive">' +
                    '<thead class="thead">' +
                    '<tr>' +
                    '<th width="30%">Context</th>' +
                    '<th>URL</th>' +
                    '</tr>' +
                    '</thead>' +
                    '<tbody>';

                for (var k = 0; k < endpoints.data.webEndpoints.length; k++) {
                    var web = endpoints.data.webEndpoints[k];
                    web_html += '<tr>' +
                        '<td>' + web.context + '</td>' +
                        '<td><a href="' + web.url + '" target="_blank">' + web.url + '</a></td>' +
                        '</tr>';
                }
                web_html += '</tbody></table>';

            }

            // Generate Swagger URL Section
            var swagger_html = "";
            if (endpoints.data.swaggerEndpoints != null) {
                swagger_html += '<h4><i class="fw fw-swagger fw-2x"></i> &nbsp; Swagger</h4>' +
                    '<table class="table table-responsive">' +
                    '<thead class="thead">' +
                    '<tr>' +
                    '<th width="30%">Context</th>' +
                    '<th>URL</th>' +
                    '</tr>' +
                    '</thead>' +
                    '<tbody>';

                for (var j = 0; j < endpoints.data.swaggerEndpoints.length; j++) {
                    var swagger = endpoints.data.swaggerEndpoints[j];
                    swagger_html += '<tr>' +
                        '<td>' + swagger.context + '</td>' +
                        '<td><a href="' + swagger.url + '" target="_blank">' + swagger.url + '</a></td>' +
                        '</tr>';
                }
                swagger_html += '</tbody></table>';

            }
            $("#app-type-data").html('<div class="block-endpoints"><h3>Endpoints</h3>' + rest_html + soap_html + web_html + swagger_html + '</div>');
            clearInterval(timerId);
        }

     }, function(jqXHR, data, errorThrown) {

     });
}

function launchApp() {
    if(selectedApplicationRevision.status == APPLICATION_RUNNING){
        var appUrl = $('#btn-launchApp').attr("url");
        var newWindow = window.open('','_blank');
        newWindow.location = appUrl;
    } else {
        displayMessage();
    }
}

function displayMessage() {
    if (selectedApplicationRevision.status != APPLICATION_RUNNING) {
        if (selectedApplicationRevision.status == APPLICATION_STOPPED) {
            jagg.message({
                modalStatus: true,
                type: 'warning',
                timeout: 3000,
                content: "<b>The " + cloudSpecificApplicationRepresentation.toLowerCase() + " has been stopped. Start the " + cloudSpecificApplicationRepresentation.toLowerCase() + " before launching it.</b>"
            });
        } else if (selectedApplicationRevision.status == APPLICATION_INACTIVE) {
            jagg.message({
                modalStatus: true,
                type: 'warning',
                timeout: 3000,
                content: "<b>The " + cloudSpecificApplicationRepresentation.toLowerCase() + " has been stopped due to inactivity. Start the " + cloudSpecificApplicationRepresentation.toLowerCase() + " before launching it.</b>"
            });
        } else {
            jagg.message({
                modalStatus: true,
                type: 'error',
                timeout: 3000,
                content: "<b>Error has occurred while " + cloudSpecificApplicationRepresentation.toLowerCase() + " creation. If the problem persists please contact system administrator.</b>"
            });
        }
    }
}
/**
 * This function is to display a message to user to inform that the application is stopped due to
 * being idle for longer period.
 */
function displayApplicationInactiveMessage() {
    jagg.message({
                     modalStatus: true,
                     type: 'warning',
                     timeout: 15000,
                     content: "<b>This " + cloudSpecificApplicationRepresentation.toLowerCase() + " is stopped because 12 hours have passed after it was last started.</b></br>" +
                              "This is a limitation of free accounts in " + pageTitle + "</br> To restart, click the <b>Start</b>. button.</br>" +
                              "<a href='"+requestNewAppTypeURL+"' target='_blank'>Contact us</a> if you need any help."
                 });
}

function requestToExtend() {
    $("<a>").attr("href", requestNewAppTypeURL).attr("target", "_blank")[0].click();
}
function displayRestartCountMessage() {
    jagg.message({
        modalStatus: false,
        type: 'warning',
        timeout: 20000,
        content: "One or more of your replicas have a higher restart count than expected. Possible reasons are high usage of memory and / or cpu. " +
        "Please try using bigger containers if the problem persists."
    });
}

function listTags(){
    var tags = selectedApplicationRevision.tags;
    var tagListLength;
    if(tags) {
        tagListLength = tags.length;
    }
    var tagString = '';
    var tagTitleString = '';
    for(var i = 0; i < tagListLength; i++){
        if(i >= 3){
            break;
        }
        tagString += tags[i].labelName + " : " + tags[i].labelValue + "</br>";
        tagTitleString += tags[i].labelName + " : " + tags[i].labelValue + "\n";
    }
    if(tagListLength > 3) {
        tagString += "</br><a class='view-tag' href='/appmgt/site/pages/tags.jag?applicationKey=" + applicationKey
                             + "&versionKey=" + selectedApplicationRevision.hashId + "' title='View All envs'>View All Tags</a>";
    }

    $('#tag-list').html(tagString);
    $('#tag-list').prop('title', tagTitleString);
}

function listEnvs(){
    var envs = selectedApplicationRevision.runtimeProperties;
    var envListLength;
    if(envs) {
        envListLength = envs.length;
    }
    var envString = '';
    var envTitleString = '';
    for(var i = 0; i < envListLength; i++){
        if(i >= 3){
            break;
        }
        envString += envs[i].propertyName + " : " + envs[i].propertyValue + "</br>";
        envTitleString += envs[i].propertyName + " : " + envs[i].propertyValue + "\n";
    }
    if(envListLength > 3) {
        envString += "</br><a class='view-tag' href='/appmgt/site/pages/envs.jag?applicationKey=" + applicationKey
                             + "&versionKey=" + selectedApplicationRevision.hashId + "' title='View All envs'>View All envs</a>";
    }

    $('#env-list').html(envString);
    $('#env-list').prop('title', envTitleString);
}

// Icon initialization
function loadAppIcon() {

    application = getIconDetail(application);

    var iconDiv;
    if(application.icon){
        iconDiv = '<img id="app-icon"  src="data:image/bmp;base64,' + application.icon + '" width="100px"/>'
    } else {
        iconDiv = '<div class="app-icon" style="background:' + application.uniqueColor + '">' +
                  '<i class="fw ' + application.appTypeIcon + ' fw-4x" data-toggle="tooltip" ></i>' +
                  '</div>';
    }

    $("#app-icon").html(iconDiv);
}

function changeSelectedRevision(newRevision){
    // change app description
    //Changing revision dropdown
    putSelectedRevisionToSession(applicationKey, newRevision);
    $('#selected-version').html(newRevision+" ");
    $("#selectedRevision").val(newRevision);
    selectedRevision = newRevision;
    selectedApplicationRevision = application.versions[newRevision];
    //Changing deploymentURL
    var deploymentURL = selectedApplicationRevision.deploymentURL;
    var repoUrlHtml = generateLunchUrl(deploymentURL);
    $("#version-url-link").html(repoUrlHtml);
    $('#btn-launchApp').attr({url:deploymentURL});

    // Changing default launch URL
    var defaultAppLaunchURL = generateDefaultLaunchUrl();
    loadEndpointView();

    var dashboardUrl = dashboardBaseUrl + applicationName + "-" + newRevision;
    $('#btn-dashboard').attr({url:dashboardUrl});

    //changing app description
    $("#app-description").text(application.description?application.description:'');

    //changing runtime
    $("#runtime").html(selectedApplicationRevision.runtimeName);
    $("#memory").html(selectedApplicationRevision.conSpecMemory);
    $("#cpu").html(selectedApplicationRevision.conSpecCpu/1000);

    //change icon
    loadAppIcon();

    // Change replica status
    $("#tableStatus").html(selectedApplicationRevision.status);

    //Change Env Variables
    $("#leftMenuEnvVars").attr('href',"/appmgt/site/pages/envs.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#envVars").attr('href',"/appmgt/site/pages/envs.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#envVarsAdd").attr('href',"/appmgt/site/pages/envs.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#runtimePropCount").text(selectedApplicationRevision.runtimeProperties.length.toString());

    //Change Tags
    $("#leftMenuTagSet").attr('href',"/appmgt/site/pages/tags.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#tagSet").attr('href',"/appmgt/site/pages/tags.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#tagSetAdd").attr('href',"/appmgt/site/pages/tags.jag?applicationKey=" + applicationKey + "&versionKey=" + selectedApplicationRevision.hashId);
    $("#tagCount").text(selectedApplicationRevision.tags.length.toString());
    listTags();
    listEnvs();

    // Change version status in UI
    if(selectedApplicationRevision.status == APPLICATION_RUNNING){

        $('#launch-default-url-block').empty();
        $('#launch-default-url-block').html('<a id="launch-default-url-a" target="_blank" href="' + defaultAppLaunchURL + '">' + defaultAppLaunchURL + '</a>' +
            '<a href="/appmgt/site/pages/customurl.jag?applicationKey=' + applicationKey + '&selectedRevision='+ newRevision+ '"><i class="fw fw-settings"></i></a>');

        $('#version-url-link').empty();
        $('#version-url-link').html('<a id="launch-version-url-a" href="' + deploymentURL + '" target="_blank"><span>' + deploymentURL + '</span></a>');

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html(
                       '<div class="btn-group ctrl-edit-button btn-edit-code"><a type="button" ' +
                       'class="btn cu-btn cu-btn-md cu-btn-red" onclick="stopApplication();">Stop' +
                       '<span id="stop-in-progress"><span></a></div><div class="btn-group ctrl-edit-button btn-edit-code">' +
                       '<a type="button" class="btn cu-btn cu-btn-md cu-btn-gray" onclick="redeployApplication();">' +
                       'Redeploy<span id="redeploy-in-progress"><span></a></div>');

        $('.block-replica').empty();
        $('.block-replica').html('<h3>Replicas</h3><div class="block-replicas"><figure class="node-cicle" ' +
                                 'data-percent="100"><figcaption>01</figcaption><svg width="200" height="200">' +
                                 '<circle class="outer" cx="95" cy="95" r="85" transform="rotate(-90, 95, 95)"/></svg>' +
                                 '<a href="/appmgt/site/pages/runtimeLogs.jag?applicationKey=' + applicationKey + '&selectedRevision=' + newRevision +
                                 '"><span class="view-log">View Logs</span></a></figure></div><div class="block-replicas">' +
                                 '<figure class="node-cicle"><figcaption><span class="fw-stack fw-lg ">' +
                                 '<i class="fw fw-ring fw-stack-2x"></i><i class="fw fw-add fw-stack-1x" ' +
                                 'data-toggle="tooltip" title="Adding replicas to your ' + cloudSpecificApplicationRepresentation.toLowerCase() + ' will not support in this release."></i>' +
                                 '</span></figcaption></figure></div>');

    } else if(selectedApplicationRevision.status == APPLICATION_STOPPED || selectedApplicationRevision.status == APPLICATION_INACTIVE){

        $('#launch-default-url-block').empty();
        $('#launch-default-url-block').html('<a id="launch-default-url-a" target="_blank">' + defaultAppLaunchURL + '</a>');

        $('#version-url-link').empty();
        $('#version-url-link').html('<a id="launch-version-url-a" target="_blank"><span>' + deploymentURL + '</span></a>');

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html(
                       '<div class="btn-group ctrl-edit-button btn-edit-code"><a type="button" ' +
                       'class="btn cu-btn cu-btn-md cu-btn-blue" onclick="startApplication();">Start</a></div>');

        $('.block-replica').empty();
        $('.block-replica').html('<h3>Replicas</h3><div class="block-replicas"><figure class="node-cicle" data-percent="100">' +
                                 '<figcaption>01</figcaption><svg width="200" height="200"><circle class="outer" ' +
                                 'style="stroke: #ACAFAD;" cx="95" cy="95" r="85" transform="rotate(-90, 95, 95)"/></svg>' +
                                 '</figure></div><div class="block-replicas"><figure class="node-cicle"><figcaption>' +
                                 '<span class="fw-stack fw-lg "><i class="fw fw-ring fw-stack-2x"></i>' +
                                 '<i class="fw fw-add fw-stack-1x" data-toggle="tooltip"' +
                                 ' title="Adding replicas to your ' + cloudSpecificApplicationRepresentation.toLowerCase() + ' will not support in this release.">' +
                                 '</i></span></figcaption></figure></div>');
    } else {

        $('#launch-default-url-block').empty();
        $('#launch-default-url-block').html('<a id="launch-default-url-a" target="_blank">' + defaultAppLaunchURL + '</a>');

        $('#version-url-link').empty();
        $('#version-url-link').html('<a id="launch-version-url-a" target="_blank"><span>' + deploymentURL + '</span></a>');

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<div class="btn-group ctrl-edit-button btn-edit-code">' +
                                            '<a type="button" class="btn cu-btn cu-btn-md cu-btn-red" ' +
                                            'href="#yourlink">Error has occurred.</a></div>');

    }
    // Set upload revision btn
    var uploadRevisionUrl = appCreationPageBaseUrl+"?appTypeName="+application.applicationType +
                            "&applicationName="+applicationName + "&encodedLabels="+encodedLabels + "&encodedEnvs="
                                    + encodedEnvs + "&newVersion=true" + "&versionArray=" + encodeURI(versionArray);
    $('#upload-revision').attr("href", uploadRevisionUrl);

    changeRuntimeProps(selectedApplicationRevision);
    changeLabels(selectedApplicationRevision);
}

function generateLunchUrl(appURL, status) {
    var message = "";
    if(appURL) {
        if(status && status == APPLICATION_RUNNING) {
            message += "<a id='launch-version-url-a' target='_blank' href='" + appURL + "' >";
        } else {
            message += "<a id='launch-version-url-a' target='_blank' >";
        }
        message += "<span>";
        message += appURL;
        message += "</span>";
        message += "</a>";
    } else {
        message += "<i class='fw fw-deploy fw-1x'></i><span>" + cloudSpecificApplicationRepresentation + " is still deploying</span>";
    }
    return message;
}

function putSelectedRevisionToSession(applicationKey, selectedRevision){
    jagg.syncPost("../blocks/home/ajax/get.jag", {
        action: "putSelectedRevisionToSession",
        applicationKey: applicationKey,
        selectedRevision: selectedRevision
    });
}

function changeRuntimeProps(selectedApplicationRevision){
    $('#runtimePropCount').html(selectedApplicationRevision.runtimeProperties.length);
}

function changeLabels(selectedApplicationRevision){
    $('#labelCount').html(selectedApplicationRevision.tags.length);
}

// Uploading application icon
function submitChangeAppIcon(newIconObj) {
    var validated = validateIconImage(newIconObj.value, newIconObj.files[0].size);
    if(validated) {
        $('#changeAppIcon').submit();
    } else {
        jagg.message({content: "Only .jpg and .png file types are allowed for the the " + cloudSpecificApplicationRepresentation.toLowerCase() + "'s icon.", type: 'error', id:'notification'});
    }
}

// check the file is an image file
function validateIconImage(filename, fileSize) {
    var ext = getFileExtension(filename);
    var extStatus = false;
    var fileSizeStatus = true;
    switch (ext.toLowerCase()) {
        case 'jpg':
        case 'jpeg':
        case 'gif':
        case 'bmp':
        case 'png':
            extStatus = true;
            break;
        default:
            jagg.message({content: "Only jpg and png file types are allowed for the the " + cloudSpecificApplicationRepresentation.toLowerCase() + "'s icon.", type: 'error', id:'notification'});
            break;
    }

    if((fileSize/1024) > 51200 && extStatus == true) {
        fileSizeStatus = false;
        jagg.message({content: "Image file size should be less than 5MB", type: 'error', id:'notification'});
    }
    if(extStatus == true && fileSizeStatus == true) {
        return true;
    }
    return false;
}

// Utility Functions Goes Here
// extract file extension
function getFileExtension(filename) {
    var parts = filename.split('.');
    return parts[parts.length - 1];
}

// Delete Application
function deleteApplication(){

    $('#app_creation_progress_modal').modal({ backdrop: 'static', keyboard: false});
    $("#app_creation_progress_modal").show();
    $("#modal-title").text("Deleting the " + cloudSpecificApplicationRepresentation.toLowerCase() + " version...");

    jagg.post("../blocks/application/application.jag", {
        action:"deleteVersion",
        versionKey:selectedApplicationRevision.hashId,
        applicationName:applicationName
    },function (result) {
        jagg.message({content: "The selected version was successfully deleted.", type: 'success', id:'view_log'});
        var versionCount = getVersionCount();
        if(versionCount == 1){
            setTimeout(redirectAppListing, 2000);
        } else {
            setTimeout(redirectAppHome, 2000);
        }
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while deleting the selected " + cloudSpecificApplicationRepresentation.toLowerCase() + " version", type: 'error', id:'view_log'});
    });
}

function deleteApplicationPopUp(){
    var versionCount = getVersionCount();
    if(versionCount == 1){
        jagg.popMessage({type:'confirm', modalStatus: true, title:'Delete ' + cloudSpecificApplicationRepresentation + ' Version',content:'You are about to delete the only available version of your ' + cloudSpecificApplicationRepresentation.toLowerCase() + ', are you sure you want to delete this "' + selectedRevision + '" version ?',
            okCallback:function(){
                deleteApplication();
            }
        });
    } else if (versionCount > 1 && (selectedRevision == application.defaultVersion)) {
        jagg.message({
            type:'warning', modalStatus: true, title:'Delete ' + cloudSpecificApplicationRepresentation + ' Version',
            content:'This version:' + selectedRevision + ' is set as the default version of the ' + cloudSpecificApplicationRepresentation.toLowerCase() + '. If you '
            + 'want to delete this particular version, please select some other version as the default version',
            timeout: 8000
        });
    } else {
        jagg.popMessage({type:'confirm', modalStatus: true, title:'Delete ' + cloudSpecificApplicationRepresentation + ' Version',content:'Are you sure you want to delete this version:' + selectedRevision + ' ?',
            okCallback:function(){
                deleteApplication();
            }, cancelCallback:function(){}
        });
    }
}

function redirectAppListing() {
    window.location.replace("index.jag");
}

function redirectAppHome() {
    window.location.replace("home.jag?applicationKey=" + applicationKey);
}

function getVersionCount(){
    return Object.keys(application.versions).length;
}
