/*
 *
 *  * Copyright (c) 2019 Ford Motor Company
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and limitations under the License.
 *  *
 *
 *
 */

import {TileProperties} from 'model/TileProperties';
import React, {ReactNode} from 'react';
import style from './Configuration.module.css';
import {EditEventDisplay} from 'components/dashboard/configuration/EditEventDisplay';
import {EventDisplayProperties} from 'model/EventDisplayProperties';

interface Props {
    tile: TileProperties;
    index: number;
    pageNumber: number;
    rows: number;
    columns: number;

    onChangeDimensions(tile: TileProperties, width?: number, height?: number): void;

    onTileDeleted(tile: TileProperties): void;
}

export function EditTile(props: Props) {

    const gridStyle = {
        gridColumnStart: props.tile.column !== undefined ? props.tile.column + 1 : undefined,
        gridRowStart: props.tile.row !== undefined ? props.tile.row + 1 : undefined,
        gridColumnEnd: 'span ' + (props.tile.width ? props.tile.width : 1),
        gridRowEnd: 'span ' + (props.tile.height ? props.tile.height : 1),
    };

    function onDragStart(event: React.DragEvent<HTMLDivElement>) {
        event.dataTransfer.setData('tile', JSON.stringify(props.tile));
        event.dataTransfer.setData('page', props.pageNumber.toString(10));
    }

    function onChangeWidth(width: number) {
        props.onChangeDimensions(props.tile, width, undefined);
    }

    function onChangeHeight(height: number) {
        props.onChangeDimensions(props.tile, undefined, height);
    }

    function content(): ReactNode {
        switch (props.tile.tileType) {
            case 'EVENT':
                return <EditEventDisplay
                    display={props.tile as EventDisplayProperties}
                    onEventDeleted={props.onTileDeleted}
                    pageNumber={props.pageNumber}
                    onChangeWidth={onChangeWidth}
                    onChangeHeight={onChangeHeight}
                    rows={props.rows}
                    columns={props.columns}
                />;
            default:
                return <>nothing here</>;
        }
    }

    return <div
        data-testid={`edit-page-${props.pageNumber}-tile-${props.index}`}
        className={style.editBoardEvent}
        style={gridStyle}
        draggable
        onDragStart={onDragStart}
    >
        {content()}
    </div>;
}
