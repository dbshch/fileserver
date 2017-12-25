var fileTable = null;
var UP_DOWN_PATH = "/updown/";
var GET_ALL_FILES_PATH = UP_DOWN_PATH + "getallfiles";
var UPLOAD_FILE_PATH  = UP_DOWN_PATH + "api/uploadfile";
var DOWNLOAD_PATH = UP_DOWN_PATH + "files/";

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
	var fileInfo = {};
	fileInfo["userId"] = 9527;
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