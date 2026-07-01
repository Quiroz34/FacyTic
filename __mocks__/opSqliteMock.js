const db = {
  execute: jest.fn(async () => ({ rows: [] })),
  executeAsync: jest.fn(async () => ({ rows: [] })),
  transaction: jest.fn(async callback => callback(db)),
  close: jest.fn(),
};

module.exports = {
  open: jest.fn(() => db),
};
