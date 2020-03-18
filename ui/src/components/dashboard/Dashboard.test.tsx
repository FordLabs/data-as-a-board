import moment from "moment";
import React from "react";
import {Provider} from "react-redux";
import {createStore} from "redux";

import {act, cleanup, fireEvent, render, wait} from "@testing-library/react";

import {rootReducer} from "store";

import {Event, Level} from "model/event/Event";
import {ApplicationState} from "store/ApplicationState";

import {humanizeDurationPrecise} from "../../converters/humanizeDurationPrecise";
import {JobEvent} from "../../model/event/JobEvent";
import {EventDisplayProperties} from "../../model/EventDisplayProperties";
import Dashboard from "./Dashboard";

describe("Dashboard", () => {
    afterEach(cleanup);

    function mountDashboard(initialState = baseState(), store = createStore(rootReducer, initialState)) {
        return {
            dashboard: render(<Provider store={store}><Dashboard/></Provider>),
            store,
        };
    }

    function baseState(): ApplicationState {
        return {
            dashboard: {
                configuration: {
                    name: "DaaB",
                    pages: [],
                },
                events: new Map<string, Event>(),
                currentPage: 0,
                isDisconnected: false,
                isEditing: false,
            },
        };
    }

    it("should render", () => {
        const {dashboard} = mountDashboard();
        expect(dashboard).toBeTruthy();
    });

    it("should show title", () => {
        const initialState = baseState();
        initialState.dashboard.configuration.name = "Expected Name";

        const {dashboard} = mountDashboard(initialState);
        expect(dashboard.baseElement).toHaveTextContent("Expected Name");
    });

    it("should show an indicator for each page", () => {
        const initialState = baseState();
        initialState.dashboard.configuration.pages = [
            {
                tiles: [],
                name: "Page 1",
            },
            {
                tiles: [],
                name: "Page 2",
            },
            {
                tiles: [],
                name: "Page 3",
            },
        ];

        initialState.dashboard.configuration.name = "Expected Name";

        const {dashboard} = mountDashboard(initialState);
        const indicators = dashboard.getByTestId("@dashboard-indicators");

        expect(indicators.childNodes).toHaveLength(3);
    });

    describe("should show an event", () => {
        let eventToTest: HTMLElement;

        beforeEach(() => {
            const initialState = baseState();
            initialState.dashboard.configuration.pages = [
                {
                    tiles: [{
                        tileType: "EVENT",
                        id: "job.test",
                    } as EventDisplayProperties],
                    name: "Page 1",
                },
            ];
            initialState.dashboard.events.set("job.test", {
                id: "job.test",
                eventType: "JOB",
                level: Level.OK,
                name: "Test Event",
                time: "2019-01-01T00:00:00.000Z",
                url: "http://testurl.local",

                status: "SUCCESS",
            } as JobEvent);

            const {dashboard} = mountDashboard(initialState);
            eventToTest = dashboard.getByTestId("@dashboard-event-job.test");
        });

        it("has a name", () => {
            expect(eventToTest).toHaveTextContent("TEST EVENT");
        });

        it("has a type", () => {
            expect(eventToTest).toHaveTextContent("JOB");
        });
        it("has a time", () => {
            const timeDifference = moment.duration(moment("2019-01-01T00:00:00.000Z").diff(moment()));
            expect(eventToTest).toHaveTextContent(
                humanizeDurationPrecise(timeDifference),
            );
        });

        it("is clickable", async () => {
            const windowOpenSpy = jest.spyOn(window, "open");
            // tslint:disable-next-line:no-empty
            windowOpenSpy.mockImplementation(() => {
            });
            act(() => {
                fireEvent.click(eventToTest);
            });
            expect(windowOpenSpy).toHaveBeenCalledWith("http://testurl.local", "_blank");
        });
    });

    it("should show notifications", () => {
        const initialState = baseState();

        initialState.dashboard.events.set("job.test", {
            id: "job.test",
            eventType: "JOB",
            level: Level.ERROR,
            name: "Test Event",
            time: "2019-01-01T00:00:00.000Z",

            status: "FAILURE",
        } as JobEvent);

        const {dashboard} = mountDashboard(initialState);
        const notifications = dashboard.getByTestId("@dashboard-notifications");

        expect(notifications.childNodes).toHaveLength(1);

        const notification = notifications.childNodes[0];

        expect(notification).toHaveTextContent("TEST EVENT");
    });

    it("should not show notifications for any events that are on a page", () => {
        const initialState = baseState();
        initialState.dashboard.configuration.pages = [
            {
                tiles: [{
                    tileType: "EVENT",
                    id: "job.test",
                } as EventDisplayProperties],
                name: "Page 1",
            },
        ];
        initialState.dashboard.events.set("job.test", {
            id: "job.test",
            eventType: "JOB",
            level: Level.ERROR,
            name: "Test Event",
            time: "2019-01-01T00:00:00.000Z",

            status: "FAILURE",
        } as JobEvent);

        const {dashboard} = mountDashboard(initialState);
        const notifications = dashboard.getByTestId("@dashboard-notifications");

        expect(notifications.childNodes).toHaveLength(0);
    });
});
