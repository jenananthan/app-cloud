/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
(function (server) {
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext,
        MultitenantConstants = Packages.org.wso2.carbon.utils.multitenancy.MultitenantConstants,
        TenantUtils = Packages.org.wso2.carbon.utils.TenantUtils,
        context = PrivilegedCarbonContext.getCurrentContext(),
        realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService'),
        tenantManager = realmService.getTenantManager();

    server.tenantDomain = function (options) {
        if (!options) {
            return context.getTenantDomain();
        }
        if (options.username) {
            return TenantUtils.getTenantDomain(options.username);
        }
        if (options.url) {
            return TenantUtils.getTenantDomainFromRequestURL(options.url);
        }
        return null;
    };

    server.tenantId = function (options) {
        var domain = options ? (options.domain || server.tenantDomain(options)) : server.tenantDomain();
        return domain ? tenantManager.getTenantId(domain) : null;
    };

    server.superTenant = {
        tenantId: MultitenantConstants.SUPER_TENANT_ID,
        domain: MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
    };

}(server));