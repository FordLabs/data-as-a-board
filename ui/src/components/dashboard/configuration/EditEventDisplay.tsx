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
import {TileProperties} from '../../../model/TileProperties';

interface Props {
    display: EventDisplayProperties;
    pageNumber: number;
    rows: number;
    columns: number;

    onChangeWidth(width: number): void;
    onChangeHeight(height: number): void

    onEventDeleted(event: TileProperties): void;
}

function displayId(id: string) {
    return <>
        {id.split(".").map((val, idx) =>
            <span key={idx}>{"　".repeat(idx)}{idx > 0 && "."}{val}</span>)
        }
    </>;
}

export function EditEventDisplay(props: Props) {
    return <>
        {displayId(props.display.id)}
        <div className={style.editBoardEventActions}>
            <div className={style.editBoardEventDimensions}>
                <Input label="width" type="number" min={1} max={props.columns} value={props.display.width || 1}
                       onChange={(event) => props.onChangeWidth(parseInt(event.target.value, 10))}/>
                <Input label="height" type="number" min={1} max={props.rows} value={props.display.height || 1}
                       onChange={(event) => props.onChangeHeight(parseInt(event.target.value, 10))}/>
            </div>
            <a aria-label="Delete Event" onClick={() => props.onEventDeleted(props.display)}>
                {Icon.delete}
            </a>
        </div>
    </>;
}
