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
import {HealthEvent} from "../../../model/HealthEvent";
import Icon from "../../icon/Icon";
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";
import {EventDisplayProperties} from "../../../model/EventDisplayProperties";

export interface Props {
    event: HealthEvent
    display: EventDisplayProperties
}

export function HealthEventDisplay(props: Props) {
    return <EventDisplay event={props.event} display={props.display} timePrefix={"since"}>
        <div className={styles.figureWithLabel}>
            {icon(props.event)}
            <span>{displayStatus(props.event)}</span>
        </div>
    </EventDisplay>
}

function icon(event: HealthEvent) {
    switch (event.status) {
        case "UP":
            return Icon.ok;
        case "DOWN":
            return Icon.warn;
        default:
            return Icon.unknown;
    }
}

function displayStatus(event: HealthEvent): string {
    switch (event.status) {
        case "UP":
            return "Up";
        case "DOWN":
            return "Down";
        default:
            return "Unknown";
    }
}
