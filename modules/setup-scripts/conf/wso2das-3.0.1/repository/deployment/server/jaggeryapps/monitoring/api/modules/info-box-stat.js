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
include('../constants.jag')
var helper = require('as-data-util.js');

function getInfoBoxMiniChartStatStat(conditions, facet, field, operation) {
    var output = [];
    var i;
    var results, result;
    var alias;

    alias = operation + '_' + field;

    results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", facet, [
        {
            "fieldName": field,
            "aggregate": operation,
            "alias": alias
        },
    ]);

    results = JSON.parse(results);

    for (i = 0; i < results.length; i++) {
        result = results[i];
        if (result) {
            result = result['values'];
            // push time and the values
            output.push([result[facet][0], result[alias].toFixed(0)]);
        }
    }

    return output;
}

function getDataForInfoBoxBarChart(type, conditions) {
    var startTime = helper.parseDate(request.getParameter('start_time'));
    var endTime = helper.parseDate(request.getParameter('end_time'));
    var timeDiff = 0;
    var i;
    var results, result;
    var operation, field;
    var arrList = [];

    if (request.getParameter('start_time') != null && request.getParameter('end_time') != null) {
        timeDiff = Math.abs((endTime.getTime() - startTime.getTime()) / 86400000);
    }

    operation = 'SUM';
    field = AVERAGE_REQUEST_COUNT;
    if (type == 'averageResponseTime') {
        operation = 'AVG';
        field = AVERAGE_RESPONSE_TIME;
    }

    if (timeDiff > 365) {
        results = getInfoBoxMiniChartStatStat(conditions, YEAR_FACET, field, operation);
    } else if (timeDiff > 30) {
        results = getInfoBoxMiniChartStatStat(conditions, MONTH_FACET, field, operation);
    } else if (timeDiff > 1) {
        results = getInfoBoxMiniChartStatStat(conditions, DAY_FACET, field, operation);
    } else {
        results = getInfoBoxMiniChartStatStat(conditions, HOUR_FACET, field, operation);
    }

    for (i = 0; i < results.length; i++) {
        result = results[i];
        var tempData = [];
        tempData[0] = i;
        tempData[1] = result[1];
        tempData[2] = result[0] + ' : ' + result[1];
        arrList.push(tempData);
    }

    return arrList;
}

function getInfoBoxRequestStat(conditions) {
    var log = new Log();
    var output = {};

    var results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", ALL_FACET, [
        {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + AVERAGE_REQUEST_COUNT
        }, {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "MIN",
            "alias": "MIN_" + AVERAGE_REQUEST_COUNT
        }, {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "MAX",
            "alias": "MAX_" + AVERAGE_REQUEST_COUNT
        }, {
            "fieldName": AVERAGE_REQUEST_COUNT,
            "aggregate": "AVG",
            "alias": "AVG_" + AVERAGE_REQUEST_COUNT
        }
    ]);

    log.info("result >>>>>>>>>>>>>>>> " + results);
    results = JSON.parse(results)[0];

    output['title'] = 'Total Requests';
    output['measure_label'] = 'Per min';

    if (results != null && results['values']['SUM_' + AVERAGE_REQUEST_COUNT] != null) {
        results = results['values'];
        output['total'] = results['SUM_' + AVERAGE_REQUEST_COUNT];
        output['max'] = results['MAX_' + AVERAGE_REQUEST_COUNT];
        output['avg'] = Math.round(results['AVG_' + AVERAGE_REQUEST_COUNT]);
        output['min'] = results['MIN_' + AVERAGE_REQUEST_COUNT]
    } else {
        output['total'] = output['max'] = output['avg'] = output['min'] = 'N/A';
    }
    
    output['graph'] = getDataForInfoBoxBarChart('averageRequestCount', conditions);
    
    print(output);
}

function getInfoBoxResponseStat(conditions) {
    var output = {};

    var results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", ALL_FACET, [
        {
            "fieldName": AVERAGE_RESPONSE_TIME,
            "aggregate": "MIN",
            "alias": "MIN_" + AVERAGE_RESPONSE_TIME
        }, {
            "fieldName": AVERAGE_RESPONSE_TIME,
            "aggregate": "MAX",
            "alias": "MAX_" + AVERAGE_RESPONSE_TIME
        }, {
            "fieldName": AVERAGE_RESPONSE_TIME,
            "aggregate": "AVG",
            "alias": "AVG_" + AVERAGE_RESPONSE_TIME
        }
    ]);

    results = JSON.parse(results)[0];

    output['title'] = 'Response Time';
    output['measure_label'] = 'ms';

    if (results != null && results['values']['MAX_' + AVERAGE_RESPONSE_TIME] != null) {
        results = results['values'];
        output['max'] = results['MAX_' + AVERAGE_RESPONSE_TIME];
        output['avg'] = Math.round(results['AVG_' + AVERAGE_RESPONSE_TIME]);
        output['min'] = results['MIN_' + AVERAGE_RESPONSE_TIME];
    } else {
        output['max'] = output['avg'] = output['min'] = 'N/A';
    }
    
    output['graph'] = getDataForInfoBoxBarChart('averageResponseTime', conditions);

    print(output);
}

function getInfoBoxSessionStat(conditions) {
    var output = {};

    var results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", ALL_FACET, [
        {
            "fieldName": SESSION_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + SESSION_COUNT
        }, {
            "fieldName": SESSION_COUNT,
            "aggregate": "AVG",
            "alias": "AVG_" + SESSION_COUNT
        }
    ]);

    if (results != null) {
        results = JSON.parse(results)[0];
    }
    output['title'] = 'Session';

    if (results != null && results['values']['SUM_' + SESSION_COUNT] != null) {
        results = results['values'];
        output['total'] = results['SUM_' + SESSION_COUNT];
        output['avg'] = Math.round(results['AVG_' + SESSION_COUNT]);
    } else {
        output['total'] = output['avg'] = 'N/A';
    }

    print(output);
}

function getInfoBoxErrorStat(conditions) {
    var output = {};

    var results = getAggregateDataFromDAS(REQUEST_SUMMARY_TABLE, conditions, "0", ALL_FACET, [
        {
            "fieldName": HTTP_SUCCESS_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + HTTP_SUCCESS_COUNT
        }, {
            "fieldName": HTTP_ERROR_COUNT,
            "aggregate": "SUM",
            "alias": "SUM_" + HTTP_ERROR_COUNT
        }
    ]);

    results = JSON.parse(results)[0];

    output['title'] = 'Errors';

    if (results != null && results['values']['SUM_' + HTTP_ERROR_COUNT] != null) {
        results = results['values'];
        output['total'] = results['SUM_' + HTTP_ERROR_COUNT];
        output['percentage'] = 
                (results['SUM_' + HTTP_ERROR_COUNT] * 100 / (results['SUM_' + HTTP_SUCCESS_COUNT] + results['SUM_' + HTTP_ERROR_COUNT])).toFixed(2) + '\x25';
    } else {
        output['total'] = output['percentage'] = 'N/A';
    }

    print(output);
}
