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

import {Store} from "redux";
import {ApplicationState} from "./store/ApplicationState";
import {loadConfiguration} from "./store/dashboard/actions";

export default class ConfigurationLoader {
    private readonly store: Store<ApplicationState>;

    constructor(store: Store<ApplicationState>) {
        this.store = store;
    }

    public async loadConfiguration() {
        const response = await window.fetch("/api/radiator/configuration");
        const configuration = await response.json();
        this.store.dispatch(loadConfiguration(configuration));
    }
}
