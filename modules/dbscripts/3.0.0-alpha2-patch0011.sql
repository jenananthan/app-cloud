--
--  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
--
--    WSO2 Inc. licenses this file to you under the Apache License,
--    Version 2.0 (the "License"); you may not use this file except
--    in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing,
--    software distributed under the License is distributed on an
--    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
--    KIND, either express or implied.  See the License for the
--    specific language governing permissions and limitations
--    under the License.
--

-- --------------------------------------------------------
-- Migration - Adding MSF4J 1.0.0 runtime deprecate notice
-- --------------------------------------------------------
UPDATE `AC_RUNTIME`
SET `name` = 'OpenJDK 8 + WSO2 MSF4J 1.0.0 - Deprecated Runtime (Will continue to work until 2016/9/30)'
WHERE `id`= 2;

-- --------------------------------------------------------
-- Migration - Adding Apache Tomcat 8.0.28 / WSO2 Application Server 6.0.0-M1 runtime deprecate notice
-- --------------------------------------------------------
UPDATE `AC_RUNTIME`
SET `name` = 'Apache Tomcat 8.0.28 / WSO2 Application Server 6.0.0-M1 - Deprecated (will work until 2016/09/30)'
WHERE `id`= 1;

-- --------------------------------------------------------
-- Adding Apache Tomcat 8.0.28 / WSO2 Application Server 6.0.0-M3 runtime
-- --------------------------------------------------------
INSERT INTO `AC_RUNTIME` (`id`, `name`, `repo_url`, `image_name`, `tag`, `description`) VALUES
(10, 'Apache Tomcat 8.0.28 / WSO2 Application Server 6.0.0-M3','registry.docker.appfactory.private.wso2.com:5000', 'wso2as', '6.0.0-m3', 'OS:Debian, JAVA Version:8u72');

INSERT INTO `AC_APP_TYPE_RUNTIME` (`app_type_id`, `runtime_id`) VALUES
(1, 10);

INSERT INTO `AC_RUNTIME_CONTAINER_SPECIFICATIONS` (`id`, `CON_SPEC_ID`) VALUES
(10, 3),
(10, 4);

INSERT INTO AC_RUNTIME_TRANSPORT (`transport_id`, `runtime_id`) VALUES
(3, 10),
(4, 10);

-- --------------------------------------------------------
-- Adding Apache Tomcat 8.0.28 / WSO2 Application Server 6.0.0-M3 runtime as a runtime of jaggery app type
-- --------------------------------------------------------
INSERT INTO `AC_APP_TYPE_RUNTIME` (`app_type_id`, `runtime_id`) VALUES
(4, 10);

-- --------------------------------------------------------
-- Deprecating  Jaggery 0.11.0 runtime
-- --------------------------------------------------------
UPDATE `AC_RUNTIME`
SET `name` = 'Jaggery 0.11.0 - Deprecated Runtime (Will continue to work until 2016/9/30)'
WHERE `id`= 5;

-- --------------------------------------------------------
-- Increasing version length
-- --------------------------------------------------------
ALTER TABLE `AC_VERSION` CHANGE COLUMN `name` `name` VARCHAR(60) NOT NULL;