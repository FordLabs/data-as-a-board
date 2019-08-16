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

import React from "react";

import {EventDisplayProperties} from "../../../model/EventDisplayProperties";
import Icon from "../../icon/Icon";
import style from "./Configuration.module.css";
import Input from "./Input";

interface Props {
    display: EventDisplayProperties;
    pageNumber: number;
    rows: number;
    columns: number;

    onChangeDimensions(eventId: string, width?: number, height?: number): void;

    onEventDeleted(eventId: string): void;
}

function displayId(id: string) {
    return <>
        {id.split(".").map((val, idx) =>
            <span key={idx}>{"　".repeat(idx)}{idx > 0 && "."}{val}</span>)
        }
    </>;
}

export function EditEventDisplay(props: Props) {
    const gridStyle = {
        gridColumnStart: props.display.column !== undefined ? props.display.column + 1 : undefined,
        gridRowStart: props.display.row !== undefined ? props.display.row + 1 : undefined,
        gridColumnEnd: "span " + (props.display.width ? props.display.width : 1),
        gridRowEnd: "span " + (props.display.height ? props.display.height : 1),
    };

    function onDragStart(event: React.DragEvent<HTMLDivElement>) {
        event.dataTransfer.setData("eventId", props.display.id);
        event.dataTransfer.setData("page", props.pageNumber.toString(10));
    }

    function onChangeWidth(event: React.ChangeEvent<HTMLInputElement>) {
        props.onChangeDimensions(props.display.id, parseInt(event.target.value, 10), undefined);
    }

    function onChangeHeight(event: React.ChangeEvent<HTMLInputElement>) {
        props.onChangeDimensions(props.display.id, undefined, parseInt(event.target.value, 10));
    }

    return <div key={`${props.display.id}-${props.display.row}-${props.display.column}`}
                data-testid={`edit-page-0-event-${props.display.id}`}
                className={style.editBoardEvent}
                style={gridStyle}
                draggable
                onDragStart={onDragStart}
    >
        {displayId(props.display.id)}
        <div className={style.editBoardEventActions}>
            <div className={style.editBoardEventDimensions}>
                <Input label="width" type="number" min={1} max={props.columns} value={props.display.width || 1}
                       onChange={onChangeWidth}/>
                <Input label="height" type="number" min={1} max={props.rows} value={props.display.height || 1}
                       onChange={onChangeHeight}/>
            </div>
            <a aria-label="Delete Event" onClick={() => props.onEventDeleted(props.display.id)}>
                {Icon.delete}
            </a>
        </div>
    </div>;
}