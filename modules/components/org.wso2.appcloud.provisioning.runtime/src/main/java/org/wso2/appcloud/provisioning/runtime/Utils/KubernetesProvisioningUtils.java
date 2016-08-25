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

package org.wso2.appcloud.provisioning.runtime.Utils;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.client.AutoAdaptableKubernetesClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.common.util.AppCloudUtil;
import org.wso2.appcloud.provisioning.runtime.KubernetesPovisioningConstants;
import org.wso2.appcloud.provisioning.runtime.RuntimeProvisioningException;
import org.wso2.appcloud.provisioning.runtime.beans.ApplicationContext;
import org.wso2.appcloud.provisioning.runtime.beans.TenantInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This will have the utility methods to provision kubernetes.
 */

public class KubernetesProvisioningUtils {
    private static final Log log = LogFactory.getLog(KubernetesProvisioningUtils.class);

    /**
     * This method will create a common Kubernetes client object with authentication to the Kubernetes master server.
     *
     * @return Kubernetes client object
     */
    public static AutoAdaptableKubernetesClient getFabric8KubernetesClient() {

        AutoAdaptableKubernetesClient kubernetesClient = null;

        Config config = new Config();
        config.setUsername(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.PROPERTY_MASTER_USERNAME));
        config.setPassword(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.PROPERTY_MASTER_PASSWORD));
        config.setNoProxy(
                new String[] { AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.PROPERTY_KUB_MASTER_URL) });
        config.setMasterUrl(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.PROPERTY_KUB_MASTER_URL));
        config.setApiVersion(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.PROPERTY_KUB_API_VERSION));
        kubernetesClient = new AutoAdaptableKubernetesClient(config);

        return kubernetesClient;
    }

    /**
     * This utility method will generate the namespace of the current application context.
     *
     * @param applicationContext context of the current application
     * @return namespace of the current application context
     */
    public static Namespace getNameSpace(ApplicationContext applicationContext) {

        // todo: consider constraints of 24 character limit in namespace.
        String ns = applicationContext.getTenantInfo().getTenantDomain();
        ns = ns.replace(".", "-").toLowerCase();
        ObjectMeta metadata = new ObjectMetaBuilder()
                .withName(ns)
                .build();
        return new NamespaceBuilder()
                .withMetadata(metadata)
                .build();
    }

    /**
     * This utility method will provide the list of pods for particular application.
     *
     * @param applicationContext context of the current application
     * @return list of pods related for the current application context
     */
    public static PodList getPods (ApplicationContext applicationContext){

        Map<String, String> selector = getLableMap(applicationContext);
        AutoAdaptableKubernetesClient kubernetesClient = getFabric8KubernetesClient();
        PodList podList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata()
                .getName()).pods().withLabels(selector).list();
        return podList;
    }

	/**
     * This utility method will provide the list of deployments for particular application.
     *
     * @param applicationContext
     * @return
     */
    public static DeploymentList getDeployments (ApplicationContext applicationContext){

        Map<String, String> selector = getLableMap(applicationContext);
        AutoAdaptableKubernetesClient kubernetesClient = getFabric8KubernetesClient();
        DeploymentList deploymentList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata().getName())
                                                        .extensions().deployments().withLabels(selector).list();
        return deploymentList;
    }

	/**
     * This utility method will provide the list of replication controllers for particular application.
     *
     * @param applicationContext
     * @return
     */
    public static ReplicationControllerList getReplicationControllers (ApplicationContext applicationContext){

        Map<String, String> selector = getLableMap(applicationContext);
        AutoAdaptableKubernetesClient kubernetesClient = getFabric8KubernetesClient();
        ReplicationControllerList rcList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata().getName())
                                                           .replicationControllers().withLabels(selector).list();
        return rcList;
    }

	/**
     * This utility method will provide the list of ingresses for particular application.
     *
     * @param applicationContext
     * @return
     */
    public static IngressList getIngresses (ApplicationContext applicationContext){

        Map<String, String> selector = getLableMap(applicationContext);
        AutoAdaptableKubernetesClient kubernetesClient = getFabric8KubernetesClient();
        IngressList ingressList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata().getName())
                                                  .extensions().ingress().withLabels(selector).list();
        return ingressList;
    }

	/**
     * This utility method will provide the list of secrets for particular application.
     *
     * @param applicationContext
     * @return
     */
    public static SecretList getSecrets (ApplicationContext applicationContext){

        Map<String, String> selector = getLableMap(applicationContext);
        AutoAdaptableKubernetesClient kubernetesClient = getFabric8KubernetesClient();
        SecretList secretList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata().getName())
                                                .secrets().withLabels(selector).list();
        return secretList;
    }

    /**
     * This utility method will provide the list of services for particular application.
     *
     * @param applicationContext context of the current application
     * @return list of services related for the current application context
     */
    public static ServiceList getServices(ApplicationContext applicationContext){
        Map<String, String> selector = getLableMap(applicationContext);
        AutoAdaptableKubernetesClient kubernetesClient = getFabric8KubernetesClient();
        ServiceList serviceList = kubernetesClient.inNamespace(getNameSpace(applicationContext).getMetadata()
                .getName()).services().withLabels(selector).list();
        return serviceList;
    }
    /**
     * This utility method will generate the appropriate selector for filter out the necessary kinds for particular application.
     *
     * @param applicationContext context of the current application
     * @return selector which can be use to filter out certain kinds
     */
    public static Map<String, String> getLableMap(ApplicationContext applicationContext) {

        //todo generate a common selector valid for all types of application
        Map<String, String> selector = new HashMap<>();
        selector.put("app", applicationContext.getId());
        selector.put("version", applicationContext.getVersion());
        selector.put("versionHashId", applicationContext.getVersionHashId());
        selector.put("type", applicationContext.getType());
        return selector;
    }

    public static Map<String, String> getDeleteLables(ApplicationContext applicationContext){

        Map<String, String> deleteLables = new HashMap<>();
        deleteLables.put("versionHashId", applicationContext.getVersionHashId());

        return deleteLables;
    }

    /**
     * Generate a unique name for an ingress.
     * @param domain
     * @return generated unique name for ingress (appName-appVersion-service)
     */
    public static String createIngressMetaName(String domain){

        return (domain).replace(".","-").toLowerCase();
    }

    /**
     * This method generates the application context
     * @param appName application name
     * @param version application version
     * @param type application type
     * @param tenantId relevant tenant id
     * @param tenantDomain relevant tenant domain
     * @param versionHashId hash id of the application
     * @return application context
     */
    public static ApplicationContext getApplicationContext(String appName, String version, String type, int tenantId,
            String tenantDomain, String versionHashId) {

        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setId(getKubernetesValidAppName(appName));
        applicationContext.setVersion(version);
        applicationContext.setType(type);
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setTenantId(tenantId);
        tenantInfo.setTenantDomain(tenantDomain);
        applicationContext.setTenantInfo(tenantInfo);
        applicationContext.setVersionHashId(versionHashId);
        return applicationContext;
    }

    /**
     * This method converts Kubernetes valid application name
     * @param applicationName application name provided by user
     * @return kubernetes valid application name
     */
    public static String getKubernetesValidAppName(String applicationName){
        if(applicationName == null || applicationName.isEmpty()){
            return null;
        }
        applicationName = applicationName.replaceAll("[^a-zA-Z0-9]+", "-");
        return applicationName;
    }

    /**
     * This util will generate the deployment path for application.
     *
     * @param applicationContext application context
     * @return
     */
    public static String getDeploymentPath(ApplicationContext applicationContext){

        String tenantDomain = applicationContext.getTenantInfo().getTenantDomain();
        String appId = applicationContext.getId();
        String appVersion = applicationContext.getVersion();

        return "/" + tenantDomain + "/webapps/" + appId + "-" + appVersion;
    }

    /**
     * This utility method will return the pod status an application.
     *
     * @param applicationContext application context object
     * @return
     */
    public static String getPodStatus(ApplicationContext applicationContext){
        //todo have to fix this for multiple replicas (this only works for one replica)
        PodList podList = getPods(applicationContext);
        String status = "Created";
        if (podList.getItems().size() > 0) {
            for (Pod pod : podList.getItems()) {
                status = KubernetesHelper.getPodStatusText(pod);
                /*if (!"Running".equals(status)) {
                    return false;
                }*/
            }
        }
        return status;

    }

    public static boolean waitForPodToGetDeleted(ApplicationContext applicationContext) throws RuntimeProvisioningException {
        boolean isPodDeleted = false;
        try {
            //Maximum waiting time to check if the pod gets completely deleted (in milliseconds)
            int timeOut = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.POD_DELETE_TIMEOUT));
            //Waiting time between calls made to check if pod has been deleted (in milliseconds)
            int waitTimePeriod = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.POD_DELETE_WAIT));

            for(int podDeleteWaitCounter = 0; podDeleteWaitCounter < timeOut; podDeleteWaitCounter += waitTimePeriod){
                java.lang.Thread.sleep(waitTimePeriod);
                List<Pod> podList = getPods(applicationContext).getItems();
                if ((podList != null) && (podList.size() == 0)) {
                    isPodDeleted = true;
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg = "Error while waiting until delete pods list for application: " + applicationContext.getId() + " and version: " + applicationContext.getVersion();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        }

        return isPodDeleted;
    }

    public static boolean waitForDeploymentToGetDeleted(ApplicationContext applicationContext) throws RuntimeProvisioningException {
        boolean isDeploymentDeleted = false;
        try {
            //Maximum waiting time to check if the deployement gets completely deleted (in milliseconds)
            int timeOut = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.DEPLOYMENT_DELETE_TIMEOUT));
            //Waiting time between calls made to check if deployement has been deleted (in milliseconds)
            int waitTimePeriod = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.DEPLOYMENT_DELETE_WAIT));

            for(int deploymentDeleteWaitCounter = 0; deploymentDeleteWaitCounter < timeOut; deploymentDeleteWaitCounter += waitTimePeriod){
                java.lang.Thread.sleep(waitTimePeriod);
                List<Deployment> deploymentList = getDeployments(applicationContext).getItems();
                if ((deploymentList != null) && (deploymentList.size() == 0)) {
                    isDeploymentDeleted = true;
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg = "Error while waiting until delete deployment list for application: " + applicationContext.getId() + " and version: " + applicationContext.getVersion();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        }

        return isDeploymentDeleted;
    }

    public static boolean waitForRCToGetDeleted(ApplicationContext applicationContext) throws RuntimeProvisioningException {
        boolean isRCDeleted = false;
        try {
            //Maximum waiting time to check if the RC gets completely deleted (in milliseconds)
            int timeOut = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.RC_DELETE_TIMEOUT));
            //Waiting time between calls made to check if RC has been deleted (in milliseconds)
            int waitTimePeriod = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.RC_DELETE_WAIT));

            for(int rcDeleteWaitCounter = 0; rcDeleteWaitCounter < timeOut; rcDeleteWaitCounter += waitTimePeriod){
                java.lang.Thread.sleep(waitTimePeriod);
                List<ReplicationController> replicationControllerList = getReplicationControllers(applicationContext).getItems();
                if ((replicationControllerList != null) && (replicationControllerList.size() == 0)) {
                    isRCDeleted = true;
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg = "Error while waiting until delete replication controller list for application: " + applicationContext.getId() + " and version: " + applicationContext.getVersion();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        }

        return isRCDeleted;
    }

    public static boolean waitForServiceToGetDeleted(ApplicationContext applicationContext) throws RuntimeProvisioningException {
        boolean isServiceDeleted = false;
        try {
            //Maximum waiting time to check if the service gets completely deleted (in milliseconds)
            int timeOut = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.SERVICE_DELETE_TIMEOUT));
            //Waiting time between calls made to check if service has been deleted (in milliseconds)
            int waitTimePeriod = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.SERVICE_DELETE_WAIT));

            for(int serviceDeleteWaitCounter = 0; serviceDeleteWaitCounter < timeOut; serviceDeleteWaitCounter += waitTimePeriod){
                java.lang.Thread.sleep(waitTimePeriod);
                List<Service> serviceList = getServices(applicationContext).getItems();
                if ((serviceList != null) && (serviceList.size() == 0)) {
                    isServiceDeleted = true;
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg = "Error while waiting until delete services list for application: " + applicationContext.getId() + " and version: " + applicationContext.getVersion();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        }

        return isServiceDeleted;
    }

    public static boolean waitForIngressesToGetDeleted(ApplicationContext applicationContext) throws RuntimeProvisioningException {
        boolean isIngressDeleted = false;
        try {
            //Maximum waiting time to check if the ingress gets completely deleted (in milliseconds)
            int timeOut = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.INGRESS_DELETE_TIMEOUT));
            //Waiting time between calls made to check if ingress has been deleted (in milliseconds)
            int waitTimePeriod = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.INGRESS_DELETE_WAIT));

            for(int ingressDeleteWaitCounter = 0; ingressDeleteWaitCounter < timeOut; ingressDeleteWaitCounter += waitTimePeriod){
                java.lang.Thread.sleep(waitTimePeriod);
                List<Ingress> ingressList = getIngresses(applicationContext).getItems();
                if ((ingressList != null) && (ingressList.size() == 0)) {
                    isIngressDeleted = true;
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg = "Error while waiting until delete ingresses list for application: " + applicationContext.getId() + " and version: " + applicationContext.getVersion();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        }

        return isIngressDeleted;
    }

    public static boolean waitForSecretToGetDeleted(ApplicationContext applicationContext) throws RuntimeProvisioningException {
        boolean isSecretDeleted = false;
        try {
            //Maximum waiting time to check if the secrets gets completely deleted (in milliseconds)
            int timeOut = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.SECRET_DELETE_TIMEOUT));
            //Waiting time between calls made to check if secrets has been deleted (in milliseconds)
            int waitTimePeriod = Integer.parseInt(AppCloudUtil.getPropertyValue(KubernetesPovisioningConstants.SECRET_DELETE_WAIT));

            for(int secretDeleteWaitCounter = 0; secretDeleteWaitCounter < timeOut; secretDeleteWaitCounter += waitTimePeriod){
                java.lang.Thread.sleep(waitTimePeriod);
                List<Secret> secretList = getSecrets(applicationContext).getItems();
                if ((secretList != null) && (secretList.size() == 0)) {
                    isSecretDeleted = true;
                    break;
                }
            }
        } catch (InterruptedException e) {
            String msg = "Error while waiting until delete secrets list for application: " + applicationContext.getId() + " and version: " + applicationContext.getVersion();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        }

        return isSecretDeleted;
    }
}
