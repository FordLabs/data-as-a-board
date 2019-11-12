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

import {action} from "typesafe-actions";
import {Event} from "../../model/Event";
import {Configuration} from 'model/Configuration';

export enum ActionTypes {
    GOTO_PAGE = "@@dashboard/GOTO_PAGE",
    INCREMENT_PAGE = "@@dashboard/INCREMENT_PAGE",
    UPDATE_EVENT = "@@dashboard/UPDATE_EVENT",
    LOAD_CONFIGURATION = "@@dashboard/LOAD_CONFIGURATION",
    EMIT_DISCONNECTED = "@@dashboard/EMIT_DISCONNECTED",
    EMIT_CONNECTED = "@@dashboard/EMIT_CONNECTED",
    EDIT = "@@dashboard/EDIT",
    DISMISS_EDIT = "@@dashboard/DISMISS_EDIT",
}

export const gotoPage = (index: number) => action(ActionTypes.GOTO_PAGE, index);
export const incrementPage = () => action(ActionTypes.INCREMENT_PAGE);

export const updateEvent = (event: Event) => action(ActionTypes.UPDATE_EVENT, event);
export const loadConfiguration =
    (configuration: Configuration) => action(ActionTypes.LOAD_CONFIGURATION, configuration);

export const emitDisconnected = () => action(ActionTypes.EMIT_DISCONNECTED);
export const emitConnected = () => action(ActionTypes.EMIT_CONNECTED);

export const edit = () => action(ActionTypes.EDIT);
export const dismissEdit = () => action(ActionTypes.DISMISS_EDIT);
