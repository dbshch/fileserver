var fileTable = null;
var resumable = null;

var UP_DOWN_PATH = "updown/";
var GET_ALL_FILES_PATH = UP_DOWN_PATH + "getallfiles";
var DOWNLOAD_PATH = UP_DOWN_PATH + "files/";
var RESUMABLE_UPLOAD_PATH = UP_DOWN_PATH + "resumable";
var DOWNLOAD_BATCH_FILES = UP_DOWN_PATH + "getfiles";

// access service with token and userId.
var fileInfo = {};
fileInfo["token"] = "token";
fileInfo["userId"] = 9527;
fileInfo["projectId"] = 1;
var fileInfoStr = JSON.stringify(fileInfo);

// selected files to download together
var rows_selected = [];
var isDownloading = false;
var zipFilename = "bim"

function updateDataTableSelectAllCtrl(table){
    var $table             = table.table().node();
    var $chkbox_all        = $('tbody input[type="checkbox"]', $table);
    var $chkbox_checked    = $('tbody input[type="checkbox"]:checked', $table);
    var chkbox_select_all  = $('thead input[name="select_all"]', $table).get(0);

    // If none of the checkboxes are checked
    if($chkbox_checked.length === 0){
//		   alert('none of the checkboxes are checked!');
        chkbox_select_all.checked = false;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = false;
        }

        // If all of the checkboxes are checked
    } else if ($chkbox_checked.length === $chkbox_all.length){
//		   alert('all of the checkboxes are checked!');
        chkbox_select_all.checked = true;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = false;
        }

        // If some of the checkboxes are checked
    } else {
//		   alert('some of the checkboxes are checked!');
        chkbox_select_all.checked = true;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = true;
        }
    }
}


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
            {
                data: null,
                'render': function (data, type, full, meta){
                    return '<input type="checkbox">';
                }

            },
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
        ],
        'rowCallback': function(row, data, dataIndex){
            // Get row ID
            var rowId = data.fileId;
            //          alert('rowCallback! rowId is ' + rowId);
            // If row ID is in the list of selected row IDs
            if($.inArray(rowId, rows_selected) !== -1){
                $(row).find('input[type="checkbox"]').prop('checked', true);
                $(row).addClass('selected');
            }
        }
    });

    // Handle click on checkbox
    $('#fileTable tbody').on('click', 'input[type="checkbox"]', function(e){
        var $row = $(this).closest('tr');

        // Get row data
        var data = fileTable.row($row).data();

        // Get row ID
        var rowId = data.idstandardAnswer;//**********************************
        // alert('Handle click on checkbox! rowId is ' + rowId);

        // Determine whether row ID is in the list of selected row IDs
        var index = $.inArray(rowId, rows_selected);

        // If checkbox is checked and row ID is not in list of selected row IDs
        if(this.checked && index === -1){
            rows_selected.push(rowId);

            // Otherwise, if checkbox is not checked and row ID is in list of selected row IDs
        } else if (!this.checked && index !== -1){
            rows_selected.splice(index, 1);
        }

        if(this.checked){
            $row.addClass('selected');
        } else {
            $row.removeClass('selected');
        }

        // Update state of "Select all" control
        updateDataTableSelectAllCtrl(fileTable);

        // Prevent click event from propagating to parent
        e.stopPropagation();
    });

    // Handle click on table cells with checkboxes
    $('#fileTable').on('click', 'tbody td, thead th:first-child', function(e){
        $(this).parent().find('input[type="checkbox"]').trigger('click');
    });

    // Handle click on "Select all" control
    $('thead input[name="select_all"]', fileTable.table().container()).on('click', function(e){
        if(this.checked){
            $('#fileTable tbody input[type="checkbox"]:not(:checked)').trigger('click');
        } else {
            $('#fileTable tbody input[type="checkbox"]:checked').trigger('click');
        }

        // Prevent click event from propagating to parent
        e.stopPropagation();
    });

    // Handle table draw event
    fileTable.on('draw', function(){
        // Update state of "Select all" control
        updateDataTableSelectAllCtrl(fileTable);
    });
});


function getAllFiles() {
    fileTable.ajax.reload();
}

function planify(data) {
    data.fileInfo = fileInfoStr;
}


/**
 * download batch files
 */
function batchDownload() {
    if ( !isDownloading) {
        alert("You are downloading now! Cancel it first before opening a new one.");
        return;
    }
    if (rows_selected.length !== 0) {
        alert("Choose some files to download!");
        return;
    }
    if (rows_selected.length > 100) {
        alert("Too many files to download!No more than 100 files once.");
        return;
    }
    isDownloading = true;
    $.ajax({
        url : DOWNLOAD_BATCH_FILES,
        data: function (data) {
            data.fileInfo = fileInfoStr;
            data.filesId = JSON.stringify(rows_selected);
            data.filename = zipFilename;
        },
        type: 'POST',
        traditional: true,
        error : function(map) {
            alert("Download fail! Try again.");
            isDownloading = false;
        },
        success : function(map) {
            console.log("Download success.");
            isDownloading = false;
        }
    });
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


