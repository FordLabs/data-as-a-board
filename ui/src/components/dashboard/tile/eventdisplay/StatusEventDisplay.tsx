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
import {HealthEvent} from 'model/event/HealthEvent';
import Icon from "../../../icon/Icon";
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";
import {EventDisplayProperties} from 'model/EventDisplayProperties';
import { Level } from "model/event/Event";
import {StatusEvent} from "../../../../model/event/StatusEvent";

interface Props {
    event: StatusEvent
    display: EventDisplayProperties
}

export function StatusEventDisplay(props: Props) {
    return <EventDisplay event={props.event} display={props.display} timePrefix={props.event.continuous ? "since" : undefined}>
        <div className={styles.figureWithLabel}>
            {icon(props.event)}
            <span>{displayStatus(props.event)}</span>
        </div>
    </EventDisplay>
}

function icon(event: StatusEvent) {
    switch (event.level) {
        case Level.OK:
            return Icon.ok;
        case Level.DISABLED:
            return Icon.disabled;
        case Level.INFO:
            return Icon.info;
        case Level.IN_PROGRESS:
            return Icon.inProgress;
        case Level.WARN:
            return Icon.warn;
        case Level.ERROR:
            return Icon.error;
        case Level.UNKNOWN:
        default:
            return Icon.unknown;
    }
}

function displayStatus(event: StatusEvent): string {
    if(event.statusText) {
        return event.statusText;
    }

    switch (event.level) {
        case Level.OK:
            return "OK";
        case Level.UNKNOWN:
            return "Unknown";
        case Level.DISABLED:
            return "Disabled";
        case Level.IN_PROGRESS:
            return "In Progress";
        case Level.INFO:
            return "Info";
        case Level.WARN:
            return "Warn";
        case Level.ERROR:
            return "Error";
    }
}
