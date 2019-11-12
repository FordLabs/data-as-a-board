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

import {QuoteEvent} from "../../../../model/QuoteEvent";
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";
import React from "react";
import {EventDisplayProperties} from "../../../../model/EventDisplayProperties";

interface Props {
    event: QuoteEvent;
    display: EventDisplayProperties;
}

export function QuoteEventDisplay(props: Props) {
    const quoteDisplayClassName = [styles.quoteDisplay];
    if (props.display && props.display.width && props.display.width > 1) {
        quoteDisplayClassName.push(styles["quoteDisplay--wide"]);
    }

    return <EventDisplay event={props.event} display={props.display}>
        <div className={quoteDisplayClassName.join(" ")}>
            <div className={styles.quoteDisplay__quote}>{props.event.quote}</div>
            {props.event.author && <div className={styles.quoteDisplay__author}>{props.event.author}</div>}
        </div>
    </EventDisplay>;
}
