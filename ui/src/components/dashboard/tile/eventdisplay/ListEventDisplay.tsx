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

import {EventDisplayProperties} from "../../../../model/EventDisplayProperties";
import {ListEvent, Section} from "../../../../model/ListEvent";
import {EventDisplay} from "./EventDisplay";

import styles from "./ListEventDisplay.module.css";

interface Props {
    event: ListEvent;
    display: EventDisplayProperties;
}

function ListItemDisplay(props: { item: string }) {
    return <li>{props.item}</li>;
}

function SectionDisplay(props: { section: Section }) {
    return <li className={styles.sectionName}>{props.section.name}
        <ul className={styles.section}>
            {props.section.items.map((item) => <ListItemDisplay key={item} item={item}/>)}
        </ul>
    </li>;
}

export function ListEventDisplay(props: Props) {
    const columns = Math.min(props.display.width || 1, props.event.sections.length);
    return <EventDisplay event={props.event} display={props.display}>
        <ul className={styles.sectionList} style={{
            gridTemplateColumns: "minmax(0, 1fr) ".repeat(columns).trim(),
        }}>
            {props.event.sections.map((section) => <SectionDisplay key={section.name} section={section}/>)}
        </ul>
    </EventDisplay>;
}
