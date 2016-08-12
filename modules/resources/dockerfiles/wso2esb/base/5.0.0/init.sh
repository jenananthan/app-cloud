#!/usr/bin/env bash

CARBON_HOME_PATH=/opt/wso2esb-5.0.0
#remove default java opts
sed -i '/-Xms256m/d' $CARBON_HOME_PATH/bin/wso2server.sh

sed -i "/port=\"9763\"/a  \\\t\t   proxyPort=\"80\"" $CARBON_HOME_PATH/repository/conf/tomcat/catalina-server.xml
sed -i "/port=\"9443\"/a  \\\t\t   proxyPort=\"443\"" $CARBON_HOME_PATH/repository/conf/tomcat/catalina-server.xml

#Remove sample
rm -rf $CARBON_HOME_PATH/repository/deployment/server/axis2services/*
rm -rf $CARBON_HOME_PATH/repository/deployment/server/webapps/*

#Calculate max heap size and the perm size for Java Opts
#Check whether TOTAL_MEMORY env variable defined or and not empty
if [[ $TOTAL_MEMORY && ${TOTAL_MEMORY-_} ]]; then
    let MAX_HEAP_SIZE=$TOTAL_MEMORY/512*256
    let PERM_SIZE=$TOTAL_MEMORY/512*64
    JAVA_OPTS="-Xms128m -Xmx"$MAX_HEAP_SIZE"m -XX:PermSize="$PERM_SIZE"m"
    export JAVA_OPTS=$JAVA_OPTS
fi

$CARBON_HOME_PATH/bin/wso2server.sh
