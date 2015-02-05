# AutoCam

This is a fork of cordova-plugin-camera-foreground.

## AutoCam: Cordova Plugin to "auto-shoot" the Camera

This plugin provides an API to deliver images from the camera without user intervention.

Plugin ID: co.orionsys.autocamplugin

### CLI Installation
	
cordova plugin add https://github.com/MikeBurgher/co.orionsys.autocamplugin

### Description

navigator.camera.getPicture( cameraSuccess, cameraError, cameraOptions)

This plugin provides an API to deliver images from the camera without user intervention. It opens the selected camera, hides the camera preview, focuses, and takes a picture. It uses a custom camera activity to prevent the app from being terminated by Android during the camera activity.

The image is passed to the success callback as a base64-encoded String or as the URI for an image file. You can do whatever you want with the encoded image or URI: display the image on the screen, save it in local storage, send it to a remote server, etc.

### Supported Platforms
* Amazon Fire OS
* Android

### Example

Retrieve an image as a base64-encoded string:

    navigator.camera.getPicture(onSuccess, onFail, { quality: 50, destinationType: 0 });

    function onSuccess(imageData) {
        var image = document.getElementById('myImage');
        image.src = "data:image/jpeg;base64," + imageData;
    }

    function onFail(message) {
        alert('Failed because: ' + message);
    }

Take a photo and retrieve the image's file location:

    navigator.camera.getPicture(onSuccess, onFail, { quality: 50, destinationType: 1 });

    function onSuccess(imageURI) {
        var image = document.getElementById('myImage');
        image.src = imageURI;
    }

    function onFail(message) {
        alert('Failed because: ' + message);
    }

### CameraOptions

CameraOptions are optional parameters to customize the camera settings.

    CameraOptions = 
        { quality : 75,  
        destinationType: 1,
        targetWidth: 100,  
        targetHeight: 100,  
        cameraDirection: 0,
        sourceType: 1 }

**quality**: Quality of the saved image, expressed as a range of 0-100, where 100 is typically full resolution with no loss from file compression.

**DestinationType**: Choose the format of the returned image: 0 for a base64-encoded string, 1 for a image file URI. The default is 1. If returned as a base64-encoded string, an image file is NOT saved on the device.

**targetWidth**: Width in pixels to scale image. Must be used with targetHeight. Aspect ratio remains constant.

**targetHeight**: Height in pixels to scale image. Must be used with targetWidth. Aspect ratio remains constant.

**cameraDirection**: Choose the camera to use: 0 for back-facing, 1 for front-facing, The default is back-facing. If the device has only one camera, it uses that camera.

**sourceType**: The source is always the camera; 1 for auto-focus before taking picture, 5 to skip auto-focus. The default is 1.

