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

import {EventType} from "./EventType";
import {HealthEvent} from "./HealthEvent";
import {JobEvent} from "./JobEvent";
import {FigureEvent} from "./FigureEvent";
import {QuoteEvent} from "./QuoteEvent";
import {PercentageEvent} from "./PercentageEvent";
import {StatisticsEvent} from "./StatisticsEvent";
import {WeatherEvent} from "./WeatherEvent";
import {ListEvent} from "./ListEvent";
import {ImageEvent} from "./ImageEvent";
import {CountdownEvent} from "./CountdownEvent";

export interface AbstractEvent {
    id: string;
    eventType?: EventType;
    level: Level;
    name: string;
    time: string;
}

export type Event =
    | HealthEvent
    | JobEvent
    | FigureEvent
    | QuoteEvent
    | PercentageEvent
    | StatisticsEvent
    | WeatherEvent
    | ListEvent
    | ImageEvent
    | CountdownEvent

export enum Level {
    OK = "OK",
    UNKNOWN = "UNKNOWN",
    DISABLED = "DISABLED",
    INFO = "INFO",
    WARN = "WARN",
    ERROR = "ERROR",
}
