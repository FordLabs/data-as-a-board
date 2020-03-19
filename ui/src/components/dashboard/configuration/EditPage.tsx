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

import React from 'react';
import {Page} from 'model/Page';

import Icon from '../../icon/Icon';
import style from './Configuration.module.css';
import Input from './Input';
import {EmptySpot} from './EmptySpot';
import {TileProperties} from 'model/TileProperties';
import {EditTile} from './EditTile';
import {EventDisplayProperties} from 'model/EventDisplayProperties';

interface Props {
    page: Page;
    pageNumber: number;
    totalPages: number;

    onPageChanged(page: Page): void;

    onTileMovedToNewPage(pageFrom: number, pageTo: number, tile: TileProperties, newRow: number, newCol: number): void;
}

enum ActionType {
    SET_NAME,
    ADD_COLUMN,
    REMOVE_COLUMN,
    ADD_ROW,
    REMOVE_ROW,
    CHANGE_TILE_DIMENSIONS,
    ADD_EVENT_TILE,
    DELETE_TILE,
}

type Action =
    | { type: ActionType.SET_NAME, name: string }
    | { type: ActionType.ADD_COLUMN }
    | { type: ActionType.REMOVE_COLUMN }
    | { type: ActionType.ADD_ROW }
    | { type: ActionType.REMOVE_ROW }
    | { type: ActionType.CHANGE_TILE_DIMENSIONS, tile: TileProperties, width?: number, height?: number }
    | { type: ActionType.ADD_EVENT_TILE, eventId: string, row: number, column: number }
    | { type: ActionType.DELETE_TILE, tile: TileProperties };

function tilesAreSame(tile1: TileProperties, tile2: TileProperties) {
    return tile1.column === tile2.column && tile1.row === tile2.row;
}

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
        case ActionType.CHANGE_TILE_DIMENSIONS:
            const tileWithNewDimensions = state.tiles.slice(0);

            const tileIndex = tileWithNewDimensions.findIndex((tile) => tilesAreSame(tile, action.tile));

            tileWithNewDimensions[tileIndex] = Object.assign({}, tileWithNewDimensions[tileIndex], {
                width: action.width || tileWithNewDimensions[tileIndex].width,
                height: action.height || tileWithNewDimensions[tileIndex].height,
            });

            return Object.assign({}, state, {tiles: tileWithNewDimensions});
        case ActionType.ADD_EVENT_TILE:
            const tilesWithNewTile = state.tiles.slice(0);
            let newEventDisplayProperties: EventDisplayProperties = {
                id: action.eventId,
                row: action.row,
                column: action.column,
                tileType: 'EVENT',
            };
            tilesWithNewTile.push(newEventDisplayProperties);

            return Object.assign({}, state, {tiles: tilesWithNewTile});
        case ActionType.DELETE_TILE:
            return Object.assign({}, state, {tiles: state.tiles.filter((tile) => !tilesAreSame(tile, action.tile))});
    }
}

export function EditPage(props: Props) {

    function dispatch(action: Action) {
        props.onPageChanged(reducer(props.page, action));
    }

    const setName = (name: string) => dispatch({
        type: ActionType.SET_NAME,
        name
    });
    const addColumn = () => dispatch({type: ActionType.ADD_COLUMN});
    const removeColumn = () => dispatch({type: ActionType.REMOVE_COLUMN});
    const addRow = () => dispatch({type: ActionType.ADD_ROW});
    const removeRow = () => dispatch({type: ActionType.REMOVE_ROW});
    const addTile = (eventId: string, row: number, column: number) =>
        dispatch({type: ActionType.ADD_EVENT_TILE, eventId, row, column});
    const deleteTile = (tile: TileProperties) => dispatch({
        type: ActionType.DELETE_TILE,
        tile
    });
    const changeTileDimensions = (tile: TileProperties, width?: number, height?: number) =>
        dispatch({
            type: ActionType.CHANGE_TILE_DIMENSIONS,
            tile,
            width,
            height
        });

    return <div className={style.editBoardPage}
                data-testid={`edit-page-${props.pageNumber}`}
                style={{
                    width: window.innerWidth * .6,
                    height: window.innerHeight * .6,
                }}>
        <div className={style.editBoardPageName}>
            <Input label="Page Name" value={props.page.name}
                   onChange={(event) => setName(event.target.value)}/>
        </div>
        <div
            className={style.editBoardPageGrid}
            style={{
                gridTemplateRows: '1fr '.repeat(props.page.rows || 3),
                gridTemplateColumns: '1fr '.repeat(props.page.columns || 5),
            }}
        >
            {
                arrayOfSize(props.page.rows || 3).flatMap((e, row) =>
                    arrayOfSize(props.page.columns || 5).map((e2, column) =>
                        <EmptySpot key={`${row},${column}`}
                                   row={row}
                                   column={column}
                                   pageNumber={props.pageNumber}
                                   onEventAdded={addTile}
                                   onTileDropped={props.onTileMovedToNewPage}/>,
                    ),
                )
            }
            {
                props.page.tiles.map((tile, index) =>
                    <EditTile key={`page-${props.pageNumber}-${index}`}
                              index={index}
                              pageNumber={props.pageNumber}
                              rows={props.page.rows || 3}
                              columns={props.page.columns || 5}
                              tile={tile}
                              onTileDeleted={deleteTile}
                              onChangeDimensions={changeTileDimensions}
                    />,
                )
            }
        </div>
        <div className={style.editBoardPageAddRemoveColumn}>
            {props.page.columns! > 1 &&
            <button type="button" aria-label="Remove Column"
                    className={style.addRemoveButton}
                    onClick={removeColumn}>{Icon.remove}</button>}
            <button type="button" aria-label="Add Column"
                    className={style.addRemoveButton}
                    onClick={addColumn}>{Icon.add}</button>
        </div>
        <div className={style.editBoardPageAddRemoveRow}>
            {props.page.rows! > 1 &&
            <button type="button" aria-label="Remove Row"
                    className={style.addRemoveButton}
                    onClick={removeRow}>{Icon.remove}</button>}
            <button type="button" aria-label="Add Row"
                    className={style.addRemoveButton}
                    onClick={addRow}>{Icon.add}</button>
        </div>
    </div>;
}

function arrayOfSize(size: number): number[] {
    return Array.from(Array(size).keys());
}
