/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.appcloud.tierapi.bean;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * Class to implement the Description Plan details
 */

@XmlRootElement
public class Plan {

	private int id;
	private String planName;
	private int maxApplications;
	private int maxDatabases;

	public Plan() {

	}

	public Plan(String planName, int maxApplications) {
		this.planName = planName;
		this.maxApplications = maxApplications;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getPlanName() {
		return planName;
	}

	public void setMaxApplications(int maxApplications) {
		this.maxApplications = maxApplications;
	}

	public int getMaxApplications() {
		return maxApplications;
	}

	public int getMaxDatabases() {
		return maxDatabases;
	}

	public void setMaxDatabases(int maxDatabases) {
		this.maxDatabases = maxDatabases;
	}
}
