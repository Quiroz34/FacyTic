/**
 * @format
 */

import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import App from '../App';

jest.mock('../src/database', () => ({
  initDatabase: jest.fn(() => Promise.resolve()),
}));

jest.mock('../src/store/useAppStore', () => ({
  useAppStore: jest.fn(() => ({ initAll: jest.fn() })),
}));

jest.mock('../src/navigation', () => ({
  AppNavigator: () => {
    const React = require('react');
    const { Text } = require('react-native');
    return React.createElement(Text, null, 'AppNavigator');
  },
}));

test('renders correctly', async () => {
  await ReactTestRenderer.act(async () => {
    ReactTestRenderer.create(<App />);
    await Promise.resolve();
  });
});
