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

import {Event, Level} from 'model/Event';

import {EventDisplayProperties} from 'model/EventDisplayProperties';
import styles from "./EventDisplay.module.css";
import {TimestampDisplay} from "./TimestampDifference";

interface Props {
    event: Event;
    display: EventDisplayProperties;
    timePrefix?: string;
}

function mapLevelToStyle(event: Event) {
    switch (event.level) {
        case Level.OK:
            return styles.ok;
        case Level.UNKNOWN:
            return styles.unknown;
        case Level.DISABLED:
            return styles.unknown;
        case Level.INFO:
            return styles.info;
        case Level.WARN:
            return styles.warn;
        case Level.ERROR:
            return styles.error;
    }
}

function gridStyle(display: EventDisplayProperties) {
    return {
        gridColumnStart: display.column !== undefined ? display.column + 1 : undefined,
        gridRowStart: display.row !== undefined ? display.row + 1 : undefined,
        gridColumnEnd: "span " + (display.width ? display.width : 1),
        gridRowEnd: "span " + (display.height ? display.height : 1),
    };
}

export const EventDisplay: React.FC<Props> = (props) => {
    const eventName = (props.event.name || "").toUpperCase();

    const eventType: string =
        props.event.eventType && props.event.eventType !== "UNKNOWN"
            ? props.event.eventType.toUpperCase()
            : " ";

    const fillStyle = props.display.fill ? styles.event_fill : "";
    const alertStyle = props.display.isNotification ? "" : styles.shake;

    return <div
        className={`${styles.event} ${fillStyle} ${alertStyle} ${mapLevelToStyle(props.event)}`}
        style={props.display.isNotification ? {} : gridStyle(props.display)}
    >
        {!props.display.fill && <div className={styles.header}>{eventName}</div>}
        <div className={styles.body}>{props.children}</div>
        {!props.display.fill && <div className={styles.footer}>
            <span className={styles.type}>{eventType}</span>
            <span className={styles.time}>
                {props.timePrefix} <TimestampDisplay time={props.event.time}/>
            </span>
        </div>}
    </div>;
};
