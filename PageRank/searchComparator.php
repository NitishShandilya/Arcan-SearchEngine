<?php

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
echo $query;
$results = false;

if ($query)
{
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample');

  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }

  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  try
  {
    if($_GET['pageRankType'] == "custom") {
      $additionalParameters = array('sort'=>'pageRankFile desc');
      $results = $solr->search($query, 0, $limit, $additionalParameters);
    } else if($_GET['pageRankType'] == "default"){
      $results = $solr->search($query, 0, $limit);  
    }    	  
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>
<html>
  <head>
    <title>PHP Solr Client Example</title>
  </head>
  <body>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search:</label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <br/>
      <input type="radio" name="pageRankType" value="default" <?php echo isset($_GET['pageRankType']) && $_GET['pageRankType'] == "custom" ? "" : "checked"; ?>/> Use Default Solr Page Rank 
      <input type="radio" name="pageRankType" value="custom" <?php echo isset($_GET['pageRankType']) && $_GET['pageRankType'] == "custom" ? "checked" : ""; ?>/> Use External Page Rank
      <br/>
      <input type="submit"/>
    </form>
<?php

// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
    <div>Total number of results <?php echo $total;?></div>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <ol>
<?php
  // iterate result documents
  foreach ($results->response->docs as $doc)
  {
?>
      <li>
        <table style="border: 1px solid black; text-align: left">
<?php
    
    $title = "N/A";
    $author = "N/A";
    $creation_date = "N/A";
    $stream_size = "N/A";
    $link = "";
      
    // iterate document fields / values
    foreach ($doc as $field => $value)
    {
      if ($field == "title"){
        $title = $value;
      } else if ($field == "author"){
        $author = $value;
      } else if ($field == "created"){
        $creation_date = $value;
      } else if ($field == "stream_size") {
        $stream_size = (string)round($value/1024.0,2);
      } else if ($field == "id") {
        $link = urldecode($value);
        //Convert to a clickable link
        $link = preg_replace('/\/home\/nitish\/shared\/crawl_data\//','',$link);
        $link = preg_replace('/\.html$/','',$link);
      }
    }
      
?>      
          <tr>
            <th><a href="<?php echo $link?>">Document</a></th>            
          </tr>
          <tr>
            <th>Title</th>
            <td><?php echo $title?></td>
          </tr>
          <tr>
            <th>Author</th>
            <td><?php echo $author?></td>
          </tr>
          <tr>
            <th>Date Created</th>
            <td><?php echo $creation_date?></td>
          </tr>
          <tr>
            <th>File Size(kB)</th>
            <td><?php echo $stream_size?></td>
          </tr>                 
</table>
      </li>
<?php
  }
?>
    </ol>
<?php
}
?>
  </body>
</html>
