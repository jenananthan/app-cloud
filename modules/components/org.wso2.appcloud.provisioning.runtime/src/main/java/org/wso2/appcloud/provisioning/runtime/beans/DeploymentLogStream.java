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

package org.wso2.appcloud.provisioning.runtime.beans;

import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.provisioning.runtime.RuntimeProvisioningException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class DeploymentLogStream {

	private static final Log log = LogFactory.getLog(DeploymentLogStream.class);

    private Map<String, BufferedReader> deploymentLogs;

	private Map<String, LogWatch> watches;

    public void setDeploymentLogs(Map<String, BufferedReader> deploymentLogs) {
        this.deploymentLogs = deploymentLogs;
    }

    public Map<String, BufferedReader> getDeploymentLogs() {
        return deploymentLogs;
    }

	public Map<String, LogWatch> getWatches() {
		return watches;
	}

	public void setWatches(Map<String, LogWatch> watches) {
		this.watches = watches;
	}

    public void closeAllLogStreams() throws IOException {
	    for (Map.Entry<String, LogWatch> entry : this.watches.entrySet()) {
		    entry.getValue().close();
	    }
        for (Map.Entry <String, BufferedReader> entry : this.deploymentLogs.entrySet()) {
            entry.getValue().close();
        }
    }

	public String getLogContent(String replicaName) throws RuntimeProvisioningException {
		BufferedReader reader = deploymentLogs.get(replicaName);
		if(reader == null){
			String msg = "Log stream for the replica name cannot be found.";
			log.error(msg);
			throw new RuntimeProvisioningException(msg);
		}
		try {
			StringBuilder stringBuilder = new StringBuilder();

			String line = null;
			boolean isReady = false;
			try {
				isReady = reader.ready();
			} catch (IOException e) {
				//Ignore exception
				isReady = false;
			}
			if (isReady) {
				line = reader.readLine();
			}
			while (line != null) {
				stringBuilder.append(line);
				stringBuilder.append("\n");
				try {
					isReady = reader.ready();
				} catch (IOException e) {
					//Ignore exception
					isReady = false;
				}
				if (!isReady) {
					break;
				}
				line = reader.readLine();
			}
			return stringBuilder.toString();
		} catch (IOException e) {
			String msg = "Error occurred while reading log line from the log stream.";
			log.error(msg, e);
			throw new RuntimeProvisioningException(msg, e);
		}
	}

	public void closeLogStream(String replicaName) throws RuntimeProvisioningException {
		BufferedReader reader = deploymentLogs.get(replicaName);
		if(reader == null){
			String msg = "Log stream for the replica name cannot be found.";
			log.error(msg);
			throw new RuntimeProvisioningException(msg);
		}
		try {
			deploymentLogs.get(replicaName).close();
		} catch (IOException e) {
			String msg = "Error occurred while closing the log stream.";
			log.error(msg, e);
			throw new RuntimeProvisioningException(msg, e);
		}
	}
}

