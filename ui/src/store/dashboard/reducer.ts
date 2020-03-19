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

import {Reducer} from "redux";
import {Configuration} from 'model/Configuration';
import {Event} from 'model/event/Event';
import {EventMap} from 'model/EventMap';
import {ActionTypes} from "./actions";
import {DashboardState} from "./state";
import moment from "moment";

const initialState: DashboardState = {
    configuration: {
        name: "",
        pages: [],
    },
    events: new Map<string, Event>() as EventMap,
    currentPage: 0,
    isDisconnected: false,
    isEditing: false,
};

function emitDisconnected(state: DashboardState, action: any): DashboardState {
    return Object.assign({}, state, {
        isDisconnected: true,
    });
}

function emitConnected(state: DashboardState, action: any): DashboardState {
    return Object.assign({}, state, {
        isDisconnected: false,
    });
}

function loadConfiguration(state: DashboardState, {payload}: { payload: Configuration }): DashboardState {
    return Object.assign({}, state, {configuration: payload});
}

function gotoPage(state: DashboardState, {payload}: { payload: number }): DashboardState {
    return Object.assign({}, state, {currentPage: payload});
}

function incrementPage(state: DashboardState, action: any): DashboardState {
    return Object.assign({}, state, {
        currentPage: (state.currentPage + 1) % state.configuration.pages.length,
    });
}

function updateEvent(state: DashboardState, {payload}: { payload: Event }): DashboardState {
    const events: EventMap = new Map<string, Event>(state.events);

    events.set(payload.id, payload);

    return Object.assign({}, state, {events});
}

function edit(state: DashboardState, action: any): DashboardState {
    return Object.assign({}, state, {isEditing: true});
}

function dismissEdit(state: DashboardState, action: any): DashboardState {
    return Object.assign({}, state, {isEditing: false});
}

const reducer: Reducer<DashboardState> = (state: DashboardState = initialState, action: any) => {
    switch (action && action.type) {
        case ActionTypes.EMIT_DISCONNECTED:
            return emitDisconnected(state, action);
        case ActionTypes.EMIT_CONNECTED:
            return emitConnected(state, action);
        case ActionTypes.LOAD_CONFIGURATION:
            return loadConfiguration(state, action);
        case ActionTypes.GOTO_PAGE:
            return gotoPage(state, action);
        case ActionTypes.INCREMENT_PAGE:
            return incrementPage(state, action);
        case ActionTypes.UPDATE_EVENT:
            return updateEvent(state, action);
        case ActionTypes.EDIT:
            return edit(state, action);
        case ActionTypes.DISMISS_EDIT:
            return dismissEdit(state, action);
        default:
            return state;
    }
};

export {reducer as DashboardReducer};
