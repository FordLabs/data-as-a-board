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

import React, {useReducer} from "react";
import {connect} from "react-redux";
import axios from "axios";

import {ApplicationState} from "../../../store/ApplicationState";
import {Configuration} from "../../../model/Configuration";
import {dismissEdit, loadConfiguration} from "../../../store/dashboard/actions";

import Icon from "../../icon/Icon";
import Input from "./Input";

import style from "./Configuration.module.css";
import {EditPage} from "./EditPage";
import {Page} from "../../../model/Page";

interface Props {
    configuration: Configuration;

    onSave(configuration: Configuration): void;

    onDismiss(): void;
}

type Action =
    | { type: ActionType.SET_NAME, name: string }
    | { type: ActionType.SET_BACKGROUND, background: string }
    | { type: ActionType.ADD_PAGE }
    | { type: ActionType.REMOVE_PAGE }
    | { type: ActionType.CHANGE_PAGE, page: Page, index: number }
    | {
    type: ActionType.MOVE_EVENT,
    pageFrom: number,
    pageTo: number,
    eventId: string,
    newRow: number,
    newCol: number,
};

enum ActionType {
    SET_NAME,
    SET_BACKGROUND,
    ADD_PAGE,
    REMOVE_PAGE,
    CHANGE_PAGE,
    MOVE_EVENT,
}

function reducer(state: Configuration, action: Action): Configuration {
    switch (action.type) {
        case ActionType.SET_NAME:
            return Object.assign({}, state, {name: action.name});
        case ActionType.SET_BACKGROUND:
            return Object.assign({}, state, {background: action.background});
        case ActionType.ADD_PAGE:
            const pagesWithNewPage = state.pages.slice(0);
            pagesWithNewPage.push(new Page());
            return Object.assign({}, state, {pages: pagesWithNewPage});
        case ActionType.REMOVE_PAGE:
            const pagesWithoutLastPage = state.pages.slice(0, -1);
            return Object.assign({}, state, {pages: pagesWithoutLastPage});
        case ActionType.CHANGE_PAGE:
            const changedPages = state.pages.slice(0);
            changedPages[action.index] = action.page;
            return Object.assign({}, state, {pages: changedPages});
        case ActionType.MOVE_EVENT:
            return moveEvent(state, action);
        default:
            return state;
    }
}

function moveEvent(
    state: Configuration,
    action: {
        type: ActionType.MOVE_EVENT,
        pageFrom: number,
        pageTo: number,
        eventId: string,
        newRow: number,
        newCol: number,
    }) {
    const newPages = state.pages.slice(0);
    const oldEventPageIndex = newPages[action.pageFrom].events
        .findIndex((event) => event.id === action.eventId);

    const newEvent = Object.assign(
        {},
        newPages[action.pageFrom].events[oldEventPageIndex], {
            row: action.newRow,
            column: action.newCol,
        });

    newPages[action.pageFrom] = Object.assign({}, newPages[action.pageFrom], {
        events: newPages[action.pageFrom].events.filter((_, idx) => idx !== oldEventPageIndex),
    });

    newPages[action.pageTo] = Object.assign({}, newPages[action.pageTo], {
        events: newPages[action.pageTo].events.concat([newEvent]),
    });
    return Object.assign({}, state, {pages: newPages});
}

function ConfigurationForm(props: Props) {
    const [configuration, dispatch] = useReducer(reducer, props.configuration);

    const setName = (name: string) => dispatch({type: ActionType.SET_NAME, name});
    const setBackground = (background: string) => dispatch({type: ActionType.SET_BACKGROUND, background});
    const addPage = () => dispatch({type: ActionType.ADD_PAGE});
    const removePage = () => dispatch({type: ActionType.REMOVE_PAGE});
    const changePage = (index: number) => (page: Page) => dispatch({type: ActionType.CHANGE_PAGE, page, index});
    const moveEventToPage = (pageFrom: number, pageTo: number, eventId: string, newRow: number, newCol: number) =>
        dispatch({type: ActionType.MOVE_EVENT, pageFrom, pageTo, eventId, newRow, newCol});

    const submit = async () => {
        await axios.post("/api/radiator/configuration", configuration);

        props.onSave(configuration);
        props.onDismiss();
    };

    return <form className={style.form}>
        <div className={style.formInputs}>
            <Input label={"Board Name"}
                   value={configuration.name}
                   onChange={(event) => setName(event.target.value)}/>
            <Input label={"Background Image URL"}
                   type="url"
                   value={configuration.background}
                   onChange={(event) => setBackground(event.target.value)}/>
            <div className={style.editBoardContainer}>
                <div className={style.editBoard} data-testid="edit-pages">
                    {configuration.pages.map((page, idx) =>
                        <EditPage key={idx}
                                  page={page}
                                  pageNumber={idx}
                                  onPageChanged={changePage(idx)}
                                  onEventMovedToNewPage={moveEventToPage}
                                  totalPages={configuration.pages.length}
                        />,
                    )}
                    <div className={style.addRemovePage}>
                        {configuration.pages.length > 1
                        && <button type="button" aria-label="Remove Page" className={style.addRemoveButton}
                                   onClick={removePage}>
                            {Icon.remove}
                        </button>
                        }
                        <button type="button" aria-label="Add Page" className={style.addRemoveButton} onClick={addPage}>
                            {Icon.add}
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <button type="button" data-testid="submit" className={[style.button, style.success].join(" ")}
                onClick={submit}>{Icon.ok}</button>
    </form>;
}

const mapStateToProps = (state: ApplicationState) => ({
    configuration: state.dashboard.configuration,
});
const mapDispatchToProps = {
    onSave: loadConfiguration,
    onDismiss: dismissEdit,
};

export default connect(mapStateToProps, mapDispatchToProps)(ConfigurationForm);