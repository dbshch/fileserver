<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link type="text/css" href="css/bootstrap.min.css" rel="stylesheet"/>
    <link type="text/css" href="css/dataTables.bootstrap.min.css" rel="stylesheet"/>

    <link type="text/css" href="myCss/progressbar.css" rel="stylesheet"/>
</head>
<body>
<h1>File Server</h1>

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

<hr/>

<div class="col-sm-offset-2" id="result"></div>

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

<script type="text/javascript" src="myJs/upload_download.js"></script>
</body>
</html>