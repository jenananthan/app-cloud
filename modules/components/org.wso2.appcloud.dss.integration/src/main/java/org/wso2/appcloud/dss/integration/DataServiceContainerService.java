/**
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.appcloud.dss.integration;

import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a axis2service to retrieve DSS endpoints
 */
public class DataServiceContainerService {

    public static final String SERVICE_ADMIN_URL = "https://localhost:9443/services/ServiceAdmin";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "ADMIN_PASSWORD";
    public static final String SERVICE_FILTER_TYPE = "data_service";
    public static final String SERVICE_SEARCH_STRING = "";

    public List<String> getEndpoints() throws Exception {
        ServiceAdminStub serviceAdminStub = new ServiceAdminStub(SERVICE_ADMIN_URL);
        String adminPassword = System.getenv(ADMIN_PASSWORD);
        CarbonUtils.setBasicAccessSecurityHeaders(ADMIN_USERNAME, adminPassword, serviceAdminStub._getServiceClient());
        ServiceMetaDataWrapper wrapper = serviceAdminStub.listServices(SERVICE_FILTER_TYPE, SERVICE_SEARCH_STRING, -1);
        ServiceMetaData[] serviceMetaDatas = wrapper.getServices();
        List<String> serviceNames = new ArrayList<String>();
        for (ServiceMetaData metaData : serviceMetaDatas) {
            if (metaData.getServiceType().equals(SERVICE_FILTER_TYPE)) {
                serviceNames.add(metaData.getName());
            }
        }
        return serviceNames;
    }
}
