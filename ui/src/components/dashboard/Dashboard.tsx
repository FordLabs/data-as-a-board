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

import {ApplicationState} from "../../store/ApplicationState";

import Carousel from "./Carousel";
import ConfigurationEdit from "./configuration/ConfigurationEdit";
import DisconnectedDisplay from "./DisconnectedDisplay";
import Header from "./header/Header";
import Notifications from "./notifications/Notifications";
import PageIndicators from "./PageIndicators";

import styles from "./Dashboard.module.css";

interface Props {
    background?: string;
}

function dashboardStyle(props: Props) {
    return {background: props.background ? `#222 url(${props.background}) no-repeat fixed center/contain` : undefined};
}

function Dashboard(props: Props) {
    const style = dashboardStyle(props);
    return <div className={styles.dashboard} style={style}>
        <Carousel/>
        <PageIndicators/>
        <Notifications/>
        <Header/>
        <ConfigurationEdit/>
        <DisconnectedDisplay/>
    </div>;
}

const mapStateToProps = (state: ApplicationState) => ({
    background: state.dashboard.configuration.background,
});

export default connect(mapStateToProps)(Dashboard);
