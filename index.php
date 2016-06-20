<?php
$dataBaseHost = "";
$dataBasePort = 3306;
$dataBaseName = "";
$dataBaseUser = "";
$dataBasePassword = "";
?>
<html>
<head>
    <title>Sales Bot</title>
    <meta charset="UTF-8">

    <link rel="stylesheet"
          href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
          integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
          crossorigin="anonymous">

    <style>
        p {
            font-style: italic;
        }

        .jumbotron {
            text-align: center;
        }

        .myContent {
            max-width: 500px;
            margin: 50px auto 150px;
        }

        .myBtn {
            width: 50%;
            margin-top: 10px;
            float: right;
        }
    </style>
</head>
<body>
<div class="jumbotron">
    <div class="container">
        <a href="index.php"><h1>Welcome to SalesUA!</h1></a>
    </div>
</div>
<div class="container myContent">
    <?php
    if (isset($_REQUEST['name'])) {
        $name = $_REQUEST['name'];
        $descShort = $_REQUEST['descShort'];
        $sourceName = $_REQUEST['sourceName'];
        $sourceUrl = $_REQUEST['sourceUrl'];
        $inStock = isset($_REQUEST['inStock']);
        $price = $_REQUEST['price'];
        $discount = $_REQUEST['discount'];
        $discount = intval(round(($discount - $price) / $discount * 100));
        $descFull = $_REQUEST['descFull'];
        $started = join('/', array_reverse(explode('-', $_REQUEST['started'])));
        $duration = $_REQUEST['duration'];

        $database = new mysqli($dataBaseHost, $dataBaseUser, $dataBasePassword, $dataBaseName, $dataBasePort);

        if (mysqli_connect_errno()) $result = "Fail.";
        else {
            $result = "Success.";
            $statement = $database->prepare('INSERT INTO `sales` (' .
                '`name`, `descFull`, `descShort`, `sourceUrl`, `sourceName`,' .
                ' `inStock`, `discount`, `price`, `started`, `duration`) ' .
                'VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');

            $statement->bind_param('sssssiiisi',
                $name,
                $descFull,
                $descShort,
                $sourceUrl,
                $sourceName,
                $inStock,
                $discount,
                $price,
                $started,
                $duration
            );

            $statement->execute();

            $statement->close();
            $database->close();
        }
        ?>
        <h3><?= $result ?></h3>
        <hr>
        <p>Name: <?= $name ?></p>
        <p>Short description: <?= $descShort ?></p>
        <p>Source name: <?= $sourceName ?></p>
        <p>Source URL: <?= $sourceUrl ?></p>
        <p>In stock: <?= $inStock ?></p>
        <p>Discount: <?= $discount ?></p>
        <p>Price: <?= $price ?></p>
        <p>Description: <?= $descFull ?></p>
        <p>Started: <?= $started ?></p>
        <p>Duration: <?= $duration ?></p>
    <?php } else { ?>
        <form method="get">
            <div class="form-group">
                <label for="inputName">Name</label>
                <input class="form-control" id="inputName" name="name" type="text"
                       placeholder="Name">
            </div>
            <div class="form-group">
                <label for="inputDescShort">Short description</label>
                <input class="form-control" id="inputDescShort" name="descShort" type="text"
                       placeholder="Short description">
            </div>
            <div class="form-group">
                <label for="inputSourceName">Source name</label>
                <input class="form-control" id="inputSourceName" name="sourceName" type="text"
                       placeholder="Source name">
            </div>
            <div class="form-group">
                <label for="inputSourceUrl">Source URL</label>
                <input class="form-control" id="inputSourceUrl" name="sourceUrl" type="text"
                       placeholder="Source URL">
            </div>
            <div class="form-group">
                <label for="inputDiscount">Discount</label>
                <input class="form-control" id="inputDiscount" name="discount" type="number"
                       placeholder="Discount">
            </div>
            <div class="form-group">
                <label for="inputPrice">Price</label>
                <input class="form-control" id="inputPrice" name="price" type="number"
                       placeholder="Price">
            </div>
            <div class="form-group">
                <label for="inputDescFull">Description</label>
                <input class="form-control" id="inputDescFull" name="descFull" type="text"
                       placeholder="Description">
            </div>
            <div class="form-group">
                <label for="inputStarted">Started</label>
                <input class="form-control" id="inputStarted" name="started" type="date"
                       placeholder="Started">
            </div>
            <div class="form-group">
                <label for="inputDuration">Duration</label>
                <input class="form-control" id="inputDuration" name="duration" type="number"
                       placeholder="Duration">
            </div>
            <hr>
            <div class="checkbox">
                <label><input name="inStock" type="checkbox"> In stock</label>
            </div>
            <input class="btn btn-lg btn-success myBtn" type="submit" value="Submit">
        </form>
    <?php } ?>
</div>
</body>
</html>