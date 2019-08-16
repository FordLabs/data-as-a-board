/*
 * Copyright (c) 2019 Ford Motor Company
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
import {connect} from "react-redux";
import {useInterval} from "../../hooks/useInterval";
import {Page} from "../../model/Page";
import {ApplicationState} from "../../store/ApplicationState";
import {incrementPage} from "../../store/dashboard/actions";
import styles from "./Dashboard.module.css";
import {PageDisplay} from "./page/PageDisplay";

interface Props {
    pages: Page[];
    currentPage: number;

    onPageIncrement(): void;
}

function Carousel(props: Props) {
    useInterval(props.onPageIncrement, 30000);

    return <div className={styles.radiatorCarousel} style={{left: "calc(-100vw * " + props.currentPage + ")"}}>
        {props.pages
        && props.pages.map((page, index) =>
            <PageDisplay
                key={index}
                name={page.name}
                pageEvents={page.events}
                rows={page.rows}
                columns={page.columns}
            />)
        }
    </div>;
}

const mapStateToProps = (state: ApplicationState) => ({
    pages: state.dashboard.configuration.pages,
    currentPage: state.dashboard.currentPage,
});

const mapDispatchToProps = {
    onPageIncrement: incrementPage,
};

export default connect(mapStateToProps, mapDispatchToProps)(Carousel);
