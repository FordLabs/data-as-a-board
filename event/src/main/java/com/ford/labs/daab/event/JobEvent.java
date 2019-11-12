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

package com.ford.labs.daab.event;import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobEvent extends Event {
    Status status;
    String url;

    @Override
    public String getEventType() {
        return EventType.JOB;
    }

    @Override
    public EventLevel getLevel() {
        switch (status) {
            case DISABLED:
                return EventLevel.DISABLED;
            case IN_PROGRESS:
                return EventLevel.INFO;
            case SUCCESS:
                return EventLevel.OK;
            case FAILURE:
                return EventLevel.ERROR;
            case UNKNOWN:
            default:
                return EventLevel.UNKNOWN;
        }
    }

    public enum Status {
        UNKNOWN,
        DISABLED,
        IN_PROGRESS,
        SUCCESS,
        FAILURE
    }
}