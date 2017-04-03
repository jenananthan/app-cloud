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

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.*;
import io.fabric8.kubernetes.client.AutoAdaptableKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PrettyLoggable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.appcloud.provisioning.runtime.Utils.KubernetesProvisioningUtils;
import org.wso2.appcloud.provisioning.runtime.beans.*;
import org.wso2.appcloud.provisioning.runtime.beans.Container;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This class will implement the runtime provisioning service specific to Kubernetes.
 */
public class KubernetesRuntimeProvisioningService implements RuntimeProvisioningService {

    private static final Log log = LogFactory.getLog(KubernetesRuntimeProvisioningService.class);
    private ApplicationContext applicationContext;
    private Namespace namespace;
    private ResourceQuotaLimit resourceQuotaLimit;

    public KubernetesRuntimeProvisioningService(ApplicationContext applicationContext, ResourceQuotaLimit resourceQuotaLimit) {
        this.applicationContext = applicationContext;
        this.namespace = KubernetesProvisioningUtils.getNameSpace(applicationContext);
        this.resourceQuotaLimit = resourceQuotaLimit;

        //Creating namespace in kubernetes if not available
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        NamespaceList namespaceList = kubernetesClient.namespaces().list();
        boolean isNamespaceExists = false;
        for (Namespace ns : namespaceList.getItems()) {
            if (ns.getMetadata().getName().equals(this.namespace.getMetadata().getName())) {
                isNamespaceExists = true;
                if (log.isDebugEnabled()) {
                    log.debug("Namespace found: " + ns.getMetadata().getName());
                }
                break;
            }
        }
        if (!isNamespaceExists) {
            if (log.isDebugEnabled()) {
                log.debug("Namespace not available hence creating namespace: " + namespace.getMetadata().getName());
            }
            kubernetesClient.namespaces().create(namespace);
        }
    }

    public KubernetesRuntimeProvisioningService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.namespace = KubernetesProvisioningUtils.getNameSpace(applicationContext);

        //Creating namespace in kubernetes if not available
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        NamespaceList namespaceList = kubernetesClient.namespaces().list();
        boolean isNamespaceExists = false;
        for (Namespace ns : namespaceList.getItems()) {
            if (ns.getMetadata().getName().equals(this.namespace.getMetadata().getName())) {
                isNamespaceExists = true;
                if (log.isDebugEnabled()) {
                    log.debug("Namespace found: " + ns.getMetadata().getName());
                }
                break;
            }
        }
        if (!isNamespaceExists) {
            if (log.isDebugEnabled()) {
                log.debug("Namespace not available hence creating namespace: " + namespace.getMetadata().getName());
            }
            kubernetesClient.namespaces().create(namespace);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws RuntimeProvisioningException {

    }

    @Override
    public void createOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        kubernetesClient.namespaces().create(this.namespace);
        kubernetesClient.close();
    }

    @Override
    public void updateOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }

    @Override
    public void deleteOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        kubernetesClient.namespaces().delete(this.namespace);
        kubernetesClient.close();
    }

    @Override
    public void archiveOrganization(TenantInfo tenantInfo) throws RuntimeProvisioningException {

    }



    /**
     * Create Kubernetes Deployment and set of services according to the deployment configuration.
     * @param config deployment configuration
     * @return list of created service names
     * @throws RuntimeProvisioningException
     */
    @Override
    public List<String> deployApplication(DeploymentConfig config) throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubClient = null;
        List<Container> containers = config.getContainers();
        ArrayList<io.fabric8.kubernetes.api.model.Container> kubContainerList = new ArrayList<>();
        List<String> serviceNameList = new ArrayList<>();
        String cpuLimitInt = resourceQuotaLimit.getCpuLimit();
        String cpuLimit = cpuLimitInt.concat("m");
        String cpuRequest = resourceQuotaLimit.getCpuRequest().concat("m");
        String memoryLimitInt = resourceQuotaLimit.getMemoryLimit();
        String memoryLimit = memoryLimitInt.concat("Mi");
        String memoryRequest = resourceQuotaLimit.getMemoryRequest().concat("Mi");

        try {
            //Deployment creation
            for (Container container : containers) {
                io.fabric8.kubernetes.api.model.Container kubContainer = new io.fabric8.kubernetes.api.model.Container();
                kubContainer.setName(container.getContainerName());
                kubContainer.setImage(container.getBaseImageName() + ":" + container.getBaseImageVersion());
                kubContainer.setImagePullPolicy(KubernetesPovisioningConstants.IMAGE_PULL_POLICY_ALWAYS);

                ResourceRequirementsBuilder resourceRequirementsBuilder = new ResourceRequirementsBuilder();
                ResourceRequirements resourceRequirement = resourceRequirementsBuilder
                                                                   .addToLimits("cpu", new Quantity(cpuLimit))
                                                                   .addToRequests("cpu", new Quantity(cpuRequest))
                                                                   .addToLimits("memory", new Quantity(memoryLimit))
                                                                   .addToRequests("memory", new Quantity(memoryRequest))
                                                                   .build();
                kubContainer.setResources(resourceRequirement);

                //Checking whether the container is including volume mounts
                if(container.getVolumeMounts()!= null) {
                    kubContainer.setVolumeMounts(container.getVolumeMounts());
                }

                List<ContainerPort> containerPorts = new ArrayList<>();
                List<ServiceProxy> serviceProxies = container.getServiceProxies();
                if( serviceProxies != null && serviceProxies.size() > 0) {
                    for (ServiceProxy serviceProxy : serviceProxies) {
                        ContainerPort kubContainerPort = new ContainerPortBuilder()
                                .withContainerPort(serviceProxy.getServiceBackendPort())
                                .build();
                        containerPorts.add(kubContainerPort);
                    }
                }
                kubContainer.setPorts(containerPorts);
                if (container.getEnvVariables() != null) {
                    List<EnvVar> envVarList = new ArrayList<>();
                    for (Map.Entry envVarEntry : container.getEnvVariables().entrySet()) {
                        EnvVar envVar = new EnvVarBuilder()
                                .withName((String) envVarEntry.getKey())
                                .withValue((String) envVarEntry.getValue())
                                .build();
                        envVarList.add(envVar);
                    }
                    kubContainer.setEnv(envVarList);
                }
                kubContainerList.add(kubContainer);
            }

            PodSpec podSpec = new PodSpecBuilder()
                    .withContainers(kubContainerList)
                    .withVolumes(config.getSecrets())
                    .build();

            PodTemplateSpec podTemplateSpec = new PodTemplateSpecBuilder()
                    .withMetadata(new ObjectMetaBuilder()
                    .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext))
                    .build())
                    .withSpec(podSpec)
                    .build();

            DeploymentSpec deploymentSpec = new DeploymentSpecBuilder()
                    .withReplicas(config.getReplicas())
                    .withTemplate(podTemplateSpec)
                    .build();

            Deployment deployment = new DeploymentBuilder()
                    .withKind(KubernetesPovisioningConstants.KIND_DEPLOYMENT)
                    .withMetadata(new ObjectMetaBuilder().withName(config.getDeploymentName().toLowerCase()).build())
                    .withSpec(deploymentSpec)
                    .build();

            //Service creation
            List<Service> serviceList = new ArrayList<>();
            for (Container container : containers) {
                List<ServiceProxy> serviceProxies = container.getServiceProxies();
                for (ServiceProxy serviceProxy : serviceProxies) {
                    Service service = getService(serviceProxy);
                    serviceList.add(service);
                }
            }

            kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

            //create a new deployement with servies
            kubClient.inNamespace(namespace.getMetadata().getName()).extensions().deployments().create(deployment);
            for (Service service : serviceList) {
                kubClient.inNamespace(namespace.getMetadata().getName()).services().create(service);
                serviceNameList.add(service.getMetadata().getName());
            }

        } catch (KubernetesClientException e) {
            String msg = "Error while creating Deployment : " + config.getDeploymentName();
            log.error(msg, e);
            throw new RuntimeProvisioningException(msg, e);
        } finally {
            if (kubClient != null) {
                kubClient.close();
            }
        }
        return serviceNameList;
    }

    /**
     * Get service with service port and service specification.
     * @param serviceProxy service information
     * @return K8s service
     */
    private Service getService(ServiceProxy serviceProxy) {
        Map<String, String> annotationMap = new HashMap<String, String>();
        annotationMap.put(KubernetesPovisioningConstants.ANNOTATION_KEY_HOST, serviceProxy.getAppHostURL());
        //Check whether service is https and enable ssl term
        if (serviceProxy.getServicePort() == KubernetesPovisioningConstants.HTTPS_SERVICE_PORT) {
            annotationMap.put(KubernetesPovisioningConstants.ANNOTATION_KEY_SSL_TERM,
                    KubernetesPovisioningConstants.ANNOTATION_VALUE_SSL_TERM);
        }
        //Enable cookie sticky session
        annotationMap.put(KubernetesPovisioningConstants.ANNOTATION_KEY_COOKIE_STICKY_SESSION,
                KubernetesPovisioningConstants.ANNOTATION_VALUE_COOKIE_STICKY_SESSION);
        ServicePort servicePorts = new ServicePortBuilder().withName(serviceProxy.getServiceName())
                .withProtocol(serviceProxy.getServiceProtocol())
                .withPort(serviceProxy.getServicePort())
                .withTargetPort(new IntOrString(serviceProxy.getServiceBackendPort())).build();
        ServiceSpec serviceSpec = new ServiceSpecBuilder()
                .withSelector(KubernetesProvisioningUtils.getLableMap(applicationContext))
                .withPorts(servicePorts)
                .withSessionAffinity(KubernetesPovisioningConstants.SERVICE_SESSION_AFFINITY_MODE).build();

        //Add tenantDomain to label map for the service
        Map<String, String> labelMap = KubernetesProvisioningUtils.getLableMap(applicationContext);
        labelMap.put("tenantDomain", applicationContext.getTenantInfo().getTenantDomain());
        labelMap.put("exposure-level", applicationContext.getExposureLevel());

        //Deployment Unique service name is built using deployment name and the service name.
        String serviceName = serviceProxy.getServiceName();
        return new ServiceBuilder()
                .withKind(KubernetesPovisioningConstants.KIND_SERVICE)
                .withSpec(serviceSpec)
                .withMetadata(new ObjectMetaBuilder()
                        .withName(serviceName.toLowerCase())
                        .withLabels(labelMap)
                        .withAnnotations(annotationMap).build()).build();
    }

    @Override
    public boolean getDeploymentStatus(DeploymentConfig config) throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubClient = null;
        DeploymentStatus deploymentStatus = kubClient.inNamespace(namespace.getMetadata().getName())
                .extensions().deployments().withName(config.getDeploymentName()).get().getStatus();
        //Assuming AF does not do zero replica deployments
        if (deploymentStatus.getReplicas() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public DeploymentLogStream streamRuntimeLogs() throws RuntimeProvisioningException {

        DeploymentLogStream deploymentLogStream = new DeploymentLogStream();
        Map<String, BufferedReader> logOutPut = new HashMap<>();
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
	    if (podList != null) {
            try {
                int podCounter = 1;
	            Map<String, LogWatch> watches = new HashMap<>();
	            for (Pod pod : podList.getItems()) {
		            for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
			            String logWatchKey = container.getName();
			            if (log.isDebugEnabled()) {
				            log.debug("Streaming logs in pod : " + pod.getMetadata().getName() + "-" + container
						            .getName());
			            }
			            LogWatch logs = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
			                                            .withName(pod.getMetadata().getName())
			                                            .inContainer(container.getName()).watchLog();

			            //logStream should close by after the streaming done in front end
			            //you can use closeLogStream() method in DeploymentStreamLogs
			            BufferedReader logStream = new BufferedReader(new InputStreamReader(logs.getOutput()));
			            logOutPut.put("Replica-" + podCounter + "-" + container.getName(), logStream);
			            deploymentLogStream.setDeploymentLogs(logOutPut);
			            watches.put(logWatchKey, logs);
		            }
		            podCounter++;
	            }
	            deploymentLogStream.setWatches(watches);
            } catch (KubernetesClientException e) {
                log.error("Error while streaming runtime logs for application : " + applicationContext.getId()
                        + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
                throw new RuntimeProvisioningException(
                        "Error while streaming runtime logs for application : " + applicationContext.getId()
                                + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
            }
	    } else {
            log.error("Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                    + applicationContext.getTenantInfo().getTenantDomain());
            throw new RuntimeProvisioningException(
                    "Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                            + applicationContext.getTenantInfo().getTenantDomain());
        }
        return deploymentLogStream;
    }

    @Override
    public DeploymentLogs getRuntimeLogs(LogQuery query) throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        DeploymentLogs deploymentLogs = new DeploymentLogs();
        Map<String, String> logOutPut = new HashMap<>();
        PrettyLoggable prettyLoggable;
        PrettyLoggable prettyLoggablePrev = null;
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        if (podList != null) {
            try {
                int podCounter = 1;
                for (Pod pod : podList.getItems()) {
                    for (io.fabric8.kubernetes.api.model.Container container : KubernetesHelper.getContainers(pod)) {
                        //Get logs from last pod if restart count > 0
                        if(pod.getStatus().getContainerStatuses().size() > 0 &&
                           pod.getStatus().getContainerStatuses().get(0).getRestartCount() > 0) {
                            prettyLoggablePrev = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                                                 .withName(pod.getMetadata().getName()).terminated();
                        }
                        //Get logs from current pod
                        if (query == null || (query.getDurationInHours() < 0 && query.getTailingLines() < 0)) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName());
                        } else if (query.getDurationInHours() < 0 && query.getTailingLines() > 0) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                    .tailingLines(query.getTailingLines());
                        } else if (query.getDurationInHours() > 0 && query.getTailingLines() < 0) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                    .sinceSeconds(query.getDurationInHours() * 3600);
                        } else if (query.getDurationInHours() > 0 && query.getTailingLines() > 0) {
                            prettyLoggable = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName())
                                    .withName(pod.getMetadata().getName()).inContainer(container.getName())
                                    .sinceSeconds(query.getDurationInHours() * 3600)
                                    .tailingLines(query.getTailingLines());
                        } else {
                            log.error("Error in log query while getting snapshot logs of application : "
                                    + applicationContext.getId() + " tenant domain : " + applicationContext
                                    .getTenantInfo().getTenantDomain());
                            throw new RuntimeProvisioningException(
                                    "Error in log query while getting snapshot logs of application : "
                                            + applicationContext.getId() + " tenant domain : " + applicationContext
                                            .getTenantInfo().getTenantDomain());
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieving logs in pod : " + pod.getMetadata().getName() + "-" + container
                                    .getName());
                        }
                        String logs = "";
                        if(prettyLoggablePrev != null) {
                            logs = (String) prettyLoggablePrev.getLog(true);
                        }
                        logs += (String) prettyLoggable.getLog(true);
                        logOutPut.put("Replica_" + podCounter + "_" + pod.getMetadata().getName(), logs);
                        deploymentLogs.setDeploymentLogs(logOutPut);
                    }
                    podCounter++;
                }
            } catch (KubernetesClientException e) {
                log.error("Error while getting snapshot logs for application : " + applicationContext.getId()
                        + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
                throw new RuntimeProvisioningException(
                        "Error while getting snapshot logs for application : " + applicationContext.getId()
                                + " tenant domain : " + applicationContext.getTenantInfo().getTenantDomain(), e);
            }
            return deploymentLogs;
        } else {
            log.error("Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                    + applicationContext.getTenantInfo().getTenantDomain());
            throw new RuntimeProvisioningException(
                    "Pod list returned as null for application : " + applicationContext.getId() + " tenant domain : "
                            + applicationContext.getTenantInfo().getTenantDomain());
        }
    }

    /**
     * Set runtime properties to kubernetes environment.
     *
     * @param runtimeProperties runtime properties
     * @param deploymentConfig  includes deployment related details
     * @throws RuntimeProvisioningException
     */
    @Override
    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties,
            DeploymentConfig deploymentConfig) throws RuntimeProvisioningException {

        runtimeProperties.addAll(getRuntimeProperties());

        //list of secretes
        List secrets = new ArrayList();

        //list of env variables
        HashMap<String, String> envVariables = new HashMap<>();

        //create a instance of kubernetes client to invoke service call
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        List<VolumeMount> volumeMounts = new ArrayList<>();

        for (RuntimeProperty runtimeProperty : runtimeProperties) {
            switch (runtimeProperty.getPropertyType()) {
            case SENSITIVE:
                if (log.isDebugEnabled()) {
                    String message = "Creating property type secret for the application:" + applicationContext.getId()
                            + " for the tenant domain:" + applicationContext.getTenantInfo().getTenantDomain();
                    log.debug(message);
                }

                Secret currentSecret = kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName())
                        .withName(runtimeProperty.getName()).get();

                //if secrete exists then replace the same secrete, otherwise create a new secrete
                if (currentSecret != null) {
                    kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName())
                            .withName(runtimeProperty.getName()).replace(currentSecret);
                } else {
                    Secret secret = new SecretBuilder()
                            .withKind(KubernetesPovisioningConstants.KIND_SECRETS)
                            .withNewMetadata()
                            .withNamespace(namespace.getMetadata().getName())
                            .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext))
                            .withName(runtimeProperty.getName())
                            .endMetadata().withData(runtimeProperty.getProperties())
                            .build();

                    kubernetesClient.secrets().create(secret);
                }

                Volume volume = new VolumeBuilder()
                        .withName(runtimeProperty.getName())
                        .withNewSecret()
                        .withSecretName(runtimeProperty.getName())
                        .endSecret()
                        .build();

                secrets.add(volume);

                //create volume mount for the secretes
                VolumeMount volumeMount = new VolumeMountBuilder()
                        .withName(runtimeProperty.getName())
                        .withMountPath(KubernetesPovisioningConstants.VOLUME_MOUNT_PATH + runtimeProperty.getName())
                        .withReadOnly(true)
                        .build();

                volumeMounts.add(volumeMount);

                break;
            case ENVIRONMENT:
                if (log.isDebugEnabled()) {
                    String message = "Creating property type environment for the application:"
                            + applicationContext.getId() + " for the tenant domain : "
                            + applicationContext.getTenantInfo().getTenantDomain();
                    log.debug(message);
                }

                envVariables.putAll(runtimeProperty.getProperties());

                break;
            default:
                String message = "Runtime property type : " + runtimeProperty.getPropertyType() + " not supported.";

                throw new IllegalArgumentException(message);

            }
        }

        //Initially assume first container is the application and set volume mounts
        deploymentConfig.getContainers().get(0).setVolumeMounts(volumeMounts);

        //Set secretes to a pod
        deploymentConfig.setSecrets(secrets);

        //Initially assume first container is the application
        deploymentConfig.getContainers().get(0).setEnvVariables(envVariables);

        //Call deploy application to redeploy application with runtime properties
        deployApplication(deploymentConfig);

    }

    /**
     * Update runtime properties already defined in the application.
     *
     * @param runtimeProperties list of runtime properties
     * @param deploymentConfig  includes deployment related details
     * @throws RuntimeProvisioningException
     */
    @Override
    public void updateRuntimeProperties(List<RuntimeProperty> runtimeProperties, DeploymentConfig deploymentConfig)
            throws RuntimeProvisioningException {

        List secrets = new ArrayList();
        Container container = new Container();

        for (RuntimeProperty runtimeProperty : runtimeProperties) {
            switch (runtimeProperty.getPropertyType()) {
            case SENSITIVE:
                if (log.isDebugEnabled()) {
                    String message = "Updating property type secret for the application:" + applicationContext.getId()
                            + " for the tenant domain:" + applicationContext.getTenantInfo().getTenantDomain();

                    log.debug(message);
                }
                Secret secret = new SecretBuilder().withKind(KubernetesPovisioningConstants.KIND_SECRETS)
                        .withNewMetadata()
                        .withNamespace(namespace.getMetadata().getNamespace())
                        .withName(runtimeProperty.getName())
                        .endMetadata()
                        .withData(runtimeProperty.getProperties())
                        .build();

                AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
                kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName())
                        .withName(runtimeProperty.getName()).replace(secret);

                Volume volume = new VolumeBuilder()
                        .withName(KubernetesPovisioningConstants.VOLUME_MOUNT)
                        .withNewSecret()
                        .withSecretName(runtimeProperty.getName())
                        .endSecret()
                        .build();

                secrets.add(volume);

                break;
            case ENVIRONMENT:
                if (log.isDebugEnabled()) {
                    String message = "Updating property type environment for the application:"
                            + applicationContext.getId() + " for the tenant domain:"
                            + applicationContext.getTenantInfo().getTenantDomain();

                    log.debug(message);
                }

                //updating environment variables for container
                container.setEnvVariables(runtimeProperty.getProperties());

                break;
            default:
                String message = "Runtime property type is not support, property type:"
                        + runtimeProperty.getPropertyType();

                throw new IllegalArgumentException(message);
            }
        }

        List<Container> containers = new ArrayList<>();
        containers.add(container);
        deploymentConfig.setContainers(containers);

        //call application deployment to re deploy the application with update variables
        deployApplication(deploymentConfig);
    }

    /**
     * Provide runtime properties for given application context.
     *
     * @return list of runtime properties
     * @throws RuntimeProvisioningException
     */
    @Override
    public List<RuntimeProperty> getRuntimeProperties() throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        SecretList secretList = kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName())
                .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext)).list();

        List<RuntimeProperty> runtimeProperties = new ArrayList<>();

        for (Secret secret : secretList.getItems()) {
            RuntimeProperty sensitiveRuntimeProperty = new RuntimeProperty();
            sensitiveRuntimeProperty.setPropertyType(RuntimeProperty.PropertyType.SENSITIVE);
            sensitiveRuntimeProperty.setName(secret.getMetadata().getName());
            sensitiveRuntimeProperty.setProperties(secret.getData());
            runtimeProperties.add(sensitiveRuntimeProperty);

        }

        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);

        HashMap<String, String> data = new HashMap<>();

        for (Pod pod : podList.getItems()) {
            //get only first container from the container list
            List<EnvVar> envVarList = pod.getSpec().getContainers().get(0).getEnv();

            for (EnvVar envVar : envVarList) {
                data.put(envVar.getName(), envVar.getValue());
            }

            RuntimeProperty environmentVariable = new RuntimeProperty();
            environmentVariable.setPropertyType(RuntimeProperty.PropertyType.ENVIRONMENT);
            environmentVariable.setProperties(data);
            runtimeProperties.add(environmentVariable);
        }

        return runtimeProperties;

    }

    /**
     * add a set of custom domains for an application version.
     * This will create an ingress for each domain and each service.
     *
     * @param domains set of domains
     * @throws RuntimeProvisioningException
     */
    @Override
    public boolean addCustomDomain(Set<String> domains) throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);
        Ingress createdIng;
        boolean created = false;

        for (String domain : domains) {
            for (Service service : serviceList.getItems()) {
                Ingress ing = new IngressBuilder()
                        .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                        .withNewMetadata()
                        .withName(KubernetesProvisioningUtils
                                .createIngressMetaName(domain))
                        .withNamespace(namespace.getMetadata().getName())
                        .endMetadata()
                        .withNewSpec()
                        .withRules().addNewRule()
                        .withHost(domain)
                        .withNewHttp()
                        .withPaths().addNewPath()
                        .withNewBackend()
                        .withServiceName(service.getMetadata().getName())
                        .withServicePort(new IntOrString(80))
                        .endBackend()
                        .endPath()
                        .endHttp()
                        .endRule()
                        .endSpec()
                        .build();

                createdIng = kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).create(ing);
                if(createdIng != null && KubernetesProvisioningUtils
                        .createIngressMetaName(domain)
                        .equals(createdIng.getMetadata().getName())){
                    created = true;
                    log.info("Kubernetes ingress : " + ing + "created for service : " +
                            service.getMetadata().getName());
                }else{
                    created = false;
                    log.error("Error occured while creating Kubernetes ingress : " + ing + "for service : " +
                            service.getMetadata().getName());
                }
            }
        }

        return created;
    }

    /**
     * update a certain domain by replacing the ingresses created for related services with new ingresses.
     *
     * @param oldDomain old domain name to be changed
     * @param newDomain new domain name to be changed to
     * @throws RuntimeProvisioningException
     */
    @Override
    public boolean updateCustomDomain(String oldDomain, String newDomain) throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);
        boolean deleted = false;
        boolean updated = false;
        Ingress createdIng;

        for (Service service : serviceList.getItems()) {

            String oldIngName =  KubernetesProvisioningUtils
                    .createIngressMetaName(oldDomain);

            String newIngName =  KubernetesProvisioningUtils
                    .createIngressMetaName(newDomain);
            Ingress oldIng = new IngressBuilder()
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(oldIngName)
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .build();

            Ingress newIng = new IngressBuilder()
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(newIngName)
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .withNewSpec()
                    .addNewRule()
                    .withHost(newDomain)
                    .withNewHttp()
                    .addNewPath()
                    .withNewBackend()
                    .withServiceName(service.getMetadata().getName())
                    .withServicePort(new IntOrString(80))
                    .endBackend()
                    .endPath()
                    .endHttp()
                    .endRule()
                    .endSpec()
                    .build();

            deleted = kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).delete(oldIng);
            if(deleted) {
                createdIng =
                        kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).create(newIng);
                if(createdIng != null && newIngName.equals(createdIng.getMetadata().getName())){
                    updated = true;
                }else{
                    updated = false;
                    log.error("Error occured while creating Kubernetes ingress : " + newIng + "for service : " +
                            service.getMetadata().getName());
                }

            }else{
                log.error("Error occured while deleting Kubernetes ingress : " + oldIng + "for service : " +
                        service.getMetadata().getName());
            }
        }
        return  updated;
    }

    /**
     * get a set of custom domains for a particular applicaiton context.
     *
     * @return set of domains
     * @throws RuntimeProvisioningException
     */
    @Override
    public Set<String> getCustomDomains() throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        Set<String> domains = new HashSet<>();

        IngressList ingressList = kubClient.extensions().ingress().
                inNamespace(namespace.getMetadata().getName()).list();
        for (Ingress ingress : ingressList.getItems()){
            domains.add(ingress.getSpec().getRules().get(0).getHost());
        }
        return domains;
    }

    /**
     * delete a custom domain and delete the ingresses created for related services.
     *
     * @param domain domain name
     * @throws RuntimeProvisioningException
     */
    @Override
    public boolean deleteCustomDomain(String domain) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();

        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);
        boolean deleted = false;

        for (Service service : serviceList.getItems()) {
            String ingName = KubernetesProvisioningUtils
                    .createIngressMetaName(domain);
            Ingress ing = new IngressBuilder()
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(ingName)
                    .withNamespace(namespace.getMetadata().getName())
                    .endMetadata()
                    .build();

            deleted = kubClient.extensions().ingress().inNamespace(namespace.getMetadata().getName()).delete(ing);
            if(!deleted){
                log.error("Error occurred while deleting Kubernetes ingress : " + ingName + "for service : " +
                        service.getMetadata().getName());
            }
        }
        return deleted;
    }

    @Override
    public boolean createDeploymentUrl(String environmentUrl) throws RuntimeProvisioningException {

        AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        ServiceList serviceList = KubernetesProvisioningUtils.getServices(applicationContext);
        if (log.isDebugEnabled()){
            log.debug("Deployment service List size: " + serviceList.getItems().size());
        }
        Ingress createdIng;
        boolean created = false;
        String ingressPathStr = KubernetesProvisioningUtils.getDeploymentPath(applicationContext);
        if (log.isDebugEnabled()){
            log.debug("Ingress path: " + ingressPathStr);
        }

        HTTPIngressPath ingressPath = new HTTPIngressPath(new IngressBackend(),
                KubernetesPovisioningConstants.DEFAULT_INGRESS_PATH);

        for (Service service : serviceList.getItems()) {
            if (log.isDebugEnabled()){
                log.debug("Ingress creating for service: " + service.getMetadata().getName());
            }
            String ingressName = environmentUrl + "-" + service.getMetadata().getName();
            Ingress ing = new IngressBuilder()
                    .withKind(KubernetesPovisioningConstants.KIND_INGRESS)
                    .withNewMetadata()
                    .withName(KubernetesProvisioningUtils.createIngressMetaName(ingressName))
                    .withNamespace(namespace.getMetadata().getName())
                    .withLabels(KubernetesProvisioningUtils.getLableMap(applicationContext))
                    .endMetadata()
                    .withNewSpec()
                    .withRules()
                    .addNewRule()
                    .withHost(environmentUrl)
                    .withNewHttp()
                    .withPaths()
                    .addNewPathLike(ingressPath)
                    .withNewBackend()
                    .withServiceName(service.getMetadata().getName())
                    .withServicePort(new IntOrString(service.getSpec().getPorts().get(0).getPort()))
                    .endBackend()
                    .endPath()
                    .endHttp()
                    .endRule()
                    .endSpec()
                    .build();

            createdIng = kubClient.extensions()
                    .ingress()
                    .inNamespace(namespace.getMetadata().getName())
                    .create(ing);

            if (createdIng != null && KubernetesProvisioningUtils
                    .createIngressMetaName(ingressName)
                    .equals(createdIng.getMetadata().getName())) {
                created = true;
                if (log.isDebugEnabled()){
                    log.debug("Kubernetes ingress : " + ing + "created for service : " + service.getMetadata().getName());
                }

            } else {
                created = false;
                log.error("Error occurred while creating Kubernetes ingress : " + ing + "for service : " +
                        service.getMetadata().getName());
            }
        }
        return created;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDeployment() throws RuntimeProvisioningException {
        try {
            deleteK8sKind(KubernetesPovisioningConstants.KIND_DEPLOYMENT);
            KubernetesProvisioningUtils.waitForDeploymentToGetDeleted(applicationContext);
            deleteK8sKind(KubernetesPovisioningConstants.KIND_REPLICATION_CONTROLLER);
            KubernetesProvisioningUtils.waitForRCToGetDeleted(applicationContext);
            deleteK8sKind(KubernetesPovisioningConstants.KIND_POD);
            KubernetesProvisioningUtils.waitForPodToGetDeleted(applicationContext);
            deleteK8sKind(KubernetesPovisioningConstants.KIND_INGRESS);
            KubernetesProvisioningUtils.waitForIngressesToGetDeleted(applicationContext);
            deleteK8sKind(KubernetesPovisioningConstants.KIND_SECRETS);
            KubernetesProvisioningUtils.waitForSecretToGetDeleted(applicationContext);
            deleteK8sKind(KubernetesPovisioningConstants.KIND_SERVICE);
            KubernetesProvisioningUtils.waitForServiceToGetDeleted(applicationContext);
            return true;
        } catch (RuntimeProvisioningException e){
            return false;
        }
    }

    /**
     * Delete K8s object for given kind with labels.
     *
     * @param k8sKind k8s object type
     */
    public void deleteK8sKind(String k8sKind) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        String namespace = this.namespace.getMetadata().getName();
        Map<String, String> labels = KubernetesProvisioningUtils.getDeleteLables(applicationContext);

        try {
            switch (k8sKind) {
            case KubernetesPovisioningConstants.KIND_REPLICATION_CONTROLLER:
                kubernetesClient.replicationControllers().inNamespace(namespace).withLabels(labels).delete();
                break;
            case KubernetesPovisioningConstants.KIND_DEPLOYMENT:
                kubernetesClient.extensions().deployments().inNamespace(namespace).withLabels(labels).delete();
                break;
            case KubernetesPovisioningConstants.KIND_POD:
                kubernetesClient.pods().inNamespace(namespace).withLabels(labels).delete();
                break;
            case KubernetesPovisioningConstants.KIND_INGRESS:
                kubernetesClient.extensions().ingress().inNamespace(namespace).withLabels(labels).delete();
                break;
            case KubernetesPovisioningConstants.KIND_SECRETS:
                kubernetesClient.secrets().inNamespace(namespace).withLabels(labels).delete();
                break;
            case KubernetesPovisioningConstants.KIND_SERVICE:
                kubernetesClient.services().inNamespace(namespace).withLabels(labels).delete();
                break;
            default:
                String message = "The kubernetes kind : " + k8sKind + " deletion is not supported";
                throw new IllegalArgumentException(message);
            }
        } catch (KubernetesClientException e) {
            String message = "Error while deleting kubernetes kind : " + k8sKind + " from deployment";
            log.error(message, e);
            throw new RuntimeProvisioningException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteK8sKindByName(String k8sKind, String name) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        String namespace = this.namespace.getMetadata().getName();

        if (log.isDebugEnabled()) {
            log.debug("Kubernetes kind : " + k8sKind + ", object name : " + name);
        }

        try {
            switch (k8sKind) {
            case KubernetesPovisioningConstants.KIND_REPLICATION_CONTROLLER:
                kubernetesClient.replicationControllers().inNamespace(namespace).withName(name).delete();
                break;
            case KubernetesPovisioningConstants.KIND_DEPLOYMENT:
                kubernetesClient.extensions().deployments().inNamespace(namespace).withName(name).delete();
                break;
            case KubernetesPovisioningConstants.KIND_POD:
                kubernetesClient.pods().inNamespace(namespace).withName(name).delete();
                break;
            case KubernetesPovisioningConstants.KIND_INGRESS:
                kubernetesClient.extensions().ingress().inNamespace(namespace).withName(name).delete();
                break;
            case KubernetesPovisioningConstants.KIND_SECRETS:
                kubernetesClient.secrets().inNamespace(namespace).withName(name).delete();
                break;
            case KubernetesPovisioningConstants.KIND_SERVICE:
                kubernetesClient.services().inNamespace(namespace).withName(name).delete();
                break;
            default:
                String message = "The kubernetes kind : " + k8sKind + " deletion is not supported";
                throw new IllegalArgumentException(message);
            }
        } catch (KubernetesClientException e) {
            String message = "Error while deleting kubernetes kind : " + k8sKind + " with name : " + name +
                    " from deployment";
            log.error(message, e);
            throw new RuntimeProvisioningException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createService(ServiceProxy serviceProxy) throws RuntimeProvisioningException {
        Service service = getService(serviceProxy);
        String namespace = this.namespace.getMetadata().getName();

        try {
            AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            kubClient.inNamespace(namespace).services().create(service);
        } catch (KubernetesClientException e) {
            String message = "Error while creating kubernetes kind service with namespace : " + namespace;
            log.error(message, e);
            throw new RuntimeProvisioningException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public Map<String, String> getPodRestartCounts() throws RuntimeProvisioningException {
		Map<String, String> podRestartCounts = new HashMap<>();
		PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
		if (podList != null) {
			int podCounter = 0;
			for (Pod pod : podList.getItems()) {
                if(pod.getStatus().getContainerStatuses().size() > 0) {
                    podRestartCounts.put(String.valueOf(podCounter),
                                         String.valueOf(pod.getStatus().getContainerStatuses().get(0).getRestartCount()));
                } else {
                    //In case query is done before the pod get fully created,
                    //Restart count wont be available so returning 0
                    podRestartCounts.put(String.valueOf(podCounter), String.valueOf(0));
                }
				podCounter++;
			}
			return podRestartCounts;
		} else {
			String message = "Could not find a pod associated with pod for application : " + applicationContext.getId() +
			                 ", version : " + applicationContext.getVersion();
			log.error(message);
			throw new RuntimeProvisioningException(message);
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONArray getReplicaInfo() throws RuntimeProvisioningException {
        PodList podList = KubernetesProvisioningUtils.getPods(applicationContext);
        if (podList != null) {
            JSONArray jsonArray = new JSONArray();
            for (Pod pod : podList.getItems()) {
                if(pod.getStatus().getContainerStatuses().size() > 0) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("podName", pod.getMetadata().getName());
                    jsonObject.put("restartCount", pod.getStatus().getContainerStatuses().get(0).getRestartCount());
                    jsonArray.put(jsonObject);
                } else {
                    //In case query is done before the pod get fully created,
                    //Restart count wont be available so returning 0
                    //Pod name return as empty
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("podName", "");
                    jsonObject.put("restartCount", String.valueOf(0));
                    jsonArray.put(jsonObject);
                }
            }
            return jsonArray;
        } else {
            String message = "Could not find a pod associated with pod for application : " + applicationContext.getId() +
                    ", version : " + applicationContext.getVersion();
            log.error(message);
            throw new RuntimeProvisioningException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeExposureLevelInServices(String serviceName, String exposureLevel, String lbHost)
            throws  RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        kubClient.services().inNamespace(this.namespace.getMetadata().getName())
                .withName(serviceName).edit().editMetadata()
                .addToLabels("exposure-level", exposureLevel).endMetadata().done();

        kubClient.services().inNamespace(this.namespace.getMetadata().getName())
                .withName(serviceName).edit().editMetadata()
                .addToAnnotations("serviceloadbalancer/lb.host", lbHost).endMetadata().done();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateKubernetesServiceWithLabel(String serviceName, String labelKey, String labelValue)
            throws RuntimeProvisioningException {
        String namespace = this.namespace.getMetadata().getName();
        try {
            AutoAdaptableKubernetesClient kubClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
            kubClient.services().inNamespace(namespace).withName(serviceName).edit().editMetadata()
                    .addToLabels(labelKey, labelValue).endMetadata().done();
        } catch (KubernetesClientException e) {
            String message = "Error while adding label to kubernetes kind service with service name: " + serviceName
                    + " in namespace: " + namespace;
            log.error(message, e);
            throw new RuntimeProvisioningException(message, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createDeploymentAutoScalePolicy(String name) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        String namespace = this.namespace.getMetadata().getName();

        HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
                .withNewMetadata().withNamespace(namespace).withName(name).endMetadata()
                .withNewSpec().withMinReplicas(1).withMaxReplicas(5).withNewCpuUtilization(50)
                .withNewScaleRef().withName(name).withKind(KubernetesPovisioningConstants.KIND_DEPLOYMENT)
                .endScaleRef().endSpec()
                .build();

        kubernetesClient.extensions().horizontalPodAutoscalers().inNamespace(namespace).create(horizontalPodAutoscaler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleDeployment(String name, int replicaCount) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        String namespace = this.namespace.getMetadata().getName();

        kubernetesClient.extensions().deployments().inNamespace(namespace).withName(name).scale(replicaCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReplicasForDeployment(String name) throws RuntimeProvisioningException {
        AutoAdaptableKubernetesClient kubernetesClient = KubernetesProvisioningUtils.getFabric8KubernetesClient();
        String namespace = this.namespace.getMetadata().getName();
        int count = kubernetesClient.extensions().deployments().inNamespace(namespace).withName(name).get().getSpec()
                .getReplicas();
        log.info("Replica count for deployment:" + name + ":" + count);
        return count;
    }
}
