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

import React from 'react';

import nock from 'nock';

import {Provider} from 'react-redux';
import {createStore} from 'redux';

import {
    act,
    cleanup,
    fireEvent,
    getByLabelText,
    getByTestId,
    render,
    RenderResult,
    wait,
} from '@testing-library/react';

import {Configuration} from 'model/Configuration';
import {rootReducer} from 'store/index';
import {ApplicationState} from 'store/ApplicationState';
import ConfigurationEdit from './ConfigurationEdit';
import {EventDisplayProperties} from 'model/EventDisplayProperties';

describe('ConfigurationEdit', () => {
    afterEach(cleanup);

    function renderConfigurationEdit(initialState?: ApplicationState, store = createStore(rootReducer, initialState)) {
        return {
            ...render(<Provider store={store}><ConfigurationEdit/></Provider>),
            store,
        };
    }

    async function testConfigurationChange(
        input: Configuration,
        expected: Configuration,
        mutation: (component: RenderResult) => Promise<void>,
    ) {
        const mockApi = nock('http://localhost:3000')
            .post('/api/radiator/configuration', expected)
            .reply(200);

        const component = renderConfigurationEdit({
            dashboard: {
                configuration: input,
                events: new Map(),
                currentPage: 0,
                isDisconnected: false,
                isEditing: true,
            },
        });

        await mutation(component);

        const submitButton = await component.findByTestId('submit');

        act(() => {
            fireEvent.click(submitButton);
        });

        await wait(() => expect(mockApi.isDone()).toBe(true));
    }

    it('should render', () => {
        const component = renderConfigurationEdit();
        expect(component).toBeTruthy();
    });

    it('should update the board name when the board name input is changed', async () => {
        const input: Configuration = {
            name: 'Ford Labs',
            pages: [],
        };

        const expected: Configuration = {
            name: 'FordLabs',
            pages: [],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const boardNameInput = await component.findByLabelText('Board Name');

            act(() => {
                fireEvent.change(boardNameInput, {target: {value: 'FordLabs'}});
            });
        });
    });

    it('should update the background URL when the background URL is changed', async () => {
        const input: Configuration = {
            name: '',
            background: 'https://host.com/incorrectimage.png',
            pages: [],
        };

        const expected: Configuration = {
            name: '',
            background: 'https://host.com/correctimage.png',
            pages: [],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const backgroundUrlInput = await component.findByLabelText('Background Image URL');

            act(() => {
                fireEvent.change(backgroundUrlInput, {target: {value: 'https://host.com/correctimage.png'}});
            });
        });
    });

    it('should add a page when the addPage button is clicked', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const addPageButton = await component.findByLabelText('Add Page');

            act(() => {
                fireEvent.click(addPageButton);
            });
        });
    });

    it('should remove a page when the removePage button is clicked', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const removePageButton = await component.findByLabelText('Remove Page');

            act(() => {
                fireEvent.click(removePageButton);
            });
        });
    });

    it('should update the page name when the Page Name input is changed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: 'yadoT',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: 'Today',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');

            const pageNameInput = getByLabelText(pageToEdit, 'Page Name');

            act(() => {
                fireEvent.change(pageNameInput, {target: {value: 'Today'}});
            });
        });
    });

    it('should add a column when the add column button is pressed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 6,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');

            const addColumnButton = getByLabelText(pageToEdit, 'Add Column');

            act(() => {
                fireEvent.click(addColumnButton);
            });
        });
    });

    it('should remove a column when the add column button is pressed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 4,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');

            const addColumnButton = getByLabelText(pageToEdit, 'Remove Column');

            act(() => {
                fireEvent.click(addColumnButton);
            });
        });
    });

    it('should add a row when the add row button is pressed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 4,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');

            const addColumnButton = getByLabelText(pageToEdit, 'Add Row');

            act(() => {
                fireEvent.click(addColumnButton);
            });
        });
    });

    it('should remove a row when the add row button is pressed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 2,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');

            const addColumnButton = getByLabelText(pageToEdit, 'Remove Row');

            act(() => {
                fireEvent.click(addColumnButton);
            });
        });
    });

    it('should remove a row when the add row button is pressed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 2,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');

            const addColumnButton = getByLabelText(pageToEdit, 'Remove Row');

            act(() => {
                fireEvent.click(addColumnButton);
            });
        });
    });

    it('should add an event when the empty spot is pressed and the input filled out', async () => {

        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [{
                        id: 'new.event',
                        row: 1,
                        column: 1,
                        tileType: "EVENT"
                    } as EventDisplayProperties],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');
            const emptySpotToAddTo = getByTestId(pageToEdit, 'edit-page-0-empty-spot-1,1');

            act(() => {
                fireEvent.click(emptySpotToAddTo);
            });

            const emptySpotTypeSelect = getByLabelText(emptySpotToAddTo, 'Add new:');

            act(() => {
                fireEvent.change(emptySpotTypeSelect, {target: {value: 'EVENT'}});
            });

            const eventIdInput = getByLabelText(emptySpotToAddTo, 'Event ID');

            act(() => {
                fireEvent.change(eventIdInput, {target: {value: 'new.event'}});
            });

            const newEventSaveButton = getByLabelText(emptySpotToAddTo, 'Add Event');

            act(() => {
                fireEvent.click(newEventSaveButton);
            });
        });
    });

    it('should delete an event when the delete button is pressed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [{
                        id: 'job.jenkins.daab',
                        tileType: "EVENT",
                    } as EventDisplayProperties],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');
            const eventToEdit = getByTestId(pageToEdit, 'edit-page-0-tile-0');
            const deleteButton = getByLabelText(eventToEdit, 'Delete Event');

            act(() => {
                fireEvent.click(deleteButton);
            });
        });
    });

    it('should change the event size event when the event width and height inputs are changed', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [{
                        id: 'job.jenkins.daab',
                        width: 1,
                        height: 1,
                        tileType: "EVENT",
                    } as EventDisplayProperties],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [{
                        id: 'job.jenkins.daab',
                        width: 2,
                        height: 2,
                        tileType: "EVENT",
                    } as EventDisplayProperties],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');
            const eventToEdit = getByTestId(pageToEdit, 'edit-page-0-tile-0');
            const widthInput = getByLabelText(eventToEdit, 'width');
            const heightInput = getByLabelText(eventToEdit, 'height');

            act(() => {
                fireEvent.change(widthInput, {target: {value: 2}});
            });
            act(() => {
                fireEvent.change(heightInput, {target: {value: 2}});
            });
        });
    });

    // jsdom does not support the DataTransfer API necessary for the drag/drop events to function.
    // disabling this test until such functionality is available.
    it.skip('should change the event position when the event is dragged to an empty spot', async () => {
        const input: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [{
                        id: 'job.jenkins.daab',
                        width: 1,
                        height: 1,
                        row: 0,
                        column: 0,
                    } as EventDisplayProperties],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        const expected: Configuration = {
            name: '',
            pages: [
                {
                    name: '',
                    tiles: [{
                        id: 'job.jenkins.daab',
                        width: 1,
                        height: 1,
                        row: 1,
                        column: 3,
                    } as EventDisplayProperties],
                    rows: 3,
                    columns: 5,
                },
            ],
        };

        await testConfigurationChange(input, expected, async (component) => {
            const editPages = component.getByTestId('edit-pages');
            const pageToEdit = getByTestId(editPages, 'edit-page-0');
            const eventToEdit = getByTestId(pageToEdit, 'edit-page-0-event-job.jenkins.daab');

            const emptySpotToDrop = component.getByTestId('edit-page-0-empty-spot-1,3');

            act(() => {
                fireEvent.dragStart(eventToEdit);
                fireEvent.drop(emptySpotToDrop);
            });
        });
    });
});
