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

package org.wso2.appcloud.provisioning.runtime;

/**
 * This class will maintains Kubernetes related constants.
 */
public class KubernetesPovisioningConstants {

    public static final String PROPERTY_KUB_MASTER_URL = "KubernetesClusterConfig.MasterURL";
    public static final String PROPERTY_KUB_API_VERSION = "KubernetesClusterConfig.APIVersion";

    public static String PROPERTY_MASTER_USERNAME = "KubernetesClusterConfig.UserName";
    public static String PROPERTY_MASTER_PASSWORD = "KubernetesClusterConfig.Password";

    public static final String INGRESS_API_NAMESPACE_RESOURCE_PATH = "apis/extensions/v1beta1/namespaces/";
    public static final String INGRESS_API_RESOURCE_PATH_SUFFIX = "/ingresses/";
    public static final String MIME_TYPE_JSON = "application/json";

    public static final String KIND_NAMESPACE = "Namespace";
    public static final String KIND_DEPLOYMENT = "Deployment";
    public static final String KIND_INGRESS = "Ingress";
    public static final String KIND_SERVICE = "Service";
    public static final String KIND_SECRETS = "Secret";
    public static final String KIND_POD = "Pod";
    public static final String KIND_REPLICATION_CONTROLLER = "ReplicationController";

    public static final String ITEMS = "items";
    public static final String SPEC = "spec";
    public static final String RULES = "rules";
    public static final String HOST = "host";

    public static final String VOLUME_MOUNT = "default";
    public static final String VOLUME_MOUNT_PATH = "/etc/";

    public static final String DEFAULT_INGRESS_PATH = "/";

    public static final String ANNOTATION_KEY_HOST = "serviceloadbalancer/lb.host";
    public static final String ANNOTATION_KEY_SSL_TERM = "serviceloadbalancer/lb.sslTerm";
    public static final String ANNOTATION_VALUE_SSL_TERM = "true";
    public static final String SERVICE_SESSION_AFFINITY_MODE = "ClientIP";
    public static final int HTTPS_SERVICE_PORT = 443;

    public static final String IMAGE_PULL_POLICY_ALWAYS = "Always";

    public static final String POD_DELETE_TIMEOUT = "Pod.Delete.Timeout";
    public static final String POD_DELETE_WAIT = "Pod.Delete.Wait";

    public static final String DEPLOYMENT_DELETE_TIMEOUT = "Deployment.Delete.Timeout";
    public static final String DEPLOYMENT_DELETE_WAIT = "Deployment.Delete.Wait";

    public static final String RC_DELETE_TIMEOUT = "RC.Delete.Timeout";
    public static final String RC_DELETE_WAIT = "RC.Delete.Wait";

    public static final String INGRESS_DELETE_TIMEOUT = "Ingress.Delete.Timeout";
    public static final String INGRESS_DELETE_WAIT = "Ingress.Delete.Wait";

    public static final String SERVICE_DELETE_TIMEOUT = "Service.Delete.Timeout";
    public static final String SERVICE_DELETE_WAIT = "Service.Delete.Wait";

    public static final String SECRET_DELETE_TIMEOUT = "Secret.Delete.Timeout";
    public static final String SECRET_DELETE_WAIT = "Secret.Delete.Wait";

}