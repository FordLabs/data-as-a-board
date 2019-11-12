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

import {Tile} from "../tile/Tile";

import styles from "./Page.module.css";
import {TileProperties} from '../../../model/TileProperties';

interface Props {
    tiles: TileProperties[];
    name: string;
    rows?: number;
    columns?: number;
}

function gridStyle(rows = 3, columns = 5) {
    return {
        gridTemplateRows: "minmax(0, 1fr) ".repeat(rows).trim(),
        gridTemplateColumns: "minmax(0, 1fr) ".repeat(columns).trim()
    }
}

export function PageDisplay(props: Props) {
    return <div className={styles.radiatorPage}>
        <div className={styles.pageName}>{props.name}</div>
        <div className={styles.radiatorPageInner} style={gridStyle(props.rows, props.columns)}>
            {props.tiles.map((tile, index) =>
                <Tile
                    key={index}
                    tile={tile}
                />
            )}
        </div>
    </div>;
}
