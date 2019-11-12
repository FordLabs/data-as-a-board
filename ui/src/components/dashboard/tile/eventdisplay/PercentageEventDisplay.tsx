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
import {PercentageEvent} from "../../../../model/PercentageEvent";
import {EventDisplay} from "./EventDisplay";
import styles from "./EventDisplay.module.css";
import {EventDisplayProperties} from "../../../../model/EventDisplayProperties";

interface Props {
    event: PercentageEvent
    display: EventDisplayProperties
}

const SvgWrapper: React.FC<{}> = ({children}) =>
    <svg viewBox="0 0 36 36" width="70%" height="70%" className={styles.percentageItem}>
        {children}
    </svg>;

const PieBackground: React.FC = () =>
    <circle cx="50%" cy="50%"
            r="15.91549430918954"
            fill="transparent"
            stroke="#ffffff11"
            strokeWidth="2"
    />;


const PieForeground: React.FC<{ value: number }> = ({value}) =>
    <circle cx="50%" cy="50%" r="15.91549430918954" fill="transparent"
            stroke="#ffffff88" strokeWidth="3"
            strokeDasharray={`${value * 100} ${(1 - value) * 100}`}
            strokeDashoffset="25"
    />;

const PercentageText: React.FC<{ value: number }> = ({value}) =>
    <text x="50%" y="50%">{value * 100}%</text>;

export function PercentageEventDisplay(props: Props) {
    return <EventDisplay event={props.event} display={props.display}>
        <SvgWrapper>
            <PieBackground/>
            <PieForeground value={props.event.value}/>
            <PercentageText value={props.event.value}/>
        </SvgWrapper>
    </EventDisplay>
}
