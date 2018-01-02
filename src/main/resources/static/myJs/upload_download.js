var fileTable = null;
var r = null;
var UP_DOWN_PATH = "updown/";
var GET_ALL_FILES_PATH = UP_DOWN_PATH + "getallfiles";
var UPLOAD_FILE_PATH  = UP_DOWN_PATH + "api/uploadfile";
var DOWNLOAD_PATH = UP_DOWN_PATH + "files/";
var RESUMABLE_UPLOAD_PATH = 'upload/resumable';
var fileInfo = {};
fileInfo["userId"] = 9527;

$(document).ready(function () {
		fileTable = $('#fileTable').DataTable( {
              "processing": true,
              "order": [[3, 'desc']],
              ajax: {
                  "dataSrc": "",// 返回数组对象
                  "url" : GET_ALL_FILES_PATH,
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
                        { data: "userId"},
                        { data: "date" },
                        { "data": "fileId",
                         "render": function(data) {
                            return '<a  href="' + DOWNLOAD_PATH + data + '"><button>download</button></a>'
                         }
                         },
                        ]
		} );

		r = new Resumable({
            target:RESUMABLE_UPLOAD_PATH,
            chunkSize:1*1024*1024,
            simultaneousUploads:3,
            testChunks: true,
            throttleProgressCallbacks:1,
            method: "octet",
            maxFiles: 10,
            maxFileSize: 1 * 1024 * 1024 * 1024,
            query: {'fileInfo': JSON.stringify(fileInfo)}
          });
        // Resumable.js isn't supported, fall back on a different method
        if(!r.support) {
          $('.resumable-error').show();
          $('#simple_updown').show();
        } else {
          // Show a place for dropping/selecting files
          $('.resumable-drop').show();
          r.assignDrop($('.resumable-drop')[0]);
          r.assignBrowse($('.resumable-browse')[0]);

          // Handle file add event
          r.on('fileAdded', function(file){
              // Show progress bar
              $('.resumable-progress, .resumable-list').show();
              // Show pause, hide resume
              $('.resumable-progress .progress-resume-link').hide();
              $('.resumable-progress .progress-pause-link').show();
              // Add the file to the list
              $('.resumable-list').append('<li class="resumable-file-'+file.uniqueIdentifier+'">Uploading <span class="resumable-file-name"></span> <span class="resumable-file-progress"></span>');
              $('.resumable-file-'+file.uniqueIdentifier+' .resumable-file-name').html(file.fileName);
              // Actually start the upload
              r.upload();
            });
          r.on('pause', function(){
              // Show resume, hide pause
              $('.resumable-progress .progress-resume-link').show();
              $('.resumable-progress .progress-pause-link').hide();
            });
          r.on('progress', function(){
              // Show resume, hide pause
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

function progress(e) {
	if (e.lengthComputable) {
		$('#progress_percent').text(Math.floor((e.loaded * 100) / e.total));
		$('progress').attr({value:e.loaded,max:e.total});
	}
}

function upload(){
	var file = $('input[name="upload_file"]').get(0).files[0];
	if(file == null){
		alert("choose file first!");
		return;
	}
    $('#progress_percent').text(0);
    $('progress').attr({value:0,max:100});
	var formData = new FormData();
	formData.append('file', file);
	formData.append('fileInfo', JSON.stringify(fileInfo));
	$.ajax({
        url: UPLOAD_FILE_PATH,
		type: 'POST',
		enctype: 'multipart/form-data',
		data: formData,
		cache: false,
		contentType: false,
		processData: false,
		success: function(response){
			$("#result").text(response);
			getAllFiles();
		},
		error: function(response){
			console.log(response);
			var error = "error";
			console.log(response.status);
			console.log(response.responseText);
			if (response.status === 400 || response.status === 409 || response.status === 403 || response.status === 500){
				error = response.status + " " + response.responseText;
			}
			alert(error);
		},
		xhr: function() {
			var myXhr = $.ajaxSettings.xhr();
			if (myXhr.upload) {
				myXhr.upload.addEventListener('progress', progress, false);
			} else {
				console.log('Upload progress is not supported.');
			}
			return myXhr;
		}
	});
}

function getAllFiles() {
	fileTable.ajax.reload();
}

function planify(data){

}

