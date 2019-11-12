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
import style from "./Configuration.module.css";
import Icon from "../../icon/Icon";
import Input from "./Input";
import {TileProperties} from '../../../model/TileProperties';

interface Props {
    row: number;
    column: number;
    pageNumber: number;

    onEventAdded(eventId: string, row: number, column: number): void;

    onTileDropped(pageFrom: number, pageTo: number, tile: TileProperties, newRow: number, newCol: number): void;
}

export function EmptySpot(props: Props) {
    const {row, column} = props;

    const [isHovered, setHovered] = useState(false);
    const [isAdding, setAdding] = useState(false);
    const [newEventId, setNewEventId] = useState("");

    const className = [
        style.editBoardPageEmptySpot,
        isHovered ? style.hover : null,
    ].filter((c) => c).join(" ");

    function onDragOver(event: React.DragEvent<HTMLDivElement>) {
        event.preventDefault();
        if (!isHovered) {
            setHovered(true);
        }
    }

    function onDragLeave(event: React.DragEvent<HTMLDivElement>) {
        event.preventDefault();
        if (isHovered) {
            setHovered(false);
        }
    }

    function onDrop(event: React.DragEvent<HTMLDivElement>) {
        event.preventDefault();
        setHovered(false);
        const tile = JSON.parse(event.dataTransfer.getData("tile") || "null");
        const pageFrom = parseInt(event.dataTransfer.getData("page"), 10);

        if (tile !== null && !isNaN(pageFrom)) {
            props.onTileDropped(pageFrom, props.pageNumber, tile, props.row, props.column);
        }
    }

    function onNewEvent(event: React.MouseEvent<HTMLButtonElement>) {
        setAdding(false);
        props.onEventAdded(newEventId, props.row, props.column);
        event.preventDefault();
    }

    function onCancelNewEvent(event: React.MouseEvent<HTMLButtonElement>) {
        setAdding(false);
        event.preventDefault();
    }

    return <div
        style={{gridColumnStart: column + 1, gridRowStart: row + 1}}
        className={className}
        data-testid={`edit-page-${props.pageNumber}-empty-spot-${props.row},${props.column}`}
        onClick={() => {
            if (!isAdding) {
                setAdding(true);
            }
        }}
        onDragOver={onDragOver}
        onDragLeave={onDragLeave}
        onDrop={onDrop}
    >
        {isHovered && Icon.ok}
        {isAdding && <div className={style.addEventForm}>
            <Input label="Event ID" list="events" value={newEventId}
                   onChange={(event) => setNewEventId(event.target.value)}/>
            <div className={style.addEventButtons}>
                <button aria-label={"Cancel New Event"}
                        className={style.addRemoveButton}
                        onClick={onCancelNewEvent}>{Icon.disabled}</button>
                <button aria-label={"Add Event"}
                        className={style.addRemoveButton}
                        onClick={onNewEvent}>{Icon.ok}</button>
            </div>
        </div>}
        {!isAdding && <div className={style.addEvent}>{Icon.add}</div>}
    </div>;
}
