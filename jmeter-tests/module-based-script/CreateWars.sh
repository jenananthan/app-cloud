#!/bin/bash
# ------------------------------------------------------------------------
#
# Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
#   WSO2 Inc. licenses this file to you under the Apache License,
#   Version 2.0 (the "License"); you may not use this file except
#   in compliance with the License.
#   You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing,
#   software distributed under the License is distributed on an
#   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#   KIND, either express or implied.  See the License for the
#   specific language governing permissions and limitations
#   under the License.
#
# ------------------------------------------------------------------------
# $1 - file path
# $2 - request number
# $3 - file name

mkdir $1\/$2
cd $1\/$2
cp $1\/$3 .
unzip -d tmp $3
cd tmp
sed -i -e "s/<h1>My first PHP page<\/h1>/<h1>My first PHP page Load Testing $2<\/h1>/g" index.php
zip -r $1\/$2.zip *
rm -r $1\/$2
