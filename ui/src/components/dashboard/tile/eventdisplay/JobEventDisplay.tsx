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

import {JobEvent} from "../../../../model/JobEvent";
import Icon from "../../../icon/Icon";
import {HealthEvent} from "../../../../model/HealthEvent";
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";
import {EventDisplayProperties} from "../../../../model/EventDisplayProperties";

export interface Props {
    event: JobEvent
    display: EventDisplayProperties
}

export function JobEventDisplay(props: Props) {
    return <EventDisplay event={props.event} display={props.display}>
        <div className={styles.figureWithLabel}>
            {icon(props.event)}
            <span>{displayStatus(props.event)}</span>
        </div>
    </EventDisplay>
}

function icon(event: HealthEvent) {
    switch (event.status) {
        case "SUCCESS":
            return Icon.ok;
        case "IN_PROGRESS":
            return Icon.inProgress;
        case "UNSTABLE":
            return Icon.unstable;
        case "FAILURE":
            return Icon.warn;
        case "DISABLED":
            return Icon.disabled;
        default:
            return Icon.unknown;
    }
}

function displayStatus(event: HealthEvent): string {
    switch (event.status) {
        case "SUCCESS":
            return "Success";
        case "IN_PROGRESS":
            return "In Progress";
        case "UNSTABLE":
            return "Unstable";
        case "FAILURE":
            return "Failure";
        case "DISABLED":
            return "Disabled";
        default:
            return "Unknown";
    }
}
