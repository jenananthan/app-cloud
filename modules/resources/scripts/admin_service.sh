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

ACTION=$1
LOGFILE=admin_service.log
source admin_service.cfg

echo "----------Login to admin service----------" >> $LOGFILE
curl -c cookies -v -X POST -k $SERVICE_URL/appmgt/site/blocks/user/login/ajax/login.jag -d "action=login&userName=$ADMIN_USERNAME&password=$ADMIN_PASSWORD" >> $LOGFILE 2>&1
echo -e "\n" >> $LOGFILE

if [ $ACTION = "help" ]; then
    echo "----------Sample admin service calls----------"
    echo -e "\n"
    echo "***For white list application version***"
    echo "./admin_service.sh whiteListAppVersion <tenant> <app> <version>"
    echo -e "\n"
    echo "***For list all version of the application***"
    echo "./admin_service.sh getApplicationVersions <tenant> <app>"
    echo -e "\n"
    echo "***For white list tenant, set max app count and max database count***"
    echo "./admin_service.sh whiteListTenant <tenant> <max-app-count> <max-database-count> <cloud-type>"
    echo -e "\n"
    echo "***For white list tenant and set max app count***"
    echo "./admin_service.sh whiteListMaximumApplicationCount <tenant> <max-app-count> <cloud-type>"
    echo -e "\n"
    echo "***For white list tenant and set max database count***"
    echo "./admin_service.sh whiteListMaximumDatabaseCount <tenant> <max-database-count> <cloud-type>"
    echo -e "\n"
    echo "***For view tenant max application count***"
    echo "./admin_service.sh getTenantMaxAppCount <tenant> <cloud-type>"
    echo -e "\n"
    echo "***For view tenant max database count***"
    echo "./admin_service.sh getTenantMaxDatabaseCount <tenant> <cloud-type>"
    echo -e "\n"
    echo "***For update application version container specification***"
    echo "./admin_service.sh updateConSpec <tenant> <app> <version> <cpu> <memory>"
    echo -e "\n"
elif [ $ACTION = "whiteListAppVersion" ]; then
    echo "----------White list application version----------" >> $LOGFILE
    curl -b cookies  -v -X POST -k $SERVICE_URL/appmgt/site/blocks/admin/admin.jag -d "action=whiteListApplicationVersion&tenantDomain=$2&applicationName=$3&applicationRevision=$4" >> $LOGFILE 2>&1
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "whiteListTenant" ]; then
    echo "----------White list tenant----------" >> $LOGFILE
    curl -b cookies  -v -X POST -k $SERVICE_URL/appmgt/site/blocks/admin/admin.jag -d "action=whiteListTenant&tenantDomain=$2&maxAppCount=$3&maxDatabaseCount=$4&cloudType=$5" >> $LOGFILE 2>&1
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "whiteListMaximumApplicationCount" ]; then
    echo "----------White list maximum application count----------" >> $LOGFILE
    curl -b cookies  -v -X POST -k $SERVICE_URL/appmgt/site/blocks/admin/admin.jag -d "action=whiteListMaximumApplicationCount&tenantDomain=$2&maxAppCount=$3&cloudType=$4" >> $LOGFILE 2>&1
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "whiteListMaximumDatabaseCount" ]; then
    echo "----------White list maximum database count----------" >> $LOGFILE
    curl -b cookies  -v -X POST -k $SERVICE_URL/appmgt/site/blocks/admin/admin.jag -d "action=whiteListMaximumDatabaseCount&tenantDomain=$2&maxDatabaseCount=$3&cloudType=$4" >> $LOGFILE 2>&1
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "getApplicationVersions" ]; then
    echo "----------List application versions----------" >> $LOGFILE
    curl -b cookies  -v -X GET -k "$SERVICE_URL/appmgt/site/blocks/admin/admin.jag?action=getApplicationVersions&tenantDomain=$2&applicationName=$3" | tee -a $LOGFILE 2>&1 | less
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "getTenantMaxAppCount" ]; then
    echo "----------Tenant max application count----------" >> $LOGFILE
    curl -b cookies  -v -X GET -k "$SERVICE_URL/appmgt/site/blocks/admin/admin.jag?action=getTenantMaxAppCount&tenantDomain=$2&cloudType=$3" | tee -a $LOGFILE 2>&1 | less
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "getTenantMaxDatabaseCount" ]; then
    echo "----------Tenant max database count----------" >> $LOGFILE
    curl -b cookies  -v -X GET -k "$SERVICE_URL/appmgt/site/blocks/admin/admin.jag?action=getTenantMaxDatabaseCount&tenantDomain=$2&cloudType=$3" | tee -a $LOGFILE 2>&1 | less
    echo -e "\n" >> $LOGFILE
elif [ $ACTION = "updateConSpec" ]; then
    echo "----------Update container specification----------" >> $LOGFILE
    curl -b cookies -v -X POST -k $SERVICE_URL/appmgt/site/blocks/admin/admin.jag -d "action=updateConSpec&tenantDomain=$2&applicationName=$3&applicationRevision=$4&memory=$5&cpu=$6" >> $LOGFILE 2>&1
    echo -e "\n" >> $LOGFILE
fi
