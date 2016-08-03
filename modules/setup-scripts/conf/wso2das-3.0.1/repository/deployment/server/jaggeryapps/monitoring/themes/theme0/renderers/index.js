/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var render = function (theme, data, meta, require) {

    if(data.error.length == 0 ){
    theme('index', {
    config: [{
        context: {
            gadgetsUrlBase: data.config.gadgetsUrlBase
        }
    }],
    title: [{
        context:{
            page_title:'AS Dashboard'
        }
    }],
    header: [
        {
            partial: 'header',
            context:{
                user_name: data.user,
                user_avatar:'user'
            }
        }
    ],
    'sub-header': [
            {
                partial: 'sub-header',
                context:{
                    appname : data.appname,
                    aspect: data.aspect
                }
            }
        ],
    left_side:[
              	{
                partial: 'left_side',
                context: {
                    nav: data.nav,
                	user_name: data.user,
                	user_avatar:'user',
                    breadcrumb:'Service Cluster System Statistics'
                }
            }
     ],
     right_side: [

            {
            	partial: 'aggregated-index',
            	context:{
            		data:  data.panels,
                    updateInterval: data.updateInterval
            	}
            }
     ]
    });

    }else{

        theme('index', {
        title: [
             
         ],
         header:[
                    {
                        partial: 'header_login'
                    }
         ],
         body: [

                {
                    partial: 'error',
                    context:{
                        error:  data.error
                    }
                }
         ]
        });
    }
};