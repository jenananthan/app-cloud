/*
 * Copyright 2016 WSO2, Inc. (http://wso2.com)
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
package org.wso2.appcloud.integration.test.scenarios;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appcloud.integration.test.utils.AppCloudIntegrationTestConstants;
import org.wso2.appcloud.integration.test.utils.AppCloudIntegrationTestException;
import org.wso2.appcloud.integration.test.utils.AppCloudIntegrationTestUtils;
import org.wso2.appcloud.integration.test.utils.clients.DatabaseClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

/**
 * Test case for testing all database related operations in app cloud.
 */
public class DatabaseTestCase {

    private DatabaseClient databaseClient;
    private String defaultAdmin;
    private String defaultAdminPassword;
    private String serverUrl;
    private String dbName;
    private String dbUserName;
    private String dbUserName2;
    private String dbUserPassword ;
    private String tenantDomain;
    private String dbNameWithTenantName;
    private String dbUserNameWithTenantHash;
    private String dbUserName2WithTenantHash;
    private final String PROPERTY_USER_NAME = "userName";


    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        serverUrl = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.URLS_APPCLOUD);
        dbName = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.DATABASE_NAME);
        dbUserName = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.DATABASE_USER_ONE);
        dbUserName2 = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.DATABASE_USER_TWO);
        dbUserPassword = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.DATABASE_PASSWORD);
        defaultAdmin = AppCloudIntegrationTestUtils.getAdminUsername();
        defaultAdminPassword = AppCloudIntegrationTestUtils.getAdminPassword();
        tenantDomain = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants
                                                                             .DEFAULT_TENANT_TENANT_DOMAIN);
        dbNameWithTenantName = dbName + AppCloudIntegrationTestConstants.PARAM_UNDERSCORE + tenantDomain.replace(".", "_");
        databaseClient = new DatabaseClient(serverUrl, defaultAdmin, defaultAdminPassword);

    }


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Checking whether database already exists")
    public void testCheckDatabaseAlreadyExists() throws AppCloudIntegrationTestException {
        Assert.assertEquals(databaseClient.getDatabases().toString()
                                          .contains(dbNameWithTenantName), false, "Database already exists!");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create database", dependsOnMethods = {"testCheckDatabaseAlreadyExists"})
    public void testCreateDatabase() throws AppCloudIntegrationTestException {
       JSONObject result =  databaseClient.createDatabaseAndAttachUser(dbName, dbUserName, dbUserPassword,
                                                             AppCloudIntegrationTestConstants.TRUE_STRING);
        dbUserNameWithTenantHash = result.get(PROPERTY_USER_NAME).toString();
        Assert.assertEquals(databaseClient.getDatabases().toString()
                                          .contains(dbNameWithTenantName), true, "Creating database not success.");
        Assert.assertEquals(databaseClient.getDatabases().toString()
                                          .contains(dbUserNameWithTenantHash), true, "Database user is not attached");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Checking whether user already exists", dependsOnMethods = {"testCreateDatabase"})
    public void testCheckUserAlreadyExists() throws AppCloudIntegrationTestException {
        Assert.assertEquals(databaseClient.getDatabaseUsers(dbNameWithTenantName).toString()
                                          .contains(dbUserName2), false, "Database user already exists!");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Create new database user", dependsOnMethods = {"testCheckUserAlreadyExists"})
    public void testCreateNewDatabaseUser() throws AppCloudIntegrationTestException {
        JSONObject result = databaseClient.createDatabaseUser(dbUserName2, dbUserPassword);
        dbUserName2WithTenantHash = result.get(PROPERTY_USER_NAME).toString();
        Assert.assertEquals(databaseClient.getDatabaseUsers(dbNameWithTenantName).toString()
                                          .contains(dbUserName2WithTenantHash), true, "Database user creation failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Attaching User For the initially created database", dependsOnMethods =
            {"testCreateNewDatabaseUser"})
    public void testAttachUserForDatabase() throws AppCloudIntegrationTestException {
        databaseClient.attachUserToDatabase(dbNameWithTenantName, dbUserName2WithTenantHash);
        Assert.assertEquals(databaseClient.getDatabases().toString()
                                          .contains(dbUserName2WithTenantHash), true, "Database user attaching failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Detaching the user from database", dependsOnMethods =
            {"testAttachUserForDatabase"})
    public void testDetachUserFromDatabase() throws AppCloudIntegrationTestException {
        databaseClient.detachUserFromDatabase(dbNameWithTenantName, dbUserName2WithTenantHash);
        Assert.assertEquals(databaseClient.getDatabases().toString()
                                          .contains(dbUserName2WithTenantHash), false, "User detaching failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Deleting database user", dependsOnMethods =
            {"testDetachUserFromDatabase"})
    public void testDeleteDatabaseUser() throws AppCloudIntegrationTestException {
        databaseClient.deleteDatabaseUser(dbUserName2WithTenantHash);
        Assert.assertEquals(databaseClient.getDatabaseUsers(dbNameWithTenantName).toString()
                                          .contains(dbUserName2WithTenantHash), false, "Database user deletion failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Deleting database", dependsOnMethods =
            {"testDeleteDatabaseUser"})
    public void testDeleteDatabase() throws AppCloudIntegrationTestException {
        databaseClient.deleteDatabase(dbNameWithTenantName);
        Assert.assertEquals(databaseClient.getDatabases().toString()
                                          .contains(dbNameWithTenantName), false, "Database deletion failed");
    }

    @AfterClass(alwaysRun = true)
    public void cleanEnvironment() throws Exception {
        databaseClient.detachUserFromDatabase(dbNameWithTenantName, dbUserNameWithTenantHash);
        databaseClient.detachUserFromDatabase(dbNameWithTenantName, dbUserName2WithTenantHash);
        databaseClient.deleteDatabaseUser(dbUserNameWithTenantHash);
        databaseClient.deleteDatabaseUser(dbUserName2WithTenantHash);
        databaseClient.deleteDatabase(dbNameWithTenantName);

    }
}
