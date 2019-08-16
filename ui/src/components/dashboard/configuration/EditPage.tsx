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

import React, {useState} from "react";
import {Page} from "../../../model/Page";

import {EventDisplayProperties} from "../../../model/EventDisplayProperties";
import Icon from "../../icon/Icon";
import style from "./Configuration.module.css";
import Input from "./Input";
import {EmptySpot} from "./EmptySpot";
import {EditEventDisplay} from "./EditEventDisplay";
import {act} from "@testing-library/react";

interface Props {
    page: Page;
    pageNumber: number;
    totalPages: number;

    onPageChanged(page: Page): void;

    onEventMovedToNewPage(pageFrom: number, pageTo: number, eventId: string, newRow: number, newCol: number): void;
}

enum ActionType {
    SET_NAME,
    ADD_COLUMN,
    REMOVE_COLUMN,
    ADD_ROW,
    REMOVE_ROW,
    CHANGE_EVENT_DIMENSIONS,
    ADD_EVENT,
    DELETE_EVENT,
}

type Action =
    | { type: ActionType.SET_NAME, name: string }
    | { type: ActionType.ADD_COLUMN }
    | { type: ActionType.REMOVE_COLUMN }
    | { type: ActionType.ADD_ROW }
    | { type: ActionType.REMOVE_ROW }
    | { type: ActionType.CHANGE_EVENT_DIMENSIONS, eventId: string, width?: number, height?: number }
    | { type: ActionType.ADD_EVENT, eventId: string, row: number, column: number }
    | { type: ActionType.DELETE_EVENT, eventId: string };

function reducer(state: Page, action: Action): Page {
    switch (action.type) {
        case ActionType.SET_NAME:
            return Object.assign({}, state, {name: action.name});
        case ActionType.ADD_COLUMN:
            return Object.assign({}, state, {columns: (state.columns || 5) + 1});
        case ActionType.REMOVE_COLUMN:
            return Object.assign({}, state, {columns: (state.columns || 5) - 1});
        case ActionType.ADD_ROW:
            return Object.assign({}, state, {rows: (state.rows || 3) + 1});
        case ActionType.REMOVE_ROW:
            return Object.assign({}, state, {rows: (state.rows || 3) - 1});
        case ActionType.CHANGE_EVENT_DIMENSIONS:
            const eventsWithNewDimensions = state.events.slice(0);

            const eventIndex = eventsWithNewDimensions.findIndex((event) => event.id === action.eventId);

            eventsWithNewDimensions[eventIndex] = Object.assign({}, eventsWithNewDimensions[eventIndex], {
                width: action.width || eventsWithNewDimensions[eventIndex].width,
                height: action.height || eventsWithNewDimensions[eventIndex].height,
            });

            return Object.assign({}, state, {events: eventsWithNewDimensions});
        case ActionType.ADD_EVENT:
            const eventsWithNewEvent = state.events.slice(0);
            eventsWithNewEvent.push({
                id: action.eventId,
                row: action.row,
                column: action.column,
            });

            return Object.assign({}, state, {events: eventsWithNewEvent});
        case ActionType.DELETE_EVENT:
            return Object.assign({}, state, {events: state.events.filter((event) => event.id !== action.eventId)});
    }
}

export function EditPage(props: Props) {

    function dispatch(action: Action) {
        props.onPageChanged(reducer(props.page, action));
    }

    const setName = (name: string) => dispatch({type: ActionType.SET_NAME, name});
    const addColumn = () => dispatch({type: ActionType.ADD_COLUMN});
    const removeColumn = () => dispatch({type: ActionType.REMOVE_COLUMN});
    const addRow = () => dispatch({type: ActionType.ADD_ROW});
    const removeRow = () => dispatch({type: ActionType.REMOVE_ROW});
    const addEvent = (eventId: string, row: number, column: number) =>
        dispatch({type: ActionType.ADD_EVENT, eventId, row, column});
    const deleteEvent = (eventId: string) => dispatch({type: ActionType.DELETE_EVENT, eventId});
    const changeEventDimensions = (eventId: string, width?: number, height?: number) =>
        dispatch({type: ActionType.CHANGE_EVENT_DIMENSIONS, eventId, width, height});

    return <div className={style.editBoardPage}
                data-testid={`edit-page-${props.pageNumber}`}
                style={{
                    width: window.innerWidth * .6,
                    height: window.innerHeight * .6,
                }}>
        <div className={style.editBoardPageName}>
            <Input label="Page Name" value={props.page.name} onChange={(event) => setName(event.target.value)}/>
        </div>
        <div
            className={style.editBoardPageGrid}
            style={{
                gridTemplateRows: "1fr ".repeat(props.page.rows || 3),
                gridTemplateColumns: "1fr ".repeat(props.page.columns || 5),
            }}
        >
            {
                arrayOfSize(props.page.rows || 3).flatMap((e, row) =>
                    arrayOfSize(props.page.columns || 5).map((e2, column) =>
                        <EmptySpot key={`${row},${column}`}
                                   row={row}
                                   column={column}
                                   pageNumber={props.pageNumber}
                                   onEventAdded={addEvent}
                                   onEventDropped={props.onEventMovedToNewPage}/>,
                    ),
                )
            }
            {
                props.page.events.map((display) =>
                    <EditEventDisplay key={display.id}
                                      rows={props.page.rows || 3}
                                      columns={props.page.columns || 5}
                                      display={display}
                                      onEventDeleted={deleteEvent}
                                      pageNumber={props.pageNumber}
                                      onChangeDimensions={changeEventDimensions}
                    />,
                )
            }
        </div>
        <div className={style.editBoardPageAddRemoveColumn}>
            {props.page.columns! > 1 &&
            <button type="button" aria-label="Remove Column" className={style.addRemoveButton}
                    onClick={removeColumn}>{Icon.remove}</button>}
            <button type="button" aria-label="Add Column" className={style.addRemoveButton}
                    onClick={addColumn}>{Icon.add}</button>
        </div>
        <div className={style.editBoardPageAddRemoveRow}>
            {props.page.rows! > 1 && <button type="button" aria-label="Remove Row" className={style.addRemoveButton}
                                             onClick={removeRow}>{Icon.remove}</button>}
            <button type="button" aria-label="Add Row" className={style.addRemoveButton}
                    onClick={addRow}>{Icon.add}</button>
        </div>
    </div>;
}

function arrayOfSize(size: number): number[] {
    return Array.from(Array(size).keys());
}
