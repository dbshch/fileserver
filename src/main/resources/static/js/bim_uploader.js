var r = null;
/**
 * Initiate a resumable object with necessary extra information
 * @param fileInfo extra information with map<string, base-type> structure
 * @param uploadCompleteHandler the entire uploading finished
 * @return resumabe object
 */
function resumableInit(fileInfo, uploadCompleteHandler) {
    var fileInfoStr = JSON.stringify(fileInfo);
    // Initialize a resumable object.
    r = new Resumable({
        target:RESUMABLE_UPLOAD_PATH,
        chunkSize:1*1024*1024,
        simultaneousUploads:3,
        testChunks: true,
        throttleProgressCallbacks:1,
        method: "octet",
        maxFiles: 10,
        maxFileSize: 1 * 1024 * 1024 * 1024,
        // extra information such as token.
        query: {'fileInfo': fileInfoStr}
    });
    // Resumable.js isn't supported, fall back on a different method
    if(!r.support) {
        $('.resumable-error').show();
    } else {
        // Show a place for dropping/selecting files
        $('.resumable-drop').show();
        r.assignDrop($('.resumable-drop')[0]);
        r.assignBrowse($('.resumable-browse')[0]);

        // Handle file-add event
        r.on('fileAdded', function(file){
            // Show progress bar
            $('.resumable-progress, .resumable-list').show();
            // Show pause, hide resume
            $('.resumable-progress .progress-resume-link').hide();
            $('.resumable-progress .progress-pause-link').show();
            // Add the file to the list
            $('.resumable-list').append('<li class="resumable-file-'+file.uniqueIdentifier+'">Uploading <span class="resumable-file-name"></span> <span class="resumable-file-progress"></span>');
            $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-name').html(file.fileName);
            // Actually start the uploading
            r.upload();
        });
        r.on('pause', function(){
            // Show resume, hide pause
            $('.resumable-progress .progress-resume-link').show();
            $('.resumable-progress .progress-pause-link').hide();
        });
        r.on('progress', function(){
            // Show hide, hide resume
            $('.resumable-progress .progress-resume-link').hide();
            $('.resumable-progress .progress-pause-link').show();
        });
        r.on('complete', function(){
            // Hide pause/resume when the upload has completed
            $('.resumable-progress .progress-resume-link, .resumable-progress .progress-pause-link').hide();
            uploadCompleteHandler();
        });
        r.on('fileSuccess', function(file,message){
            // Reflect that the file upload has completed
            $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-progress').html('(completed)');
        });
        r.on('fileError', function(file, message){
            // Reflect that the file upload has resulted in error
            $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-progress').html('(file could not be uploaded: '+message+')');
        });
        r.on('fileProgress', function(file){
            // Handle progress for both the file and the overall upload
            $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-progress').html(Math.floor(file.progress()*100) + '%');
            $('.progress-bar').css({width:Math.floor(r.progress()*100) + '%'});
        });
    }
    return r;
}

/**
 * Cancel the uploading when user closes the modal of uploading.
 */
function cancelUploading() {
    if (r != null) {
        // cancel and uploading task.
        r.cancel();
        // clean uploading history.
        $('.resumable-list').empty();
    }
    console.log("Upload canceled!")
}

/**
 * Uploading is activated by user.
 */
function uploadAction() {
    if(r != null) {
        r.upload();
    }
    console.log("Upload activated.")
}

/**
 * Uploading is paused by user.
 */
function pauseAction() {
    if (r != null) {
        r.pause();
    }
    console.log("Upload paused.")
}