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

import {createStore} from "redux";
import {Level} from "model/event/Event";
import {Event} from "model/event/Event";
import {
    emitConnected,
    emitDisconnected,
    gotoPage,
    incrementPage,
    loadConfiguration,
    updateEvent,
} from "./dashboard/actions";
import {DashboardState} from "./dashboard/state";
import {rootReducer} from "./index";

function createTestStore(dashboardState: DashboardState) {
    return createStore(rootReducer, {dashboard: dashboardState});
}

it("emitDisconnected sets isDisconnected state to true", async () => {
    const store = createTestStore({
        configuration: {
            pages: [],
            name: "",
        },
        events: new Map(),
        currentPage: 0,
        isDisconnected: false,
        isEditing: false,
    });

    store.dispatch(emitDisconnected());

    expect(store.getState().dashboard.isDisconnected).toBe(true);
});

it("emitConnected sets isDisconnected state to false", () => {
    const store = createTestStore({
        configuration: {
            pages: [],
            name: "",
        },
        events: new Map(),
        currentPage: 0,
        isDisconnected: true,
        isEditing: false,
    });

    store.dispatch(emitConnected());

    expect(store.getState().dashboard.isDisconnected).toBe(false);
});

it("loadConfiguration sets configuration state", () => {
    const store = createTestStore({
        configuration: {
            pages: [],
            name: "",
        },
        events: new Map(),
        currentPage: 0,
        isDisconnected: false,
        isEditing: false,
    });

    const configuration = {
        name: "The Team",
        pages: [{
            name: "The Page",
            rows: 3,
            columns: 5,
            tiles: [],
        }],
        background: "https://imagehost.local/image.jpg",
    };

    store.dispatch(loadConfiguration(configuration));

    expect(store.getState().dashboard.configuration.pages).toBe(configuration.pages);
    expect(store.getState().dashboard.configuration.name).toBe(configuration.name);
    expect(store.getState().dashboard.configuration.background).toBe(configuration.background);
    expect(store.getState().dashboard.currentPage).toBe(0);
});

it("gotoPage goes to specified page", () => {
    const store = createTestStore({
        configuration: {

            pages: [
                {
                    name: "The Page",
                    rows: 3,
                    columns: 5,
                    tiles: [],
                },
                {
                    name: "The Other Page",
                    rows: 3,
                    columns: 5,
                    tiles: [],
                },
            ],
            name: "",
        },
        events: new Map(),
        currentPage: 0,
        isDisconnected: false,
        isEditing: false,
    });

    store.dispatch(gotoPage(1));
    expect(store.getState().dashboard.currentPage).toBe(1);
});

it("incrementPage increments page", () => {
    const store = createTestStore({
        configuration: {
            pages: [
                {
                    name: "The Page",
                    rows: 3,
                    columns: 5,
                    tiles: [],
                },
                {
                    name: "The Other Page",
                    rows: 3,
                    columns: 5,
                    tiles: [],
                },
            ],
            name: "",
        },
        events: new Map(),
        currentPage: 0,
        isDisconnected: false,
        isEditing: false,
    });

    store.dispatch(incrementPage());

    expect(store.getState().dashboard.currentPage).toBe(1);
});

it("updateEvent updates event", () => {
    const initialState: DashboardState = {
        configuration: {
            pages: [],
            name: "",
        },
        events: new Map<string, Event>(),
        currentPage: 0,
        isDisconnected: false,
        isEditing: false,
    };

    initialState.events.set("some.random.event", {
        id: "some.random.event",
        // @ts-ignore
        eventType: "UNKNOWN",
        level: Level.OK,
        name: "Some Random Event",
        time: "2017-01-01T00:00:00.000Z",
    });

    const store = createTestStore(initialState);

    const event: Event = {
        id: "some.random.event",
        // @ts-ignore
        eventType: "UNKNOWN",
        level: Level.OK,
        name: "Some New Random Event",
        time: "2020-01-01T00:00:00.000Z",
    };

    store.dispatch(updateEvent(event));

    expect(store.getState().dashboard.events.get("some.random.event")).toBe(event);
});
