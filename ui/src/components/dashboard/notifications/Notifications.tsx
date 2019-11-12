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
import {animated, useTransition} from "react-spring";

import {Event, Level} from 'model/Event';
import {ApplicationState} from 'store/ApplicationState';

import EventWrapper from "../tile/eventdisplay/EventWrapper";
import styles from "./Notifications.module.css";

function compareLevel(a: Level, b: Level) {
    const map: Map<Level, number> = new Map<Level, number>();
    map.set(Level.OK, 0);
    map.set(Level.DISABLED, 1);
    map.set(Level.UNKNOWN, 2);
    map.set(Level.INFO, 3);
    map.set(Level.WARN, 4);
    map.set(Level.ERROR, 5);

    return map.get(b)! - map.get(a)!;
}

function compareTimestamp(a: string, b: string) {
    return new Date(a).getUTCMilliseconds() - new Date(b).getUTCMilliseconds();
}

interface Props {
    events: Event[];
}

function Notifications(props: Props) {
    const transitions = useTransition(
        props.events,
        (event) => event.id,
        {
            from: {transform: "translate3d(420px,0,0)", maxHeight: 0},
            leave: {transform: "translate3d(420px,0,0)", maxHeight: 0},
            enter: {transform: "translate3d(0,0,0)", maxHeight: 300},
        },
    );

    return <div className={styles.notificationsContainer}>
        {transitions.map(({item, props: animatedProps, key}) =>
            (<animated.div
                key={key}
                className={styles.eventContainer}
                style={animatedProps}
            >
                <EventWrapper event={item} eventDisplay={{id: item.id, isNotification: true, tileType: "EVENT"}}/>
            </animated.div>),
        )}
    </div>;
}

const mapStateToProps = (state: ApplicationState): Props => ({
    events: Array.from(state.dashboard.events.values())
        .filter((event) => event.level !== Level.OK)
        .sort((a, b) => {
            const comparingLevel = compareLevel(a.level, b.level);

            if (comparingLevel === 0) {
                return compareTimestamp(a.time, b.time);
            }

            return comparingLevel;
        }),
});

export default connect(mapStateToProps)(Notifications);
