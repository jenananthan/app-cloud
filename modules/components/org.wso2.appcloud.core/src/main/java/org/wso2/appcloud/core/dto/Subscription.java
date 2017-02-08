/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.appcloud.core.dto;

public class Subscription {
    private int tenantId;
    private String plan;
    private int maxApplicationCount;
    private int maxDatabaseCount;
    private String cloudType;
    private int maxReplicaCount;
    private int maxMemory;
    private int maxCpu;
    private String startDate;
    private String endDate;
    private int isWhiteListed;
    private String status;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getMaxApplicationCount() {
        return maxApplicationCount;
    }

    public void setMaxApplicationCount(int maxApplicationCount) {
        this.maxApplicationCount = maxApplicationCount;
    }

    public int getMaxDatabaseCount() {
        return maxDatabaseCount;
    }

    public void setMaxDatabaseCount(int maxDatabaseCount) {
        this.maxDatabaseCount = maxDatabaseCount;
    }

    public String getCloudType() {
        return cloudType;
    }

    public void setCloudType(String cloudType) {
        this.cloudType = cloudType;
    }

    public int getMaxReplicaCount() {
        return maxReplicaCount;
    }

    public void setMaxReplicaCount(int maxReplicaCount) {
        this.maxReplicaCount = maxReplicaCount;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getMaxCpu() {
        return maxCpu;
    }

    public void setMaxCpu(int maxCpu) {
        this.maxCpu = maxCpu;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getIsWhiteListed() {
        return isWhiteListed;
    }

    public void setIsWhiteListed(int isWhiteListed) {
        this.isWhiteListed = isWhiteListed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
