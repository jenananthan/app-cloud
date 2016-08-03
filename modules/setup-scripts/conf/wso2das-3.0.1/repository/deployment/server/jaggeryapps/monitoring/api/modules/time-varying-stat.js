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

// This map holds the field name, operation and alias and for request count, response time and error count
var parameterMapping = {
    'request': ['avg_request_count', 'AVG', 'AVG_avg_request_count'],
    'response': ['avg_response_time', 'AVG', 'AVG_avg_response_time'],
    'error': ['http_error_count','SUM', 'AVG_avg_error_count']
};

function getTimeVaryingStatData(conditions, mappedParameters) {
    results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", TIME_FACET, [{
        "fieldName": mappedParameters[0],
        "aggregate": mappedParameters[1],
        "alias": mappedParameters[2]
    }
    ]);
    
    return results;
}

function getTimeVaryingStat(conditions, type, color) {
    var dataArray = [];
    var i;
    var row;
    var mappedParameters = parameterMapping[type];
    
    var results = JSON.parse(getTimeVaryingStatData(conditions, mappedParameters));
    var chartOptions = {};

    for (i = 0; i < results.length; i++) {
        row = results[i]['values'];
        var time = new Date(String(row[TIME_FACET][0]).replace(' ', 'T') + ':00.000Z').getTime();
        dataArray.push([Number(time).toPrecision(), row[mappedParameters[2]]]);
    }

    // sorting the results
    dataArray.sort(function (a, b) {
        return Number(a[0]) - Number(b[0]);
    });
    
    if (color != null) {
        chartOptions = {
            'colors': [color]
        }
    }

    print([
        {'series1': {'label': 's', 'data': dataArray}}, chartOptions
    ]);
}