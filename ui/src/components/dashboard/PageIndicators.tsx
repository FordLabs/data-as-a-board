/*
 * Copyright (c) 2020 Ford Motor Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

import React from "react";
import styles from "./Dashboard.module.css";
import {PageIndicator} from "./PageIndicator";
import {ApplicationState} from 'store/ApplicationState';
import {gotoPage} from 'store/dashboard/actions';
import {connect} from "react-redux";

interface Props {
    currentPage: number;
    totalPages: number;

    onIndicatorClick(pageNumber: number): any;
}

function PageIndicators(props: Props) {
    return (
        <div className={styles.radiatorPageIndicators} data-testid={"@dashboard-indicators"}>
            {
                [...Array(props.totalPages)].map(
                    (x, i) => <PageIndicator
                        key={i}
                        forPage={i}
                        currentPage={props.currentPage}
                        onIndicatorClick={props.onIndicatorClick}/>,
                )
            }
        </div>
    );
}

const mapStateToProps = (state: ApplicationState) => ({
    currentPage: state.dashboard.currentPage,
    totalPages: state.dashboard.configuration.pages.length,
});

const mapDispatchToProps = {
    onIndicatorClick: gotoPage,
};

export default connect(mapStateToProps, mapDispatchToProps)(PageIndicators);
