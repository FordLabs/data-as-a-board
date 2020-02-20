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

import moment from "moment";

moment.locale("precise-en", {
    relativeTime: {
        future : "%s",
        past : "%s ago",
        s : "%d seconds",
        m : "%d minute",
        mm : "%d minutes",
        h : "%d hour",
        hh : "%d hours",
        d : "%d day",
        dd : "%d days",
        M : "%d month",
        MM : "%d months",
        y : "%d year",
        yy : "%d years"
    }
});

export function humanizeDurationPrecise(duration: moment.Duration): string {
    return duration.locale("precise-en").humanize(true);
}