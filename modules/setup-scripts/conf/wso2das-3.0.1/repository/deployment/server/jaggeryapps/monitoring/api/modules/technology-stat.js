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
var helper = require('as-data-util.js');

function getHttpStatusAllRequests(conditions) {
    var results = getAggregateDataFromDAS(HTTP_STATUS_TABLE, conditions, "0", ALL_FACET, [
        {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + AVERAGE_REQUEST_COUNT
        }
    ]);

    results = JSON.parse(results);

    if (results.length > 0) {
        return results[0]['values']['SUM_' + AVERAGE_REQUEST_COUNT];
    }
}

function getHttpStatusStatData(conditions) {
    var output = [];
    var i, total_request_count;
    var results, result;

    total_request_count = getHttpStatusAllRequests(conditions);
    
    if(total_request_count <= 0){
        return;
        
    }
    results = getAggregateDataFromDAS(HTTP_STATUS_TABLE, conditions, "0", HTTP_STATUS_FACET, [
        {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + AVERAGE_REQUEST_COUNT
        }
    ]);

    results = JSON.parse(results);

    if (results.length > 0) {
        for (i = 0; i < results.length; i++) {
            result = results[i]['values'];
            output.push([result[HTTP_STATUS_FACET], result['SUM_' + AVERAGE_REQUEST_COUNT], 
                         (result['SUM_' + AVERAGE_REQUEST_COUNT]*100/total_request_count).toFixed(2)]);
        }
    }
    
    return output;
}

function getHttpStatusStat(conditions) {
    var dataArray = [];
    var ticks = [];
    var i;
    var opt;
    var total_request_count = 0;
    var results, result;

    results = getHttpStatusStatData(conditions);

    if (results.length > 0) {
        for (i = 0; i < results.length && (i < 5); i++) {
            result = results[i];
            dataArray.push([i, result[1]]);
            ticks.push([i, result[0]]);
        }
    }

    chartOptions = {
        'xaxis': {
            'ticks': ticks,
            'axisLabel': 'Top 5 HTTP Response Codes'
        },
        'yaxis': {
            'axisLabel': 'Number of requests'
        }
    };

    print([
        {'series1': {'label': 's', 'data': dataArray}},
        chartOptions
    ]);
}

function getHttpStatusTabularStat(conditions, tableHeadings, sortColumn) {
    print(helper.getTabularData(getHttpStatusStatData(conditions), tableHeadings, sortColumn));
}