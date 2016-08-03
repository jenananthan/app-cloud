/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

include('../db.jag');
include('../constants.jag');

function getPastStat(conditions, endTime, timePeriod) {
    var results, result;
    var output = {};
    var i;
    var reg;
    var startTime;
    
    // replace the time condition in the query
    reg = /\d+ TO \d+/;
    startTime = endTime - timePeriod;

    conditions = conditions.replace(reg, startTime + ' TO ' + endTime);
    
    results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", FULL_APP_NAME_FACET, [
        {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "AVG",
            "alias": "AVG_" + AVERAGE_REQUEST_COUNT
        }
    ]);

    results = JSON.parse(results);

    for (i = 0; i < results.length; i++) {
        result = results[i]['values'];
        output[result[FULL_APP_NAME_FACET][0]] = Math.round(result['AVG_' + AVERAGE_REQUEST_COUNT]);
    }

    return output;
}

function matchPastStatWithApp(webappName, pastDataArray) {
    var i, len;
    for (i = 0, len = pastDataArray.length; i < len; i++) {
        if (pastDataArray[i]['webappName'] == webappName) {
            return pastDataArray[i]['averageRequestCount'];
        }
    }
    return '-';
}

function getTableHeadings() {
    return [
        'Application / Service',
        'Type',
        {
            'parent': 'Average request count',
            'sub': ['Last minute', 'Last hour', 'Last day']
        },
        'Total number of requests',
        'Percentage error'
    ];
}

function getAppsStat(conditions, endTime) {
    var appList = [];
    var tempArray = [];
    var i;
    var lastMinute, lastHour, lastDay, apps;
    var app = {};
    var webappName;
    var key;
    var results;

    apps = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", FULL_APP_NAME_FACET, [
        {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + AVERAGE_REQUEST_COUNT
        }, {
            "fieldName": HTTP_SUCCESS_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + HTTP_SUCCESS_COUNT
        }, {
            "fieldName": HTTP_ERROR_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + HTTP_ERROR_COUNT
        }
    ]);
    
    lastMinute = getPastStat(conditions, endTime, 60);
    lastHour = getPastStat(conditions, endTime, 3600);
    lastDay = getPastStat(conditions, endTime, 86400);

    var log = new Log();
    log.info("apps >>>>>>>>>>  " + apps);
    apps = JSON.parse(apps);
    
    for (i = 0; i < apps.length; i++) {
        results = apps[i]['values'];

        webappName = results[FULL_APP_NAME_FACET][0];
        
        app['webappName'] = webappName;
        app['webappType'] = 'Microservice';
        app['lastMinute'] = lastMinute[webappName] || '-';
        app['lastHour'] = lastHour[webappName] || '-';
        app['lastDay'] = lastDay[webappName] || '-';
        app['totalRequests'] = results['SUM_' + AVERAGE_REQUEST_COUNT];
        app['percentageError'] = (results['SUM_' + HTTP_ERROR_COUNT] / results['SUM_' + HTTP_SUCCESS_COUNT]).toFixed(2);

        tempArray = [];
        for (key in app) {
            if (app.hasOwnProperty(key)) {
                tempArray.push(app[key]);
            }
        }
        appList.push(tempArray);
    }

    print({
        'data': appList,
        'headings': getTableHeadings(),
        'orderColumn': ['1'],
        'applist': 'true'
    });

}
