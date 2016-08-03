/**
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

package org.wso2.appcloud.tierapi;

public class SQLQueryConstants {

    /*==============================
        Database Column Constants
      ==============================*/

    public static final String CON_SPEC_ID = "CON_SPEC_ID";
    public static final String CON_SPEC_NAME = "CON_SPEC_NAME";
    public static final String CPU = "CPU";
    public static final String MEMORY = "MEMORY";
    public static final String COST_PER_HOUR = "COST_PER_HOUR";
    public static final String PLAN_ID = "PLAN_ID";
    public static final String PLAN_NAME = "PLAN_NAME";
    public static final String MAX_APPLICATIONS = "MAX_APPLICATIONS";
    public static final String MAX_DATABASES = "MAX_DATABASES";

     /*==============================
        SQL Query Constants
      ==============================*/


    /*Insert Queries*/

    public static final String ADD_SUBSCRIPTION = "INSERT INTO AC_SUBSCRIPTION_PLANS (PLAN_NAME, TEAM, MAX_INSTANCES)"
            + " VALUES (?, ?)";

    public static final String ADD_CONTAINER_SPECIFICATION = "INSERT INTO AC_CONTAINER_SPECIFICATIONS (CON_SPEC_NAME, "
            + "CPU, MEMORY,COST_PER_HOUR) VALUES (?, ?, ?, ?)";

    /*Select Queries*/

    public static final String GET_ALL_SUBSCRIPTION_PLANS = "select * from AC_SUBSCRIPTION_PLANS";

    public static final String GET_SUBSCRIPTION_PLANS_BY_PLAN_ID = "select * from AC_SUBSCRIPTION_PLANS WHERE " +
            "PLAN_ID = ?";

    public static final String GET_SUBSCRIPTION_PLANS_BY_PLAN_NAME_AND_CLOUD = "select * from AC_SUBSCRIPTION_PLANS WHERE " +
            "PLAN_NAME = ? AND CLOUD_ID = (SELECT id from AC_CLOUD WHERE name=?)";

    public static final String GET_SUBSCRIPTION_PLANS_BY_PLAN_NAME = "select * from AC_SUBSCRIPTION_PLANS WHERE " +
            "PLAN_NAME = ?";

    public static final String GET_ALLOWED_CONTAINER_SPECIFICATIONS = "select * from AC_CONTAINER_SPECIFICATIONS WHERE "
            + "CON_SPEC_ID NOT IN (SELECT CON_SPEC_ID FROM AC_SUBSCRIPTION_PLANS JOIN RestrictedPlanContainerSpecs ON"
            + " AC_SUBSCRIPTION_PLANS.PLAN_ID = RestrictedPlanContainerSpecs.PLAN_ID WHERE"
            + " RestrictedPlanContainerSpecs.PLAN_ID = ?)";

    public static final String GET_ALL_CONTAINER_SPECIFICATIONS = "select * from AC_CONTAINER_SPECIFICATIONS";

    public static final String GET_CONTAINER_SPECIFICATIONS_BY_RUNTIME_ID = "SELECT * FROM AC_CONTAINER_SPECIFICATIONS "
            + "JOIN AC_RUNTIME_CONTAINER_SPECIFICATIONS ON AC_CONTAINER_SPECIFICATIONS.CON_SPEC_ID = " +
            "AC_RUNTIME_CONTAINER_SPECIFICATIONS.CON_SPEC_ID WHERE AC_RUNTIME_CONTAINER_SPECIFICATIONS.id = ?";

    public static final String GET_CONTAINER_SPECIFICATION_BY_ID = "select * from AC_CONTAINER_SPECIFICATIONS WHERE " +
            "CON_SPEC_ID = ?";

    public static final String GET_CONTAINER_SPECIFICATION_BY_NAME = "select * from AC_CONTAINER_SPECIFICATIONS WHERE "
            + "CON_SPEC_NAME= ?";

    /*Delete Queries*/

    public static final String DELETE_SUBSCRIPTION_PLAN = "DELETE FROM AC_SUBSCRIPTION_PLANS WHERE PLAN_ID = ?";

    public static final String DELETE_CONTAINER_SPECIFICATION = "DELETE FROM AC_CONTAINER_SPECIFICATIONS WHERE " +
            "CON_SPEC_ID=?";

    /*Update Queries*/

    public static final String UPDATE_SUBSCRIPTION_PLAN = "Update AC_SUBSCRIPTION_PLANS SET PLAN_NAME=?, " +
            "MAX_INSTANCES=?, WHERE PLAN_ID = ?";

    public static final String UPDATE_CONTAINER_SPECIFICATION = "Update AC_CONTAINER_SPECIFICATIONS SET " +
            "CON_SPEC_NAME=?, CPU= ?, MEMORY=?, COST_PER_HOUR=? WHERE CON_SPEC_ID = ?";
}
