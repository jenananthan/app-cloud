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
                "show": false
            },
            "series": {
                "stack": true,
                "lines": {
                    "show": true,
                    "fill": true
                }
            },
            "colors": ["#3da0ea", "#bacf0b", "#e7912a", "#4ec9ce", "#f377ab", "#ec7337", "#bacf0b", "#f377ab", "#3da0ea", "#e7912a", "#bacf0b"],
            "grid": {
                "show": true,
                "aboveData": false,
                "color": "#aaa",
                "backgroundColor": "#FFFFFF",
                "labelMargin": 8,
                "axisMargin": null,
                "markings": null,
                "borderWidth": 0.2,
                "borderColor": "#aaa",
                "minBorderMargin": null,
                "clickable": false,
                "hoverable": true,
                "autoHighlight": true,
                "mouseActiveRadius": 0.1
            },
            "yaxis": {

                "show": true,
                "position": "left",
                "mode": null,

                "color": null,
                "tickColor": null,

                "font": null,
                "min": null,
                "max": null,
                "autoscaleMargin": 0.05,

                "transform": null,
                "inverseTransform": null,

                "ticks": null,
                "tickLength": 0,
                "tickDecimals": 0,
                "tickFormatter": null,
                "tickLength": null,

                "labelWidth": null,
                "labelHeight": null,
                "reserveSpace": null,

                "axisLabel": "",
                "axisLabelUseCanvas": true,
                "axisLabelFontSizePixels": 14,
                "axisLabelFontFamily": "Arial",
                "axisLabelPadding": 5,

                "panRange": null,
                "zoomRange": false
            },
            "xaxis": {

                "show": true,
                "position": "bottom",
                "mode": "time",

                "color": null,
                "tickColor": null,

                "font": null,
                "min": null,
                "max": null,
                "autoscaleMargin": 0.1,

                "transform": null,
                "inverseTransform": null,

                "ticks": null,
                "tickDecimals": null,
                "tickFormatter": null,
                "tickLength": null,

                "labelWidth": null,
                "labelHeight": null,
                "reserveSpace": null,

                "axisLabel": "Time",
                "axisLabelUseCanvas": true,
                "axisLabelFontSizePixels": 12,
                "axisLabelFontFamily": "Arial",
                "axisLabelPadding": 5,

                "panRange": null,

                "rotateTicks": 0
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
        }
    }
}());
