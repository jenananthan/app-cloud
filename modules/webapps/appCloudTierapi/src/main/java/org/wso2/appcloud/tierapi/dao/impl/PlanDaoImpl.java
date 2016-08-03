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

package org.wso2.appcloud.tierapi.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.tierapi.SQLQueryConstants;
import org.wso2.appcloud.tierapi.bean.ContainerSpecifications;
import org.wso2.appcloud.tierapi.bean.Plan;
import org.wso2.appcloud.tierapi.dao.PlanDao;
import org.wso2.appcloud.tierapi.util.DBUtil;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PlanDaoImpl implements PlanDao{

	private static final Log log = LogFactory.getLog(PlanDaoImpl.class);

	@Override
	public List<Plan> getAllPlans() throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		List<Plan> plans = new ArrayList<Plan>();
		ResultSet rs = null;
		try {
			dbConnection = DBUtil.getConnection();

			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_SUBSCRIPTION_PLANS);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				Plan plan = getPlan(rs);
				plans.add(plan);
			}
		} catch (SQLException e) {
			String msg = "Error while getting details of Plans";
			log.error(msg, e);
			throw e;
		} finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closePreparedStatement(preparedStatement);
			DBUtil.closeDatabaseConnection(dbConnection);
		}
		return plans;
	}

	@Override
	public Plan getPlanByPlanId(int planId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		Plan plan = new Plan();
		ResultSet rs = null;
		try {
			dbConnection = DBUtil.getConnection();
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_SUBSCRIPTION_PLANS_BY_PLAN_ID);
			preparedStatement.setInt(1, planId);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				plan = getPlan(rs);
			}
			rs.close();
		} catch (SQLException e) {
			String msg = "Error while getting plan for plan ID: " + planId;
			log.error(msg, e);
			throw e;
		} finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closePreparedStatement(preparedStatement);
			DBUtil.closeDatabaseConnection(dbConnection);
		}
		return plan;
	}

    @Override
    public Plan getPlanByPlanName(String cloudType, String planName) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Plan plan = new Plan();
        ResultSet rs = null;
        try {
            dbConnection = DBUtil.getConnection();
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_SUBSCRIPTION_PLANS_BY_PLAN_NAME_AND_CLOUD);
            preparedStatement.setString(1, planName);
            preparedStatement.setString(2, cloudType);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                plan = getPlan(rs);
            }
            rs.close();
        } catch (SQLException e) {
            String msg = "Error while getting plan for plan name: " + planName + " and cloud : " + cloudType;
            log.error(msg, e);
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeDatabaseConnection(dbConnection);
        }
        return plan;
    }

	@Override
	public Plan definePlan(Plan plan) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;

		try {
			dbConnection = DBUtil.getConnection();
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_SUBSCRIPTION);
			preparedStatement.setString(1, plan.getPlanName());
			preparedStatement.setInt(2, plan.getMaxApplications());

			preparedStatement.executeUpdate();
			preparedStatement.close();

			preparedStatement= dbConnection.prepareStatement(SQLQueryConstants.GET_SUBSCRIPTION_PLANS_BY_PLAN_NAME);
			preparedStatement.setString(1, plan.getPlanName());
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				plan = getPlan(rs);
			}

		} catch (SQLException e) {
			String msg = "Error while adding the Plans to Data Base";
			log.error(msg, e);
			throw e;
		} finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closePreparedStatement(preparedStatement);
			DBUtil.closeDatabaseConnection(dbConnection);
		}
		return plan;
	}

	@Override
	public boolean deletePlanById(int planId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		boolean isDeleted;
		try {
			dbConnection = DBUtil.getConnection();
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_SUBSCRIPTION_PLAN);
			preparedStatement.setInt(1, planId);
			isDeleted = preparedStatement.executeUpdate() == 1 ? true : false;
		} catch (SQLException e) {
			String msg = "Error while deleting the Plan with ID "+planId+"from Data Base";
			log.error(msg, e);
			throw e;
		} finally {
			DBUtil.closePreparedStatement(preparedStatement);
			DBUtil.closeDatabaseConnection(dbConnection);
		}
		return isDeleted;
	}

	@Override
	public Plan updatePlanById(int planId, Plan plan) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			dbConnection = DBUtil.getConnection();
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_SUBSCRIPTION_PLAN);

			preparedStatement.setString(1, plan.getPlanName());
			preparedStatement.setInt(2, plan.getMaxApplications());
			preparedStatement.setInt(3, planId);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_SUBSCRIPTION_PLANS_BY_PLAN_ID);
			preparedStatement.setInt(1, planId);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				plan = getPlan(rs);
			}
		} catch (SQLException e) {
			String msg = "Error while updating the Plan with ID "+planId+"from Data Base";
			log.error(msg, e);
			throw e;
		} finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closePreparedStatement(preparedStatement);
			DBUtil.closeDatabaseConnection(dbConnection);
		}
		return plan;
	}

	@Override
	public List<ContainerSpecifications> getAllowedConSpecs(int planId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		List<ContainerSpecifications> allowedContainerSpecs = new ArrayList<ContainerSpecifications>();
		try {
			dbConnection = DBUtil.getConnection();
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALLOWED_CONTAINER_SPECIFICATIONS);
			preparedStatement.setInt(1, planId);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				ContainerSpecifications containerSpecification = new ContainerSpecifications();
				containerSpecification.setId(rs.getInt("CON_SPEC_ID"));
				containerSpecification.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpecification.setCpu(rs.getInt("CPU"));
				containerSpecification.setMemory(rs.getInt("MEMORY"));
				containerSpecification.setCostPerHour(rs.getInt("COST_PER_HOUR"));
				allowedContainerSpecs.add(containerSpecification);
			}
		} catch (SQLException e) {
			String msg = "Error while getting details of container specifications that are allowed in Plan "
			             + "with ID the Plan with ID "+planId;
			log.error(msg, e);
			throw e;
		} finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closePreparedStatement(preparedStatement);
			DBUtil.closeDatabaseConnection(dbConnection);
		}
		return allowedContainerSpecs;
	}

	private Plan getPlan(ResultSet rs) throws SQLException {
		Plan plan = new Plan();
		plan.setId(rs.getInt(SQLQueryConstants.PLAN_ID));
		plan.setPlanName(rs.getString(SQLQueryConstants.PLAN_NAME));
		plan.setMaxApplications(rs.getInt(SQLQueryConstants.MAX_APPLICATIONS));
		plan.setMaxDatabases(rs.getInt(SQLQueryConstants.MAX_DATABASES));
		return plan;
	}
}
