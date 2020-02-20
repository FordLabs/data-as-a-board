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
import {WeatherEvent} from 'model/event/WeatherEvent';
import {WeatherIcon, WeatherType} from "../../../icon/WeatherIcon";
import {EventDisplay} from "./EventDisplay";

import styles from "./EventDisplay.module.css";
import {EventDisplayProperties} from 'model/EventDisplayProperties';

interface Props {
    event: WeatherEvent
    display: EventDisplayProperties
}

function conditionToWeatherType(condition: string): WeatherType {
    if (condition.includes("snow")) {
        if (condition.includes("rain")) {
            return WeatherType.RAIN_AND_SNOW;
        } else {
            return WeatherType.SNOW;
        }
    } else if (condition.includes("rain")) {
        return WeatherType.RAIN;
    } else if (condition.includes("cloudy")) {
        return WeatherType.CLOUDY;
    } else if (condition.includes("sunny")) {
        return WeatherType.SUNNY;
    } else {
        return WeatherType.UNKNOWN;
    }
}

const Icon: React.FC<{ condition: string }> = (props) =>
    <span className={styles.weatherIcon}>
        <WeatherIcon type={conditionToWeatherType(props.condition.toLowerCase())}/>
    </span>;

const Temperature: React.FC<{ value: number, unit: string }> = (props) =>
    <span className={styles.temperature}>
        {props.value}&deg; {props.unit}
    </span>;

const Condition: React.FC<{ condition: string }> = (props) =>
    <span className={styles.weatherCondition}>
        {props.condition}
    </span>;

export function WeatherEventDisplay(props: Props) {
    return <EventDisplay event={props.event} display={props.display}>
        <div className={styles.weather}>
            <Icon condition={props.event.condition}/>
            <Temperature value={props.event.temperature} unit={props.event.temperatureUnit}/>
            <Condition condition={props.event.condition}/>
        </div>
    </EventDisplay>
}
