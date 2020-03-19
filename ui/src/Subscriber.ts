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

import {Store} from "redux";
import {ApplicationState} from 'store/ApplicationState';
import {emitConnected, emitDisconnected, updateEvent} from 'store/dashboard/actions';

export default class Subscriber {
    private readonly store: Store<ApplicationState>;
    private readonly websocketUrl: string;
    private socket: WebSocket | undefined;
    private pollInterval?: number;

    constructor(store: Store<ApplicationState>) {
        this.store = store;

        this.websocketUrl = process.env.NODE_ENV === "development"
            ? "ws://localhost:8080/event"
            : `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/event`;

        this.pollForBackendAvailability = this.pollForBackendAvailability.bind(this);
    }

    public subscribe() {
        this.socket = new WebSocket(this.websocketUrl);
        this.socket.onopen = () => this.store.dispatch(emitConnected());
        this.socket.onmessage = (event) => this.store.dispatch(updateEvent(JSON.parse(event.data)));
        this.socket.onerror = () => {
            this.store.dispatch(emitDisconnected());
            this.pollInterval = window.setInterval(this.pollForBackendAvailability, 30000);
        };
        this.socket.onclose = () => {
            this.store.dispatch(emitDisconnected());
            this.pollInterval = window.setInterval(this.pollForBackendAvailability, 30000);
        };
    }

    public async pollForBackendAvailability() {
        const response = await window.fetch("/actuator/health");
        if (response.status === 200) {
            window.clearInterval(this.pollInterval);
            window.location.reload();
        }
    }
}
