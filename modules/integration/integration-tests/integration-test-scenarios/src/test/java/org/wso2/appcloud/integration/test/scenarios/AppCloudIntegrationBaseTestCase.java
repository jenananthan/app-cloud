package org.wso2.appcloud.integration.test.scenarios;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appcloud.integration.test.utils.AppCloudIntegrationTestConstants;
import org.wso2.appcloud.integration.test.utils.AppCloudIntegrationTestException;
import org.wso2.appcloud.integration.test.utils.AppCloudIntegrationTestUtils;
import org.wso2.appcloud.integration.test.utils.clients.ApplicationClient;
import org.wso2.appcloud.integration.test.utils.clients.LogsClient;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;

import java.io.File;
import java.util.Map;

/**
 * Basic test case to implement things common to all app types.
 */
public abstract class AppCloudIntegrationBaseTestCase {

	private static final Log log = LogFactory.getLog(AppCloudIntegrationBaseTestCase.class);
	public static final String PARAM_NAME_KEY = "key";
	public static final String PARAM_NAME_VALUE = "value";
	protected String defaultAdmin;
	protected String defaultAdminPassword;
	protected String defaultAppName;
	protected String serverUrl;
	protected String tenantDomain;
	protected String fileName;
	private String runtimeID;
	protected String sampleAppContent;
	protected long runtimeStartTimeout;
    protected ApplicationClient applicationClient;
	private LogsClient logsClient;
	protected String applicationName;
	protected String applicationType;
	protected String applicationRevision;
	protected String applicationDescription;
	protected String properties;
	protected String tags;
	private String applicationContext;
	private String conSpec;
	private boolean setDefaultVersion;
	private String defaultVersion;

	public AppCloudIntegrationBaseTestCase(String runtimeID, String fileName, String applicationType,
	                                       String sampleAppContent, long runtimeStartTimeout, String applicationContext,
	                                       String conSpec, boolean setDefaultVersion, String defaultVersion) {
		this.runtimeID = runtimeID;
		this.fileName = fileName;
		this.sampleAppContent = sampleAppContent;
		this.runtimeStartTimeout = runtimeStartTimeout;
		this.applicationContext = applicationContext;
		this.setDefaultVersion = setDefaultVersion;
		//Application details
		this.applicationName = AppCloudIntegrationTestUtils
				.getPropertyValue(AppCloudIntegrationTestConstants.APP_NAME_KEY);
		this.applicationType = applicationType;
		this.applicationRevision = AppCloudIntegrationTestUtils
				.getPropertyValue(AppCloudIntegrationTestConstants.APP_REVISION_KEY);
		this.applicationDescription = AppCloudIntegrationTestUtils
				.getPropertyValue(AppCloudIntegrationTestConstants.APP_DESC_KEY);
		this.properties = AppCloudIntegrationTestUtils.getKeyValuePairAsJsonFromConfig(
				AppCloudIntegrationTestUtils.getPropertyNodes(AppCloudIntegrationTestConstants.APP_PROPERTIES_KEY));
		this.tags = AppCloudIntegrationTestUtils.getKeyValuePairAsJsonFromConfig(
				AppCloudIntegrationTestUtils.getPropertyNodes(AppCloudIntegrationTestConstants.APP_TAGS_KEY));
		this.conSpec = conSpec;
		this.defaultVersion = defaultVersion;
	}

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		defaultAdmin = AppCloudIntegrationTestUtils.getAdminUsername();
		defaultAdminPassword = AppCloudIntegrationTestUtils.getAdminPassword();
		defaultAppName = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.APP_NAME_KEY);
		serverUrl = AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.URLS_APPCLOUD);
		tenantDomain =  AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.DEFAULT_TENANT_TENANT_DOMAIN);

		applicationClient = new ApplicationClient(serverUrl, defaultAdmin, defaultAdminPassword);
		logsClient = new LogsClient(serverUrl, defaultAdmin, defaultAdminPassword);

		createApplication();
	}

	public void createApplication() throws Exception {
		log.info("Application creation started for application type : " + applicationType);
		//Application creation
		File uploadArtifact = new File(TestConfigurationProvider.getResourceLocation() + fileName);
		applicationClient.createNewApplication(applicationName, this.runtimeID, applicationType, applicationRevision,
		                                       applicationDescription, this.fileName, properties, tags, uploadArtifact,
		                                       false, applicationContext, conSpec, true);

		//Wait until creation finished
		log.info("Waiting until application comes to running state...");
		RetryApplicationActions(applicationRevision, AppCloudIntegrationTestConstants.STATUS_RUNNING, "Application " +
		                                                                                              "creation");
		log.info("Testing default version update");
		boolean isDefaultVersionSet = isDefaultVersionNameSet(applicationRevision);
		Assert.assertTrue(isDefaultVersionSet, "Default version not set to the initial version");
	}

	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing application launch")
	public void testLaunchApplication() throws Exception {
		log.info("Waiting " + runtimeStartTimeout + "milliseconds before trying application launch...");
		Thread.sleep(runtimeStartTimeout);
		JSONObject applicationBean = applicationClient.getApplicationBean(applicationName);
		String launchURL = ((JSONObject) ((JSONObject) applicationBean
				.get(AppCloudIntegrationTestConstants.PROPERTY_VERSIONS_NAME))
				.get(applicationRevision)).getString(AppCloudIntegrationTestConstants.PROPERTY_DEPLOYMENT_URL);
		launchURL += this.applicationContext;
        //make the launch url http
		launchURL = launchURL.replace("https", "http");
		Boolean isLaunchSuccessfull = applicationClient.launchApplication(launchURL, sampleAppContent);
		Assert.assertTrue(isLaunchSuccessfull, "Application launch failed!");

		log.info("Testing default version launch...");
		Assert.assertTrue(isDefaultVersionLaunch(), "Default version is not launch");
	}

	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing application icon change", dependsOnMethods = {"testLaunchApplication"})
	public void testChangeApplicationIcon() throws Exception {
        String appIconImageFileName = "appIcon.png";
        File appIcon = new File(TestConfigurationProvider.getResourceLocation() + appIconImageFileName);
		String applicationHash = applicationClient.getApplicationHash(applicationName);
		applicationClient.changeAppIcon(applicationHash, appIcon);
		JSONObject applicationBean = applicationClient.getApplicationBean(applicationName);
        boolean isIconNull = (null == applicationBean.get(AppCloudIntegrationTestConstants.PARAM_ICON));
        // applicationBean.get("icon") should be NOT null, therefore isIconNull variable should be false,
        Assert.assertFalse(isIconNull, "Application icon change has been failed!");

	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing stop application action",  dependsOnMethods = {"testChangeApplicationIcon"})
	public void testStopApplication() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		applicationClient.stopApplicationRevision(applicationName, applicationRevision, versionHash);

		//Wait until stop application finished
		log.info("Waiting until application comes to stopped state");
		RetryApplicationActions(applicationRevision, AppCloudIntegrationTestConstants.STATUS_STOPPED, "Application stop action");
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing start application action", dependsOnMethods = {"testStopApplication"})
	public void testStartApplication() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
        applicationClient.startApplicationRevision(applicationName, applicationRevision, versionHash);

		//Wait until start application finished
		log.info("Waiting until application comes to running state...");
		RetryApplicationActions(applicationRevision, AppCloudIntegrationTestConstants.STATUS_RUNNING,
		                        "Application start action");
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing add runtime properties", dependsOnMethods = {"testStartApplication"})
	public void testAddEnvironmentalVariables() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		Map<String, String> properties = AppCloudIntegrationTestUtils.getKeyValuePairsFromConfig(
				AppCloudIntegrationTestUtils.getPropertyNodes(AppCloudIntegrationTestConstants.APP_NEW_PROPERTIES_KEY));
		for (String key : properties.keySet()) {
			applicationClient.addRuntimeProperty(versionHash, key, properties.get(key));
		}
		JSONArray jsonArray = applicationClient.getRuntimeProperties(versionHash);
		int i = 0;
		for (Object object : jsonArray) {
			JSONObject jsonObject = (JSONObject)object;
			if(properties.containsKey(jsonObject.getString(PARAM_NAME_KEY))){
				i++;
				Assert.assertEquals(jsonObject.getString(PARAM_NAME_VALUE), properties
						.get(jsonObject.getString(PARAM_NAME_KEY)), "Value of the property doesn't match.");
			}
		}
		Assert.assertTrue(i == properties.size(), "One or more Properties are not added.");
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing update runtime properties", dependsOnMethods = {"testAddEnvironmentalVariables"})
	public void testUpdateEnvironmentalVariables() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		JSONArray jsonArray = applicationClient.getRuntimeProperties(versionHash);
		JSONObject jsonObject = (JSONObject)jsonArray.get(0);
		String prevKey = jsonObject.getString(PARAM_NAME_KEY);
		String newKey = RandomStringUtils.random(5, true, false);
		String newValue = RandomStringUtils.random(6, true, false);
		applicationClient.updateRuntimeProperty(versionHash, prevKey, newKey, newValue);
		JSONArray updatedJSONArray = applicationClient.getRuntimeProperties(versionHash);
		boolean containsNewKey = false;
		for (Object object : updatedJSONArray) {
			JSONObject jsonOBJ = (JSONObject)object;
			if(newKey.equals(jsonOBJ.getString(PARAM_NAME_KEY))){
				containsNewKey = true;
				Assert.assertEquals(jsonOBJ.getString(PARAM_NAME_VALUE), newValue, "Property value doesn't match.");
			}
		}
		Assert.assertTrue(containsNewKey, "Property is not updated.");
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing update runtime properties", dependsOnMethods = {"testUpdateEnvironmentalVariables"})
	public void testDeleteEnvironmentalVariables() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		JSONArray jsonArray = applicationClient.getRuntimeProperties(versionHash);
		JSONObject jsonObject = (JSONObject)jsonArray.get(0);
		String key = jsonObject.getString(PARAM_NAME_KEY);
		applicationClient.deleteRuntimeProperty(versionHash, key);
		JSONArray updatedJSONArray = applicationClient.getRuntimeProperties(versionHash);
		boolean containsKey = false;
		for (Object object : updatedJSONArray) {
			JSONObject jsonOBJ = (JSONObject)object;
			if(key.equals(jsonOBJ.getString(PARAM_NAME_KEY))){
				containsKey = true;
			}
		}
		Assert.assertNotEquals("Property is not deleted.", containsKey);
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing add tags", dependsOnMethods = {"testDeleteEnvironmentalVariables"})
	public void testAddTags() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		Map<String, String> properties = AppCloudIntegrationTestUtils.getKeyValuePairsFromConfig(
				AppCloudIntegrationTestUtils.getPropertyNodes(AppCloudIntegrationTestConstants.APP_NEW_TAGS_KEY));
		for (String key : properties.keySet()) {
			applicationClient.addTag(versionHash, key, properties.get(key));
		}
		JSONArray jsonArray = applicationClient.getTags(versionHash);
		int i = 0;
		for (Object object : jsonArray) {
			JSONObject jsonObject = (JSONObject)object;
			if(properties.containsKey(jsonObject.getString(PARAM_NAME_KEY))){
				i++;
				Assert.assertEquals(jsonObject.getString(PARAM_NAME_VALUE), properties
						.get(jsonObject.getString(PARAM_NAME_KEY)), "Value of the property doesn't match.");
			}
		}
		Assert.assertTrue(i == properties.size(), "One or more Properties are not added.");
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing update tags", dependsOnMethods = {"testAddTags"})
	public void testUpdateTags() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		JSONArray jsonArray = applicationClient.getTags(versionHash);
		JSONObject jsonObject = (JSONObject)jsonArray.get(0);
		String prevKey = jsonObject.getString(PARAM_NAME_KEY);
		String newKey = RandomStringUtils.random(5, true, false);
		String newValue = RandomStringUtils.random(6, true, false);
		applicationClient.updateTag(versionHash, prevKey, newKey, newValue);
		JSONArray updatedJSONArray = applicationClient.getTags(versionHash);
		boolean containsNewKey = false;
		for (Object object : updatedJSONArray) {
			JSONObject jsonOBJ = (JSONObject)object;
			if(newKey.equals(jsonOBJ.getString(PARAM_NAME_KEY))){
				containsNewKey = true;
				Assert.assertEquals(jsonOBJ.getString(PARAM_NAME_VALUE), newValue, "Property value doesn't match.");
			}
		}
		Assert.assertTrue(containsNewKey, "Property is not updated.");
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing update tags", dependsOnMethods = {"testUpdateTags"})
	public void testDeleteTags() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		JSONArray jsonArray = applicationClient.getTags(versionHash);
		JSONObject jsonObject = (JSONObject)jsonArray.get(0);
		String key = jsonObject.getString(PARAM_NAME_KEY);
		applicationClient.deleteTag(versionHash, key);
		JSONArray updatedJSONArray = applicationClient.getTags(versionHash);
		boolean containsKey = false;
		for (Object object : updatedJSONArray) {
			JSONObject jsonOBJ = (JSONObject)object;
			if(key.equals(jsonOBJ.getString(PARAM_NAME_KEY))){
				containsKey = true;
			}
		}
		Assert.assertNotEquals("Property is not deleted.", containsKey);
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing create version", dependsOnMethods = {"testDeleteTags"})
	public void testCreateVersion() throws Exception {
		String applicationRevision =
				AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.APP_NEW_REVISION_KEY);
		File uploadArtifact = new File(TestConfigurationProvider.getResourceLocation() + fileName);
        applicationClient.createNewApplication(applicationName, this.runtimeID, applicationType, applicationRevision,
                                               applicationDescription, this.fileName, properties, tags, uploadArtifact,
                                               true, applicationContext, conSpec, setDefaultVersion);
		//Wait until creation finished
		log.info("Waiting until new version comes to running state");
		RetryApplicationActions(applicationRevision, AppCloudIntegrationTestConstants.STATUS_RUNNING,
				"Application version creation");

		if (setDefaultVersion) {
			log.info("Testing default version update");
			boolean isDefaultVersionSet = isDefaultVersionNameSet(applicationRevision);
			Assert.assertTrue(isDefaultVersionSet, "Default version not set to the current version");
			log.info("Waiting " + runtimeStartTimeout + "milliseconds before trying default version launch...");
			Thread.sleep(runtimeStartTimeout);
			Assert.assertTrue(isDefaultVersionLaunch(), "Default version is not launch");
		}
	}


	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing get logs", dependsOnMethods = {"testCreateVersion"})
	public void testGetLogs() throws Exception {
		log.info("Waiting " + runtimeStartTimeout + "milliseconds till runtime is started.");
		Thread.sleep(runtimeStartTimeout);
		String applicationHash = applicationClient.getApplicationHash(applicationName);
		String applicationRevision =
				AppCloudIntegrationTestUtils.getPropertyValue(AppCloudIntegrationTestConstants.APP_NEW_REVISION_KEY);
		// this array contains two elements , 0th element is response code , 1st is log
		String[] responseArray = logsClient.getSnapshotLogs(applicationHash, applicationRevision);
		Assert.assertEquals(responseArray[0], String.valueOf(HttpStatus.SC_OK), "Retrieving logs failed. " +
		                                                                        "Cannot connect to pod.");
		assertLogContent(responseArray[1]);
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM})
	@Test(description = "Testing delete version", dependsOnMethods = {"testGetLogs"})
	public void testDeleteVersion() throws Exception {
		String versionHash = applicationClient.getVersionHash(applicationName, applicationRevision);
		applicationClient.deleteVersion(versionHash);
		JSONArray jsonArray = applicationClient.getVersions(applicationName);
		boolean isDeleted = true;
		for (Object obj : jsonArray) {
			String versionName = obj.toString();
			if(versionName.equals(applicationRevision)){
				isDeleted = false;
			}
		}
		Assert.assertTrue(isDeleted, "Version Deletion Failed");
	}


	@AfterClass(alwaysRun = true)
	public void cleanEnvironment() throws Exception {
		String applicationHash = applicationClient.getApplicationHash(applicationName);
		boolean isDeleted = applicationClient.deleteApplication(applicationHash);
		Assert.assertTrue(isDeleted, "Application deletion failed");
		long timeOutPeriod = AppCloudIntegrationTestUtils.getTimeOutPeriod() / 5;
		log.info("Waiting " + timeOutPeriod + "milliseconds till application is deleted.");
		JSONObject applicationObj;
		int round = 1;
		int retryCount = AppCloudIntegrationTestUtils.getTimeOutRetryCount();
		while (round <= retryCount) {
			applicationObj = applicationClient.getApplicationBean(applicationName);
			if (!applicationObj.get(AppCloudIntegrationTestConstants.PROPERTY_APPLICATION_NAME).equals(null)) {
				Thread.sleep(timeOutPeriod);
				round++;
				continue;
			}
			break;
		}
	}

	/**
	 * Retry for application status to be changed to expected value for configured no of retries.
	 * @param applicationRevision Revision of the application wanted to check status for
	 * @param expectedStatus Expected Status of the application
	 * @param action Action to log in error messages
	 * @throws java.lang.Exception
	 */
	private void RetryApplicationActions(String applicationRevision, String expectedStatus, String action)
			throws Exception {
		long timeOutPeriod = AppCloudIntegrationTestUtils.getTimeOutPeriod() / 5;
		int retryCount = AppCloudIntegrationTestUtils.getTimeOutRetryCount();
		log.info("Time out period is set to - " + timeOutPeriod + " and retry count is set to - " + retryCount);
		int round = 1;
		String actualStatus = null;
		while (round <= retryCount) {
			log.info("RetryApplicationActions round : " + round + " for application : " + this.applicationName + " and"
			         + " application revision : " + applicationRevision);
			JSONObject result = applicationClient.getApplicationBean(applicationName);
			actualStatus = ((JSONObject) ((JSONObject) result
					.get(AppCloudIntegrationTestConstants.PROPERTY_VERSIONS_NAME))
					.get(applicationRevision)).getString(AppCloudIntegrationTestConstants.PROPERTY_STATUS_NAME);
			log.info("Application " + this.applicationName + " is : " + actualStatus);
			if (!expectedStatus.equals(actualStatus)) {
				log.info("Waiting " + timeOutPeriod + "milliseconds till " + action + " is completed.");
				Thread.sleep(timeOutPeriod);
				round++;
				continue;
			}
			break;
		}
		Assert.assertTrue(expectedStatus.equals(actualStatus), action + " failed");
	}

	protected abstract void assertLogContent(String logContent);

	private boolean isDefaultVersionNameSet(String versionName) throws AppCloudIntegrationTestException {
		try {
			JSONObject applicationObj = applicationClient.getApplicationBean(applicationName);
			String defaultVersion = applicationObj.getString(AppCloudIntegrationTestConstants.DEFAULT_VERSION);
			return versionName.equals(defaultVersion);
		} catch (Exception e) {
			throw new AppCloudIntegrationTestException(
					"Error while testing the set default version for initial application version", e);
		}
	}

	//TODO This method should uncomment when we removed the free tire restriction.
	/*
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.PLATFORM })
	@Test(description = "Testing change default version", dependsOnMethods = { "testCreateVersion" })
	public void changeDefaultVersion() {
		try {
			applicationClient.updateDefaultVersion(applicationName, defaultVersion);
			boolean isDefaultVersionSet = isDefaultVersionNameSet(defaultVersion);
			Assert.assertTrue(isDefaultVersionSet, "Default version change is not set");
			log.info("Waiting " + runtimeStartTimeout + "milliseconds before trying default version launch...");
			Thread.sleep(runtimeStartTimeout);
			Assert.assertTrue(isDefaultVersionLaunch(), "Default version is not launch");
		} catch (AppCloudIntegrationTestException e) {
			log.error("Error while testing change default version " + defaultVersion +
					"for application name : " + applicationName);
		} catch (InterruptedException e) {
			log.error("Error while sleep the thread", e);
		}
	}
	*/

	protected boolean isDefaultVersionLaunch() throws AppCloudIntegrationTestException {
		try {
			JSONObject applicationBean = applicationClient.getApplicationBean(applicationName);
			String defaultVersionLaunchURL = applicationBean
					.getString(AppCloudIntegrationTestConstants.DEFAULT_VERSION_URL);
			defaultVersionLaunchURL += this.applicationContext;
			//make the launch url http
			defaultVersionLaunchURL = defaultVersionLaunchURL.replace("https", "http");
			return applicationClient.launchApplication(defaultVersionLaunchURL, sampleAppContent);
		} catch (InterruptedException e) {
			throw new AppCloudIntegrationTestException("Error while sleep the thread", e);
		} catch (Exception e) {
			throw new AppCloudIntegrationTestException(
					"Error while default version launch for application : " + applicationName, e);
		}
	}
}
