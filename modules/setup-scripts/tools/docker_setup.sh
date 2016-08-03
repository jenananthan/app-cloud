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
echo "$(tput setaf 2)Enter the Domain name :$(tput sgr0)(e.g.:- registry.docker.appfactory.private.wso2.com:5000) "
read domain

echo "$(tput setaf 2)Enter the Host Port of the Remote Registry :$(tput sgr0)(e.g. :- 5000) "
read hostPort

sudo openssl s_client -showcerts -connect $domain:$hostPort </dev/null 2>/dev/null|openssl x509 -outform PEM >$domain.crt


sudo cp $domain.crt /usr/local/share/ca-certificates/$domain.crt
sudo cp $domain.crt /etc/ssl/certs/$domain.crt

# update certificcates
sudo update-ca-certificates

#update docker file
sudo sed -i '$ a DOCKER_OPTS="--insecure-registry '$domain:$hostPort'"' /etc/default/docker

#restart the docker service
sudo service docker stop
sudo service docker start

echo "$(tput setaf 2)Enter the IP of the Remote Host :"$(tput sgr0)"(e.g. :- 192.168.16.2) "
read remoteIP


# add host entry to domain
sudo sed -i "$ a $remoteIP $domain" /etc/hosts
echo "$(tput setaf 3)Updated the host entry$(tput sgr0)"

