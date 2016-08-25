#!/bin/sh
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

# This script build initial base docker images to be used in app cloud setup.
CURRENT_DIR=`pwd`

#msf4j base image
docker build --no-cache -t wso2-appcloud/msf4j:1.0.0-base -f $CURRENT_DIR/msf4j/base/1.0.0/Dockerfile.wso2-appcloud-msf4j-1.0.0.base $CURRENT_DIR/msf4j/base/1.0.0/
docker build --no-cache -t wso2-appcloud/msf4j:2.0.0-base -f $CURRENT_DIR/msf4j/base/2.0.0/Dockerfile.wso2-appcloud-msf4j-2.0.0.base $CURRENT_DIR/msf4j/base/2.0.0/

#carbon 4.2.0 base image
docker build --no-cache -t wso2-appcloud/carbon:4.2.0-base -f $CURRENT_DIR/carbon/base/1.0.0/Dockerfile.wso2-appcloud-carbon-4.2.0.base $CURRENT_DIR/carbon/base/1.0.0/

#php base image
docker build --no-cache -t wso2-appcloud/php:1.0.0-base -f $CURRENT_DIR/php/base/1.0.0/Dockerfile.wso2-appcloud-php-1.0.0.base $CURRENT_DIR/php/base/1.0.0

#tomcat base images
docker build --no-cache -t wso2-appcloud/wso2as:6.0.0-m1-base -f $CURRENT_DIR/wso2as/base/6.0.0-m1/Dockerfile.wso2-appcloud-wso2as-6.0.0-m1.base $CURRENT_DIR/wso2as/base/6.0.0-m1
docker build --no-cache -t wso2-appcloud/wso2as:6.0.0-m2-base -f $CURRENT_DIR/wso2as/base/6.0.0-m2/Dockerfile.wso2-appcloud-wso2as-6.0.0-m2.base $CURRENT_DIR/wso2as/base/6.0.0-m2
docker build --no-cache -t wso2-appcloud/wso2as:6.0.0-m3-base -f $CURRENT_DIR/wso2as/base/6.0.0-m3/Dockerfile.wso2-appcloud-wso2as-6.0.0-m3.base $CURRENT_DIR/wso2as/base/6.0.0-m3

#jaggery base image
docker build --no-cache -t wso2-appcloud/jaggery:0.11.0-base -f $CURRENT_DIR/jaggery/base/0.11.0/Dockerfile.wso2-appcloud-jaggery-0.11.0.base $CURRENT_DIR/jaggery/base/0.11.0

#wso2dataservice base image
docker build --no-cache -t wso2-appcloud/wso2dataservice:3.5.0-base -f $CURRENT_DIR/wso2dataservice/base/3.5.0/Dockerfile.wso2-appcloud-wso2dataservice-3.5.0.base $CURRENT_DIR/wso2dataservice/base/3.5.0

#wso2esb base image
docker build --no-cache -t wso2-appcloud/wso2esb:5.0.0-base -f $CURRENT_DIR/wso2esb/base/5.0.0/Dockerfile.wso2-appcloud-wso2esb-5.0.0.base $CURRENT_DIR/wso2esb/base/5.0.0
