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
$(document).ready(function() {
    // add upload app icon listener
    $("#change_app_icon").change(function(event) {
        submitChangeAppIcon(this);
    });
    initPageView();
    var nextVersion = generateNextPossibleVersion(application.versions);
    var uploadRevisionUrl = appCreationPageBaseUrl+"?appTypeName="+application.applicationType +
                            "&applicationName="+applicationName + "&encodedLabels="+encodedLabels + "&encodedEnvs="
                                    + encodedEnvs + "&newVersion=true&nextVersion=" + nextVersion + "&conSpecCpu=" + conSpecCpu + "&conSpecMemory=" + conSpecMemory;
    $('#upload-revision').attr("href", uploadRevisionUrl);

    if(selectedApplicationRevision.status==APPLICATION_INACTIVE){
        displayApplicationInactiveMessage();
    }
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

    if (selectedApplicationRevision.status == APPLICATION_RUNNING) {
        loadEndpoints(deploymentURL, applicationType);
    }

}

function loadEndpoints(deploymentURL, applicationType) {
    jagg.post("../blocks/application/application.jag", {
     action: "loadEndpoints",
     appType: applicationType,
     deploymentURL: deploymentURL
     }, function(result) {
        var endpoints = JSON.parse(result);
        var html_1 = '<div class="block-endpoints">'+
            '<h3>Endpoints</h3>'+
            '<h4>SOAP Services</h4>'+
            '<table class="table table-responsive">'+
            '<thead class="thead">'+
            '<tr>'+
            '<th>Name</th>'+
            '<th>WSDL</th>'+
            '<th>WSDL2</th>'+
            '</tr>'+
            '</thead>'+
            '<tbody>';

        var html_2;
        for (var i=0; i < endpoints.data.urls.proxies.length; i++) {
            var proxy = endpoints.data.urls.proxies[i];
            html_2 = '<tr>'+
                '<td>' + proxy.name+'</td>'+
                '<td><a href="' +proxy.wsdl[1]+ '">'+proxy.wsdl[1] +'</a></td>'+
                '<td><a href="'+proxy.wsdl[0]+'">'+proxy.wsdl[0]+'</a></td>'+
                '</tr>';
        }

        var html_3 =
            '</tbody>'+
            '</table>'+

            '<h4>REST APIs</h4>'+
            '<table class="table table-responsive">'+
            '<thead class="thead">'+
            '<tr>'+
            '<th>Name</th>'+
            '<th>URL</th>'+
            '</tr>'+
            '</thead>'+
            '<tbody>';


        var html_4;
        for (var j=0; j < endpoints.data.urls.apis.length; j++) {
            var api = endpoints.data.urls.apis[j];
            var url = deploymentURL + api.context;
            if (api.name != "ContainerAPI") {

                html_4 = '<tr>' +
                    '<td>'+ api.name +'</td>'+
                    '<td><a href="' + url+ '">' + url + '</a></td>'+
                    '</tr>';
            }
        }

        var html_5 =       '</tbody>'+
            '</table>'+
            '</div>';

        $("#app-type-data").html(html_1 + html_2 + html_3 + html_4 + html_5);

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
                content: "<b>Application has been stopped. Start the application before launch.</b>"
            });
        } else if (selectedApplicationRevision.status == APPLICATION_INACTIVE) {
            jagg.message({
                modalStatus: true,
                type: 'warning',
                timeout: 3000,
                content: "<b>Application has been stopped due to inactivity. Start the application before launch.</b>"
            });
        } else {
            jagg.message({
                modalStatus: true,
                type: 'error',
                timeout: 3000,
                content: "<b>Error has occurred while application creation. If the problem persists please contact system administrator.</b>"
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
                     content: "<b>This application has been stopped due to inactivity for more than 24 hours</b></br>" +
                              "This is a limitation of free accounts in " + pageTitle + "</br> To restart, click the <b>Start</b>. button.</br>" +
                              "Click the Support menu to contact us if you need any help."
                 });
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
    for(var i = 0; i < tagListLength; i++){
        if(i >= 3){
            break;
        }
        tagString += tags[i].labelName + " : " + tags[i].labelValue + "</br>";
    }
    if(tagListLength > 3) {
        tagString += "</br><a class='view-tag' href='/appmgt/site/pages/tags.jag?applicationKey=" + applicationKey
                             + "&versionKey=" + selectedApplicationRevision.hashId + "'>View All Tags</a>";
    }

    $('#tag-list').html(tagString);
}

function listEnvs(){
    var envs = selectedApplicationRevision.runtimeProperties;
    var envListLength;
    if(envs) {
        envListLength = envs.length;
    }
    var envString = '';
    for(var i = 0; i < envListLength; i++){
        if(i >= 3){
            break;
        }
        envString += envs[i].propertyName + " : " + envs[i].propertyValue + "</br>";
    }
    if(envListLength > 3) {
        envString += "</br><a class='view-tag' href='/appmgt/site/pages/envs.jag?applicationKey=" + applicationKey
                             + "&versionKey=" + selectedApplicationRevision.hashId + "'>View All envs</a>";
    }

    $('#env-list').html(envString);
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

    var dashboardUrl = dashboardBaseUrl + applicationName + "-" + newRevision;
    $('#btn-dashboard').attr({url:dashboardUrl});

    //changing app description
    $("#app-description").text(application.description?application.description:'');

    //changing runtime
    $("#runtime").html(selectedApplicationRevision.runtimeName);

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
        $('#launch-default-url-block').html('<a id="launch-default-url-a" target="_blank" href="' + deploymentURL + '">' + deploymentURL + '</a>');

        $('#version-url-link').empty();
        $('#version-url-link').html('<a id="launch-version-url-a" href="' + deploymentURL + '" target="_blank"><span><b>URL : </b>' + deploymentURL + '</span></a>');

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<button class="cu-btn cu-btn-md cu-btn-gr-dark btn-launch-app" id="btn-launchApp"' +
                       'url="' + deploymentURL + '">Launch App</button>' +
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
                                 'data-toggle="tooltip" title="Adding replicas to your application will not support in this release."></i>' +
                                 '</span></figcaption></figure></div>');

        loadEndpoints(deploymentURL, applicationType);
    } else if(selectedApplicationRevision.status == APPLICATION_STOPPED || selectedApplicationRevision.status == APPLICATION_INACTIVE){

        $('#launch-default-url-block').empty();
        $('#launch-default-url-block').html('<a id="launch-default-url-a" target="_blank">' + deploymentURL + '</a>');

        $('#version-url-link').empty();
        $('#version-url-link').html('<a id="launch-version-url-a" target="_blank"><span><b>URL : </b>' + deploymentURL + '</span></a>');

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<button class="cu-btn cu-btn-md cu-btn-gr-dark btn-launch-app" id="btn-launchApp"' +
                       'url="' + deploymentURL + '">Launch App</button>' +
                       '<div class="btn-group ctrl-edit-button btn-edit-code"><a type="button" ' +
                       'class="btn cu-btn cu-btn-md cu-btn-blue" onclick="startApplication();">Start</a></div>');

        $('.block-replica').empty();
        $('.block-replica').html('<h3>Replicas</h3><div class="block-replicas"><figure class="node-cicle" data-percent="100">' +
                                 '<figcaption>01</figcaption><svg width="200" height="200"><circle class="outer" ' +
                                 'style="stroke: #ACAFAD;" cx="95" cy="95" r="85" transform="rotate(-90, 95, 95)"/></svg>' +
                                 '</figure></div><div class="block-replicas"><figure class="node-cicle"><figcaption>' +
                                 '<span class="fw-stack fw-lg "><i class="fw fw-ring fw-stack-2x"></i>' +
                                 '<i class="fw fw-add fw-stack-1x" data-toggle="tooltip"' +
                                 ' title="Adding replicas to your application will not support in this release.">' +
                                 '</i></span></figcaption></figure></div>');
    } else {

        $('#launch-default-url-block').empty();
        $('#launch-default-url-block').html('<a id="launch-default-url-a" target="_blank">' + deploymentURL + '</a>');

        $('#version-url-link').empty();
        $('#version-url-link').html('<a id="launch-version-url-a" target="_blank"><span><b>URL : </b>' + deploymentURL + '</span></a>');

        $('#version-app-launch-block').empty();
        $('#version-app-launch-block').html('<div class="btn-group ctrl-edit-button btn-edit-code">' +
                                            '<a type="button" class="btn cu-btn cu-btn-md cu-btn-red" ' +
                                            'href="#yourlink">Error has occurred.</a></div>');

    }
    // Set upload revision btn
    var uploadRevisionUrl = appCreationPageBaseUrl+"?appTypeName="+application.applicationType + //"&applicationName="+applicationName;
                            "&applicationName="+applicationName + "&encodedLabels="+encodedLabels + "&encodedEnvs="
                                    + encodedEnvs + "&newVersion=true&nextVersion=" + nextVersion;
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
        message += "<b>URL : </b>";
        message += appURL;
        message += "</span>";
        message += "</a>";
    } else {
        message += "<i class='fw fw-deploy fw-1x'></i><span>Application is still deploying</span>";
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
        jagg.message({content: "Invalid image selected for application icon. Please select a valid image.", type: 'error', id:'notification'});
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
            jagg.message({content: "Invalid image selected for application icon. Please select a valid image.", type: 'error', id:'notification'});
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
    $("#modal-title").text("Deleting...");

    jagg.post("../blocks/application/application.jag", {
        action:"deleteVersion",
        versionKey:selectedApplicationRevision.hashId
    },function (result) {
        jagg.message({content: "Selected version deleted successfully", type: 'success', id:'view_log'});
        var versionCount = 0;
        for (var version in application.versions){
            versionCount++;
        }
        if(versionCount == 1){
            setTimeout(redirectAppListing, 2000);
        } else {
            setTimeout(redirectAppHome, 2000);
        }
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while deleting the selected application version", type: 'error', id:'view_log'});
    });
}

function deleteApplicationPopUp(){
    jagg.popMessage({type:'confirm', modalStatus: true, title:'Delete Application Version',content:'Are you sure you want to delete this version:' + selectedRevision + ' ?',
                        okCallback:function(){
                            deleteApplication();
                        }, cancelCallback:function(){}
                    });
}

function redirectAppListing() {
    window.location.replace("index.jag");
}

function redirectAppHome() {
    window.location.replace("home.jag?applicationKey=" + applicationKey);
}
