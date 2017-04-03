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

package org.wso2.appcloud.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.common.AppCloudException;
import org.wso2.appcloud.core.dao.EventsDAO;
import org.wso2.appcloud.core.dto.Event;
import org.wso2.carbon.context.CarbonContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * This class provide the interface for accessing the dao layer.
 */
public class EventsManager {

    private static final Log log = LogFactory.getLog(EventsManager.class);

    /**
     * Method for updating app creation events.
     *
     * @param versionHashId version Hash id
     * @param event         event object
     * @throws AppCloudException
     */
    public void addAppCreationEvent(String versionHashId, Event event) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            EventsDAO.getInstance().addAppCreationEvent(dbConnection, versionHashId, event, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error occured while adding application creation event for event with name : "
                    + event.getEventName() + ", status: " + event.getEventStatus() + ", timestamp : " +
                    event.getTimestamp() + " for version : " + versionHashId + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured committing transaction for adding application creation event for event with " +
                    "name : " + event.getEventName() + ", status : " + event.getEventStatus() + ", timestamp : " +
                    event.getTimestamp() + " for version : " + versionHashId + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for retrieving application creation event stream.
     *
     * @param versionHashId version hash id
     * @return array of event objects
     * @throws AppCloudException
     */
    public Event[] getEventsOfApplication(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<Event> events = EventsDAO.getInstance().getEventsOfApplication(dbConnection, versionHashId, tenantId);
            return events.toArray(new Event[events.size()]);
        } catch (AppCloudException e) {
            String msg = "Error while retrieving Application creation event stream for application with hash id : "
                    + versionHashId + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Delete all events related to a particluar version.
     *
     * @param versionHashId version hash id
     * @throws AppCloudException
     */
    public void deleteAllEventsofAppVersion(String versionHashId) throws AppCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            EventsDAO.getInstance().deleteAppVersionEvents(dbConnection, versionHashId, tenantId);
            dbConnection.commit();
        } catch (AppCloudException e) {
            String msg = "Error occurred while deleting all the events for the application version hash id : "
                    + versionHashId + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while committing transaction for deleting all the events for application " +
                    "version hash id : " + versionHashId + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }
}
