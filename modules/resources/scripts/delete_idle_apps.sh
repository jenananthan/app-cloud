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

SERVICE_URL=https://10.100.7.115:9443
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"
NUMBER_OF_HOURS=2
LOGFILE=delete_idle_apps.log

echo "----------Login to admin service----------" >> $LOGFILE
curl -c cookies -v -X POST -k $SERVICE_URL/appmgt/site/blocks/user/login/ajax/login.jag -d "action=login&userName=$ADMIN_USERNAME&password=$ADMIN_PASSWORD" >> $LOGFILE 2>&1
echo -e "\n" >> $LOGFILE
echo "----------Delete all idle applications----------" >> $LOGFILE
curl -b cookies  -v -X POST -k $SERVICE_URL/appmgt/site/blocks/admin/admin.jag -d "action=stopIdleApplications&numberOfHours=$NUMBER_OF_HOURS" >> $LOGFILE 2>&1
echo -e "\n" >> $LOGFILE