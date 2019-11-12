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

import {TileProperties} from "./TileProperties";

export class Page {
    public tiles: TileProperties[];
    public name: string;
    public rows?: number;
    public columns?: number;

    constructor(tiles = [], name = "", rows = 3, columns = 5) {
        this.tiles = tiles;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }
}
