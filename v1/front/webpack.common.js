const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyPlugin = require("copy-webpack-plugin");
const LicenseWebpackPlugin =
  require("license-webpack-plugin").LicenseWebpackPlugin;

const path = require("path");
const version = require("./package.json").version;
const isProduction = process.env.NODE_ENV == "production";

module.exports = {
  entry: {
    index: "./src/js/index.js",
    validate: "./src/js/app/entrypoints/validate.js",
    reset_password: "./src/js/app/entrypoints/reset_password.js",
  },
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: `[name].${version}.js`,
    clean: true,
  },
  optimization: {
    splitChunks: {
      chunks: "all",
    },
  },

  plugins: [
    new LicenseWebpackPlugin({
      addBanner: true,
      licenseTypeOverrides: {
        foopkg: "MIT",
      },
    }),
    new HtmlWebpackPlugin({
      inject: true,
      template: "src/index.html",
      filename: "index.html",
      chunks: ["index"],
    }),
    new HtmlWebpackPlugin({
      inject: true,
      template: "src/validate.html",
      filename: "validate.html",
      chunks: ["validate"],
    }),
    new HtmlWebpackPlugin({
      inject: true,
      template: "src/reset_password.html",
      filename: "reset_password.html",
      chunks: ["reset_password"],
    }),
    new HtmlWebpackPlugin({
      inject: true,
      template: "src/maintenance.html",
      filename: "maintenance.html",
      chunks: [],
    }),
    new webpack.DefinePlugin({
      __VERSION__: JSON.stringify(version),
    }),
    new CopyPlugin({
      patterns: [
        { from: "src/robots.txt", to: "robots.txt" },
        { from: "src/fonts/*", to: "fonts/[name][ext]" },
        { from: "src/img/favicon.ico", to: "favicon.ico" },
        { from: "src/img/*", to: "img/[name][ext]" },
        {
          from: "src/img/parcoursup_square.jpg",
          to: "img/parcoursup_square.jpg",
        },
        { from: "src/data/frontendData.zip", to: "data/data.zip" },
      ],
    }),
  ],
  module: {
    rules: [
      {
        test: /\.(scss)$/,
        exclude: /(node_modules|bower_components)/,
        use: [
          {
            loader: "babel-loader",
            options: {
              presets: ["@babel/preset-env"],
              plugins: [
                [
                  "polyfill-corejs3",
                  { method: "usage-global", version: "3.20" },
                ],
              ],
            },
          },
          {
            loader: "style-loader",
          },
          {
            loader: "css-loader",
          },
          {
            loader: "postcss-loader",
            options: {
              postcssOptions: {
                plugins: () => [require("autoprefixer")],
              },
            },
          },
          {
            loader: "sass-loader",
          },
        ],
      },
    ],
  },
};
