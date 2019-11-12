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

import React from "react";

import style from "./Configuration.module.css";

interface Props extends React.DetailedHTMLProps<React.SelectHTMLAttributes<HTMLSelectElement>, HTMLSelectElement> {
    label?: string;
}

export default function Select(props: Props) {
    return <div className={style.formSection}>
        {props.label && <label className={style.label}>{props.label}</label>}
        <select aria-label={props.label} className={style.input} {...props}/>
    </div>;
}
