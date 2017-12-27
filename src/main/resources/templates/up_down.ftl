<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link type="text/css" href="css/bootstrap.min.css" rel="stylesheet"/>
    <link type="text/css" href="css/dataTables.bootstrap.min.css" rel="stylesheet"/>

    <link type="text/css" href="myCss/progressbar.css" rel="stylesheet"/>

    <meta charset="utf-8" />
    <link rel="stylesheet" type="text/css" href="css/style.css" />

</head>
<body>
<h1>File Server</h1>

<div id="frame">

    <hr/>

    <h3>Demo</h3>

    <div class="resumable-error">
        Your browser, unfortunately, is not supported by Resumable.js. The library requires support for <a href="http://www.w3.org/TR/FileAPI/">the HTML5 File API</a> along with <a href="http://www.w3.org/TR/FileAPI/#normalization-of-params">file slicing</a>.
    </div>

    <div class="resumable-drop" ondragenter="jQuery(this).addClass('resumable-dragover');" ondragend="jQuery(this).removeClass('resumable-dragover');" ondrop="jQuery(this).removeClass('resumable-dragover');">
        Drop video files here to upload or <a class="resumable-browse"><u>select from your computer</u></a>
    </div>

    <div class="resumable-progress">
        <table>
            <tr>
                <td width="100%"><div class="progress-container"><div class="progress-bar"></div></div></td>
                <td class="progress-text" nowrap="nowrap"></td>
                <td class="progress-pause" nowrap="nowrap">
                    <a href="#" onclick="r.upload(); return(false);" class="progress-resume-link"><img src="img/resume.png" title="Resume upload" /></a>
                    <a href="#" onclick="r.pause(); return(false);" class="progress-pause-link"><img src="img/pause.png" title="Pause upload" /></a>
                </td>
            </tr>
        </table>
    </div>

    <ul class="resumable-list"></ul>


</div>

<div id="simple_updown" hidden>
    <div style="margin:10px auto;width: 80%;border: 1px solid black;padding: 5px;">

        <div>
            <progress max=100></progress>&nbsp;&nbsp;
            <span id="progress_percent">0</span>%
            <br/><br/>

            <input type="file" name="upload_file" />
            <br/><br/>
        </div>

        <button id="upload_btn" class="btn btn-info btn-sm" onclick="upload()">upload</button>

        <br/><br/>

    </div>


    <div class="col-sm-offset-2" id="result"></div>
</div>

<hr/>

<div style="margin:10px auto;width: 80%;padding: 5px;">
    <table id="fileTable"
           class="table table-striped">
        <thead>
        <tr>
            <th>file ID</th>
            <th>file name</th>
            <th>user ID</th>
            <th>date</th>
            <th>operations</th>
        </tr>
        </thead>
    </table>
</div>


<script type="text/javascript" src="js/jquery-1.12.0.min.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="js/dataTables.bootstrap.min.js"></script>
<script type="text/javascript" src="js/bootstrap.min.js"></script>

<script src="js/resumable.js"></script>

<script type="text/javascript" src="myJs/upload_download.js"></script>

</body>
</html>