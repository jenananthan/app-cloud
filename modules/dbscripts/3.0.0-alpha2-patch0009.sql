-------------------------------------------------------------------------------------------------
-- app types per cloud
-------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `AppCloudDB`.`AC_CLOUD` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE (`name`))
ENGINE = InnoDB;

INSERT INTO `AC_CLOUD` (`id`, `name`) VALUES
(1, 'app-cloud'),
(2, 'integration-cloud');

CREATE TABLE IF NOT EXISTS `AppCloudDB`.`AC_CLOUD_APP_TYPE` (
  `cloud_id` INT NOT NULL,
  `app_type_id` INT NOT NULL,
  CONSTRAINT `fk_cloud_has_cloudAppType_cloud`
    FOREIGN KEY (`cloud_id`)
    REFERENCES `AppCloudDB`.`AC_CLOUD` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cloud_has_cloudAppType_appType`
    FOREIGN KEY (`app_type_id`)
    REFERENCES `AppCloudDB`.`AC_APP_TYPE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-------------------------------------------------------------------------------------------------
-- wso2esb app type
-------------------------------------------------------------------------------------------------

INSERT INTO `AC_APP_TYPE` (`id`, `name`, `description`) VALUES
(6, 'wso2esb', 'Allows you to deploy a esb configuration that is supported in WSO2 Enterprise Service Bus');

INSERT INTO `AC_CLOUD_APP_TYPE` (`cloud_id`, `app_type_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(2, 6);

INSERT INTO `AC_RUNTIME` (`id`, `name`, `repo_url`, `image_name`, `tag`, `description`) VALUES
(9, 'WSO2 Enterprise Service Bus - 5.0.0','registry.docker.appfactory.private.wso2.com:5000', 'wso2esb', '5.0.0', 'OS:Debian, Java Version:7u101');

INSERT INTO `AC_APP_TYPE_RUNTIME` (`app_type_id`, `runtime_id`) VALUES
(6, 9);

INSERT INTO `AC_RUNTIME_CONTAINER_SPECIFICATIONS` (`id`, `CON_SPEC_ID`) VALUES
(9, 3),
(9, 4);

INSERT INTO AC_TRANSPORT (`id`, `name`, `port`, `protocol`, `service_prefix`, `description`) VALUES
(7, "http", 8280, "TCP", "htp", "HTTP Protocol"),
(8, "https", 8243, "TCP", "hts", "HTTPS Protocol");

INSERT INTO AC_RUNTIME_TRANSPORT (`transport_id`, `runtime_id`) VALUES
(7, 9),
(8, 9);

-------------------------------------------------------------------------------------------------
-- AC_APPLICATION table schema update
-------------------------------------------------------------------------------------------------

ALTER TABLE AC_APPLICATION ADD `cloud_id` INT NOT NULL;
UPDATE AC_APPLICATION set `cloud_id` = 1;
ALTER TABLE AC_APPLICATION ADD CONSTRAINT fk_Application_CloudType1 FOREIGN KEY (cloud_id) REFERENCES AppCloudDB.AC_CLOUD (id) ON DELETE NO ACTION ON UPDATE NO ACTION;

-------------------------------------------------------------------------------------------------
-- subscription plan schema update
-------------------------------------------------------------------------------------------------

ALTER TABLE AC_SUBSCRIPTION_PLANS ADD CLOUD_ID INT NOT NULL;
UPDATE AC_SUBSCRIPTION_PLANS set CLOUD_ID = 1;
ALTER TABLE AC_SUBSCRIPTION_PLANS ADD CONSTRAINT fk_SubscriptionPlans_CloudType1 FOREIGN KEY (CLOUD_ID) REFERENCES AppCloudDB.AC_CLOUD (id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE AC_SUBSCRIPTION_PLANS ADD CONSTRAINT uk_SubscriptionPlans_PlanName_CloudId UNIQUE(PLAN_NAME, CLOUD_ID);

INSERT INTO AC_SUBSCRIPTION_PLANS (PLAN_ID, PLAN_NAME, MAX_APPLICATIONS, MAX_DATABASES, CLOUD_ID) VALUES
(3, 'FREE', 3, 3, 2),
(4, 'PAID', 10, 6, 2);

-------------------------------------------------------------------------------------------------
-- white listed tenats schema update
-------------------------------------------------------------------------------------------------

ALTER TABLE AC_WHITE_LISTED_TENANTS ADD cloud_id INT NOT NULL;
UPDATE AC_WHITE_LISTED_TENANTS set cloud_id = 1;
ALTER TABLE AC_WHITE_LISTED_TENANTS ADD CONSTRAINT fk_WhiteListedTenants_CloudType1 FOREIGN KEY (cloud_id) REFERENCES AppCloudDB.AC_CLOUD (id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE AC_WHITE_LISTED_TENANTS DROP index Unique_Constraint;
ALTER TABLE AC_WHITE_LISTED_TENANTS ADD CONSTRAINT uk_WhiteListedTenants UNIQUE (tenant_id, cloud_id);

-------------------------------------------------------------------------------------------------
-- remove 128 spec from MSF4J app type ******************** check before remove
-------------------------------------------------------------------------------------------------
delete from AC_RUNTIME_CONTAINER_SPECIFICATIONS  where id=8 and CON_SPEC_ID=1;

