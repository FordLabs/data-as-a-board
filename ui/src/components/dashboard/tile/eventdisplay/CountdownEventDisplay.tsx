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

import React, {useMemo, useState} from "react";
import moment from "moment";

import {EventDisplayProperties} from 'model/EventDisplayProperties';
import {CountdownEvent} from "model/event/CountdownEvent";
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";
import {useInterval} from "../../../../hooks/useInterval";
import {humanizeDurationPrecise} from "../../../../converters/humanizeDurationPrecise";
import {Level} from "../../../../model/event/Event";

interface Props {
    event: CountdownEvent;
    display: EventDisplayProperties;
}

export function CountdownEventDisplay(props: Props) {
    const [now, setNow] = useState(moment());
    useInterval(() => setNow(moment()), 1000);
    const countdownTime = useMemo(
        () => moment(props.event.countdownTime),
        [props.event.countdownTime]
    );

    const difference = moment.duration(countdownTime.diff(now));

    const humanized = humanizeDurationPrecise(difference);
    const indexOfFirstSpace = humanized.indexOf(' ');

    const value = humanized.slice(0, indexOfFirstSpace);
    const subtext = humanized.slice(indexOfFirstSpace);

    const renderEvent = {
        ...props.event,
        level: countdownTime.isBefore(now) ? Level.ERROR : props.event.level
    };

    return <EventDisplay event={renderEvent} display={props.display}>
        <div className={styles.figureWithLabel}>
            <span>{value}</span>
            <span>{subtext}</span>
        </div>
    </EventDisplay>;
}
