# This Fork Changed Android Part 
Add function to open a existing picture from album and scan it;
Add back button and changed UI;
Changed success callback return param. Now it's a Object,include value and format;
Add format limit,Now can only scan the bar type that your choose;
(API changed)
How to use?
Install cordova-plugin-cszbar first;
Clone this fork and replace this plugin (Android Part Only);

# ZBar Barcode Scanner Plugin

This plugin integrates with the [ZBar](http://zbar.sourceforge.net/) library,
exposing a JavaScript interface for scanning barcodes (QR Code, EAN-13/UPC-A, UPC-E, EAN-8, Code 128, Code 39, Interleaved 2 of 5, etc).
In this fork a button has been added to turn off and on device flash. In addition the plugin can now handle the device orientation change.

## Installation

    cordova plugin add cordova-plugin-cszbar

## API

### Scan barcode

    cloudSky.zBar.scan(params, onSuccess, onFailure)

Arguments:

- **params**: Optional parameters:

    ```javascript
    {
        text_title: "OPTIONAL Title Text - default = 'Scan QR Code'", // Android only
        text_instructions: "OPTIONAL Instruction Text - default = 'Please point your camera at the QR code.'", // Android only
        camera: "front" || "back" // defaults to "back",
        flash: "on" || "off" || "auto", // defaults to "auto". See Quirks
        drawSight: true || false, //defaults to true, create a red sight/line in the center of the scanner view.
        format: "QRCODE" //defaults to ALL(No type limit)
    }
    ```
    formatList:
    ALL(No type limit,Default Value);
    PARTIAL;EAN8;UPCE;ISBN10;UPCA;EAN13;ISBN13;I25;DATABAR;DATABAR_EXP;
    CODABAR;CODE39;PDF417;QRCODE;CODE93;CODE128;

- **onSuccess**: function (s) {...} _Callback for successful scan._
- **onFailure**: function (s) {...} _Callback for cancelled scan or error._

Return:

- success( values ) _Successful scan with value and format of scanned code_
- ↑ values : {value:"code your scanned",format:"type your scanned"}
- ↑ values.value _value of scanned code_ 
- ↑ values.format _type of scanned code_
- error('cancelled') _If user cancelled the scan (with back button etc)_
- error('misc error message') _Misc failure_

Status:

- Android: DONE
- iOS: DONE


## LICENSE [Apache 2.0](LICENSE.md)

This plugin is released under the Apache 2.0 license, but the ZBar library on which it depends (and which is distribute with this plugin) is under the LGPL license (2.1).
