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
var defaultPalette = [
    "#1abc9c", "#16a085", "#2ecc71", "#27ae60",
    "#3498db", "#2980b9", "#9b59b6", "#8e44ad",
    "#34495e", "#2c3e50", "#f1c40f", "#f39c12",
    "#e67e22", "#d35400", "#e74c3c", "#c0392b"
];

var getIconDetail = function(application){
    var appName = application.applicationName;
    var appType = application.applicationType;

    if (appName) {
        var str = appName.trim().split(" ");
        try {
            var firstChar = str[0].charAt(0);
            var secondChar = str[1].charAt(0).toLowerCase();
        } catch (err) {
            if (typeof secondChar === 'undefined') {
                if (str.length > 2){
                    secondChar =str[0].charAt(1);
                } else {
                    secondChar = "";
                }
            }
        }

        application.nameToChar = firstChar + secondChar;
        application.uniqueColor = getColorCode(application.nameToChar, appType);
    }
    return application;
};

var getColorCode = function (nameToChar, type) {
    var colorArr = nameToChar.split('');
    var typeArr = type.split('');

    var intValue = 0;
    for (i = 0; i < typeArr.length; i++) {
        intValue += typeArr[i].charCodeAt(0);
    }
    for (var i = 0; i < colorArr.length; i++) {
        intValue += colorArr[i].charCodeAt(0);
    }
    intValue = (intValue % defaultPalette.length);
    return defaultPalette[Math.round(intValue)];
};
