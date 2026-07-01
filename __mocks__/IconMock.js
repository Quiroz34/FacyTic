const React = require('react');
const { Text } = require('react-native');

const IconMock = ({ name, ...props }) => React.createElement(Text, props, name);

module.exports = IconMock;
module.exports.default = IconMock;
