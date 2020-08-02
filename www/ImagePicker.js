var exec = require('cordova/exec');

var ImagePicker = {
    showPickerActivity: function (arg0, success, error) {
        exec(success, error, 'ImagePicker', 'showPickerActivity', [arg0]);
    },
    deleteFiles: function (arg0, success, error) {
        exec(success, error, 'ImagePicker', 'deleteFiles', [arg0]);
    }
}

module.exports = ImagePicker;

