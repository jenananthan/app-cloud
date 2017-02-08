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

package org.wso2.appcloud.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.common.AppCloudException;
import org.wso2.appcloud.core.dao.ApplicationDAO;
import org.wso2.appcloud.core.dto.*;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This class provide the interface for accessing the dao layer.
 */
public class ApplicationManager {

    private static Log log = LogFactory.getLog(ApplicationManager.class);

    /**
     * Method for adding application.
     *
     * @param application application object
     * @throws AppCloudException
     */
    public static void addApplication(Application application) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().addApplication(dbConnection, application, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding application with name : " + application.getApplicationName() +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding application with name : " +
                    application.getApplicationName() + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for adding application version.
     *
     * @param version version object
     * @throws AppCloudException
     */
    public static void addApplicationVersion(Version version, String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            int applicationId = ApplicationDAO.getInstance().
                    getApplicationId(dbConnection, applicationHashId, tenantId);
            ApplicationDAO.getInstance().addVersion(dbConnection, version, applicationId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding application version for application hash id : " + applicationHashId +
                    "and version :" + version.getVersionName() + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding application version, for application id : " +
                    applicationHashId + " and version : " + version.getVersionName() + " for tenant id : "
                    + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for adding runtime properties for a specific version.
     *
     * @param runtimeProperties list of runtime properties
     * @param versionHashId     version hash id
     * @throws AppCloudException
     */
    public static void addRuntimeProperties(List<RuntimeProperty> runtimeProperties, String versionHashId)
            throws AppCloudException {
        if (runtimeProperties == null || runtimeProperties.isEmpty()) {
            return;
        }
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().addRunTimeProperties(dbConnection, runtimeProperties, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding runtime properties for version with version id : " + versionHashId +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding runtime properties for version with " +
                    "version id : " + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for adding tags for a specific version.
     *
     * @param tags          list of tags
     * @param versionHashId version hash id
     * @throws AppCloudException
     */
    public static void addTags(List<Tag> tags, String versionHashId)
            throws AppCloudException {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().addTags(dbConnection, tags, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding tags for version with hash id : " + versionHashId + " for tenant id : " +
                    tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding tags for version with hash id : "
                    + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    /**
     * Method for getting the list of application of a tenant per cloud
     * @param cloudType cloud type
     * @return array of application objects
     * @throws AppCloudException
     */
    public static Application[] getApplicationList(String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<Application> applications = ApplicationDAO.getInstance().
                    getAllApplicationsList(dbConnection, tenantId, cloudType);
            return applications.toArray(new Application[applications.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while getting application list for tenant id : " + tenantId + " and cloud : " + cloudType ;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting version list of application.
     *
     * @param applicationHashId hash id of application object
     * @return list of versions
     * @throws AppCloudException
     */
    public static List<String> getVersionListOfApplication(String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().
                    getAllVersionListOfApplication(dbConnection, applicationHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting version list for application with hash id : " + applicationHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting version hash ids of application.
     *
     * @param applicationHashId hash id of application object
     * @return list of version hash ids
     * @throws AppCloudException
     */
    public static List<String> getVersionHashIdsOfApplication(String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().
                    getAllVersionHashIdsOfApplication(dbConnection, applicationHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting version hash ids for application with hash id : " + applicationHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for checking if the given version is the only version.
     *
     * @param versionHashId version id of application version
     * @return is only version or not
     * @throws AppCloudException
     */
    public static boolean isSingleVersion(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().isSingleVersion(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting if version is a single version for version with hash id : "
                    + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting application hash id by version hash id.
     *
     * @param versionHashId version id of application version
     * @return application hash id
     * @throws AppCloudException
     */
    public static String getApplicationHashIdByVersionHashId(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().
                    getApplicationHashIdByVersionHashId(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting application hash id by version hash id for version with hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting application name by hash id.
     *
     * @param applicationHashId hash id of application object
     * @return application name
     * @throws AppCloudException
     */
    public static String getApplicationNameByHashId(String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {

            return ApplicationDAO.getInstance().getApplicationNameByHashId(dbConnection, applicationHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting application name by hash id for application with hash id : " +
                    applicationHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting application hash id of matching application name.
     *
     * @param applicationName name of application object
     * @return application hash id
     * @throws AppCloudException
     */
    public static String getApplicationHashIdOfMatchingAppName(String applicationName) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getApplicationHashIdOfMatchingAppName(dbConnection, applicationName, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting application hash id of matching app name : "
                    + applicationName + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting application by hash id.
     *
     * @param applicationHashId application hash id
     * @return application object
     * @throws AppCloudException
     */
    public static Application getApplicationByHashId(String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getApplicationByHashId(dbConnection, applicationHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting application by hash id for hash id : " + applicationHashId +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting all runtime properties of version.
     *
     * @param versionHashId hash id of version
     * @return list of runtime properties
     * @throws AppCloudException
     */
    public static List<RuntimeProperty> getAllRuntimePropertiesOfVersion(String versionHashId)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getAllRuntimePropertiesOfVersion(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting all runtime properties of version for version hash id : " + versionHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting all tags of version.
     *
     * @param versionHashId hash id of version
     * @return list of tags
     * @throws AppCloudException
     */
    public static List<Tag> getAllTagsOfVersion(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getAllTagsOfVersion(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting all tags of version for version with hash id : " + versionHashId +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for updating runtime property.
     *
     * @param versionHashId hash id of version
     * @param oldKey        old key of runtime property
     * @param newKey        new key of runtime property
     * @param newValue      new value of runtime property
     * @throws AppCloudException
     */
    public static void updateRuntimeProperty(String versionHashId, String oldKey, String newKey,
                                             String newValue) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().
                    updateRuntimeProperty(dbConnection, versionHashId, oldKey, newKey, newValue, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding runtime property with key : " + oldKey + " for version with hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding runtime property with key : " + oldKey +
                    " for version with hash id : " + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for updating tag.
     *
     * @param versionHashId hash id of version
     * @param oldKey        old key of tag
     * @param newKey        new key of tag
     * @param newValue      new value of tag
     * @throws AppCloudException
     */
    public static void updateTag(String versionHashId, String oldKey, String newKey, String newValue)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().updateTag(dbConnection, versionHashId, oldKey, newKey, newValue, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while updating tag with the key : " + oldKey + " for version with hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for updating tag with the key : " + oldKey +
                    " for version with hash id : " + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for deleting runtime property.
     *
     * @param versionHashId hash id of version
     * @param key           key of runtime property
     * @throws AppCloudException
     */
    public static void deleteRuntimeProperty(String versionHashId, String key)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().deleteRuntimeProperty(dbConnection, versionHashId, key, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while deleting runtime property with key : " + key + " for version with hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for deleting runtime property with key : " + key +
                    " for version with hash id : " + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for deleting tag.
     *
     * @param versionHashId hash id of version
     * @param key           key of tag
     * @throws AppCloudException
     */
    public static void deleteTag(String versionHashId, String key)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().deleteTag(dbConnection, versionHashId, key, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while deleting tag with key : " + key + " for version with hash id : " + versionHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for deleting tag with key : " + key +
                    " for version with hash id : " + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for updating application icon.
     *
     * @param applicationHashId hash id of version
     * @param iconStream        icon stream object
     * @throws AppCloudException
     */
    public static void updateApplicationIcon(String applicationHashId, Object iconStream)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (iconStream instanceof InputStream) {
            InputStream iconInputStream = (InputStream) iconStream;
            try {
                int applicationId = ApplicationDAO.getInstance().
                        getApplicationId(dbConnection, applicationHashId, tenantId);
                ApplicationDAO.getInstance().
                        updateApplicationIcon(dbConnection, iconInputStream, applicationId, tenantId);
                dbConnection.commit();
            } catch (AppCloudException e) {
                String msg = "Error while updating the application icon for application with hash id : "
                        + applicationHashId + " for tenant id : " + tenantId;
                log.error(msg, e);
                throw new AppCloudException(msg, e);
            } catch (SQLException e) {
                String msg = "Error while committing transaction for updating the application icon for " +
                        "application with hash id : " + applicationHashId + " for tenant id : " + tenantId;
                log.error(msg, e);
                throw new AppCloudException(msg, e);
            } finally {
                try {
                    iconInputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing input stream for application with hash id : " +
                            applicationHashId + " for tenant id : " + tenantId);
                } finally {
                    DBUtil.closeConnection(dbConnection);
                }
            }
        } else {
            String msg = "Cannot read the provided icon stream for application with hash id : " + applicationHashId
                    + " for tenant id : " + tenantId;
            log.error(msg);
            throw new AppCloudException(msg);
        }
    }

    /**
     * Method for getting all apptypes for a given cloud type.
     *
     * @param cloudType cloud type
     * @return list of app types
     * @throws AppCloudException
     */
    public static ApplicationType[] getAllAppTypesForCloud(String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<ApplicationType> applicationTypeList = ApplicationDAO.getInstance().
                    getAllApplicationTypesForCloud(dbConnection, tenantId, cloudType);
            return applicationTypeList.toArray(new ApplicationType[applicationTypeList.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while getting all application types for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting all runtimes for a given application type.
     *
     * @param appType application type
     * @return list of application runtimes
     * @throws AppCloudException
     */
    public static ApplicationRuntime[] getAllRuntimesForAppType(String appType)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<ApplicationRuntime> runtimes = ApplicationDAO.getInstance().
                    getRuntimesForAppType(dbConnection, appType, tenantId);
            return runtimes.toArray(new ApplicationRuntime[runtimes.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while getting all runtimes for application type for type: " + appType +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for updating application status.
     *
     * @param versionHashId hash id of version
     * @param status        status of application
     * @throws AppCloudException
     */
    public static void updateVersionStatus(String versionHashId, String status) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().updateVersionStatus(dbConnection, status, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while updating version status with status : " + status + " for version with hash id : "
                    + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for updating version status with status : " + status +
                    " for version with hash id : " + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for delete an application completely.
     *
     * @param applicationHashId application hash id
     * @throws AppCloudException
     */
    public static void deleteApplication(String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().deleteAllDeploymentOfApplication(dbConnection, applicationHashId, tenantId);
            ApplicationDAO.getInstance().deleteApplication(dbConnection, applicationHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while deleting application with hash id : " + applicationHashId + " for tenant id : "
                    + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while commiting transaction for deleting application with hash id : " +
                    applicationHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for deleting version of application.
     *
     * @param versionHashId hash id of version
     * @throws AppCloudException
     */
    public static void deleteVersion(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().deleteDeployment(dbConnection, versionHashId, tenantId);
            ApplicationDAO.getInstance().deleteVersion(dbConnection, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while deleting the version with hash id : " + versionHashId + " for tenant id : " +
                    tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for deleting the version with hash id : "
                    + versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for adding deployment.
     *
     * @param versionHashId hash id of version
     * @param deployment    deployment object
     * @throws AppCloudException
     */
    public static void addDeployment(String versionHashId, Deployment deployment) throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection dbcConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().addDeploymentForVersion(dbcConnection, versionHashId, deployment, tenantId);
            dbcConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding deployment for version with hash id : " + versionHashId +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding deployment for version with hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbcConnection);
        }

    }

    /**
     * Method for getting deployments.
     *
     * @param versionHashId hash id of version
     * @return deployment object
     * @throws AppCloudException
     */
    public static Deployment getDeployment(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getDeployment(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting deployment for version with hash id : " + versionHashId +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for deleting deployment.
     *
     * @param versionHashId hash id of version
     * @throws AppCloudException
     */
    public static void deleteDeployment(String versionHashId) throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().deleteDeployment(dbConnection, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while deleting deployment for version with hash id : " + versionHashId +
                    " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for deleting deployment for version with hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting transports for runtime.
     *
     * @param runtimeId id of runtime
     * @return transports array
     * @throws AppCloudException
     */
    public static Transport[] getTransportsForRuntime(int runtimeId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<Transport> transports = ApplicationDAO.getInstance().
                    getTransportsForRuntime(dbConnection, runtimeId, tenantId);
            return transports.toArray(new Transport[transports.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while getting transports for runtime with id : " + runtimeId + " for tenant id : "
                    + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting runtime by id.
     *
     * @param runtimeId id of runtime
     * @return application runtime
     * @throws AppCloudException
     */
    public static ApplicationRuntime getRuntimeById(int runtimeId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getRuntimeById(dbConnection, runtimeId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting runtime by id for id : " + runtimeId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

     /**
     * Method for getting existing applications count per cloud
     *
     * @param cloudType cloud type
     * @return application count
     * @throws AppCloudException
     */
    public static int getRunningApplicationVersionCount(String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getRunningApplicationVersionCount(dbConnection, tenantId, cloudType);
        } catch (AppCloudException e) {
            String msg =
                    "Error while getting application count for tenant id : " + tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get custom domain by application hash id.
     *
     * @param applicationId hash id of application
     * @return custom domain
     * @throws AppCloudException
     */
    public static String getCustomDomain(String applicationId) throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getCustomDomain(dbConnection, applicationId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting custom domain with application hash id : " + applicationId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get default version by application hash id.
     *
     * @param applicationId hash id of application
     * @return default version
     * @throws AppCloudException
     */
    public static String getDefaultVersion(String applicationId) throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getDefaultVersion(dbConnection, applicationId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting default version with application hash id : " + applicationId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get container service proxy by version hash id.
     *
     * @param versionHashId hash id of version
     * @return list of container service proxies
     * @throws AppCloudException
     */
    public static List<ContainerServiceProxy> getContainerServiceProxyByVersion(String versionHashId)
            throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().
                    getContainerServiceProxyByVersion(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting container service proxy with version hash id : " + versionHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Update custom domain for particular application.
     *
     * @param applicationHashId hash id of application
     * @param customDomain      custom domain
     * @return is container service proxy service update successful or not
     * @throws AppCloudException
     */
    public static boolean updateCustomDomain(String applicationHashId, String customDomain) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            boolean isUpdateSuccess = ApplicationDAO.getInstance().
                    updateCustomDomain(dbConnection, applicationHashId, customDomain, tenantId);
            dbConnection.commit();
            return isUpdateSuccess;
        } catch (AppCloudException e) {
            String msg = "Error while updating the custom domain with application hash id : " + applicationHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for update custom domain with application hash id : "
                    + applicationHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Update default version field with mapped version for custom url.
     *
     * @param applicationHashId  hash id of application object
     * @param defaultVersionName name of default version
     * @return is default version update successful or not
     * @throws AppCloudException
     */
    public static boolean updateDefaultVersion(String applicationHashId, String defaultVersionName)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            boolean isUpdatedSuccess = ApplicationDAO.getInstance().
                    updateDefaultVersion(dbConnection, applicationHashId, defaultVersionName, tenantId);
            dbConnection.commit();
            return isUpdatedSuccess;
        } catch (AppCloudException e) {
            String msg = "Error while updating default version with application hash id : " + applicationHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String message = "Error while committing transaction for updating default version with application hash " +
                    "id : " + applicationHashId + " for tenant id : " + tenantId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting application version by running time period.
     *
     * @param numberOfHours number of hours the version has been running
     * @return array of version objects
     * @throws AppCloudException
     */
    public static Version[] getApplicationVersionsByRunningTimePeriod(int numberOfHours) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().
                    getApplicationVersionsByRunningTimePeriod(dbConnection, numberOfHours, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting application version by running time period for " + numberOfHours +
                    " numberOfHours for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get running applications of all tenants
     *
     * @return map of running applications
     * @throws AppCloudException
     */
    public static Map<Integer, List<Application>> getRunningApplicationsOfAllTenants() throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getRunningApplicationsOfAllTenants(dbConnection);
        } catch (AppCloudException e) {
            String msg = "Error while getting running applications of all tenants.";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get all application with running state.
     * @param tenantId
     * @return
     * @throws AppCloudException
     */
    public static Map<Integer, List<Application>> getRunningApplicationsOfTenant(int tenantId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getRunningApplicationsOfTenant(dbConnection, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting running applications of a tenant.";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting maximum application count for whitelisted tenant per cloud
     * @param tenantId id of tenant
     * @param cloudType cloud type
     * @return maximum application count
     * @throws AppCloudException
     */
    public static int getMaxAppCountForWhiteListedTenants(int tenantId, String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getWhiteListedTenantMaxAppCount(dbConnection, tenantId, cloudType);
        } catch (AppCloudException e) {
            String msg = "Error while getting maximum application count for whitelisted tenant for tenant id : " +
                    tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting maximum replication count for whitelisted tenant per cloud
     *
     * @param tenantId id of tenant
     * @param cloudType cloud type
     * @return maximum replication count
     * @throws AppCloudException
     */
    public static int getMaxReplicaCountForWhiteListedTenants(int tenantId, String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getWhiteListedTenantMaxReplicaCount(dbConnection, tenantId, cloudType);
        } catch (AppCloudException e) {
            String msg = "Error while getting maximum replication count for whitelisted tenant for tenant id : " +
                    tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting all versions of application object.
     *
     * @param applicationHashId hash id of application object
     * @return list of version objects
     * @throws AppCloudException
     */
    public static List<Version> getAllVersionsOfApplication(String applicationHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getAllVersionsOfApplication(dbConnection, applicationHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while getting versions list for application with hash id : " + applicationHashId
                    + " for tenant id : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for whitelisting application version.
     *
     * @param versionHashId hash id of version
     * @throws AppCloudException
     */
    public static void whiteListApplicationVersion(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().whiteListApplicationVersion(dbConnection, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error whitelisting application version hash id : " + versionHashId + " for tenant id : "
                    + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for whitelisting application version hash id : " +
                    versionHashId + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for whitelisting tenant per cloud
     *
     * @param tenantId         id of tenant
     * @param maxAppCount      maximum application count
     * @param maxDatabaseCount maximum database count
     * @param cloudType        cloud type
     * @param replicaCount        replication count
     * @throws AppCloudException
     */
    public static void whiteListTenant(int tenantId, int maxAppCount, int maxDatabaseCount, String cloudType, int replicaCount) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().whiteListTenant(dbConnection, tenantId, maxAppCount, maxDatabaseCount, cloudType, replicaCount);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error whitelisting tenant for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for whitelisting tenant for tenant id : " + tenantId +
                    " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for updating container specification.
     *
     * @param versionHashId hash id of version
     * @param memory        memory specification
     * @param cpu           cpu specification
     * @return is container specification update successful or not
     * @throws AppCloudException
     */
    public static boolean updateContainerSpecification(String versionHashId, int memory, int cpu)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            boolean isUpdatedSuccess = ApplicationDAO.getInstance().
                    updateContainerSpecification(dbConnection, versionHashId, memory, cpu, tenantId);
            dbConnection.commit();
            return isUpdatedSuccess;
        } catch (AppCloudException e) {
            String msg = "Error while updating container specification with application hash id : " + versionHashId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String message = "Error while committing transaction for updating container specification with " +
                    "application hash id : " + versionHashId + " for tenant id : " + tenantId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting maximum database count for white listed tenant per cloud
     * @param tenantId id of tenant
     * @param cloudType cloud type
     * @return maximum database count
     * @throws AppCloudException
     */
    public static int getMaxDatabaseCountForWhiteListedTenants(int tenantId, String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getWhiteListedTenantMaxDatabaseCount(dbConnection, tenantId, cloudType);
        } catch (AppCloudException e) {
            String msg = "Error while getting maximum database count for whitelisted tenant for tenant id : " +
                    tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for whitelisting maximum database count for tenant per cloud
     *
     * @param tenantId         id of tenant
     * @param maxDatabaseCount maximum database count
     * @param cloudType        cloud type
     * @throws AppCloudException
     */
    public static void whiteListMaxDatabaseCount(int tenantId, int maxDatabaseCount, String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().whiteListMaxDatabaseCount(dbConnection, tenantId, maxDatabaseCount, cloudType);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error whitelisting maximum database count for tenant id : " + tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for whitelisting maximum database count for tenant id : "
                    + tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for whitelisting maximum application count for tenant per cloud
     *
     * @param tenantId    id of tenant
     * @param maxAppCount maximum application count
     * @param cloudType   cloud type
     * @throws AppCloudException
     */
    public static void whiteListMaxAppCount(int tenantId, int maxAppCount, String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().whiteListMaxAppCount(dbConnection, tenantId, maxAppCount, cloudType);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error whitelisting maximum application count for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for whitelisting maximum application count for " +
                    "tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for whitelist and increase replica count for a given tenant
     *
     * @param tenantId tenant id
     * @param cloudType cloud type
     * @param replicaCount new replication count
     * @throws AppCloudException
     */
    public static void whiteListMaxReplicaCount(int tenantId, String cloudType, int replicaCount) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().whiteListMaxReplicaCount(dbConnection, tenantId, cloudType, replicaCount);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error whitelisting maximum replication count for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for whitelisting maximum replication count for " +
                    "tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    /**
     * Get the list of tagged applications per cloud
     *
     * @param cloudType cloud type
     * @return List of all the tagged applications per cloud
     * @throws AppCloudException
     */
    public static Application[] getTaggedApplicationsList(String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<Application> applications = ApplicationDAO.getInstance().
                    getTaggedApplicationsList(dbConnection, tenantId, cloudType);
            return applications.toArray(new Application[applications.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving tagged applications for tenant id : " + tenantId + " and cloud : " + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get version id.
     *
     * @param versionHashId hash id of version
     * @return version Id
     * @throws AppCloudException
     */
    public static int getVersionId(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getVersionId(dbConnection, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error occured while getting version id for version hash id : " + versionHashId
                         + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get list of application contexts.
     *
     * @param versionId version id
     * @return List of application contexts
     * @throws AppCloudException
     */
    public static List<String> getApplicationContexts(int versionId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getApplicationContexts(dbConnection, tenantId, versionId);
        } catch (AppCloudException e) {
            String msg = "Error occured while getting application context for version id : " + versionId
                    + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Add application context.
     *
     * @param versionId          version id
     * @param applicationContext application context
     * @throws AppCloudException
     */
    public static void addApplicationContext(int versionId, String applicationContext) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().addApplicationContext(dbConnection, tenantId, versionId, applicationContext);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error while adding application context for versionId : " + versionId +
                    " and applicationContext : " + applicationContext + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error while committing transaction for adding application context for versionId : " +
                    versionId + " and applicationContext : " + applicationContext + " for tenant id : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get all cloud types
     *
     * @return cloud types
     * @throws AppCloudException
     */
    public static String[] getCloudTypes() throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            List<String> cloudTypes = ApplicationDAO.getInstance().getCloudTypes(dbConnection);
            return cloudTypes.toArray(new String[cloudTypes.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving cloud types from database";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to check if given environment variable exists
     *
     * @param versionKey     version hash id
     * @param envVariableKey environment variable key
     * @return if the environment variable exists or not
     * @throws AppCloudException
     */
    public static boolean isEnvironmentVariableExist(String versionKey, String envVariableKey)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().
                    isEnvironmentVariableExist(dbConnection, versionKey, envVariableKey, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while checking if environment variable exists for version hash id: " + versionKey +
                    " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to check if given tag exists
     *
     * @param versionKey version hash id
     * @param tagKey     tag key
     * @return if the tag exists or not
     * @throws AppCloudException
     */
    public static boolean isTagExist(String versionKey, String tagKey) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().isTagExist(dbConnection, versionKey, tagKey, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while checking if tag exists for version hash id: " + versionKey +
                    " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to check if the given version exists in the applications version list
     *
     * @param applicationName application name
     * @param versionName     version name
     * @return if the given version exists or not
     * @throws AppCloudException
     */
    public static boolean isVersionExist(String applicationName, String versionName) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().isVersionExist(dbConnection, applicationName, versionName, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while checking if version exists for application name: " + applicationName +
                    " version name: " + versionName + " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to get the exposure level of a given application version
     *
     * @param versionKey version hash id
     * @return exposure level of the given application version
     * @throws AppCloudException
     */
    public static String getExposureLevel(String versionKey) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getExposureLevel(dbConnection, versionKey, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while checking for the exposure level for application version: " + versionKey +
                    " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * This method updates the exposure level of a given version
     *
     * @param exposureLevel exposure level to be updated for the given version
     * @param versionKey version hash id
     * @throws AppCloudException
     */
    public static void updateVersionExposureLevel(String exposureLevel, String versionKey) throws  AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().updateVersionExposureLevel(dbConnection, versionKey, tenantId, exposureLevel);
        } catch (AppCloudException e) {
            String msg = "Error while checking for the exposure level for application version: " + versionKey +
                    " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to get custom domain details for tenant
     *
     * @return list of custom domain details for all applications of tenant
     * @throws AppCloudException
     */
    public static Application[] getCustomDomainDetailsForTenant() throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<Application> applicationDetailsList = ApplicationDAO.getInstance().
                    getCustomDomainDetailsForTenant(dbConnection, tenantId);
            return applicationDetailsList.toArray(new Application[applicationDetailsList.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while getting custom domain details for tenant with tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to check if the custom domain is available
     *
     * @param customDomain custom domain
     * @return if the custom domain is available or not
     * @throws AppCloudException
     */
    public static boolean isCustomDomainAvailable(String customDomain) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().isCustomDomainAvailable(dbConnection, customDomain, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while checking if the custom domain exists for domain: " + customDomain +
                    " for tenant with tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * This method will update the replication count for a given version
     *
     * @param replicaCount replication count to be updated
     * @param versionKey version hash id
     * @throws AppCloudException
     */
    public static void updateReplicationCountForDeployment(int replicaCount, String versionKey) throws  AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ApplicationDAO.getInstance().updateReplicationCountForDeployment(dbConnection, versionKey, replicaCount, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while updating database for scaling deployment: " + versionKey +
                    " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    /**
     * Method for get replica existing replica count for a given version
     *
     * @param versionKey version hash id
     * @return current replica count
     * @throws AppCloudException
     */
    public static int getReplicaCountForVersion(String versionKey) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return ApplicationDAO.getInstance().getReplicaCountForVersion(dbConnection, versionKey, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while checking replica count for the application version: " + versionKey +
                    " and tenant id: " + tenantId + ".";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to get subscriotion details
     *
     * @param tenantId  tenant id
     * @param cloudType cloud type
     * @return Subscriotion object
     * @throws AppCloudException
     */
    public static Subscription getSubscription(int tenantId, String cloudType) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getSubscription(dbConnection, tenantId, cloudType);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving subscription info for tenant id : " + tenantId + " and cloud : "
                    + cloudType;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to create new subscription
     *
     * @param subscription subscription object
     * @throws AppCloudException
     */
    public static void addNewSubscription(Subscription subscription) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().addSubscription(dbConnection, subscription);
        } catch (AppCloudException e) {
            String msg = "Error while adding subscription for tenant id: " + subscription.getTenantId() + ", cloud : "
                    + subscription.getCloudType();
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method to update subscription
     *
     * @param subscription subscription object
     * @throws AppCloudException
     */
    public static void updateSubscription(Subscription subscription) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            ApplicationDAO.getInstance().updateSubscription(dbConnection, subscription);
        } catch (AppCloudException e) {
            String msg = "Error while updating subscription for tenant id: " + subscription.getTenantId() + ", cloud : "
                    + subscription.getCloudType();
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get version by hash id
     *
     * @param hashId hash id of version
     * @return version info
     * @throws AppCloudException
     */
    public static Version getVersionByHashId(String hashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().getVersionByHashId(dbConnection, hashId);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving version info for version hash id : " + hashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get allowed container specifications for runtime
     *
     * @param runtimeId id of runtime
     * @return list of container specifications
     * @throws AppCloudException
     */
    public static ContainerSpecification[] getAllowedContainerSpecificationsForRuntime(int runtimeId)
            throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            List<ContainerSpecification> containerSpecifications = ApplicationDAO.getInstance()
                    .getAllowedContainerSpecificationsForRuntime(dbConnection, runtimeId);
            return containerSpecifications.toArray(new ContainerSpecification[containerSpecifications.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving container specs for runtime id : " + runtimeId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get trial application versions by running time period.
     *
     * @param numberOfHours number of hours the application version has been running
     * @return array of version objects
     * @throws AppCloudException
     */
    public static Version[] getTrialApplicationVersionsByRunningTimePeriod(int numberOfHours) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().
                    getTrialApplicationVersionsByRunningTimePeriod(dbConnection, numberOfHours);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving TRIAL application versions which was created before " + numberOfHours + " hours ";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Get application versions which have reached cancel effective date.
     *
     * @return array of version objects
     * @throws AppCloudException
     */
    public static Version[] getApplicationVersionsReachedCancelEffectiveDate() throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            return ApplicationDAO.getInstance().
                    getApplicationVersionsReachedCancelEffectiveDate(dbConnection);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving application versions which have reached cancel effective date";
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }
}
