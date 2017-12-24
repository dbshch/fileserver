$(document).ready(function () {

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
	var formData = new FormData();
	formData.append('file', file);
	var fileInfo = {"userId": 9527};
	formData.append('fileInfo', fileInfo);
	$.ajax({
        url: "/api/uploadfile",
		type: 'POST',
		enctype: 'multipart/form-data',
		data: formData,
		cache: false,
		contentType: false,
		processData: false,
		success: function(response){
			$("#result").text(data);
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
	if(fileTable != null){
		fileTable.ajax.reload();
	}
	else{
		fileTable = $('#fileTable').DataTable( {
			//支持下载
			dom: 'lBfrtip',
			buttons: [
			          {
			        	  extend: 'excel',
			        	  text: 'download file lists',
			          }
			          ],
              "processing": true,
              "order": [[2, 'desc']],
              ajax: {
                  "dataSrc": "",// 返回数组对象
                  "url" : "/getallfiles",
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
                        { data: "createdAt" },
                        { "data": "fileId",
                         "render": function(data) {
                            return '<a  href="/files/' + data + '"><button>download</button></a>'
                         }
                         },
                        ]
		} );
	}
}

function planify(data){

}