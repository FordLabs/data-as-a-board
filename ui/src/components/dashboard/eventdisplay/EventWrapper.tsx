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

import {connect} from "react-redux";
import {Event} from "../../../model/Event";
import {EventDisplayProperties} from "../../../model/EventDisplayProperties";
import {FigureEvent} from "../../../model/FigureEvent";
import {HealthEvent} from "../../../model/HealthEvent";
import {ImageEvent} from "../../../model/ImageEvent";
import {JobEvent} from "../../../model/JobEvent";
import {ListEvent} from "../../../model/ListEvent";
import {PercentageEvent} from "../../../model/PercentageEvent";
import {QuoteEvent} from "../../../model/QuoteEvent";
import {StatisticsEvent} from "../../../model/StatisticsEvent";
import {WeatherEvent} from "../../../model/WeatherEvent";
import {ApplicationState} from "../../../store/ApplicationState";
import {FigureEventDisplay} from "./FigureEventDisplay";
import {HealthEventDisplay} from "./HealthEventDisplay";
import {ImageEventDisplay} from "./ImageEventDisplay";
import {JobEventDisplay} from "./JobEventDisplay";
import {ListEventDisplay} from "./ListEventDisplay";
import {PercentageEventDisplay} from "./PercentageEventDisplay";
import {QuoteEventDisplay} from "./QuoteEventDisplay";
import {StatisticsEventDisplay} from "./StatisticsEventDisplay";
import {UnknownEventDisplay} from "./UnknownEventDisplay";
import {WeatherEventDisplay} from "./WeatherEventDisplay";

export interface Props {
    eventDisplay: EventDisplayProperties;
    event?: Event;
}

function EventWrapper({event, display}: { event?: Event, display: EventDisplayProperties }) {
    if (!event) {
        return <div/>;
    }
    switch (event.eventType ? event.eventType : "UNKNOWN") {
        case "HEALTH":
            return <HealthEventDisplay event={event as HealthEvent} display={display}/>;
        case "JOB":
            return <JobEventDisplay event={event as JobEvent} display={display}/>;
        case "FIGURE":
            return <FigureEventDisplay event={event as FigureEvent} display={display}/>;
        case "QUOTE":
            return <QuoteEventDisplay event={event as QuoteEvent} display={display}/>;
        case "PERCENTAGE":
            return <PercentageEventDisplay event={event as PercentageEvent} display={display}/>;
        case "STATISTICS":
            return <StatisticsEventDisplay event={event as StatisticsEvent} display={display}/>;
        case "WEATHER":
            return <WeatherEventDisplay event={event as WeatherEvent} display={display}/>;
        case "LIST":
            return <ListEventDisplay event={event as ListEvent} display={display}/>;
        case "IMAGE":
            return <ImageEventDisplay event={event as ImageEvent} display={display}/>;
        default:
            return <UnknownEventDisplay event={event} display={display}/>;
    }
}

const mapStateToProps = (state: ApplicationState, ownProps: Props) => {
    const event = ownProps.event ? ownProps.event : state.dashboard.events.get(ownProps.eventDisplay.id);
    return {
        event,
        display: ownProps.eventDisplay,
    };
};

export default connect(mapStateToProps)(EventWrapper);
