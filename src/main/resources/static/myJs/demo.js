var fileTable = null;
var resumable = null;

var UP_DOWN_PATH = "updown/";
var GET_ALL_FILES_PATH = UP_DOWN_PATH + "getallfiles";
var DOWNLOAD_PATH = UP_DOWN_PATH + "files/";
var RESUMABLE_UPLOAD_PATH = UP_DOWN_PATH + "resumable";

// access service with token and userId.
var fileInfo = {};
fileInfo["token"] = "token";
fileInfo["userId"] = 9527;
fileInfo["projectId"] = 1;
var fileInfoStr = JSON.stringify(fileInfo);

// initial DataTable
$(document).ready(function () {
    // select by ID
    fileTable = $('#fileTable').DataTable({
        "processing": true,
        "order": [[3, 'desc']],
        ajax: {
            "dataSrc": "",// 返回数组对象
            "url": GET_ALL_FILES_PATH,
            "method": "POST",
            "data": function (data) {
                // 添加其他参数
                planify(data)
            },
            error: function (e) {
                $("#result").html(e.responseText);
            }
        },
        columns: [
            {data: "fileId"},
            {data: "filename"},
            {data: "bytes"},
            {data: "date"},
            {
                "data": "fileId",
                "render": function (data) {
                    return '<form method="POST" action="' + DOWNLOAD_PATH + data + '">'
                        // Be careful of double quotes in fileInfoStr!
                        + "<input hidden type='text' name='fileInfo' value='" + fileInfoStr + "'/>"
                        + '<button type="submit">download</button>'
                        + '</form>';
                }
            },
        ]
    });
});


function getAllFiles() {
    fileTable.ajax.reload();
}

function planify(data) {
    data.fileInfo = fileInfoStr;
}


/**
 * handle uploading finish event.
 */
function uploadCompleteHandler() {
    console.log("Upload finished.");
    getAllFiles();
    /*
    Do what you like here.
     */
}


/**
 * Access to file server for uploading service.
 */
function upload() {
    if (resumable == null)
        resumable = resumableInit(fileInfo, uploadCompleteHandler);
}


