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

import {Event} from 'model/event/Event';

import {EventDisplayProperties} from 'model/EventDisplayProperties';
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";

interface Props {
    event: Event;
    display: EventDisplayProperties;
}

export function UnknownEventDisplay(props: Props) {
    return <EventDisplay event={props.event} display={props.display}>
        <p className={styles.textWrap}>{JSON.stringify(props.event)}</p>
    </EventDisplay>;
}
