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

function getContextAllRequests(conditions) {
    
    var results = getAggregateDataFromDAS(CONTEXT_TABLE, conditions, "0", ALL_FACET, [
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

function getTrafficStatData(conditions) {
    var output = [];
    var total_request_count;
    var results, result;

    total_request_count = getContextAllRequests(conditions);

    if (total_request_count < 0) {
        return;
    }

    results = getAggregateDataFromDAS(CONTEXT_TABLE, conditions, "0", CONTEXT_FACET, [
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
            output.push([result[CONTEXT_FACET][0], result['SUM_' + AVERAGE_REQUEST_COUNT],
                         (result['SUM_' + AVERAGE_REQUEST_COUNT] * 100 / total_request_count).toFixed(2)]);
        }
    }

    return output;
}

function getTrafficStat(conditions, tableHeadings, sortColumn) {
    var dataArray = [];
    var i;
    var results, result;

    dataArray = getTrafficStatData(conditions);

    print({
        'data': dataArray,
        'headings': tableHeadings,
        'orderColumn': [sortColumn, 'desc']
    });
}