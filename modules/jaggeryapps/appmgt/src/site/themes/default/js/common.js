/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
/**
 * @param data - datasource for select2 initialization
 * @param element - html element containing the select2 container
 * @returns {*|jQuery}
 */
function initSelect2(data, element, path) {
    /* Initialize select2 selection box for html element*/
    var $select = $(element).select2({
        placeholder: "Value",
        data: data,
        multiple: true,
        dropdownAutoWidth: true,
        containerCssClass: 'custom-env-class-for-demo select',
        width: '350px',
        tags: true,
        selectOnBlur: true,
        createSearchChoice: function(term, data) {
            if ($(data).filter(function() {
                    return this.text.localeCompare(term) === 0;
                }).length === 0) {
                return {
                    id: term,
                    text: term
                };
            }
        }
    });

    /* Display values based on selected value in dropdown */
    $select.on("select2:select", function(e) {
        e.params.data.text = e.params.data.id;
        $select.val(e.params.data.id).trigger('change');
        if (e.params.data.isNew != undefined && e.params.data.isNew) {
            $select.empty();
            $select.select2("val", "Invalid Selection");
        } else if (e.params.data.isNextLevel) {
            $select.empty();
            $select.trigger('change');
            $select = initSelect2(e.params.data.data, element, "");
            $select.select2('open');
        } else {
            var highlighted = $(e.target).data('select2').$dropdown.find('.select2-results__option--highlighted');
            if (highlighted) {
                var data = highlighted.data('data');
                var id = data.id;

                /* Display selected value in selection box */
                if (id != 0) {
                    var selection = data.text;
                    if (path.indexOf("database:") >= 0) {
                        if (id.toLowerCase() === selection.toLowerCase()) {
                            path += ("/" + selection);
                        } else {
                            path += ("/" + selection + "/" + id);
                        }
                        $select.attr("data-placeholder", path);
                    }
                    $select.select2("val", id);
                } else {
                    $select.select2("val", $(this).val());
                }
            }
        }
    });

    $(".select2-search__field").on('input', function(e) {
        if ($(this).val() == "database:") {
            var dbs;
            jagg.post("../blocks/database/list/ajax/list.jag", {
                action: "getAllDatabasesInfo"
            }, function(result) {
                dbs = JSON.parse(result);
                $select.trigger('change');
                $select = initSelect2(dbs, element, "database:");
                $select.select2('open');
            });
        }
    });

    $select.on("change", function(e) {
        var text = e.target.value;

        if (text.length > 45) {
            var truncatedText = jQuery.trim(text).substring(0, 42).slice(0, -1) + "...";
            $('.select2-selection__choice').text(truncatedText);
        }
    });

    //noinspection JSAnnotator
    return $select;
};