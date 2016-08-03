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

#This script is run as a cronjob and it will clean up application server node where applications are created in app cloud.

#delete older docker images.
docker rmi -f $(docker images | grep -v "base" | grep -E 'hours ago | days ago | weeks ago' | grep -E 'registry.cloudstaging.wso2.com | <none>' | awk '{print $3}')

#delete files older than 5 days on app creation temp directory. Make sure to update direcotry path of tmpUploadedApps directory
find /mnt/xxx.xxx.xxx.xxx/wso2as-5.2.1/repository/deployment/server/jaggeryapps/appmgt/tmpUploadedApps/ -mtime +5 -type f -exec rm {} \;

