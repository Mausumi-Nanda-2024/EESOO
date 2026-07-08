const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');
const { withNativeWind } = require('nativewind/metro');

const config = mergeConfig(getDefaultConfig(__dirname), {
  // Ensure no 'plugins' property is set here
});

module.exports = withNativeWind(config, { input: './global.css' });
