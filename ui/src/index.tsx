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
import ReactDOM from "react-dom";
import {Provider} from "react-redux";

import "material-icons-font/material-icons-font.css";
import "weather-icons/css/weather-icons.css";

import "typeface-montserrat";

import {App} from 'components/App';
import ConfigurationLoader from "./ConfigurationLoader";
import {store} from "./store";
import Subscriber from "./Subscriber";

const subscriber = new Subscriber(store);
const configurationLoader = new ConfigurationLoader(store);

subscriber.subscribe();
configurationLoader.loadConfiguration()
    .then(() => ReactDOM.render(
        <Provider store={store}>
            <App/>
        </Provider>,
        document.getElementById("root"),
    ));
