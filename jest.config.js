module.exports = {
  preset: 'react-native',
  setupFiles: ['react-native-gesture-handler/jestSetup'],
  moduleNameMapper: {
    '^react-native-vector-icons/(.*)$': '<rootDir>/__mocks__/IconMock.js',
    '^@op-engineering/op-sqlite$': '<rootDir>/__mocks__/opSqliteMock.js',
  },
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native|@react-navigation)/)',
  ],
};
