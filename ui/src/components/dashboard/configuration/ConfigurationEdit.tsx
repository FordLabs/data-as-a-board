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
import {connect} from "react-redux";
import {animated, useSpring} from "react-spring";

import {ApplicationState} from 'store/ApplicationState';

import styles from "./Configuration.module.css";
import ConfigurationForm from "./ConfigurationForm";

interface Props {
    isEditing: boolean;
    eventIds: string[];
}

function ConfigurationEdit(props: Props) {
    const transition = useSpring({
        bottom: props.isEditing ? 64 : window.innerHeight,
        top: props.isEditing ? 0 : -window.innerHeight + 64,
        backgroundColor: props.isEditing ? "#444444FF" : "#444444AA",
    });

    return <animated.div className={styles.container} style={transition}>
        <ConfigurationForm/>
        <datalist id="events">
            {props.eventIds.map(id => <option value={id} key={id}/>)}
        </datalist>
    </animated.div>;
}

const mapStateToProps = (state: ApplicationState): Props => ({
    isEditing: state.dashboard.isEditing,
    eventIds: Array.from(state.dashboard.events.keys()),
});

export default connect(mapStateToProps)(ConfigurationEdit);
