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
jQuery(window).load(function() {
   jQuery('#loader').fadeOut();
   jQuery('#contentloader').delay(350).fadeOut(function(){
      jQuery('as_body').delay(350).css({'overflow':'visible'});
   });
});

jQuery(document).ready(function() {
   
   jQuery('.toggle-sidebar').tooltip('hide');

   //My toggle menu
   jQuery('.toggle-sidebar').click(function(){
     
      var as_body = jQuery('body');
      
      if(as_body.css('position') != 'relative') {
         
         if(!as_body.hasClass('leftpanel-collapsed')) {
            as_body.addClass('leftpanel-collapsed');
            jQuery('.nav-as ul').attr('style','');
            
            jQuery(this).addClass('menu-collapsed');
            
         } else {
            as_body.removeClass('leftpanel-collapsed chat-view');
            jQuery('.nav-as li.active ul').css({display: 'block'});
            
            jQuery(this).removeClass('menu-collapsed');
            
         }
      } else {
         
         if(as_body.hasClass('leftpanel-show'))
            as_body.removeClass('leftpanel-show');
         else
            as_body.addClass('leftpanel-show');
         
         panelfixdevice();         
      }

   });

   function panelfixdevice() {
      if(jQuery(document).height() > jQuery('.right-side').height()){
         jQuery('.right-side').height(jQuery(document).height());
      }
   }
   panelfixdevice();


   // Toggle Left Menu
   jQuery('.left-content-list .nav-parent > a').on('click', function() {

      var parent = jQuery(this).parent();
      var sub = parent.find('> ul');

      if(!jQuery('as_body').hasClass('leftpanel-collapsed')) {
         if(sub.is(':visible')) {
            sub.slideUp(200, function(){
               parent.removeClass('nav-active');
               jQuery('.right-side').css({height: ''});
               panelfixdevice();
            });
         } else {
            hideSMenu();
            parent.addClass('nav-active');
            sub.slideDown(200, function(){
               panelfixdevice();
            });
         }
      }
      return false;
   });
   

   function hideSMenu() {
      jQuery('.left-content-list .nav-parent').each(function() {
         var t = jQuery(this);
         if(t.hasClass('nav-active')) {
            t.find('> ul').slideUp(200, function(){
               t.removeClass('nav-active');
            });
         }
      });
   }



   jQuery('.nav-as > li').hover(function(){
      jQuery(this).addClass('nav-hover');
   }, function(){
      jQuery(this).removeClass('nav-hover');
   });
   
});