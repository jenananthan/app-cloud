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

package org.wso2.appcloud.tierapi.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Service class defines operations related to Plan related services.
 */

public interface PlanService {

    /**
     * Get all Plans
     *
     * @return {@link Response}
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPlans();

    /**
     * Get Plan using Plan ID
     *
     * @param planId    Plan ID of the plan
     * @return {@link Response}
     */
    @GET
    @Path("/{planId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPlan(@PathParam("planId") int planId);

    /**
     * Get Plan using Plan name and cloud
     *
     * @param cloudType cloud type
     * @param planName name of the plan
     * @return {@link Response}
     */
    @GET
    @Path("/{cloudType}/{planName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPlan(@PathParam("cloudType") String cloudType, @PathParam("planName") String planName);

    /**
     * Get allowed container specifications using Plan ID
     *
     * @param planId    Plan ID of the plan
     * @return {@link Response}
     */
    @GET
    @Path("/allowedSpecs/{planId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllowedConSpecs(@PathParam("planId") int planId);

}
