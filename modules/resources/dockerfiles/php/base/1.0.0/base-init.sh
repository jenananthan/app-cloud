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
echo "Creating SSL certificates"
openssl genrsa -out /etc/ssl/private/${KEY_NAME}.key 1024

openssl req  -new -newkey rsa:4096 -days 365 -nodes -config /crt-config.cnf -subj "/" -keyout /etc/ssl/private/${KEY_NAME}.key -out /etc/ssl/private/${KEY_NAME}.csr  && \
    openssl x509 -req -days 365 -in /etc/ssl/private/${KEY_NAME}.csr -signkey /etc/ssl/private/${KEY_NAME}.key -out /etc/ssl/private/${KEY_NAME}.crt

cat /etc/ssl/private/${KEY_NAME}.crt /etc/ssl/private/${KEY_NAME}.key \
           |  tee /etc/ssl/certs/${KEY_NAME}.pem

