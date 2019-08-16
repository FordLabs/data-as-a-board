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

import moment from "moment";
import React, {useState} from "react";
import {useInterval} from "../../../hooks/useInterval";

interface Props {
    time?: string;
}

function formatFromNow(time: string) {
    const timeMoment = moment(time);

    return timeMoment.isBefore(moment().subtract(1, "days"))
        ? timeMoment.calendar(undefined, {
            lastDay: "[yesterday]",
            lastWeek: "[last] dddd ",
        })
        : timeMoment.fromNow();
}

function PresentTimestampDisplay(props: { time: string }) {
    const [displayTime, setDisplayTime] = useState(formatFromNow(props.time));
    useInterval(() => setDisplayTime(formatFromNow(props.time!)), 1000);

    return <>{displayTime}</>;
}

function NullTimestampDisplay() {
    return null;
}

export function TimestampDisplay(props: Props) {
    return props.time
        ? <PresentTimestampDisplay time={props.time}/>
        : <NullTimestampDisplay/>;
}