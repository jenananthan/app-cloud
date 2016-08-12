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
 *  software distributed under the License is distributed on anselectedRevision
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

var selectedRevisionLogMap = {};
var selectedRevisionReplicaList = [];
var selectedReplica;
var editor;
var isLogsAvailable = false;
var timerId;
var fullLogVal = "";
$(document).ready(function () {
    editor = CodeMirror.fromTextArea(document.getElementById("build-logs"), {
        styleActiveLine: true,
        lineNumbers: true,
        readOnly: true,
        searchonly: true,
        lineWrapping: true,
        theme:'icecoder'
    });
    initData(selectedRevision, true);
    timerId = setInterval(function(){ initData(selectedRevision, false); }, 3000);
    $('#noOfLines').on('change', function() {
      setLogArea(fullLogVal, true);
    });
});

function regerateReplicasList(selectedRevisionReplicaList) {
    $('#replicas').empty();
    for (var i = 0; i < selectedRevisionReplicaList.length; i++) {
        var $option = $('<option value="' + selectedRevisionReplicaList[i] + '">' + selectedRevisionReplicaList[i] + '</option>');
        if (i == 0) {
            selectedReplica = selectedRevisionReplicaList[i];
            $option.attr('selected', 'selected');
        }
        $('#replicas').append($option);
    }
}

function setLogArea(logVal, isFirstRequest){
    $('#build-logs').empty();
    editor.setValue("");
    if(!isFirstRequest) {
        var currentLog = $('#build-logs').val();
        fullLogVal = fullLogVal + logVal;
        logVal = currentLog + logVal;
    }
    var noOfLines = parseInt($("#noOfLines").val()) + 1;
    var logValArray = logVal.split(/\r?\n/);
    var startNumber = 0;
    logVal = "";
    if(logValArray.length > noOfLines){
        startNumber = logValArray.length - noOfLines;
    }
    for (i = startNumber; i < logValArray.length; i++) {
        logVal += logValArray[i];
        if(i != logValArray.length - 1){
            logVal = logVal + "\n";
        }
    }
    $('#build-logs').val(logVal);
    editor.setValue(logVal);
    var scroller = editor.getScrollInfo();
    editor.scrollTo(0, scroller.height);
    $('.log-search').focus();
}

function initData(selectedRevision, isFirstRequest){
    if(isFirstRequest){
        $('#replicas').empty();
        setLogArea("Loading...", true);
    }
    jagg.post("../blocks/runtimeLogs/ajax/runtimeLogs.jag", {
        action:"getSnapshotLogs",
        applicationKey:applicationKey,
        selectedRevision:selectedRevision,
        isFirstRequest:isFirstRequest
    },function (result) {
        initelements();
        result = result.replace(/\t+/g, "    ");
        selectedRevisionLogMap = jQuery.parseJSON(result);
        if(!jQuery.isEmptyObject(selectedRevisionLogMap)){
            $("#log-download").removeClass("btn-action btn disabled").addClass("btn-action");
            selectedRevisionReplicaList = Object.keys(selectedRevisionLogMap);
            if(isFirstRequest){
                regerateReplicasList(selectedRevisionReplicaList);
            }
            setLogArea(selectedRevisionLogMap[selectedRevisionReplicaList[0]], isFirstRequest);
        } else {
            //Check for application revision status and display correct message
            jagg.post("../blocks/runtimeLogs/ajax/runtimeLogs.jag", {
                action: "getApplicationRevisionStatus",
                applicationKey: applicationKey,
                selectedRevision: selectedRevision
            }, function(result) {
                result = result.trim();
                var revisionStatus = result;
                if (revisionStatus == APPLICATION_STOPPED) {
                    clearInterval(timerId);
                    jagg.message({
                        content: "The application is currently stopped. Please restart the application to see its logs.",
                        type: 'information',
                        id: 'view_log',
                        timeout: '20000'
                    });
                    setLogArea("Logs are unavailable as the application is stopped.", true);
                } else if (revisionStatus == APPLICATION_INACTIVE) {
                    clearInterval(timerId);
                    jagg.message({
                        content: "The application is stopped due to inactivity. Please restart it to see its logs.",
                        type: 'information',
                        id: 'view_log',
                        timeout: '20000'
                    });
                    setLogArea("Logs are unavailable as the application is stopped..", true);
                } else {
                    clearInterval(timerId);
                    jagg.message({
                        content: "Deployment is in progress. Please wait.",
                        type: 'information',
                        id: 'view_log',
                        timeout: '8000'
                    });
                }
            }, function(jqXHR, textStatus, errorThrown) {
                jagg.message({
                    content: "An error occurred while getting the application revision's status.",
                    type: 'error',
                    id: 'view_log'
                });
            });
        }
    },function (jqXHR, textStatus, errorThrown) {
        $('#revision').prop("disabled", false);
        jagg.message({content: "An error occurred while loading the logs.", type: 'error', id:'view_log'});
    });
}

function initelements(){
    //Url text loaded from the span element
    var urlText = $('.version-url a span').text();

    //Maximum character limit is 90. further than that the text would not show and the title would!
    if(urlText.length > 90){
        $('.version-url a').prop('title',urlText).find('span').text(urlText);
    }else{
        $('.version-url a span').text(urlText);
    }

    var revisionElement = $('#revision');
    revisionElement.prop("disabled", false);

    revisionElement.on('change', function (e) {
        selectedRevision = this.value;
        $(this).prop("disabled", true);
        initData(selectedRevision, true);
    });

    $('#replicas').on('change', function (e) {
        selectedReplica = this.value;
        setLogArea(selectedRevisionLogMap[selectedReplica],true);
    });

    $('#log-download').off('click').on('click', downloadLogs);
}

function downloadLogs(e) {
    $('#log-download').off('click');
    var modalBody = '<div class="container-fluid">'+
                        '<div class="row">'+
                            '<div id="progress_table" class="col-xs-12 col-md-12 section-title">' +
                                '<i class="fa fa-2x fa-circle-o-notch fa-spin"></i>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
    var table = "<table class='table' style='width:100%; color:black'>"
                + "<tr class='active'><td>Get logs from the server</td>"
                + "<td></td>" + "<td><i class=\"fa fa-circle-o-notch fa-spin\"></i></td></tr>" + "</table>";
    $("#log-download").removeClass("btn-action").addClass("btn-action btn disabled");
    $("#log_download_progress_modal_body").html(modalBody);
    $("#progress_table").html(table);
    $('#log_download_progress_modal').modal({ backdrop: 'static', keyboard: false});
    $("#log_download_progress_modal").show();
    jagg.post("../blocks/runtimeLogs/ajax/runtimeLogs.jag", {
        action:"downloadLogs",
        applicationKey:applicationKey,
        selectedRevision:selectedRevision
    },function (result) {
        result = result.replace(/\t+/g, "    ");
        selectedRevisionLogMap = jQuery.parseJSON(result);
        if(!jQuery.isEmptyObject(selectedRevisionLogMap)){
            table = "<table class='table' style='width:100%; color:black'>"
                    + "<tr class='success'><td>Get logs from the server</td>"
                    + "<td></td>" + "<td><i class=\"fa fa-check\"></i></td></tr>"
                    + "<tr class='active'><td>Generate a downloadable file</td>"
                    + "<td></td>" + "<td><i class=\"fa fa-circle-o-notch fa-spin\"></i></td></tr>"
                    + "</table>";
            $("#progress_table").html(table);
            selectedRevisionReplicaList = Object.keys(selectedRevisionLogMap);
            saveTextAsFile(selectedRevisionLogMap[selectedRevisionReplicaList[0]]);
        } else {
            $("#log_download_progress_modal").hide();
            jagg.message({content: "No logs found in the server.", type: 'information', id:'view_log'});
        }
    },function (jqXHR, textStatus, errorThrown) {
        $('#revision').prop("disabled", false);
        jagg.message({content: "An error occurred while downloading the logs.", type: 'error', id:'view_log'});
    });
}

function saveTextAsFile(textToWrite) {
    var textFileAsBlob = new Blob([textToWrite], {type:'text/plain'});
    var fileNameToSaveAs = applicationKey + "-" + selectedRevision + ".log";

    var downloadLink = document.createElement("a");
    downloadLink.download = fileNameToSaveAs;
    downloadLink.innerHTML = "Download File";
    downloadLink.href = window.URL.createObjectURL(textFileAsBlob);
    downloadLink.onclick = function (e){downloadLink.remove();};
    downloadLink.style.display = "none";
    document.body.appendChild(downloadLink);
    var table = "<table class='table' style='width:100%; color:black'>"
        + "<tr class='success'><td>Get logs from the server</td>"
        + "<td></td>" + "<td><i class=\"fa fa-check\"></i></td></tr>"
        + "<tr class='success'><td>Generate a downloadable file</td>"
        + "<td></td>" + "<td><i class=\"fa fa-check\"></i></td></tr>"
        + "</table>";
    $("#progress_table").html(table);
    $(".modal-backdrop").remove();
    $("#log_download_progress_modal").hide();
    downloadLink.click();
}
