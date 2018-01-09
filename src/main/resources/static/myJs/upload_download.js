var fileTable = null;
// resumable object for uploading files.
var r = null;

var UP_DOWN_PATH = "updown/";
var GET_ALL_FILES_PATH = UP_DOWN_PATH + "getallfiles";
var DOWNLOAD_PATH = UP_DOWN_PATH + "files/";
var RESUMABLE_UPLOAD_PATH = UP_DOWN_PATH + "resumable";

// access service with token.
var fileInfo = {};
fileInfo["token"] = "token";
var fileInfoStr = JSON.stringify(fileInfo);

// bind event
$(document).ready(function () {
    // select by ID
		fileTable = $('#fileTable').DataTable( {
              "processing": true,
              "order": [[3, 'desc']],
              ajax: {
                  "dataSrc": "",// 返回数组对象
                  "url" : GET_ALL_FILES_PATH,
                  "method": "POST",
                  "data" : function(data) {
                      // 添加其他参数
                      planify(data)
                  },
                error : function(e) {
                    $("#result").html(e.responseText);
                }
              },
              columns: [
                        { data: "fileId" },
                        { data: "filename" },
                        { data: "bytes"},
                        { data: "date" },
                        { "data": "fileId",
                         "render": function(data) {
                            return '<form method="POST" action="' + UP_DOWN_PATH + "files/" + data +  '">'
                                // Be careful of double quotes in fileInfoStr!
                                + "<input hidden type='text' name='fileInfo' value='" + fileInfoStr + "'/>"
                                + '<button type="submit">download</button>'
                                + '</form>';
                         }
                         },
                        ]
		} );

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
              getAllFiles();
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
});


function getAllFiles() {
	fileTable.ajax.reload();
}

function planify(data){
    data.fileInfo = fileInfoStr;
}
