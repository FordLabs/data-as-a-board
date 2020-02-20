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
import {Event} from 'model/event/Event';
import {EventDisplayProperties} from 'model/EventDisplayProperties';
import {FigureEvent} from 'model/event/FigureEvent';
import {HealthEvent} from 'model/event/HealthEvent';
import {ImageEvent} from 'model/event/ImageEvent';
import {JobEvent} from 'model/event/JobEvent';
import {ListEvent} from 'model/event/ListEvent';
import {PercentageEvent} from 'model/event/PercentageEvent';
import {QuoteEvent} from 'model/event/QuoteEvent';
import {StatisticsEvent} from 'model/event/StatisticsEvent';
import {WeatherEvent} from 'model/event/WeatherEvent';
import {ApplicationState} from 'store/ApplicationState';
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
import {CountdownEvent} from "../../../../model/event/CountdownEvent";
import {CountdownEventDisplay} from "./CountdownEventDisplay";

export interface Props {
    eventDisplay: EventDisplayProperties;
    event?: Event;
}

function EventWrapper({event, display}: { event?: Event, display: EventDisplayProperties }) {
    if (!event) {
        return <div/>;
    }
    switch (event.eventType) {
        case "HEALTH":
            return <HealthEventDisplay event={event} display={display}/>;
        case "JOB":
            return <JobEventDisplay event={event} display={display}/>;
        case "FIGURE":
            return <FigureEventDisplay event={event} display={display}/>;
        case "QUOTE":
            return <QuoteEventDisplay event={event} display={display}/>;
        case "PERCENTAGE":
            return <PercentageEventDisplay event={event} display={display}/>;
        case "STATISTICS":
            return <StatisticsEventDisplay event={event} display={display}/>;
        case "WEATHER":
            return <WeatherEventDisplay event={event} display={display}/>;
        case "LIST":
            return <ListEventDisplay event={event} display={display}/>;
        case "IMAGE":
            return <ImageEventDisplay event={event} display={display}/>;
        case "COUNTDOWN":
            return <CountdownEventDisplay event={event} display={display}/>;
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
