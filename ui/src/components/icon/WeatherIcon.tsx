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

import React from "react";

export enum WeatherType {
    SUNNY = "day-sunny",
    CLOUDY = "day-cloudy",
    FOG = "day-fog",
    HAIL = "day-hail",
    LIGHTNING = "day-lightning",
    RAIN = "day-rain",
    SNOW = "day-snow",
    RAIN_AND_SNOW = "day-rain-mix",
    UNKNOWN = "na",
}

export interface Props {
    type: WeatherType;
}

export function WeatherIcon(props: Props) {
    return <i className={"wi wi-" + props.type}/>;
}
