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

package org.wso2.appcloud.core.dao;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.common.AppCloudException;
import org.wso2.appcloud.core.DBUtil;
import org.wso2.appcloud.core.SQLQueryConstants;
import org.wso2.appcloud.core.dto.Application;
import org.wso2.appcloud.core.dto.ApplicationRuntime;
import org.wso2.appcloud.core.dto.ApplicationType;
import org.wso2.appcloud.core.dto.Container;
import org.wso2.appcloud.core.dto.ContainerServiceProxy;
import org.wso2.appcloud.core.dto.Deployment;
import org.wso2.appcloud.core.dto.RuntimeProperty;
import org.wso2.appcloud.core.dto.Tag;
import org.wso2.appcloud.core.dto.Transport;
import org.wso2.appcloud.core.dto.Version;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO class for persisting or retrieving application related data to database.
 */
public class ApplicationDAO {

    private static final ApplicationDAO applicationDAO = new ApplicationDAO();

    /**
     * Constructor.
     */
    private ApplicationDAO() {

    }

    /**
     * Method for getting current instance.
     *
     * @return ApplicationDAO
     */
    public static ApplicationDAO getInstance() {
        return applicationDAO;
    }

    /**
     * Method for adding application details to database.
     *
     * @param dbConnection database connection
     * @param application  application object
     * @param tenantId     tenant id
     * @throws AppCloudException
     */
    public void addApplication(Connection dbConnection, Application application, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            int applicationId = 0;
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_APPLICATION,
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, application.getApplicationName());
            preparedStatement.setString(2, application.getHashId());
            preparedStatement.setString(3, application.getDescription());
            preparedStatement.setInt(4, tenantId);
            preparedStatement.setString(5, application.getDefaultVersion());
            preparedStatement.setString(6, application.getApplicationType());
            preparedStatement.setString(7, application.getCloudType());

            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();
            while (resultSet.next()) {
                applicationId = resultSet.getInt(1);
            }

            List<Version> versions = application.getVersions();

            if (versions != null) {
                for (Version version : versions) {
                    addVersion(dbConnection, version, applicationId, tenantId);
                }
            }

            InputStream iconInputStream;
            if (application.getIcon() != null) {
                iconInputStream = IOUtils.toBufferedInputStream(application.getIcon().getBinaryStream());
                if (iconInputStream.available() != 0) {
                    updateApplicationIcon(dbConnection, iconInputStream, applicationId, tenantId);
                }
            }

        } catch (SQLException e) {

            String msg =
                    "Error occurred while adding application : " + application.getApplicationName() + " to database " +
                            "in tenant : " + tenantId + " and cloud : " + application.getCloudType();
            throw new AppCloudException(msg, e);

        } catch (IOException e) {
            String msg =
                    "Error while generating stream of the icon for application : " + application.getApplicationName() +
                            " in tenant : " + tenantId + " and cloud : " + application.getCloudType();
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

    }


    /**
     * Method for adding version details to database.
     *
     * @param dbConnection  database connection
     * @param version       version object
     * @param applicationId application id
     * @param tenantId      tenant id
     * @throws AppCloudException
     */
    public void addVersion(Connection dbConnection, Version version, int applicationId, int tenantId)
            throws AppCloudException {


        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.
                    prepareStatement(SQLQueryConstants.ADD_VERSION, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, version.getVersionName());
            preparedStatement.setString(2, version.getHashId());
            preparedStatement.setInt(3, applicationId);
            preparedStatement.setInt(4, version.getRuntimeId());
            preparedStatement.setInt(5, tenantId);
            preparedStatement.setString(6, version.getConSpecCpu());
            preparedStatement.setString(7, version.getConSpecMemory());

            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();

            List<Tag> tags = version.getTags();
            if (tags != null) {
                addTags(dbConnection, tags, version.getHashId(), tenantId);
            }

            List<RuntimeProperty> runtimeProperties = version.getRuntimeProperties();
            if (runtimeProperties != null) {
                addRunTimeProperties(dbConnection, runtimeProperties, version.getHashId(), tenantId);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while adding application version to database for application id : "
                    + applicationId + " version : " + version.getVersionName() + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

    }


    /**
     * Method for adding label, which associated with a version of an application, to database.
     *
     * @param dbConnection  database Connection
     * @param tags          list of tags
     * @param versionHashId version hash id
     * @param tenantId      tenant id
     * @throws AppCloudException
     */
    public void addTags(Connection dbConnection, List<Tag> tags, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_TAG);

            for (Tag tag : tags) {

                preparedStatement.setString(1, tag.getTagName());
                preparedStatement.setString(2, tag.getTagValue());
                preparedStatement.setString(3, versionHashId);
                preparedStatement.setString(4, tag.getDescription());
                preparedStatement.setInt(5, tenantId);

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while adding tags to database for version with hash id : " + versionHashId +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);

        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for adding runtime property, which belongs to a version of an application, to the database.
     *
     * @param dbConnection      database connecton
     * @param runtimeProperties list of runtime properties
     * @param versionHashId     version hash id
     * @param tenantId          tenant id
     * @throws AppCloudException
     */
    public void addRunTimeProperties(Connection dbConnection, List<RuntimeProperty> runtimeProperties,
                                     String versionHashId, int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_RUNTIME_PROPERTY);

            for (RuntimeProperty runtimeProperty : runtimeProperties) {

                preparedStatement.setString(1, runtimeProperty.getPropertyName());
                preparedStatement.setString(2, runtimeProperty.getPropertyValue());
                preparedStatement.setString(3, versionHashId);
                preparedStatement.setString(4, runtimeProperty.getDescription());
                preparedStatement.setInt(5, tenantId);
                preparedStatement.setBoolean(6, runtimeProperty.isSecured());

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while adding property to the database for version id : " + versionHashId +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);

        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for adding deployment detail for version.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param deployment    deployment object
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void addDeploymentForVersion(Connection dbConnection, String versionHashId, Deployment deployment,
                                        int tenantId) throws AppCloudException {
        try {
            int deploymentId = addDeployment(dbConnection, deployment, tenantId);
            updateVersionWithDeployment(dbConnection, deploymentId, versionHashId, tenantId);
        } catch (AppCloudException e) {
            String msg = "Error while updating deployment detail for version with hash id : " + versionHashId +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        }
    }

    /**
     * Method for updating each version with the deployment.
     *
     * @param dbConnection  database connection
     * @param deploymentId  id of deployment
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    private void updateVersionWithDeployment(Connection dbConnection, int deploymentId, String versionHashId,
                                             int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_VERSION_WITH_DEPLOYMENT);
            preparedStatement.setInt(1, deploymentId);
            preparedStatement.setString(2, versionHashId);
            preparedStatement.setInt(3, tenantId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error while updating deployment detail for version with hash id : " + versionHashId +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for inserting deployment record.
     *
     * @param dbConnection database connection
     * @param deployment   deployment object
     * @param tenantId     id of tenant
     * @return deployment ID
     * @throws AppCloudException
     */
    private int addDeployment(Connection dbConnection, Deployment deployment, int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;
        int deploymentId = -1;
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.
                    prepareStatement(SQLQueryConstants.ADD_DEPLOYMENT, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, deployment.getDeploymentName());
            preparedStatement.setInt(2, deployment.getReplicas());
            preparedStatement.setInt(3, tenantId);
            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                deploymentId = resultSet.getInt(1);
            }

            for (Container container : deployment.getContainers()) {
                addContainer(dbConnection, container, deploymentId, tenantId);
            }

        } catch (SQLException e) {
            String msg = "Error while inserting deployment record in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        if (deploymentId == -1) {
            throw new AppCloudException("Failed to insert deployment record in tenant : " + tenantId);
        }
        return deploymentId;
    }

    /**
     * Method for adding container.
     *
     * @param dbConnection database connection
     * @param container    container object
     * @param deploymentId id of deployment
     * @param tenantId     id of tenant
     * @throws AppCloudException
     */
    public void addContainer(Connection dbConnection, Container container, int deploymentId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {
            int containerId = -1;
            preparedStatement = dbConnection.
                    prepareStatement(SQLQueryConstants.ADD_CONTAINER, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, container.getImageName());
            preparedStatement.setString(2, container.getImageVersion());
            preparedStatement.setInt(3, deploymentId);
            preparedStatement.setInt(4, tenantId);

            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                containerId = resultSet.getInt(1);
            }
            for (ContainerServiceProxy containerServiceProxy : container.getServiceProxies()) {
                addContainerServiceProxy(dbConnection, containerServiceProxy, containerId, tenantId);
            }

        } catch (SQLException e) {
            String msg = "Error while inserting deployment container record in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for adding container service proxy.
     *
     * @param dbConnection          database connection
     * @param containerServiceProxy container service proxy object
     * @param containerId           id of container object
     * @param tenantId              id of tenant
     * @throws AppCloudException
     */
    public void addContainerServiceProxy(Connection dbConnection, ContainerServiceProxy containerServiceProxy,
                                         int containerId, int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.
                    prepareStatement(SQLQueryConstants.ADD_CONTAINER_SERVICE_PROXY, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, containerServiceProxy.getServiceName());
            preparedStatement.setString(2, containerServiceProxy.getServiceProtocol());
            preparedStatement.setInt(3, containerServiceProxy.getServicePort());
            preparedStatement.setString(4, containerServiceProxy.getServiceBackendPort());
            preparedStatement.setInt(5, containerId);
            preparedStatement.setInt(6, tenantId);
            preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while inserting container service proxy record in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for updating application icon.
     *
     * @param dbConnection  database connection
     * @param inputStream   input stream object
     * @param applicationId id of application object
     * @throws AppCloudException
     */
    public void updateApplicationIcon(Connection dbConnection, InputStream inputStream, int applicationId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_ICON);
            preparedStatement.setBlob(1, inputStream);
            preparedStatement.setInt(2, applicationId);
            preparedStatement.execute();

        } catch (SQLException e) {
            String msg =
                    "Error occurred while updating application icon for application with id : " + applicationId +
                            " in tenant " + tenantId;
            throw new AppCloudException(msg, e);

        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }


    /**
     * Method for updating the status of the given version.
     *
     * @param status        status of the version
     * @param versionHashId version hash id
     * @throws AppCloudException
     */
    public void updateVersionStatus(Connection dbConnection, String status, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_STATUS);
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, versionHashId);
            preparedStatement.setInt(3, tenantId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error while updating application status : " + status + " for version with the hash id : " +
                    versionHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for updating runtime property.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param oldKey        old key of runtime property
     * @param newKey        new key of runtime property
     * @param newValue      new value of runtime property
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void updateRuntimeProperty(Connection dbConnection, String versionHashId, String oldKey, String newKey,
                                      String newValue, int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_RUNTIME_PROPERTIES);
            preparedStatement.setString(1, newKey);
            preparedStatement.setString(2, newValue);
            preparedStatement.setString(3, versionHashId);
            preparedStatement.setString(4, oldKey);
            preparedStatement.setInt(5, tenantId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error while updating runtime property Key : " + oldKey + " for version with hash id : " +
                    versionHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for updating tag.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param oldKey        old key of tag
     * @param newKey        new key of tag
     * @param newValue      new value of tag
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void updateTag(Connection dbConnection, String versionHashId, String oldKey, String newKey, String newValue,
                          int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_TAG);
            preparedStatement.setString(1, newKey);
            preparedStatement.setString(2, newValue);
            preparedStatement.setString(3, versionHashId);
            preparedStatement.setString(4, oldKey);
            preparedStatement.setInt(5, tenantId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error while updating tag Key : " + oldKey + " for version with hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }


    /**
     * Method for getting the list of applications of a tenant from database with minimal information.
     *
     * @param dbConnection  database connection
     * @param tenantId      tenant id
     * @param cloudType     cloud type
     * @return list of applications
     * @throws AppCloudException
     */
    public List<Application> getAllApplicationsList(Connection dbConnection, int tenantId, String cloudType) throws AppCloudException {

        PreparedStatement preparedStatement = null;

        List<Application> applications = new ArrayList<>();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_APPLICATIONS_LIST);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, cloudType);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Application application = new Application();
                application.setApplicationName(resultSet.getString(SQLQueryConstants.APPLICATION_NAME));
                application.setApplicationType(resultSet.getString(SQLQueryConstants.APPLICATION_TYPE_NAME));
                application.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                application.setIcon(resultSet.getBlob(SQLQueryConstants.ICON));

                applications.add(application);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving application list from database in tenant : " + tenantId + " and cloud : " + cloudType ;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applications;
    }

    /**
     * Method for getting all versions list of application object.
     *
     * @param dbConnection      database connection
     * @param applicationHashId hash id of application object
     * @param tenantId          id of tenant
     * @return list of versions
     * @throws AppCloudException
     */
    public List<String> getAllVersionListOfApplication(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ArrayList<String> versionList = new ArrayList<>();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_VERSION_LIST_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                versionList.add(resultSet.getString(SQLQueryConstants.NAME));
            }

        } catch (SQLException e) {
            String msg = "Error while getting the list of versions for the application with hash id : "
                    + applicationHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return versionList;
    }

    /**
     * Method for getting all version hash ids of application.
     *
     * @param dbConnection      database connection
     * @param applicationHashId hash id of application
     * @param tenantId          id of tenant
     * @return hash ids list of versions
     * @throws AppCloudException
     */
    public List<String> getAllVersionHashIdsOfApplication(Connection dbConnection, String applicationHashId,
                                                          int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ArrayList<String> hashIdList = new ArrayList<>();
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_VERSION_HASH_IDS_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                hashIdList.add(resultSet.getString(SQLQueryConstants.HASH_ID));
            }

        } catch (SQLException e) {
            String msg = "Error while getting the list of version hash ids of application : " + applicationHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return hashIdList;
    }

    /**
     * Method for checking if version is the only version.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @return if the version is the only version or not
     * @throws AppCloudException
     */
    public boolean isSingleVersion(Connection dbConnection, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean isSingleVersion = true;

        try {
            preparedStatement = dbConnection.
                    prepareStatement(SQLQueryConstants.GET_VERSION_HASH_IDS_OF_APPLICATION_BY_VERSION_HASH_ID);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            resultSet.last();

            if (resultSet.getRow() > 1) {
                isSingleVersion = false;
            }
        } catch (SQLException e) {
            String msg = "Error while retrieving the data for checking whether an application has multiple versions" +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return isSingleVersion;
    }

    /**
     * Method for getting application hash ids by version hash id.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @return application hash id
     * @throws AppCloudException
     */
    public String getApplicationHashIdByVersionHashId(Connection dbConnection, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String applicationHashId = null;

        try {
            preparedStatement = dbConnection.
                    prepareStatement(SQLQueryConstants.GET_APPLICATION_HASH_ID_BY_VERSION_HASH_ID);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applicationHashId = resultSet.getString(SQLQueryConstants.HASH_ID);
            }

        } catch (SQLException e) {
            String msg = "Error while getting application hash id by version hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applicationHashId;
    }

    /**
     * Method for getting application name by hash id.
     *
     * @param dbConnection      database connection
     * @param applicationHashId hash id of application
     * @param tenantId          id of tenant
     * @return application name
     * @throws AppCloudException
     */
    public String getApplicationNameByHashId(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String applicationName = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_NAME_BY_HASH_ID);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applicationName = resultSet.getString(SQLQueryConstants.NAME);
            }

        } catch (SQLException e) {
            String msg = "Error while getting the application name of application with hash id : " + applicationHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return applicationName;
    }

    /**
     * Method for getting application hash id by name.
     *
     * @param dbConnection    database connection
     * @param applicationName application name
     * @param tenantId        id of tenant
     * @return application hash id
     * @throws AppCloudException
     */
    public String getApplicationHashIdByName(Connection dbConnection, String applicationName, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String applicationHashId = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_HASH_ID_BY_NAME);
            preparedStatement.setString(1, applicationName);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applicationHashId = resultSet.getString(SQLQueryConstants.HASH_ID);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving application hash id using application name : " + applicationName +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return applicationHashId;
    }

    /**
     * Method for getting application from database using application hash id.
     *
     * @param dbConnection      database connection
     * @param applicationHashId application hash id
     * @return application object
     * @throws AppCloudException
     */
    public Application getApplicationByHashId(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        Application application = new Application();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_BY_HASH_ID);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                application.setApplicationName(resultSet.getString(SQLQueryConstants.NAME));
                application.setHashId(applicationHashId);
                application.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
                application.setDefaultVersion(resultSet.getString(SQLQueryConstants.DEFAULT_VERSION));
                application.setApplicationType(resultSet.getString(SQLQueryConstants.APPLICATION_TYPE_NAME));
                application.setIcon(resultSet.getBlob(SQLQueryConstants.ICON));
                application.setCustomDomain(resultSet.getString(SQLQueryConstants.CUSTOM_DOMAIN));
                application.setVersions(getAllVersionsOfApplication(dbConnection, applicationHashId, tenantId));

            }

        } catch (SQLException e) {
            String msg =
                    "Error while retrieving application detail for application with hash id : " + applicationHashId
                            + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return application;
    }

    /**
     * Method for retrieving all the versions of a specific application.
     *
     * @param dbConnection      database connection
     * @param applicationHashId hash id of application
     * @return list of versions
     * @throws AppCloudException
     */
    public List<Version> getAllVersionsOfApplication(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Version> versions = new ArrayList<>();
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_VERSIONS_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                Version version = new Version();
                version.setVersionName(resultSet.getString(SQLQueryConstants.NAME));
                version.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                version.setRuntimeName(resultSet.getString(SQLQueryConstants.RUNTIME_NAME));
                version.setRuntimeId(resultSet.getInt(SQLQueryConstants.RUNTIME_ID));
                version.setStatus(resultSet.getString(SQLQueryConstants.STATUS));
                version.setConSpecCpu(resultSet.getString(SQLQueryConstants.CON_SPEC_CPU));
                version.setConSpecMemory(resultSet.getString((SQLQueryConstants.CON_SPEC_MEMORY)));
                version.setIsWhiteListed(resultSet.getInt(SQLQueryConstants.IS_WHITE_LISTED));
                version.setTags(getAllTagsOfVersion(dbConnection, version.getHashId(), tenantId));
                version.setRuntimeProperties(
                        getAllRuntimePropertiesOfVersion(dbConnection, version.getHashId(), tenantId));

                versions.add(version);
            }

        } catch (SQLException e) {
            String msg = "Error while getting all versions of application with application hash id : "
                    + applicationHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return versions;
    }


    /**
     * Method for retrieving list of labels belongs to a given version of an application.
     *
     * @param dbConnection  database connection
     * @param versionHashId version hash id
     * @return list of tags
     * @throws AppCloudException
     */
    public List<Tag> getAllTagsOfVersion(Connection dbConnection, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Tag> tags = new ArrayList<>();
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_TAGS_OF_VERSION);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                Tag tag = new Tag();
                tag.setTagName(resultSet.getString(SQLQueryConstants.NAME));
                tag.setTagValue(resultSet.getString(SQLQueryConstants.VALUE));
                tag.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));

                tags.add(tag);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving tags from database for version with hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return tags;
    }


    /**
     * Method for retrieving all the runtime properties of a given version of an application.
     *
     * @param dbConnection  database connection
     * @param versionHashId version hash id
     * @return list of runtime properties
     * @throws AppCloudException
     */
    public List<RuntimeProperty> getAllRuntimePropertiesOfVersion(Connection dbConnection, String versionHashId,
                                                                  int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<RuntimeProperty> runtimeProperties = new ArrayList<>();

        try {

            preparedStatement = dbConnection.prepareStatement(
                    SQLQueryConstants.GET_ALL_RUNTIME_PROPERTIES_OF_VERSION);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                RuntimeProperty runtimeProperty = new RuntimeProperty();
                runtimeProperty.setPropertyName(resultSet.getString(SQLQueryConstants.NAME));
                runtimeProperty.setPropertyValue(resultSet.getString(SQLQueryConstants.VALUE));
                runtimeProperty.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
                runtimeProperty.setSecured(resultSet.getBoolean(SQLQueryConstants.IS_SECURED));

                runtimeProperties.add(runtimeProperty);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving the runtime properties from the database for version with hash id : " +
                    versionHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return runtimeProperties;
    }


    /**
     * Method for getting the id of an application with the given hash id.
     *
     * @param dbConnection      database connection
     * @param applicationHashId application hash id
     * @return application id
     * @throws AppCloudException
     */
    public int getApplicationId(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int applicationId = 0;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_ID);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applicationId = resultSet.getInt(SQLQueryConstants.ID);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving the id of application with hash value : " + applicationHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return applicationId;
    }


    /**
     * Method for getting the version id with the given hash id.
     *
     * @param dbConnection database connection
     * @param hashId       hash id of version
     * @return version id
     * @throws AppCloudException
     */
    public int getVersionId(Connection dbConnection, String hashId, int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;
        int versionId = 0;
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_VERSION_ID);
            preparedStatement.setString(1, hashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                versionId = resultSet.getInt(SQLQueryConstants.ID);
            }

        } catch (SQLException e) {
            String msg = "Error while retreiving id of version with hash value : " + hashId + " in tenant : "
                    + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return versionId;
    }

    /**
     * Method for retrieving all the application types for a given cloud type
     *
     * @param dbConnection database Connection
     * @param tenantId     tenant id
     * @param cloudType    cloud type
     * @return list of app types
     * @throws AppCloudException
     */
    public List<ApplicationType> getAllApplicationTypesForCloud(Connection dbConnection, int tenantId, String cloudType)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<ApplicationType> applicationTypeList = new ArrayList<>();

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_APP_TYPES_FOR_CLOUD);
            preparedStatement.setString(1, cloudType);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                ApplicationType applicationType = new ApplicationType();
                applicationType.setAppTypeName(resultSet.getString(SQLQueryConstants.NAME));
                applicationType.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
                applicationType.setBuildable(resultSet.getInt(SQLQueryConstants.BUILDABLE) == 1 ? true : false);
                applicationTypeList.add(applicationType);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving app types from database in tenant " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applicationTypeList;
    }

    /**
     * Method for retrieving all the runtimes for a given application type.
     *
     * @param appType application type
     * @return list of all application runtimes
     * @throws AppCloudException
     */
    public List<ApplicationRuntime> getRuntimesForAppType(Connection dbConnection, String appType, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<ApplicationRuntime> applicationRuntimeList = new ArrayList<ApplicationRuntime>();

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_RUNTIMES_FOR_APP_TYPE_OF_TENANT);
            preparedStatement.setString(1, appType);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                ApplicationRuntime applicationRuntime = new ApplicationRuntime();
                applicationRuntime.setId(resultSet.getInt(SQLQueryConstants.ID));
                applicationRuntime.setRuntimeName(resultSet.getString(SQLQueryConstants.NAME));
                applicationRuntime.setImageName(resultSet.getString(SQLQueryConstants.RUNTIME_IMAGE_NAME));
                applicationRuntime.setRepoURL(resultSet.getString(SQLQueryConstants.RUNTIME_REPO_URL));
                applicationRuntime.setTag(resultSet.getString(SQLQueryConstants.RUNTIME_TAG));
                applicationRuntime.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));

                applicationRuntimeList.add(applicationRuntime);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving list of runtime from database for app type : " + appType +
                    " in tenant " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applicationRuntimeList;
    }

    /**
     * Method for getting runtime by id.
     *
     * @param dbConnection database connection
     * @param runtimeId    id of runtime
     * @return application runtime object
     * @throws AppCloudException
     */
    public ApplicationRuntime getRuntimeById(Connection dbConnection, int runtimeId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        ApplicationRuntime applicationRuntime = new ApplicationRuntime();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_RUNTIME_BY_ID);
            preparedStatement.setInt(1, runtimeId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applicationRuntime.setId(resultSet.getInt(SQLQueryConstants.ID));
                applicationRuntime.setImageName(resultSet.getString(SQLQueryConstants.RUNTIME_IMAGE_NAME));
                applicationRuntime.setRepoURL(resultSet.getString(SQLQueryConstants.RUNTIME_REPO_URL));
                applicationRuntime.setRuntimeName(resultSet.getString(SQLQueryConstants.NAME));
                applicationRuntime.setTag(resultSet.getString(SQLQueryConstants.RUNTIME_TAG));
                applicationRuntime.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving runtime info from database for runtime : " + runtimeId +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applicationRuntime;
    }

    /**
     * Method for getting deployment.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @return deployment object
     * @throws AppCloudException
     */
    public Deployment getDeployment(Connection dbConnection, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        Deployment deployment = new Deployment();
        ResultSet resultSet = null;
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_DEPLOYMENT);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                deployment.setDeploymentName(resultSet.getString(SQLQueryConstants.NAME));
                deployment.setReplicas(resultSet.getInt(SQLQueryConstants.REPLICAS));
                deployment.setContainers(
                        getContainers(dbConnection, resultSet.getInt(SQLQueryConstants.ID), versionHashId, tenantId));
            }


        } catch (SQLException e) {
            String msg = "Error while inserting deployment record in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return deployment;
    }

    /**
     * Method for getting containers.
     *
     * @param dbConnection  database connection
     * @param deploymentId  id of deployment object
     * @param versionHashId hahs id of version
     * @param tenantId      id of tenant
     * @return
     * @throws AppCloudException
     */
    public Set<Container> getContainers(Connection dbConnection, int deploymentId, String versionHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        Set<Container> containers = new HashSet<>();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_CONTAINER);
            preparedStatement.setInt(1, deploymentId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                Container container = new Container();
                container.setImageName(resultSet.getString(SQLQueryConstants.NAME));
                container.setImageVersion(resultSet.getString(SQLQueryConstants.VERSION));
                container.setServiceProxies(
                        getContainerServiceProxies(dbConnection, resultSet.getInt(SQLQueryConstants.ID), tenantId));

                List<RuntimeProperty> runtimeProperties = getAllRuntimePropertiesOfVersion(dbConnection, versionHashId,
                        tenantId);
                container.setRuntimeProperties(runtimeProperties);
                containers.add(container);

            }

        } catch (SQLException e) {
            String msg = "Error while inserting deployment container record in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return containers;
    }

    /**
     * Method for getting container service proxies.
     *
     * @param dbConnection database connection
     * @param containerId  id of container object
     * @param tenantId     id of tenant
     * @return set of container service proxies
     * @throws AppCloudException
     */
    private Set<ContainerServiceProxy> getContainerServiceProxies(Connection dbConnection, int containerId,
                                                                  int tenantId) throws AppCloudException {

        PreparedStatement preparedStatement = null;
        Set<ContainerServiceProxy> containerServiceProxies = new HashSet<>();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_CONTAINER_SERVICE_PROXIES);
            preparedStatement.setInt(1, containerId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ContainerServiceProxy containerServiceProxy = new ContainerServiceProxy();
                containerServiceProxy.setServiceName(resultSet.getString(SQLQueryConstants.NAME));
                containerServiceProxy.setServiceProtocol(resultSet.getString(SQLQueryConstants.PROTOCOL));
                containerServiceProxy.setServicePort(resultSet.getInt(SQLQueryConstants.PORT));
                containerServiceProxy.setServiceBackendPort(resultSet.getString(SQLQueryConstants.BACKEND_PORT));
                containerServiceProxies.add(containerServiceProxy);
            }


        } catch (SQLException e) {
            String msg = "Error while inserting deployment service proxy record in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return containerServiceProxies;
    }

    /**
     * Method for getting transports for runtime.
     *
     * @param dbConnection database connection
     * @param runtimeId    id of runtime
     * @return list of transports
     * @throws AppCloudException
     */
    public List<Transport> getTransportsForRuntime(Connection dbConnection, int runtimeId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        List<Transport> transports = new ArrayList<>();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_TRANSPORTS_FOR_RUNTIME);
            preparedStatement.setInt(1, runtimeId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Transport transport = new Transport();
                transport.setServiceName(resultSet.getString(SQLQueryConstants.NAME));
                transport.setServiceProtocol(resultSet.getString(SQLQueryConstants.PROTOCOL));
                transport.setServicePort(resultSet.getInt(SQLQueryConstants.PORT));
                transport.setServiceNamePrefix(resultSet.getString(SQLQueryConstants.SERVICE_NAME_PREFIX));
                transports.add(transport);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving runtime transport detail for runtime : " + runtimeId + " in tenant : "
                    + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return transports;
    }

    /**
     * Method for deleteing runtime properties.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param key           key of runtime property
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void deleteRuntimeProperty(Connection dbConnection, String versionHashId, String key, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_RUNTIME_PROPERTY);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setString(2, key);
            preparedStatement.setInt(3, tenantId);

            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while deleting runtime property Key : " + key + " for version with hash id : " +
                    versionHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for deleting tag.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param key           key of tag
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void deleteTag(Connection dbConnection, String versionHashId, String key, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_TAG);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setString(2, key);
            preparedStatement.setInt(3, tenantId);

            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while deleting tag with Key : " + key + " for version with hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }


    /**
     * Method for deleting an application.
     *
     * @param applicationHashId application hash id.
     * @throws AppCloudException
     */
    public void deleteApplication(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_APPLICATION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while executing the application deletion sql query with applicationHashId : "
                    + applicationHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for deleting version.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void deleteVersion(Connection dbConnection, String versionHashId, int tenantId) throws AppCloudException {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_VERSION);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while executing the version deletion sql query with versionHashId : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Delete all the versions of an application.
     *
     * @param applicationHashId application hash id
     * @throws AppCloudException
     */
    public void deleteAllVersionsOfApplication(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_VERSIONS_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while deleting the versions of the application with hash id : " + applicationHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for deleting all deployments of application.
     *
     * @param dbConnection      database connection
     * @param applicationHashId hash id of application
     * @param tenantId          id of tenant
     * @throws AppCloudException
     */
    public void deleteAllDeploymentOfApplication(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_ALL_DEPLOYMENT_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);

            preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting all deployment of application with hash id : " + applicationHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }

    }

    /**
     * Method for deleting deployment.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void deleteDeployment(Connection dbConnection, String versionHashId, int tenantId) throws AppCloudException {
        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_DEPLOYMENT);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);

            preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting deployment record for version with the hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for getting application count per cloud
     *
     * @param dbConnection database connection
     * @param tenantId     id of tenant
     * @param cloudType    cloud type
     * @return application count
     * @throws AppCloudException
     */
    public int getRunningApplicationVersionCount(Connection dbConnection, int tenantId, String cloudType) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int appCount = 0;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_TENANT_RUNNING_CONTAINER_COUNT);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, cloudType);
            resultSet = preparedStatement.executeQuery();
            dbConnection.commit();
            if (resultSet.next()) {
                appCount = resultSet.getInt("ACTIVE_CONTAINERS_COUNT");
            }
        } catch (SQLException e) {
            String msg =
                    "Error while getting the application count in tenant : " + tenantId + " and cloud :" + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return appCount;
    }

    /**
     * Get service proxy for given version.
     *
     * @param versionHashId
     * @return list of container service proxies
     * @throws AppCloudException
     */
    public List<ContainerServiceProxy> getContainerServiceProxyByVersion(Connection dbConnection, String versionHashId,
                                                                         int tenantId) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        List<ContainerServiceProxy> containerServiceProxies = new ArrayList<ContainerServiceProxy>();
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_CONTAINER_SERVICE_PROXY);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setInt(2, tenantId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ContainerServiceProxy containerServiceProxy = new ContainerServiceProxy();
                containerServiceProxy.setServiceName(resultSet.getString(SQLQueryConstants.NAME));
                containerServiceProxy.setServiceProtocol(resultSet.getString(SQLQueryConstants.PROTOCOL));
                containerServiceProxy.setServicePort(resultSet.getInt(SQLQueryConstants.PORT));
                containerServiceProxy.setServiceBackendPort(resultSet.getString(SQLQueryConstants.BACKEND_PORT));
                containerServiceProxies.add(containerServiceProxy);
            }

            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while getting container service proxy with version hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return containerServiceProxies;
    }

    /**
     * Update custom domain for a particular application.
     *
     * @param dbConnection
     * @param applicationHashId
     * @param customDomain
     * @param tenantId
     * @return if successfully updated cosutom domain
     * @throws AppCloudException
     */
    public boolean updateCustomDomain(Connection dbConnection, String applicationHashId, String customDomain,
            int tenantId) throws AppCloudException {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_CUSTOM_DOMAIN);
            preparedStatement.setString(1, customDomain);
            preparedStatement.setString(2, applicationHashId);
            preparedStatement.setInt(3, tenantId);
            return preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error occurred while updating custom domain : " + customDomain +
                    " with application hash id : " + applicationHashId + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Get custom domain for particular application.
     *
     * @param dbConnection
     * @param applicationHashId
     * @param tenantId
     * @return if custom domain exist
     * @throws AppCloudException
     */
    public String getCustomDomain(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String customDomain = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_CUSTOM_DOMAIN);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                customDomain = resultSet.getString(SQLQueryConstants.CUSTOM_DOMAIN);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String message = "Error while getting custom domain for application hash id : " + applicationHashId +
                    " in tenant id : " + tenantId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return customDomain;
    }

    /**
     * Get default version for particular application.
     *
     * @param dbConnection
     * @param applicationHashId
     * @param tenantId
     * @return if default version exist
     * @throws AppCloudException
     */
    public String getDefaultVersion(Connection dbConnection, String applicationHashId, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String defaultVersion = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_DEFAULT_VERSION);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setInt(2, tenantId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                defaultVersion = resultSet.getString(SQLQueryConstants.DEFAULT_VERSION);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String message = "Error while getting default version for application hash id : " + applicationHashId +
                    " in tenant id : " + tenantId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return defaultVersion;
    }

    /**
     * Update default version for given application.
     *
     * @param applicationHashId
     * @return if sucessfully update the default version
     * @throws AppCloudException
     */
    public boolean updateDefaultVersion(Connection dbConnection, String applicationHashId, String defaultVersionName,
            int tenantId) throws AppCloudException {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_DEFAULT_VERSION);
            preparedStatement.setString(1, defaultVersionName);
            preparedStatement.setString(2, applicationHashId);
            preparedStatement.setInt(3, tenantId);
            return preparedStatement.execute();
        } catch (SQLException e) {
            String message = "Error while updating default version with application hash id : " + applicationHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for getting application versions by running time period.
     *
     * @param dbConnection  database connection
     * @param numberOfHours number of hours the application version has been running
     * @return array of version objects
     * @throws AppCloudException
     */
    public Version[] getApplicationVersionsByRunningTimePeriod(Connection dbConnection, int numberOfHours, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Version> versions = new ArrayList<>();

        try {

            preparedStatement = dbConnection.prepareStatement(
                    SQLQueryConstants.GET_ALL_APP_VERSIONS_CREATED_BEFORE_X_DAYS_AND_NOT_WHITE_LISTED);
            preparedStatement.setInt(1, numberOfHours);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Version version = new Version();
                version.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                version.setCreatedTimestamp(resultSet.getTimestamp(SQLQueryConstants.EVENT_TIMESTAMP));
                version.setTenantId(resultSet.getInt(SQLQueryConstants.TENANT_ID));

                versions.add(version);
            }


        } catch (SQLException e) {
            String msg = "Error while retrieving application version detail for non white listed applications in tenant"
                    + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return versions.toArray(new Version[versions.size()]);
    }

    /**
     * Method for getting maximum application count for whitelisted tenant per cloud.
     *
     * @param dbConnection database connection
     * @param tenantId     id of tenant
     * @param cloudType    cloud type
     * @return maximum application count
     * @throws AppCloudException
     */
    public int getWhiteListedTenantMaxAppCount(Connection dbConnection, int tenantId, String cloudType) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int maxAppCount;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_WHITE_LISTED_TENANT_DETAILS);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, cloudType);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                maxAppCount = resultSet.getInt(SQLQueryConstants.MAX_APP_COUNT);
            } else {
                maxAppCount = -1;
            }
        } catch (SQLException e) {
            String msg = "Error while getting Max App Count in tenant : " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return maxAppCount;
    }

    /**
     * Method for whitelisting application version.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param tenantId      id of tenant
     * @throws AppCloudException
     */
    public void whiteListApplicationVersion(Connection dbConnection, String versionHashId, int tenantId)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_WHITE_LIST_APPLICATION_VERSION);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, versionHashId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while white listing application version for version hash : " + versionHashId +
                    " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for whitelisting tenant per cloud
     *
     * @param dbConnection     database connection
     * @param tenantId         id of tenant
     * @param maxAppCount      maximum application count
     * @param maxDatabaseCount maximum database count
     * @param cloudType        cloud type
     * @throws AppCloudException
     */
    public void whiteListTenant(Connection dbConnection, int tenantId, int maxAppCount, int maxDatabaseCount, String cloudType)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_WHITE_LISTED_TENANT);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, maxAppCount);
            preparedStatement.setInt(3, maxDatabaseCount);
            preparedStatement.setString(4, cloudType);
            preparedStatement.setInt(5, maxAppCount);
            preparedStatement.setInt(6, maxDatabaseCount);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while white listing tenant in tenant : " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Update applications' version container specification.
     *
     * @param dbConnection  database connection
     * @param versionHashId hash id of version
     * @param memory        container memory
     * @param cpu           container cpu
     * @return if container specification was successfully updated or not
     * @throws AppCloudException
     */
    public boolean updateContainerSpecification(Connection dbConnection, String versionHashId, int memory, int cpu,
                                                int tenantId) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        boolean successfullyUpdated = false;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APP_VERSION_CON_SPEC);
            preparedStatement.setInt(1, memory);
            preparedStatement.setInt(2, cpu);
            preparedStatement.setString(3, versionHashId);
            preparedStatement.setInt(4, tenantId);
            successfullyUpdated = preparedStatement.execute();
        } catch (SQLException e) {
            String message = "Error while updating container specification with version hash id : " + versionHashId
                    + " in tenant : " + tenantId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return successfullyUpdated;
    }

    /**
     * Method for getting maximum database count for whitelisted tenant per cloud
     *
     * @param dbConnection database connection
     * @param tenantId     id of tenant
     * @param cloudType    cloud type
     * @return maximum database count
     * @throws AppCloudException
     */
    public int getWhiteListedTenantMaxDatabaseCount(Connection dbConnection, int tenantId, String cloudType) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int maxDatabaseCount;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_WHITE_LISTED_TENANT_DETAILS);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, cloudType);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                maxDatabaseCount = resultSet.getInt(SQLQueryConstants.MAX_DATABASE_COUNT);
            } else {
                maxDatabaseCount = -1;
            }
        } catch (SQLException e) {
            String msg = "Error while getting maximum database count in tenant : " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return maxDatabaseCount;
    }

    /**
     * Method for whitelisting maximum database count per cloud
     *
     * @param dbConnection     database connection
     * @param tenantId         id of tenant
     * @param maxDatabaseCount maximum database count
     * @param cloudType        cloud type
     * @throws AppCloudException
     */
    public void whiteListMaxDatabaseCount(Connection dbConnection, int tenantId, int maxDatabaseCount, String cloudType)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.
                    ADD_WHITE_LISTED_MAX_DATABASE_COUNT_FOR_TENANT);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, maxDatabaseCount);
            preparedStatement.setString(3, cloudType);
            preparedStatement.setInt(4, maxDatabaseCount);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while white listing maximum database count in tenant : " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for whitelisting maximum application count per cloud
     *
     * @param dbConnection database connection
     * @param tenantId     id of tenant
     * @param maxAppCount  maximum application count
     * @param cloudType    cloud type
     * @throws AppCloudException
     */
    public void whiteListMaxAppCount(Connection dbConnection, int tenantId, int maxAppCount, String cloudType)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.
                    ADD_WHITE_LISTED_MAX_APP_COUNT_FOR_TENANT);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, maxAppCount);
            preparedStatement.setString(3, cloudType);
            preparedStatement.setInt(4, maxAppCount);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while white listing maximum application count for tenant : " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Method for getting the list of tagged applications per cloud
     *
     * @param dbConnection database connection
     * @param tenantId     tenant id
     * @param cloudType    cloud type
     * @return taggedApplicationsList List of all the tagged applications per cloud
     * @throws AppCloudException
     */
    public List<Application> getTaggedApplicationsList(Connection dbConnection, int tenantId, String cloudType) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        List<Application> taggedApplicationsList = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_APPLICATIONS_LIST_WITH_TAG);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, cloudType);
            resultSet = preparedStatement.executeQuery();
            boolean applicationAddedtoList;
            while (resultSet.next()) {
                Tag tag;
                applicationAddedtoList = false;
                //Iterating the existing tagged application list to check whether the application is already added into the list
                for (Application application : taggedApplicationsList) {
                    String hashId = resultSet.getString(SQLQueryConstants.HASH_ID);
                    if (application.getHashId().equals(hashId)) {
                        applicationAddedtoList = true;
                        tag = new Tag();
                        tag.setTagName(resultSet.getString(SQLQueryConstants.TAG_KEY));
                        tag.setTagValue(resultSet.getString(SQLQueryConstants.TAG_VALUE));
                        if(application.getVersions() != null && application.getVersions().get(0) != null) {
                            application.getVersions().get(0).getTags().add(tag);
                        }
                    }
                }
                //Adding a new application if it is not already in the tagged application list
                if (!applicationAddedtoList) {
                    Application application = new Application();
                    application.setApplicationName(resultSet.getString(SQLQueryConstants.APPLICATION_NAME));
                    application.setApplicationType(resultSet.getString(SQLQueryConstants.APPLICATION_TYPE_NAME));
                    application.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                    application.setIcon(resultSet.getBlob(SQLQueryConstants.ICON));
                    List<Tag> tags = new ArrayList<>();
                    Version version = new Version();
                    tag = new Tag();
                    tag.setTagName(resultSet.getString(SQLQueryConstants.TAG_KEY));
                    tag.setTagValue(resultSet.getString(SQLQueryConstants.TAG_VALUE));
                    tags.add(tag);
                    version.setTags(tags);
                    application.setVersions(Collections.singletonList(version));
                    taggedApplicationsList.add(application);
                }
            }
        } catch (SQLException e) {
            String msg = "Error while retrieving application list from database in tenant : " + tenantId + " and cloud : " + cloudType;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return taggedApplicationsList;
    }

    /**
     * Method for getting the list of application contexts.
     *
     * @param dbConnection database connection
     * @param tenantId     id of tenant
     * @param versionId    id of version
     * @return
     * @throws AppCloudException
     */
    public List<String> getApplicationContexts(Connection dbConnection, int tenantId, int versionId)
            throws AppCloudException {
        List<String> applicationContextList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_CONTEXT);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, versionId);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                applicationContextList.add(resultSet.getString(SQLQueryConstants.CONTEXT));
            }
        } catch (SQLException e) {
            String msg = "Error while getting application contexts for version id :  " + versionId + " in tenant  : "
                    + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applicationContextList;
    }

    /**
     * Method for adding application context.
     *
     * @param dbConnection       database connection
     * @param tenantId           id of tenant
     * @param versionId          id of version
     * @param applicationContext application context
     * @throws AppCloudException
     */
    public void addApplicationContext(Connection dbConnection, int tenantId, int versionId, String applicationContext)
            throws AppCloudException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_APPLICATION_CONTEXT_FOR_APPLICATION);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, versionId);
            preparedStatement.setString(3, applicationContext);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while adding application context for version Id : " + versionId +
                    " and applicationContext : " + applicationContext + " in tenant : " + tenantId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    public List<String> getCloudTypes(Connection dbConnection) throws AppCloudException {
        PreparedStatement preparedStatement = null;
        List<String> cloudTypes = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_CLOUDS);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                cloudTypes.add(resultSet.getString(SQLQueryConstants.NAME));
            }
        } catch (SQLException e) {
            String msg = "Error while retrieving cloud types from database";
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return cloudTypes;
    }

}
