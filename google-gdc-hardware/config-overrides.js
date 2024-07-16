const webpack = require('webpack');

module.exports = function override(config, env) {
  // Fallback configuration for Node.js modules
  config.resolve.fallback = {
    ...config.resolve.fallback,
    "crypto": require.resolve("crypto-browserify"),
    "https": require.resolve("https-browserify"),
    "http": require.resolve("stream-http"),
    "stream": require.resolve("stream-browserify"),
    "path": require.resolve("path-browserify"),
    "os": require.resolve("os-browserify/browser"),
    "querystring": require.resolve("querystring-es3"),
    "fs": false,
    "net": false,
    "tls": false,
    "child_process": false,
    "process": require.resolve("process/browser")
  };

  // Add polyfill plugins
  config.plugins = [
    ...config.plugins,
    new webpack.ProvidePlugin({
      process: 'process/browser',
      Buffer: ['buffer', 'Buffer'],
    })
  ];

  // Polyfill process.emitWarning
  config.plugins.push(new webpack.DefinePlugin({
    'process.emitWarning': () => {}
  }));

  return config;
};
