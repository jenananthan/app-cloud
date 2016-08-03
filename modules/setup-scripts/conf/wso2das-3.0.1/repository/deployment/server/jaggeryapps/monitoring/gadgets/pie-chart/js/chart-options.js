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
(function () {

    chartConfigs = function () {
        return {
            "legend": {
                "show": false,
                "labelFormatter": null,
                "labelBoxBorderColor": "#E9E8E8",
                "noColumns": 1,
                "position": "ne",
                "backgroundColor": "#FFFFFF",
                "backgroundOpacity": 0.8,
                "container": null
            },
            "series": {
                "pie": {
                    "show": true,
                    "radius": 0.75,
                    "innerRadius": 0,
                    "label": {
                        "show": true
                    }
                }
            },
            "colors": ["#005a32", "#238b45", "#41ab5d", "#74c476", "#a1d99b", "#c7e9c0", "#edf8e9"],
            "grid": {
                "show": true,
                "aboveData": false,
                "color": "#000000",
                "backgroundColor": "#333333",
                "labelMargin": 8,
                "axisMargin": null,
                "markings": null,
                "borderWidth": 0.5,
                "borderColor": "#FFFFFF",
                "minBorderMargin": null,
                "clickable": false,
                "hoverable": false,
                "autoHighlight": true,
                "mouseActiveRadius": 0.1
            },
            "pan": {
                "interactive": true
            },
            "zoom": {
                "interactive": true
            },
            "selection": {
                "mode": null
            }

        };
    };
}());

