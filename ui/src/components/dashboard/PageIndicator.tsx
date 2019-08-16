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
import styles from "./Dashboard.module.css";

interface Props {
    key: number;
    forPage: number;
    currentPage: number;

    onIndicatorClick(pageNumber: number): any;
}

export function PageIndicator(props: Props) {
    const className = props.currentPage === props.forPage
        ? styles.radiatorPageIndicatorActive
        : styles.radiatorPageIndicator;
    return (
        <div className={className}
             onClick={() => props.onIndicatorClick(props.forPage)}/>
    );
}
